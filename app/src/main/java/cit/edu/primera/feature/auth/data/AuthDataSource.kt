package cit.edu.primera.feature.auth.data

import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import android.util.Log
import java.util.*

class AuthDataSource {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun isUserAuthenticated(): Boolean = auth.currentUser != null

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun hasPasswordAuthProvider(): Boolean {
        return auth.currentUser?.providerData
            ?.any { it.providerId == EmailAuthProvider.PROVIDER_ID }
            ?: false
    }

    fun getAuthProviders(): Set<String> {
        return auth.currentUser?.providerData
            ?.map { it.providerId }
            ?.filter { it != "firebase" }
            ?.toSet()
            ?: emptySet()
    }

    suspend fun login(email: String, password: String) {
        val normalizedEmail = normalizeEmail(email)
        val result = auth.signInWithEmailAndPassword(normalizedEmail, password).await()
        val user = result.user
        if (user != null) {
            try {
                syncUserDocument(
                    user = user,
                    fallbackUsername = normalizedEmail.substringBefore("@"),
                    lastLoginProvider = EmailAuthProvider.PROVIDER_ID
                )
            } catch (e: Exception) {
                Log.e(TAG, "Login profile sync failed", e)
            }
        }
    }

    suspend fun signUp(email: String, password: String): String {
        val normalizedEmail = normalizeEmail(email)
        val result = auth.createUserWithEmailAndPassword(normalizedEmail, password).await()
        val user = result.user ?: throw Exception("User creation failed")
        try {
            syncUserDocument(
                user = user,
                fallbackUsername = normalizedEmail.substringBefore("@"),
                lastLoginProvider = EmailAuthProvider.PROVIDER_ID
            )
        } catch (e: Exception) {
            Log.e(TAG, "SignUp profile sync failed", e)
        }
        return user.uid
    }

    suspend fun loginWithGoogle(idToken: String): String {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val user = linkOrSignIn(credential, GoogleAuthProvider.PROVIDER_ID, "Google")

        try {
            syncUserDocument(
                user = user,
                fallbackUsername = "Google User",
                lastLoginProvider = GoogleAuthProvider.PROVIDER_ID
            )
        } catch (e: Exception) {
            Log.e(TAG, "Google authentication succeeded, but profile sync failed", e)
        }
        return user.uid
    }

    suspend fun loginWithFacebook(accessToken: String): String {
        val credential = FacebookAuthProvider.getCredential(accessToken)
        val user = linkOrSignIn(credential, FacebookAuthProvider.PROVIDER_ID, "Facebook")

        try {
            syncUserDocument(
                user = user,
                fallbackUsername = "Facebook User",
                lastLoginProvider = FacebookAuthProvider.PROVIDER_ID
            )
        } catch (e: Exception) {
            Log.e(TAG, "Facebook authentication succeeded, but profile sync failed", e)
        }
        return user.uid
    }

    suspend fun linkCredential(credential: AuthCredential) {
        val user = auth.currentUser ?: throw Exception("User is not authenticated")
        user.linkWithCredential(credential).await()
    }

    suspend fun unlinkProvider(providerId: String) {
        val user = auth.currentUser ?: throw Exception("User is not authenticated")
        val validProviders = user.providerData
            .map { it.providerId }
            .filter { it != "firebase" }
        if (validProviders.size <= 1) {
            throw Exception("Cannot unlink sole remaining authentication provider")
        }
        user.unlink(providerId).await()
    }

    private suspend fun linkOrSignIn(
        credential: AuthCredential,
        providerId: String,
        providerName: String
    ): com.google.firebase.auth.FirebaseUser {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (currentUser.providerData.any { it.providerId == providerId }) {
                return currentUser
            }
            return currentUser.linkWithCredential(credential).await().user
                ?: throw Exception("$providerName account linking failed")
        }

        return auth.signInWithCredential(credential).await().user
            ?: throw Exception("$providerName login failed")
    }

    private suspend fun syncUserDocument(
        user: FirebaseUser,
        fallbackUsername: String,
        lastLoginProvider: String
    ) {
        val userRef = firestore.collection("users").document(user.uid)
        val userDoc = userRef.get().await()

        val updateData = hashMapOf<String, Any>()
        if (userDoc.exists() && userDoc.data != null) {
            updateData.putAll(userDoc.data!!)
        }

        val cleanEmail = user.email?.let(::normalizeEmail)
        if (!cleanEmail.isNullOrBlank()) {
            updateData["email"] = cleanEmail
        }

        val currentUsername = (userDoc.get("username") as? String)?.takeIf { it.isNotBlank() }
            ?: user.displayName?.takeIf { it.isNotBlank() }
            ?: fallbackUsername

        updateData["username"] = currentUsername
        updateData["authProviders"] = user.providerData
            .map { it.providerId }
            .filter { it != "firebase" }
            .distinct()
            .sorted()
        updateData["lastLoginProvider"] = lastLoginProvider
        if (!updateData.containsKey("role")) {
            updateData["role"] = "patient"
        }

        userRef.set(updateData, SetOptions.merge()).await()
    }

    suspend fun logout() {
        auth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(normalizeEmail(email)).await()
    }

    private suspend fun reauthenticate(currentPassword: String) {
        val user = auth.currentUser ?: throw Exception("User is not authenticated")
        val email = user.email ?: throw Exception("A current password is not available for this account")
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).await()
    }

    suspend fun updateEmail(newEmail: String) {
        auth.currentUser?.verifyBeforeUpdateEmail(normalizeEmail(newEmail))?.await()
    }

    suspend fun updateEmail(currentPassword: String, newEmail: String) {
        if (hasPasswordAuthProvider()) {
            reauthenticate(currentPassword)
        }
        val user = auth.currentUser ?: throw Exception("User is not authenticated")
        val normalizedEmail = normalizeEmail(newEmail)
        user.updateEmail(normalizedEmail).await()
        firestore.collection("users").document(user.uid)
            .set(mapOf("email" to normalizedEmail), SetOptions.merge())
            .await()
    }

    suspend fun updatePassword(currentPassword: String, newPassword: String) {
        if (hasPasswordAuthProvider()) {
            reauthenticate(currentPassword)
        }
        val user = auth.currentUser ?: throw Exception("User is not authenticated")
        user.updatePassword(newPassword).await()
    }

    suspend fun createUserDocument(
        userId: String,
        username: String,
        email: String
    ) {
        val userData = hashMapOf(
            "username" to username,
            "email" to normalizeEmail(email),
            "role" to "patient",
            "createdAt" to Date()
        )
        firestore.collection("users").document(userId).set(userData, SetOptions.merge()).await()
    }

    suspend fun logActivity(type: String, message: String, userId: String?) {
        // Redact PII (emails) from log messages to protect user privacy
        val sanitizedMessage = message.replace(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"), "[REDACTED_EMAIL]")
        val logData = hashMapOf(
            "type" to type,
            "message" to sanitizedMessage,
            "userId" to userId,
            "timestamp" to Date()
        )
        try {
            firestore.collection("activity_logs").add(logData).await()
        } catch (e: Exception) {
            // Silently fail logging
        }
    }

    companion object {
        private const val TAG = "AuthDataSource"

        fun normalizeEmail(email: String): String = email.trim().lowercase(Locale.ROOT)
    }
}

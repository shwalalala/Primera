package cit.edu.primera.feature.auth.data

import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
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

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUp(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw Exception("User creation failed")
    }

    suspend fun loginWithGoogle(idToken: String): String {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: throw Exception("Google login failed")
        try {
            ensureSocialUserDocument(user.uid, user.displayName, user.email, "Google User")
        } catch (e: Exception) {
            Log.e(TAG, "Google authentication succeeded, but profile sync failed", e)
        }
        return user.uid
    }

    suspend fun loginWithFacebook(accessToken: String): String {
        val credential = FacebookAuthProvider.getCredential(accessToken)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: throw Exception("Facebook login failed")
        try {
            ensureSocialUserDocument(user.uid, user.displayName, user.email, "Facebook User")
        } catch (e: Exception) {
            Log.e(TAG, "Facebook authentication succeeded, but profile sync failed", e)
        }
        return user.uid
    }

    private suspend fun ensureSocialUserDocument(
        userId: String,
        displayName: String?,
        email: String?,
        fallbackUsername: String
    ) {
        val userDocument = firestore.collection("users").document(userId)
            .get()
            .await()
        if (userDocument.exists()) return

        val username = displayName?.takeIf { it.isNotBlank() } ?: fallbackUsername
        val userData = hashMapOf<String, Any>(
            "username" to username,
            "email" to (email ?: ""),
            "role" to "patient"
        )

        userDocument.reference
            .set(userData, SetOptions.merge())
            .await()
    }

    suspend fun logout() {
        auth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    private suspend fun reauthenticate(currentPassword: String) {
        val user = auth.currentUser ?: throw Exception("User is not authenticated")
        val email = user.email ?: throw Exception("A current password is not available for this account")
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).await()
    }

    suspend fun updateEmail(newEmail: String) {
        auth.currentUser?.verifyBeforeUpdateEmail(newEmail)?.await()
    }

    suspend fun updateEmail(currentPassword: String, newEmail: String) {
        if (hasPasswordAuthProvider()) {
            reauthenticate(currentPassword)
        }
        val user = auth.currentUser ?: throw Exception("User is not authenticated")
        user.updateEmail(newEmail).await()
        firestore.collection("users").document(user.uid)
            .set(mapOf("email" to newEmail), SetOptions.merge())
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
            "email" to email,
            "role" to "patient",
            "createdAt" to Date()
        )
        firestore.collection("users").document(userId).set(userData).await()
    }

    suspend fun logActivity(type: String, message: String, userId: String?) {
        val logData = hashMapOf(
            "type" to type,
            "message" to message,
            "userId" to userId,
            "timestamp" to Date()
        )
        try {
            firestore.collection("activity_logs").add(logData).await()
        } catch (e: Exception) {
            // Silently fail logging
        }
    }

    private companion object {
        const val TAG = "AuthDataSource"
    }
}

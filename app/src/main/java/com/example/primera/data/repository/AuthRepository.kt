package com.example.primera.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun isUserAuthenticated(): Boolean = auth.currentUser != null

    suspend fun logout() {
        val email = auth.currentUser?.email ?: "Unknown"
        logActivity("Logout", "User logged out: $email")
        auth.signOut()
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            logActivity("Login", "User logged in: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(fullName: String, email: String, password: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("User creation failed")

            // Create user document
            val userData = hashMapOf(
                "fullName" to fullName,
                "email" to email,
                "createdAt" to Date()
            )
            firestore.collection("users").document(userId).set(userData).await()

            // Verbose log in Firebase
            logActivity("Sign Up", "New user registered: $email with name $fullName", userId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveTranscription(
        text: String,
        audioDurationMs: Long,
        confidence: Float,
        languageCode: String = "en-US"
    ): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val transcriptionData = hashMapOf(
                "userId" to userId,
                "text" to text,
                "timestamp" to Date(),
                "verboseMetadata" to hashMapOf(
                    "audioDurationMs" to audioDurationMs,
                    "confidenceScore" to confidence,
                    "languageCode" to languageCode,
                    "deviceInfo" to android.os.Build.MODEL,
                    "version" to "1.0.0"
                )
            )
            firestore.collection("transcriptions").add(transcriptionData).await()
            logActivity("Transcription", "Verbose transcription saved (length: ${text.length})")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun logActivity(type: String, message: String, userId: String? = null) {
        val logData = hashMapOf(
            "type" to type,
            "message" to message,
            "userId" to (userId ?: auth.currentUser?.uid),
            "timestamp" to Date()
        )
        try {
            firestore.collection("activity_logs").add(logData).await()
        } catch (e: Exception) {
            // Silently fail logging if it breaks
        }
    }
}

package com.example.primera.feature.auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class AuthDataSource {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun isUserAuthenticated(): Boolean = auth.currentUser != null

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUp(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw Exception("User creation failed")
    }

    suspend fun logout() {
        auth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun updateEmail(newEmail: String) {
        auth.currentUser?.updateEmail(newEmail)?.await()
    }

    suspend fun updatePassword(newPassword: String) {
        auth.currentUser?.updatePassword(newPassword)?.await()
    }

    suspend fun createUserDocument(
        userId: String,
        firstName: String,
        lastName: String,
        middleName: String,
        email: String
    ) {
        val userData = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "middleName" to middleName,
            "fullName" to "$firstName $lastName",
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
}

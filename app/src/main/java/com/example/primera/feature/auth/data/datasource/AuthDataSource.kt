package com.example.primera.feature.auth.data.datasource

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

    suspend fun createUserDocument(userId: String, fullName: String, email: String) {
        val userData = hashMapOf(
            "fullName" to fullName,
            "email" to email,
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

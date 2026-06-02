package com.example.primera.feature.auth.data

interface AuthRepository {
    fun isUserAuthenticated(): Boolean
    fun getCurrentUserId(): String?
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun signUp(firstName: String, lastName: String, middleName: String, email: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun logActivity(type: String, message: String): Result<Unit>
}

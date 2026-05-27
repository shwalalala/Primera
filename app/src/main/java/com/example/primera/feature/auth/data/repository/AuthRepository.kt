package com.example.primera.feature.auth.data.repository

interface AuthRepository {
    fun isUserAuthenticated(): Boolean
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun signUp(fullName: String, email: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
}

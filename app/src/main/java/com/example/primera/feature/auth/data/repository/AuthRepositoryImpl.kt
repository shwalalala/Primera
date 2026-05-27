package com.example.primera.feature.auth.data.repository

import com.example.primera.feature.auth.data.datasource.AuthDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val dataSource: AuthDataSource
) : AuthRepository {

    override fun isUserAuthenticated(): Boolean = dataSource.isUserAuthenticated()

    override suspend fun logout() {
        val email = dataSource.getCurrentUserEmail() ?: "Unknown"
        logActivity("Logout", "User logged out: $email")
        dataSource.signOut()
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            dataSource.login(email, password)
            logActivity("Login", "User logged in: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(fullName: String, email: String, password: String): Result<Unit> {
        return try {
            val userId = dataSource.signUp(email, password)
            dataSource.createUserDocument(userId, fullName, email)
            logActivity("Sign Up", "New user registered: $email with name $fullName", userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun logActivity(type: String, message: String, userId: String? = null) {
        try {
            dataSource.logActivity(type, message, userId)
        } catch (e: Exception) {
            // Silently fail logging if it breaks
        }
    }
}

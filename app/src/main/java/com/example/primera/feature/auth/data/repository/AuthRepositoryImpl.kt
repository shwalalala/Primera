package com.example.primera.feature.auth.data.repository

import com.example.primera.feature.auth.data.datasource.AuthDataSource

class AuthRepositoryImpl(
    private val dataSource: AuthDataSource
) : AuthRepository {

    override fun isUserAuthenticated(): Boolean = dataSource.isUserAuthenticated()

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            dataSource.login(email, password)
            dataSource.logActivity("Login", "User logged in: $email", dataSource.getCurrentUserId())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(fullName: String, email: String, password: String): Result<Unit> {
        return try {
            val userId = dataSource.signUp(email, password)
            dataSource.createUserDocument(userId, fullName, email)
            dataSource.logActivity("Sign Up", "New user registered: $email with name $fullName", userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val userId = dataSource.getCurrentUserId()
            dataSource.logActivity("Logout", "User logged out", userId)
            dataSource.logout()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

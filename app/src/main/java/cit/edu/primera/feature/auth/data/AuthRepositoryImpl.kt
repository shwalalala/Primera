package cit.edu.primera.feature.auth.data

import com.google.firebase.auth.AuthCredential

class AuthRepositoryImpl(
    private val dataSource: AuthDataSource
) : AuthRepository {

    override fun isUserAuthenticated(): Boolean = dataSource.isUserAuthenticated()

    override fun getCurrentUserId(): String? = dataSource.getCurrentUserId()

    override fun hasPasswordAuthProvider(): Boolean = dataSource.hasPasswordAuthProvider()

    override fun getAuthProviders(): Set<String> = dataSource.getAuthProviders()

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            dataSource.login(email, password)
            dataSource.logActivity("Login", "User logged in", dataSource.getCurrentUserId())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<Unit> {
        return try {
            val userId = dataSource.loginWithGoogle(idToken)
            dataSource.logActivity("Login", "User logged in with Google", userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithFacebook(accessToken: String): Result<Unit> {
        return try {
            val userId = dataSource.loginWithFacebook(accessToken)
            dataSource.logActivity("Login", "User logged in with Facebook", userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun linkCredential(credential: AuthCredential): Result<Unit> {
        return try {
            dataSource.linkCredential(credential)
            dataSource.logActivity("Account Linking", "Credential linked", dataSource.getCurrentUserId())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlinkProvider(providerId: String): Result<Unit> {
        return try {
            dataSource.unlinkProvider(providerId)
            dataSource.logActivity("Account Unlinking", "Provider unlinked: $providerId", dataSource.getCurrentUserId())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(
        username: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            val userId = dataSource.signUp(email, password)
            dataSource.createUserDocument(userId, username, email)
            dataSource.logActivity("Sign Up", "New user registered", userId)
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

    override suspend fun logActivity(type: String, message: String): Result<Unit> {
        return try {
            dataSource.logActivity(type, message, dataSource.getCurrentUserId())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            dataSource.sendPasswordResetEmail(email)
            dataSource.logActivity("Password Reset", "Password reset email requested", dataSource.getCurrentUserId())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEmail(currentPassword: String, newEmail: String): Result<Unit> {
        return try {
            dataSource.updateEmail(currentPassword, newEmail)
            dataSource.logActivity("Email Update", "User updated email", dataSource.getCurrentUserId())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            dataSource.updatePassword(currentPassword, newPassword)
            dataSource.logActivity("Password Update", "User updated password", dataSource.getCurrentUserId())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

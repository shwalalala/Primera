package cit.edu.primera.feature.auth.data

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
            dataSource.logActivity("Login", "User logged in: $email", dataSource.getCurrentUserId())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<Unit> {
        return try {
            val userId = dataSource.loginWithGoogle(idToken)
            // Note: In a real app, you might also want to check if the user document exists 
            // and create it via `createUserDocument` if it's their first time logging in with Google.
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

    override suspend fun signUp(
        username: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            val userId = dataSource.signUp(email, password)
            dataSource.createUserDocument(userId, username, email)
            dataSource.logActivity("Sign Up", "New user registered: $email with username $username", userId)
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
            dataSource.logActivity("Password Reset", "Requested for: $email", dataSource.getCurrentUserId())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEmail(currentPassword: String, newEmail: String): Result<Unit> {
        return try {
            dataSource.updateEmail(currentPassword, newEmail)
            dataSource.logActivity("Email Update", "Updated to: $newEmail", dataSource.getCurrentUserId())
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

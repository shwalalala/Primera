package cit.edu.primera.feature.auth.data

class AuthRepositoryImpl(
    private val dataSource: AuthDataSource
) : AuthRepository {

    override fun isUserAuthenticated(): Boolean = dataSource.isUserAuthenticated()

    override fun getCurrentUserId(): String? = dataSource.getCurrentUserId()

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            dataSource.login(email, password)
            dataSource.logActivity("Login", "User logged in: $email", dataSource.getCurrentUserId())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(
        firstName: String,
        lastName: String,
        middleName: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            val userId = dataSource.signUp(email, password)
            dataSource.createUserDocument(userId, firstName, lastName, middleName, email)
            dataSource.logActivity("Sign Up", "New user registered: $email with name $firstName $lastName", userId)
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

    override suspend fun updateEmail(newEmail: String): Result<Unit> {
        return try {
            dataSource.updateEmail(newEmail)
            dataSource.logActivity("Email Update", "Updated to: $newEmail", dataSource.getCurrentUserId())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            dataSource.updatePassword(newPassword)
            dataSource.logActivity("Password Update", "User updated password", dataSource.getCurrentUserId())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

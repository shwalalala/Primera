package cit.edu.primera.feature.auth.data

interface AuthRepository {
    fun isUserAuthenticated(): Boolean
    fun getCurrentUserId(): String?
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun signUp(firstName: String, lastName: String, middleName: String, email: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun logActivity(type: String, message: String): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun updateEmail(newEmail: String): Result<Unit>
    suspend fun updatePassword(newPassword: String): Result<Unit>
}

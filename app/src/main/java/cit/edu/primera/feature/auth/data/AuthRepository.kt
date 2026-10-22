package cit.edu.primera.feature.auth.data

import com.google.firebase.auth.AuthCredential

interface AuthRepository {
    fun isUserAuthenticated(): Boolean
    fun getCurrentUserId(): String?
    fun hasPasswordAuthProvider(): Boolean
    fun getAuthProviders(): Set<String>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun loginWithGoogle(idToken: String): Result<Unit>
    suspend fun loginWithFacebook(accessToken: String): Result<Unit>
    suspend fun linkCredential(credential: AuthCredential): Result<Unit>
    suspend fun unlinkProvider(providerId: String): Result<Unit>
    suspend fun signUp(username: String, email: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun logActivity(type: String, message: String): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun updateEmail(currentPassword: String, newEmail: String): Result<Unit>
    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit>
}

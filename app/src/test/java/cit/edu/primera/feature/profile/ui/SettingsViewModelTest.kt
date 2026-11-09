package cit.edu.primera.feature.profile.ui

import cit.edu.primera.feature.auth.data.AuthRepository
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: TestAuthRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = TestAuthRepository()
        viewModel = SettingsViewModel(fakeAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun requestEmailChange_withInvalidEmail_setsErrorState() = runTest {
        viewModel.onNewEmailChange("invalid-email")
        viewModel.requestEmailChange()

        val state = viewModel.uiState.value
        assertEquals("Invalid email format", state.error)
        assertNull(state.successMessage)
    }

    @Test
    fun requestEmailChange_withValidEmail_setsSuccessMessage() = runTest {
        viewModel.onNewEmailChange("new.user@example.com")
        viewModel.requestEmailChange()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.error)
        assertNotNull(state.successMessage)
        assertEquals("", state.newEmail)
    }

    @Test
    fun deleteAccount_success_callsCallback() = runTest {
        var callbackCalled = false
        viewModel.deleteAccount {
            callbackCalled = true
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, callbackCalled)
    }
}

private class TestAuthRepository : AuthRepository {
    private var authenticated = true

    override fun isUserAuthenticated(): Boolean = authenticated
    override fun getCurrentUserId(): String = "user_123"
    override fun hasPasswordAuthProvider(): Boolean = true
    override fun getAuthProviders(): Set<String> = setOf("password")

    override suspend fun login(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun loginWithGoogle(idToken: String): Result<Boolean> = Result.success(false)
    override suspend fun loginWithFacebook(accessToken: String): Result<Boolean> = Result.success(false)
    override suspend fun linkCredential(credential: AuthCredential): Result<Unit> = Result.success(Unit)
    override suspend fun unlinkProvider(providerId: String): Result<Unit> = Result.success(Unit)
    override suspend fun signUp(username: String, email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun logout(): Result<Unit> {
        authenticated = false
        return Result.success(Unit)
    }
    override suspend fun logActivity(type: String, message: String): Result<Unit> = Result.success(Unit)
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.success(Unit)
    override suspend fun requestEmailChange(newEmail: String): Result<Unit> = Result.success(Unit)
    override suspend fun updateEmail(currentPassword: String, newEmail: String): Result<Unit> = Result.success(Unit)
    override suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> = Result.success(Unit)
    override suspend fun deleteAccount(): Result<Unit> {
        authenticated = false
        return Result.success(Unit)
    }
}

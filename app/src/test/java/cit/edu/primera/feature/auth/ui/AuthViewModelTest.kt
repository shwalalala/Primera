package cit.edu.primera.feature.auth.ui

import cit.edu.primera.core.data.PreferenceRepository
import cit.edu.primera.feature.auth.data.AuthRepository
import cit.edu.primera.feature.auth.domain.PasswordRuleViolation
import cit.edu.primera.feature.auth.domain.PasswordValidator
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakePreferenceRepository: FakePreferenceRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        fakePreferenceRepository = FakePreferenceRepository()
        viewModel = AuthViewModel(fakeAuthRepository, fakePreferenceRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun passwordValidator_validatesMinLengthAndCharacterTypes() {
        val weakResult = PasswordValidator.validate("123")
        assertFalse(weakResult.isValid)
        assertTrue(weakResult.violations.contains(PasswordRuleViolation.TOO_SHORT))

        val noDigitResult = PasswordValidator.validate("abcdefgh")
        assertFalse(noDigitResult.isValid)
        assertTrue(noDigitResult.violations.contains(PasswordRuleViolation.MISSING_DIGIT_OR_SYMBOL))

        val validResult = PasswordValidator.validate("Password123!")
        assertTrue(validResult.isValid)
        assertTrue(validResult.violations.isEmpty())
    }

    @Test
    fun passwordValidator_calculatesStrengthLevels() {
        assertEquals(PasswordStrength.NONE, PasswordValidator.calculateStrength(""))
        assertEquals(PasswordStrength.WEAK, PasswordValidator.calculateStrength("12345"))
        assertEquals(PasswordStrength.FAIR, PasswordValidator.calculateStrength("abcdef12"))
        assertEquals(PasswordStrength.STRONG, PasswordValidator.calculateStrength("Pass1234!"))
    }

    @Test
    fun loadSavedPreferences_restoresEmailWhenRememberMeWasEnabled() {
        fakePreferenceRepository.setSavedEmail("remembered@example.com")
        val newViewModel = AuthViewModel(fakeAuthRepository, fakePreferenceRepository)

        val state = newViewModel.uiState.value
        assertEquals("remembered@example.com", state.email)
        assertTrue(state.rememberMe)
    }

    @Test
    fun onNavigateToRegisterClicked_wipesPasswordStateAndEmitsEffect() = runTest {
        viewModel.onPasswordChange("Secret123!")
        viewModel.onConfirmPasswordChange("Secret123!")

        viewModel.onNavigateToRegisterClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)

        val effect = viewModel.effect.first()
        assertTrue(effect is AuthEffect.NavigateToRegister)
    }

    @Test
    fun onNavigateToLoginClicked_wipesPasswordStateAndEmitsEffect() = runTest {
        viewModel.onPasswordChange("Secret123!")

        viewModel.onNavigateToLoginClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.password)

        val effect = viewModel.effect.first()
        assertTrue(effect is AuthEffect.NavigateToLogin)
    }

    @Test
    fun logout_clearsAuthStateAndEmitsNavigateToLogin() = runTest {
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertEquals("", state.password)

        val effect = viewModel.effect.first()
        assertTrue(effect is AuthEffect.NavigateToLogin)
    }

    @Test
    fun onDismissAccountLinkingDialog_clearsPendingState() {
        viewModel.onDismissAccountLinkingDialog()
        val state = viewModel.uiState.value
        assertFalse(state.showAccountLinkingDialog)
        assertNull(state.pendingSocialProvider)
    }
}

private class FakePreferenceRepository : PreferenceRepository {
    private var onboardingCompleted = false
    private var onboardingStep: String? = null
    private var watchSyncEnabled = false
    private var customOptions = mutableMapOf<String, Set<String>>()
    private var everLoggedIn = false
    private var savedEmail: String? = null

    override suspend fun shouldShowOnboarding(userId: String): Boolean = !onboardingCompleted

    override fun setOnboardingCompleted(userId: String) {
        onboardingCompleted = true
    }

    override fun getOnboardingLastStep(userId: String): String? = onboardingStep

    override fun setOnboardingLastStep(userId: String, step: String) {
        onboardingStep = step
    }

    override fun isWatchSyncEnabled(userId: String): Boolean = watchSyncEnabled

    override fun setWatchSyncEnabled(userId: String, enabled: Boolean) {
        watchSyncEnabled = enabled
    }

    override fun getCustomOptions(userId: String, category: String): Set<String> = customOptions[category] ?: emptySet()

    override fun addCustomOption(userId: String, category: String, label: String) {
        val set = (customOptions[category] ?: emptySet()).toMutableSet()
        set.add(label)
        customOptions[category] = set
    }

    override fun hasEverLoggedIn(): Boolean = everLoggedIn

    override fun setHasEverLoggedIn(hasLoggedIn: Boolean) {
        everLoggedIn = hasLoggedIn
    }

    override fun getSavedEmail(): String? = savedEmail

    override fun setSavedEmail(email: String?) {
        savedEmail = email
    }

    override fun clearLocalData() {
        onboardingCompleted = false
        onboardingStep = null
        watchSyncEnabled = false
        customOptions.clear()
    }
}

private class FakeAuthRepository : AuthRepository {
    private var authenticated = false
    private var userId: String? = "test_user_id"
    private var providers = mutableSetOf("password")

    override fun isUserAuthenticated(): Boolean = authenticated

    override fun getCurrentUserId(): String? = userId

    override fun hasPasswordAuthProvider(): Boolean = providers.contains("password")

    override fun getAuthProviders(): Set<String> = providers

    override suspend fun login(email: String, password: String): Result<Unit> {
        authenticated = true
        return Result.success(Unit)
    }

    override suspend fun loginWithGoogle(idToken: String): Result<Unit> {
        authenticated = true
        providers.add("google.com")
        return Result.success(Unit)
    }

    override suspend fun loginWithFacebook(accessToken: String): Result<Unit> {
        authenticated = true
        providers.add("facebook.com")
        return Result.success(Unit)
    }

    override suspend fun linkCredential(credential: AuthCredential): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun unlinkProvider(providerId: String): Result<Unit> {
        if (providers.size <= 1) return Result.failure(Exception("Cannot unlink sole provider"))
        providers.remove(providerId)
        return Result.success(Unit)
    }

    override suspend fun signUp(username: String, email: String, password: String): Result<Unit> {
        authenticated = true
        return Result.success(Unit)
    }

    override suspend fun logout(): Result<Unit> {
        authenticated = false
        return Result.success(Unit)
    }

    override suspend fun logActivity(type: String, message: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun updateEmail(currentPassword: String, newEmail: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return Result.success(Unit)
    }
}

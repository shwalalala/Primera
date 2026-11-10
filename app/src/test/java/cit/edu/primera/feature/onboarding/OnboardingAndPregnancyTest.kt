package cit.edu.primera.feature.onboarding

import cit.edu.primera.core.data.PreferenceRepository
import cit.edu.primera.core.util.PregnancyCalculator
import cit.edu.primera.feature.auth.data.AuthRepository
import cit.edu.primera.feature.auth.ui.AuthEffect
import cit.edu.primera.feature.auth.ui.AuthViewModel
import cit.edu.primera.feature.dashboard.domain.DashboardBusinessLogic
import cit.edu.primera.feature.onboarding.data.OnboardingRepository
import cit.edu.primera.feature.onboarding.domain.OnboardingProfile
import cit.edu.primera.feature.onboarding.ui.OnboardingStep
import cit.edu.primera.feature.onboarding.ui.OnboardingViewModel
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingAndPregnancyTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakePreferenceRepository: FakePreferenceRepository
    private lateinit var fakeOnboardingRepository: FakeOnboardingRepository

    private lateinit var authViewModel: AuthViewModel
    private lateinit var onboardingViewModel: OnboardingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        fakePreferenceRepository = FakePreferenceRepository()
        fakeOnboardingRepository = FakeOnboardingRepository()

        authViewModel = AuthViewModel(fakeAuthRepository, fakePreferenceRepository)
        onboardingViewModel =
            OnboardingViewModel(fakeOnboardingRepository, fakePreferenceRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =========================================================================
    // 1. REGISTRATION AS A NEW PERSON TESTS
    // =========================================================================

    @Test
    fun register_newPerson_withInvalidDetails_showsValidationErrors() {
        // Leave username empty
        authViewModel.onUsernameChange("")
        authViewModel.onEmailChange("invalid-email")
        authViewModel.onPasswordChange("123") // Weak password
        authViewModel.onConfirmPasswordChange("1234") // Password mismatch
        authViewModel.onAgreedToTermsToggle(false)

        authViewModel.onSignUpClicked()

        val state = authViewModel.uiState.value
        assertEquals("Username is required", state.usernameError)
        assertEquals("Invalid email format", state.emailError)
        assertNotNull(state.passwordError)
        assertEquals("Passwords do not match", state.confirmPasswordError)
        assertEquals("You must agree to terms", state.termsError)
        assertFalse(state.isAuthenticated)
    }

    @Test
    fun register_newPerson_withValidDetails_succeedsAndNavigatesToOnboarding() = runTest {
        authViewModel.onUsernameChange("JaneDoe")
        authViewModel.onEmailChange("janedoe@example.com")
        authViewModel.onPasswordChange("StrongPass123!")
        authViewModel.onConfirmPasswordChange("StrongPass123!")
        authViewModel.onAgreedToTermsToggle(true)

        authViewModel.onSignUpClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = authViewModel.uiState.value
        assertTrue(state.isAuthenticated)

        val effect = authViewModel.effect.first()
        assertTrue(
            "Expected NavigateToOnboarding effect",
            effect is AuthEffect.NavigateToOnboarding
        )
    }

    // =========================================================================
    // 2. ONBOARDING FLOW & NAVIGATION TESTS (REAL PERSON JOURNEY)
    // =========================================================================

    @Test
    fun onboarding_stepByStep_regularCycle_firstPregnancy_calculatesPregnancyLogicAccurately() =
        runTest {
            // Step 1: Birthday
            assertEquals(OnboardingStep.BIRTHDAY, onboardingViewModel.uiState.value.currentStep)
            val birthdayCal = Calendar.getInstance().apply { add(Calendar.YEAR, -28) }
            onboardingViewModel.onBirthdayChange(birthdayCal.time)
            onboardingViewModel.nextStep()

            // Step 2: Body Measurements (Weight & Height on same slide)
            assertEquals(OnboardingStep.BODY_MEASUREMENTS, onboardingViewModel.uiState.value.currentStep)
            onboardingViewModel.onWeightChange(62)
            onboardingViewModel.onHeightChange(165)
            onboardingViewModel.nextStep()

            // Step 4: Cycle Regularity
            assertEquals(
                OnboardingStep.CYCLE_REGULARITY,
                onboardingViewModel.uiState.value.currentStep
            )
            onboardingViewModel.onIsCycleRegularChange(true)
            onboardingViewModel.nextStep()

            // Step 5: LMP (Regular cycle goes straight to LMP)
            assertEquals(OnboardingStep.LMP, onboardingViewModel.uiState.value.currentStep)
            // Set LMP to 70 days ago (10 weeks gestation)
            val lmpCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -70) }
            onboardingViewModel.onLmpDateChange(lmpCal.time)
            onboardingViewModel.nextStep()

            // Step 6: EDD
            assertEquals(OnboardingStep.EDD, onboardingViewModel.uiState.value.currentStep)
            val stateAfterLmp = onboardingViewModel.uiState.value
            assertNotNull("EDD should be calculated automatically from LMP", stateAfterLmp.eddDate)

            // Verify Pregnancy Logic Accuracy for 10 weeks gestation:
            val expectedEdd = Calendar.getInstance().apply {
                time = lmpCal.time
                add(Calendar.DAY_OF_YEAR, 280)
            }.time
            assertEquals(expectedEdd.time / 1000, stateAfterLmp.eddDate!!.time / 1000)

            val gestationalWeeks = PregnancyCalculator.getGestationalWeeks(stateAfterLmp.eddDate)
            assertEquals(10, gestationalWeeks)
            assertEquals("first trimester", DashboardBusinessLogic.getTrimester(gestationalWeeks))
            assertEquals("Date", DashboardBusinessLogic.getBabySize(gestationalWeeks))

            onboardingViewModel.nextStep()

            // Step 7: First Pregnancy
            assertEquals(
                OnboardingStep.FIRST_PREGNANCY,
                onboardingViewModel.uiState.value.currentStep
            )
            onboardingViewModel.onIsFirstPregnancyChange(true)
            onboardingViewModel.nextStep()

            // Step 8: Review Summary (First pregnancy skips past pregnancy history)
            assertEquals(
                OnboardingStep.REVIEW_SUMMARY,
                onboardingViewModel.uiState.value.currentStep
            )
            onboardingViewModel.nextStep()

            // Step 9: Preparing & Finishing
            assertEquals(OnboardingStep.PREPARING, onboardingViewModel.uiState.value.currentStep)
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(onboardingViewModel.uiState.value.isCompleted)
            assertNotNull(fakeOnboardingRepository.savedProfile)

            val savedProfile = fakeOnboardingRepository.savedProfile!!
            assertEquals(62, savedProfile.weightKg)
            assertEquals(165, savedProfile.heightCm)
            assertTrue(savedProfile.isCycleRegular == true)
            assertTrue(savedProfile.isFirstPregnancy == true)
            assertNotNull(savedProfile.eddDate)
        }

    @Test
    fun onboarding_stepByStep_irregularCycle_withUltrasoundScan_calculatesEddAndGestationalAgeAccurately() =
        runTest {
            // Step 1-2: Birthday, Body Measurements
            onboardingViewModel.nextStep() // BODY_MEASUREMENTS
            onboardingViewModel.nextStep() // CYCLE_REGULARITY

            // Step 4: Irregular Cycle
            assertEquals(
                OnboardingStep.CYCLE_REGULARITY,
                onboardingViewModel.uiState.value.currentStep
            )
            onboardingViewModel.onIsCycleRegularChange(false)
            onboardingViewModel.nextStep()

            // Step 5: Cycle Variance
            assertEquals(
                OnboardingStep.CYCLE_VARIANCE,
                onboardingViewModel.uiState.value.currentStep
            )
            onboardingViewModel.onShortestCycleChange(25)
            onboardingViewModel.onLongestCycleChange(38)
            onboardingViewModel.nextStep()

            // Step 6: Had Ultrasound
            assertEquals(
                OnboardingStep.HAD_ULTRASOUND,
                onboardingViewModel.uiState.value.currentStep
            )
            onboardingViewModel.onHasHadUltrasoundChange(true)
            onboardingViewModel.nextStep() // Goes to ULTRASOUND step because hasHadUltrasound == true

            // Step 7: Ultrasound details (Fetus was 18 weeks 0 days at scan date 14 days ago -> Fetus is 20 weeks today)
            assertEquals(OnboardingStep.ULTRASOUND, onboardingViewModel.uiState.value.currentStep)
            val scanDateCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -14) }
            onboardingViewModel.onScanDateChange(scanDateCal.time)
            onboardingViewModel.onScanWeeksChange(18)
            onboardingViewModel.onScanDaysChange(0)
            onboardingViewModel.nextStep()

            // Step 8: First Pregnancy
            assertEquals(
                OnboardingStep.FIRST_PREGNANCY,
                onboardingViewModel.uiState.value.currentStep
            )
            val stateAfterScan = onboardingViewModel.uiState.value
            assertNotNull("EDD should be calculated from Ultrasound scan", stateAfterScan.eddDate)

            // Fetus was 18 weeks at scan 2 weeks ago -> 20 weeks today
            val weeksFromUltrasound =
                PregnancyCalculator.getGestationalWeeks(stateAfterScan.eddDate)
            assertEquals(20, weeksFromUltrasound)
            assertEquals(
                "second trimester",
                DashboardBusinessLogic.getTrimester(weeksFromUltrasound)
            )
            assertEquals("Mango", DashboardBusinessLogic.getBabySize(weeksFromUltrasound))
            assertEquals("🥭", DashboardBusinessLogic.getBabyEmoji(weeksFromUltrasound))
        }

    @Test
    fun onboarding_stepByStep_pastPregnanciesHistory_addsAndRemovesPregnanciesWithComplications() =
        runTest {
            // Navigate to FIRST_PREGNANCY
            onboardingViewModel.onIsCycleRegularChange(true)
            onboardingViewModel.onLmpDateChange(
                Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -140) }.time
            ) // 20 weeks
            onboardingViewModel.nextStep() // BODY_MEASUREMENTS
            onboardingViewModel.nextStep() // CYCLE_REGULARITY
            onboardingViewModel.nextStep() // LMP
            onboardingViewModel.nextStep() // EDD
            onboardingViewModel.nextStep() // FIRST_PREGNANCY

            assertEquals(
                OnboardingStep.FIRST_PREGNANCY,
                onboardingViewModel.uiState.value.currentStep
            )
            onboardingViewModel.onIsFirstPregnancyChange(false)
            onboardingViewModel.nextStep()

            // Step: PREGNANCY_HISTORY
            assertEquals(
                OnboardingStep.PREGNANCY_HISTORY,
                onboardingViewModel.uiState.value.currentStep
            )

            // Configure first past pregnancy
            val deliveryDate1 = Calendar.getInstance().apply { add(Calendar.YEAR, -3) }.time
            onboardingViewModel.onHistoryDeliveryDateChange(deliveryDate1)
            onboardingViewModel.onDeliveryTypeChange("Vaginal")
            onboardingViewModel.onChildrenDeliveredChange("1")
            onboardingViewModel.toggleStructuredComplication("GDM")
            onboardingViewModel.onAreChildrenLivingChange(true)

            // Add second past pregnancy
            onboardingViewModel.addPregnancy()
            assertEquals(2, onboardingViewModel.uiState.value.pregnancyHistories.size)
            assertEquals(1, onboardingViewModel.uiState.value.selectedPregnancyIndex)

            val deliveryDate2 = Calendar.getInstance().apply { add(Calendar.YEAR, -1) }.time
            onboardingViewModel.onHistoryDeliveryDateChange(deliveryDate2)
            onboardingViewModel.onDeliveryTypeChange("C-Section")
            onboardingViewModel.onChildrenDeliveredChange("1")
            onboardingViewModel.toggleStructuredComplication("Pre-eclampsia")

            // Verify pregnancy history state
            val histories = onboardingViewModel.uiState.value.pregnancyHistories
            assertEquals(2, histories.size)
            assertTrue(histories[0].hasGdm)
            assertFalse(histories[0].hasPreeclampsia)
            assertTrue(histories[1].hasPreeclampsia)
            assertEquals("C-Section", histories[1].deliveryType)

            // Remove second pregnancy
            onboardingViewModel.removePregnancy(1)
            assertEquals(1, onboardingViewModel.uiState.value.pregnancyHistories.size)

            onboardingViewModel.nextStep() // REVIEW_SUMMARY
            assertEquals(
                OnboardingStep.REVIEW_SUMMARY,
                onboardingViewModel.uiState.value.currentStep
            )
        }

    // =========================================================================
    // 3. PREGNANCY LOGIC ACCURACY & EDD CALCULATIONS
    // =========================================================================

    @Test
    fun pregnancyLogic_lmpToEdd_calculatesCorrectGestationalWeeksDaysTrimesterAndBabySize() {
        // Test LMP 84 days ago (12 weeks)
        val lmp = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -84) }.time
        val calculatedEdd = PregnancyCalculator.calculateEdd(lmpDate = lmp)
        assertNotNull(calculatedEdd)

        val weeks = PregnancyCalculator.getGestationalWeeks(calculatedEdd)
        val dayOfWeek = PregnancyCalculator.getGestationalDayOfCurrentWeek(calculatedEdd)
        val daysLeft = PregnancyCalculator.getDaysLeft(calculatedEdd)

        assertEquals(12, weeks)
        assertEquals(0, dayOfWeek)
        assertEquals(196, daysLeft) // 280 - 84 = 196
        assertEquals("first trimester", DashboardBusinessLogic.getTrimester(weeks))
        assertEquals("Plum", DashboardBusinessLogic.getBabySize(weeks))
    }

    @Test
    fun pregnancyLogic_ultrasoundScan_calculatesCorrectGestationalAgeAndTrimester() {
        // Fetus was 28 weeks 3 days at scan 10 days ago -> 29 weeks 6 days today (30 weeks)
        val scanDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -10) }.time
        val calculatedEdd = PregnancyCalculator.calculateEdd(
            scanDate = scanDate,
            scanWeeks = 28,
            scanDays = 3
        )
        assertNotNull(calculatedEdd)

        val weeks = PregnancyCalculator.getGestationalWeeks(calculatedEdd)
        assertEquals(29, weeks) // 28w 3d + 10d = 29w 6d (which is 29 completed weeks)
        assertEquals("third trimester", DashboardBusinessLogic.getTrimester(weeks))
        assertEquals("Pumpkin", DashboardBusinessLogic.getBabySize(weeks))
        assertEquals("🎃", DashboardBusinessLogic.getBabyEmoji(weeks))
    }

    @Test
    fun pregnancyLogic_positiveTestDate_calculatesTentativeEddAndGestationalAge() {
        // Positive test 28 days ago (4 weeks ago -> 8 weeks gestation assuming 4 weeks at test)
        val testDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -28) }.time
        val calculatedEdd = PregnancyCalculator.calculateEdd(positiveTestDate = testDate)
        assertNotNull(calculatedEdd)

        val weeks = PregnancyCalculator.getGestationalWeeks(calculatedEdd)
        assertEquals(8, weeks)
        assertEquals("first trimester", DashboardBusinessLogic.getTrimester(weeks))
        assertEquals("Raspberry", DashboardBusinessLogic.getBabySize(weeks))
    }

    @Test
    fun pregnancyLogic_allTrimesters_mapsToCorrectBabySizeAndEmoji() {
        // Trimester 1 (Week 4: Poppy seed)
        assertEquals("first trimester", DashboardBusinessLogic.getTrimester(4))
        assertEquals("Poppy seed", DashboardBusinessLogic.getBabySize(4))
        assertEquals("🌱", DashboardBusinessLogic.getBabyEmoji(4))

        // Trimester 2 (Week 16: Avocado)
        assertEquals("second trimester", DashboardBusinessLogic.getTrimester(16))
        assertEquals("Avocado", DashboardBusinessLogic.getBabySize(16))
        assertEquals("🥑", DashboardBusinessLogic.getBabyEmoji(16))

        // Trimester 2 (Week 24: Eggplant)
        assertEquals("second trimester", DashboardBusinessLogic.getTrimester(24))
        assertEquals("Eggplant", DashboardBusinessLogic.getBabySize(24))
        assertEquals("🍆", DashboardBusinessLogic.getBabyEmoji(24))

        // Trimester 3 (Week 38: Watermelon)
        assertEquals("third trimester", DashboardBusinessLogic.getTrimester(38))
        assertEquals("Watermelon", DashboardBusinessLogic.getBabySize(38))
        assertEquals("🍉", DashboardBusinessLogic.getBabyEmoji(38))

        // Full-term / Post-date (Week 41)
        assertEquals("third trimester", DashboardBusinessLogic.getTrimester(41))
        assertEquals("Your baby is full-term!", DashboardBusinessLogic.getBabySize(41))
        assertEquals("👶", DashboardBusinessLogic.getBabyEmoji(41))
    }

    @Test
    fun onboarding_previousStepNavigation_retainsUserDataAccurately() {
        onboardingViewModel.onWeightChange(58)
        onboardingViewModel.onHeightChange(165)
        onboardingViewModel.nextStep() // BIRTHDAY -> BODY_MEASUREMENTS
        onboardingViewModel.nextStep() // BODY_MEASUREMENTS -> CYCLE_REGULARITY

        assertEquals(OnboardingStep.CYCLE_REGULARITY, onboardingViewModel.uiState.value.currentStep)

        onboardingViewModel.previousStep() // Back to BODY_MEASUREMENTS
        assertEquals(OnboardingStep.BODY_MEASUREMENTS, onboardingViewModel.uiState.value.currentStep)
        assertEquals(58, onboardingViewModel.uiState.value.weightKg)
        assertEquals(165, onboardingViewModel.uiState.value.heightCm)

        onboardingViewModel.previousStep() // Back to BIRTHDAY
        assertEquals(OnboardingStep.BIRTHDAY, onboardingViewModel.uiState.value.currentStep)
    }

    @Test
    fun compositeCondition_multiUserAndNewDevice_triggersOnboardingAndTooltips() = runTest {
        val repo = fakePreferenceRepository
        val userA = "user_A"
        val userB = "user_B"

        // 1. Initial state for User A -> Should trigger onboarding and tooltips
        assertTrue("New User A should show onboarding", repo.shouldShowOnboarding(userA))
        assertFalse("New User A should see tooltip pop up", repo.hasSeenMicCoachmark(userA))

        // 2. User A completes onboarding and dismisses tooltip on Device 1
        repo.setOnboardingCompleted(userA)
        repo.setMicCoachmarkDismissed(userA)

        assertFalse("User A should not show onboarding again on same device", repo.shouldShowOnboarding(userA))
        assertTrue("User A should have seen tooltip on same device", repo.hasSeenMicCoachmark(userA))

        // 3. Shared device scenario: User B logs in on SAME physical device
        // Composite condition forces onboarding and tooltips to trigger for User B even if User A completed it
        assertTrue("User B on shared device should trigger onboarding", repo.shouldShowOnboarding(userB))
        assertFalse("User B on shared device should see tooltip pop up", repo.hasSeenMicCoachmark(userB))

        // 4. User B completes onboarding on shared device
        repo.setOnboardingCompleted(userB)
        repo.setMicCoachmarkDismissed(userB)

        assertFalse("User B should not show onboarding after completing", repo.shouldShowOnboarding(userB))
        assertTrue("User B should have seen tooltip after dismissing", repo.hasSeenMicCoachmark(userB))

        // 5. Switching back to User A on shared device: User B was last completed user, so User A is triggered
        assertTrue("User A should trigger onboarding when switching users on shared machine", repo.shouldShowOnboarding(userA))
    }
}

// =============================================================================
// FAKE REPOSITORIES FOR ISOLATED UNIT TESTING
// =============================================================================

private class FakeOnboardingRepository : OnboardingRepository {
    var savedProfile: OnboardingProfile? = null

    override suspend fun saveProfile(profile: OnboardingProfile): Result<Unit> {
        savedProfile = profile
        return Result.success(Unit)
    }
}

private class FakePreferenceRepository : PreferenceRepository {
    private var completedUsersOnDevice = mutableMapOf<String, Boolean>()
    private var lastCompletedUserId: String? = null
    private var seenMicCoachmarks = mutableMapOf<String, Boolean>()
    private var lastTooltipUserId: String? = null
    private var onboardingStep: String? = null
    private var savedEmail: String? = null
    private var everLoggedIn = false
    private var testDeviceId = "test_device_id"

    override fun getDeviceId(): String = testDeviceId

    override suspend fun shouldShowOnboarding(userId: String): Boolean {
        if (userId.isBlank()) return true
        val isCompletedLocally = completedUsersOnDevice[userId] == true
        val isLastUser = lastCompletedUserId == userId
        return !(isCompletedLocally && isLastUser)
    }

    override fun setOnboardingCompleted(userId: String) {
        if (userId.isBlank()) return
        completedUsersOnDevice[userId] = true
        lastCompletedUserId = userId
    }

    override fun getOnboardingLastStep(userId: String): String? = onboardingStep

    override fun setOnboardingLastStep(userId: String, step: String) {
        onboardingStep = step
    }

    override fun isWatchSyncEnabled(userId: String): Boolean = false
    override fun setWatchSyncEnabled(userId: String, enabled: Boolean) {}

    override fun getCustomOptions(userId: String, category: String): Set<String> = emptySet()
    override fun addCustomOption(userId: String, category: String, label: String) {}

    override fun hasEverLoggedIn(): Boolean = everLoggedIn
    override fun setHasEverLoggedIn(hasLoggedIn: Boolean) {
        everLoggedIn = hasLoggedIn
    }

    override fun getSavedEmail(): String? = savedEmail
    override fun setSavedEmail(email: String?) {
        savedEmail = email
    }

    override fun getProfilePictureUri(userId: String): String? = null
    override fun setProfilePictureUri(userId: String, uri: String?) {}

    override fun hasSeenMicCoachmark(userId: String): Boolean {
        val effectiveUser = if (userId.isBlank()) "guest" else userId
        val isSeen = seenMicCoachmarks[effectiveUser] == true
        val isLastUser = lastTooltipUserId == effectiveUser
        return isSeen && isLastUser
    }

    override fun setMicCoachmarkDismissed(userId: String) {
        val effectiveUser = if (userId.isBlank()) "guest" else userId
        seenMicCoachmarks[effectiveUser] = true
        lastTooltipUserId = effectiveUser
    }

    override fun isReminderEnabled(category: String): Boolean = true
    override fun setReminderEnabled(category: String, enabled: Boolean) {}
    override fun getReminderTime(category: String): Pair<Int, Int> = Pair(9, 0)
    override fun setReminderTime(category: String, hour: Int, minute: Int) {}

    override fun clearLocalData() {
        completedUsersOnDevice.clear()
        lastCompletedUserId = null
        seenMicCoachmarks.clear()
        lastTooltipUserId = null
        onboardingStep = null
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

    override suspend fun loginWithGoogle(idToken: String): Result<Boolean> {
        authenticated = true
        return Result.success(false)
    }

    override suspend fun loginWithFacebook(accessToken: String): Result<Boolean> {
        authenticated = true
        return Result.success(false)
    }

    override suspend fun linkCredential(credential: AuthCredential): Result<Unit> =
        Result.success(Unit)

    override suspend fun unlinkProvider(providerId: String): Result<Unit> = Result.success(Unit)

    override suspend fun signUp(username: String, email: String, password: String): Result<Unit> {
        authenticated = true
        return Result.success(Unit)
    }

    override suspend fun logout(): Result<Unit> {
        authenticated = false
        return Result.success(Unit)
    }

    override suspend fun logActivity(type: String, message: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.success(Unit)
    override suspend fun requestEmailChange(newEmail: String): Result<Unit> = Result.success(Unit)
    override suspend fun updateEmail(currentPassword: String, newEmail: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun updatePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = Result.success(Unit)

    override suspend fun deleteAccount(): Result<Unit> {
        authenticated = false
        return Result.success(Unit)
    }
}

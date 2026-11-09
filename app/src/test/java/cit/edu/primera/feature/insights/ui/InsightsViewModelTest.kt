package cit.edu.primera.feature.insights.ui

import cit.edu.primera.core.util.NetworkMonitor
import cit.edu.primera.feature.checkins.data.CheckinLogDto
import cit.edu.primera.feature.checkins.data.CheckinUserDto
import cit.edu.primera.feature.checkins.data.CheckinsRepository
import cit.edu.primera.feature.dashboard.data.DashboardRepository
import cit.edu.primera.feature.dashboard.domain.DashboardData
import cit.edu.primera.feature.goals.data.GoalDto
import cit.edu.primera.feature.goals.data.GoalsRepository
import cit.edu.primera.feature.smartwatchconnection.domain.SmartwatchHealth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeDashboardRepository: FakeDashboardRepository
    private lateinit var fakeCheckinsRepository: FakeCheckinsRepository
    private lateinit var fakeGoalsRepository: FakeGoalsRepository
    private lateinit var fakeNetworkMonitor: FakeNetworkMonitor

    private lateinit var viewModel: InsightsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        fakeDashboardRepository = FakeDashboardRepository()
        fakeCheckinsRepository = FakeCheckinsRepository()
        fakeGoalsRepository = FakeGoalsRepository()
        fakeNetworkMonitor = FakeNetworkMonitor()

        viewModel = InsightsViewModel(
            dashboardRepository = fakeDashboardRepository,
            checkinsRepository = fakeCheckinsRepository,
            goalsRepository = fakeGoalsRepository,
            networkMonitor = fakeNetworkMonitor
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createDashboardData(dueDate: Date? = null): DashboardData {
        return DashboardData(
            userName = "Jane",
            dueDate = dueDate,
            steps = 5000,
            stepsGoal = 10000,
            heartRateBpm = 75,
            sleepHours = 8,
            sleepMinutes = 0,
            recentLogs = emptyList()
        )
    }

    @Test
    fun uiState_calculatesGestationalAgeAndPregnancyProgress_correctly() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        // Due date set to 140 days in the future -> 20 weeks gestation completed
        val dueDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 140) }.time
        fakeDashboardRepository.setDashboardData(createDashboardData(dueDate = dueDate))

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Success", state is InsightsUiState.Success)

        val successData = (state as InsightsUiState.Success).data
        assertEquals(20, successData.gestationalAgeWeeks)
        assertEquals("20 Weeks 0 Days", successData.gestationalAgeText)
        assertEquals(
            0.5f,
            successData.pregnancyProgress,
            0.02f
        ) // 140 days completed / 280 total = 0.5
    }

    @Test
    fun uiState_handlesPostTermPregnancy_correctly() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        // Due date is today -> 40 weeks gestation completed
        val dueDate = Calendar.getInstance().time
        fakeDashboardRepository.setDashboardData(createDashboardData(dueDate = dueDate))

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is InsightsUiState.Success)

        val successData = (state as InsightsUiState.Success).data
        assertEquals(40, successData.gestationalAgeWeeks)
        assertTrue(
            "Text should indicate Post-term",
            successData.gestationalAgeText.contains("Post-term")
        )
        assertEquals(1.0f, successData.pregnancyProgress, 0.01f)
    }

    @Test
    fun uiState_filtersGoalsAndCalculatesGoalCompletionRatio_correctly() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        val today = Date()
        val goals = listOf(
            GoalDto(
                id = "1",
                title = "Hydration",
                currentValue = 2.0,
                targetValue = 2.0,
                unit = "L",
                timestamp = today
            ),
            GoalDto(
                id = "2",
                title = "Movement",
                currentValue = 4000.0,
                targetValue = 10000.0,
                unit = "steps",
                timestamp = today
            ),
            GoalDto(
                id = "3",
                title = "Sleep",
                currentValue = 8.0,
                targetValue = 8.0,
                unit = "hrs",
                timestamp = today
            )
        )
        fakeGoalsRepository.setGoals(goals)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is InsightsUiState.Success)

        val successData = (state as InsightsUiState.Success).data
        assertEquals(3, successData.totalGoalsCount)
        assertEquals(2, successData.completedGoalsCount)
        assertEquals(0.667f, successData.goalCompletionRatio, 0.01f)
        assertEquals(3, successData.wellnessGoals.size)

        // Hydration and Movement are mandatory and cannot be deleted
        val hydrationItem = successData.wellnessGoals.find { it.title == "Hydration" }
        assertNotNull(hydrationItem)
        assertEquals(false, hydrationItem!!.isDeletable)

        val sleepItem = successData.wellnessGoals.find { it.title == "Sleep" }
        assertNotNull(sleepItem)
        assertEquals(true, sleepItem!!.isDeletable)
    }

    @Test
    fun uiState_generatesHealthWarning_whenCriticalSymptomsReported() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        val log = CheckinLogDto(
            id = "log_1",
            description = "Symptoms: High Blood Pressure, Severe Headache; Mood: Normal;",
            timestamp = Date()
        )
        fakeCheckinsRepository.setLogs(listOf(log))

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is InsightsUiState.Success)

        val successData = (state as InsightsUiState.Success).data
        assertNotNull("Health warning tip should be generated", successData.healthWarningTip)
        assertTrue(
            "Tip should mention symptoms",
            successData.healthWarningTip!!.contains("High Blood Pressure and Severe Headache")
        )
    }

    @Test
    fun uiState_calculatesLongitudinalInsights_fromSmartwatchData() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        // Create 14 days of smartwatch records with an elevated heart rate in the last 7 days
        val records = mutableListOf<SmartwatchHealth>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -13)

        for (i in 0..13) {
            val dateStr = String.format(
                "%04d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
            // Days 0..6: baseline HR = 70. Days 7..13: elevated HR = 85 (+21%)
            val hr = if (i < 7) 70L else 85L
            records.add(
                SmartwatchHealth(
                    date = dateStr,
                    averageHeartRate = hr,
                    steps = 6000,
                    sleepMinutes = 480
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        fakeDashboardRepository.setHealthRecords(records)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is InsightsUiState.Success)

        val successData = (state as InsightsUiState.Success).data
        assertTrue(
            "Should contain longitudinal insights",
            successData.longitudinalInsights.isNotEmpty()
        )
        val hrInsight =
            successData.longitudinalInsights.find { it.contains("heart rate is trending") }
        assertNotNull("Should detect heart rate trend", hrInsight)
    }

    @Test
    fun onPeriodSelected_updatesActivePeriodAndResetsOffsets() = runTest {
        viewModel.onWeightPrevious() // offset = -1
        viewModel.onPeriodSelected("Monthly")

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Monthly", viewModel.activePeriod.value)
        assertEquals("Monthly", viewModel.weightPeriod.value)
        assertEquals(0, viewModel.weightOffset.value)
    }

    @Test
    fun trendOffsetNavigation_incrementsAndDecrementsOffsetsCorrectly() {
        assertEquals(0, viewModel.weightOffset.value)

        viewModel.onWeightPrevious()
        assertEquals(-1, viewModel.weightOffset.value)

        viewModel.onWeightNext()
        assertEquals(0, viewModel.weightOffset.value)

        viewModel.onMoodPrevious()
        assertEquals(-1, viewModel.moodOffset.value)

        viewModel.onActivityNext()
        assertEquals(1, viewModel.activityOffset.value)
    }

    @Test
    fun saveGoal_addsNewGoal_orUpdatesExistingGoal() = runTest {
        val newGoal = GoalDto(
            id = null,
            title = "Daily Meditation",
            currentValue = 0.0,
            targetValue = 15.0,
            unit = "mins"
        )
        viewModel.saveGoal(newGoal)

        testDispatcher.scheduler.advanceUntilIdle()

        val addedGoal = fakeGoalsRepository.goalsFlow.value.find { it.title == "Daily Meditation" }
        assertNotNull("Goal should be added to repository", addedGoal)
    }

    @Test
    fun deleteGoal_removesGoalFromRepository() = runTest {
        val goal =
            GoalDto(id = "delete_me", title = "Old Goal", currentValue = 0.0, targetValue = 10.0)
        fakeGoalsRepository.setGoals(listOf(goal))

        viewModel.deleteGoal("delete_me")
        testDispatcher.scheduler.advanceUntilIdle()

        val remaining = fakeGoalsRepository.goalsFlow.value.find { it.id == "delete_me" }
        assertEquals(null, remaining)
    }

    @Test
    fun summaryQrContent_generatesFormattedHealthSummary() = runTest {
        val content = viewModel.summaryQrContent.first()
        assertNotNull(content)
        assertTrue(
            "Content should contain PRIMERA HEALTH SUMMARY",
            content.contains("PRIMERA HEALTH SUMMARY")
        )
    }
}

// =============================================================================
// FAKE REPOSITORIES FOR INSIGHTS VIEWMODEL TESTING
// =============================================================================

private class FakeDashboardRepository : DashboardRepository {
    private val dashboardDataFlow = MutableStateFlow<DashboardData?>(null)
    private val healthRecordsFlow = MutableStateFlow<List<SmartwatchHealth>>(emptyList())

    fun setDashboardData(data: DashboardData?) {
        dashboardDataFlow.value = data
    }

    fun setHealthRecords(records: List<SmartwatchHealth>) {
        healthRecordsFlow.value = records
    }

    override fun observeDashboardData(): Flow<DashboardData?> = dashboardDataFlow

    override suspend fun updateStepsGoal(goal: Long): Result<Unit> = Result.success(Unit)

    override suspend fun updateHealthData(
        steps: Long,
        heartRate: Long,
        sleepHours: Long,
        sleepMinutes: Long,
        spO2: Long?
    ): Result<Unit> = Result.success(Unit)

    override suspend fun saveHistoricalRecord(record: SmartwatchHealth): Result<Unit> =
        Result.success(Unit)

    override fun observeHealthRecords(): Flow<List<SmartwatchHealth>> = healthRecordsFlow

    override suspend fun updatePhotoUri(photoUri: String): Result<Unit> = Result.success(Unit)
}

private class FakeCheckinsRepository : CheckinsRepository {
    private val logsFlow = MutableStateFlow<List<CheckinLogDto>>(emptyList())
    private val weightFlow = MutableStateFlow<CheckinUserDto?>(null)
    private val customOptionsMap = mutableMapOf<String, MutableSet<String>>()

    fun setLogs(logs: List<CheckinLogDto>) {
        logsFlow.value = logs
    }

    override fun observeLogs(): Flow<List<CheckinLogDto>> = logsFlow

    override fun observeUserWeight(): Flow<CheckinUserDto?> = weightFlow

    override suspend fun saveLog(log: CheckinLogDto): Result<Unit> {
        logsFlow.value = logsFlow.value + log
        return Result.success(Unit)
    }

    override suspend fun updateUserWeight(weightKg: Int): Result<Unit> {
        weightFlow.value = CheckinUserDto(weightKg = weightKg)
        return Result.success(Unit)
    }

    override fun getCustomOptions(category: String): Set<String> =
        customOptionsMap[category] ?: emptySet()

    override fun addCustomOption(category: String, label: String) {
        val set = customOptionsMap.getOrPut(category) { mutableSetOf() }
        set.add(label)
    }
}

private class FakeGoalsRepository : GoalsRepository {
    val goalsFlow = MutableStateFlow<List<GoalDto>>(emptyList())

    fun setGoals(goals: List<GoalDto>) {
        goalsFlow.value = goals
    }

    override fun observeGoals(): Flow<List<GoalDto>> = goalsFlow

    override suspend fun addGoal(goal: GoalDto): Result<Unit> {
        val id = goal.id ?: "goal_${System.currentTimeMillis()}"
        goalsFlow.value = goalsFlow.value + goal.copy(id = id)
        return Result.success(Unit)
    }

    override suspend fun updateGoalProgress(goalId: String, newValue: Double): Result<Unit> {
        goalsFlow.value = goalsFlow.value.map {
            if (it.id == goalId) it.copy(currentValue = newValue) else it
        }
        return Result.success(Unit)
    }

    override suspend fun updateGoal(goal: GoalDto): Result<Unit> {
        goalsFlow.value = goalsFlow.value.map {
            if (it.id == goal.id) goal else it
        }
        return Result.success(Unit)
    }

    override suspend fun deleteGoal(goalId: String): Result<Unit> {
        goalsFlow.value = goalsFlow.value.filterNot { it.id == goalId }
        return Result.success(Unit)
    }

    override suspend fun ensureMandatoryGoals(): Result<Unit> {
        return Result.success(Unit)
    }
}

private class FakeNetworkMonitor : NetworkMonitor {
    override val isOnline: Flow<Boolean> = MutableStateFlow(true)
}

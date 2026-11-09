package cit.edu.primera.feature.notification.ui

import android.content.Context
import cit.edu.primera.core.data.PreferenceRepository
import cit.edu.primera.core.notification.ReminderManager
import cit.edu.primera.feature.notification.data.NotificationRepository
import cit.edu.primera.feature.notification.domain.NotificationItem
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeNotificationRepo: FakeNotificationRepository
    private lateinit var fakePreferenceRepo: FakePreferenceRepository
    private lateinit var fakeReminderManager: FakeReminderManager
    private lateinit var viewModel: NotificationsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeNotificationRepo = FakeNotificationRepository()
        fakePreferenceRepo = FakePreferenceRepository()
        fakeReminderManager = FakeReminderManager()
        viewModel = NotificationsViewModel(
            fakeNotificationRepo,
            fakePreferenceRepo,
            fakeReminderManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadNotifications_emitsItemsAndUnreadCount() = runTest {
        val sample1 = NotificationItem(
            id = "1",
            title = "Morning Checkin",
            message = "Time to log",
            category = NotificationItem.CATEGORY_CHECKIN,
            isRead = false
        )
        val sample2 = NotificationItem(
            id = "2",
            title = "Hydration",
            message = "Drink water",
            category = NotificationItem.CATEGORY_HYDRATION,
            isRead = true
        )
        fakeNotificationRepo.setNotifications(listOf(sample1, sample2))

        viewModel.loadNotifications()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.notifications.size)
        assertEquals(1, state.unreadCount)
    }

    @Test
    fun filterByCategory_filtersNotificationsCorrectly() = runTest {
        val sample1 = NotificationItem(
            id = "1",
            title = "Morning Checkin",
            message = "Time to log",
            category = NotificationItem.CATEGORY_CHECKIN,
            isRead = false
        )
        val sample2 = NotificationItem(
            id = "2",
            title = "Hydration",
            message = "Drink water",
            category = NotificationItem.CATEGORY_HYDRATION,
            isRead = false
        )
        fakeNotificationRepo.setNotifications(listOf(sample1, sample2))

        viewModel.loadNotifications()
        advanceUntilIdle()

        viewModel.filterByCategory(NotificationItem.CATEGORY_CHECKIN)
        val filteredState = viewModel.uiState.value
        assertEquals(1, filteredState.notifications.size)
        assertEquals(NotificationItem.CATEGORY_CHECKIN, filteredState.notifications[0].category)
    }

    @Test
    fun toggleReminder_updatesPreferenceRepository() = runTest {
        viewModel.toggleReminder(NotificationItem.CATEGORY_CHECKIN, false)
        advanceUntilIdle()

        assertFalse(fakePreferenceRepo.isReminderEnabled(NotificationItem.CATEGORY_CHECKIN))
        val settingsState = viewModel.settingsState.value
        assertFalse(settingsState.checkinEnabled)
        assertEquals("Reminder setting updated", settingsState.successMessage)
    }

    @Test
    fun updateReminderTime_validatesAndUpdatesTime() = runTest {
        viewModel.updateReminderTime(NotificationItem.CATEGORY_CHECKIN, 10, 30)
        advanceUntilIdle()

        val time = fakePreferenceRepo.getReminderTime(NotificationItem.CATEGORY_CHECKIN)
        assertEquals(10, time.first)
        assertEquals(30, time.second)

        // Invalid hour
        viewModel.updateReminderTime(NotificationItem.CATEGORY_CHECKIN, 25, 0)
        val settingsState = viewModel.settingsState.value
        assertEquals("Invalid time selected", settingsState.error)
    }
}

class FakeNotificationRepository : NotificationRepository {
    private val notificationsFlow = MutableStateFlow<List<NotificationItem>>(emptyList())

    fun setNotifications(items: List<NotificationItem>) {
        notificationsFlow.value = items
    }

    override fun getNotifications(userId: String): Flow<List<NotificationItem>> = notificationsFlow

    override suspend fun addNotification(userId: String, notification: NotificationItem) {
        val current = notificationsFlow.value.toMutableList()
        current.add(0, notification)
        notificationsFlow.value = current
    }

    override suspend fun markAsRead(userId: String, notificationId: String) {
        notificationsFlow.value = notificationsFlow.value.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
    }

    override suspend fun markAllAsRead(userId: String) {
        notificationsFlow.value = notificationsFlow.value.map { it.copy(isRead = true) }
    }

    override suspend fun clearAllNotifications(userId: String) {
        notificationsFlow.value = emptyList()
    }
}

class FakePreferenceRepository : PreferenceRepository {
    private val remindersEnabled = mutableMapOf<String, Boolean>()
    private val reminderTimes = mutableMapOf<String, Pair<Int, Int>>()

    override suspend fun shouldShowOnboarding(userId: String): Boolean = false
    override fun setOnboardingCompleted(userId: String) {}
    override fun getOnboardingLastStep(userId: String): String? = null
    override fun setOnboardingLastStep(userId: String, step: String) {}
    override fun isWatchSyncEnabled(userId: String): Boolean = false
    override fun setWatchSyncEnabled(userId: String, enabled: Boolean) {}
    override fun getCustomOptions(userId: String, category: String): Set<String> = emptySet()
    override fun addCustomOption(userId: String, category: String, label: String) {}
    override fun hasEverLoggedIn(): Boolean = false
    override fun setHasEverLoggedIn(hasLoggedIn: Boolean) {}
    override fun getSavedEmail(): String? = null
    override fun setSavedEmail(email: String?) {}
    override fun getProfilePictureUri(userId: String): String? = null
    override fun setProfilePictureUri(userId: String, uri: String?) {}
    override fun hasSeenMicCoachmark(userId: String): Boolean = false
    override fun setMicCoachmarkDismissed(userId: String) {}
    override fun clearLocalData() {}

    override fun isReminderEnabled(category: String): Boolean {
        return remindersEnabled[category.lowercase()] ?: true
    }

    override fun setReminderEnabled(category: String, enabled: Boolean) {
        remindersEnabled[category.lowercase()] = enabled
    }

    override fun getReminderTime(category: String): Pair<Int, Int> {
        return reminderTimes[category.lowercase()] ?: Pair(9, 0)
    }

    override fun setReminderTime(category: String, hour: Int, minute: Int) {
        reminderTimes[category.lowercase()] = Pair(hour, minute)
    }
}

class FakeReminderManager : ReminderManager() {
    var scheduleCount = 0
    override fun scheduleDailyReminders() {
        scheduleCount++
    }
}

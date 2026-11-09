package cit.edu.primera.feature.notification.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cit.edu.primera.core.data.PreferenceRepository
import cit.edu.primera.core.notification.ReminderManager
import cit.edu.primera.feature.notification.data.NotificationRepository
import cit.edu.primera.feature.notification.domain.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReminderSettingsUiState(
    val checkinEnabled: Boolean = true,
    val checkinHour: Int = 9,
    val checkinMinute: Int = 0,
    val hydrationEnabled: Boolean = true,
    val hydrationHour: Int = 14,
    val hydrationMinute: Int = 0,
    val wellnessEnabled: Boolean = true,
    val wellnessHour: Int = 20,
    val wellnessMinute: Int = 0,
    val successMessage: String? = null,
    val error: String? = null
)

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository,
    private val preferenceRepository: PreferenceRepository,
    private val reminderManager: ReminderManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _settingsState = MutableStateFlow(ReminderSettingsUiState())
    val settingsState: StateFlow<ReminderSettingsUiState> = _settingsState.asStateFlow()

    private var allNotifications: List<NotificationItem> = emptyList()

    init {
        loadNotifications()
        loadReminderSettings()
    }

    private fun currentUserId(): String {
        return try {
            FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        } catch (_: Exception) {
            "guest"
        }
    }

    fun loadNotifications() {
        val userId = currentUserId()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            notificationRepository.getNotifications(userId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load notifications") }
                }
                .collect { items ->
                    allNotifications = items
                    applyFilterAndEmit()
                }
        }
    }

    fun filterByCategory(category: String?) {
        _uiState.update { it.copy(filteredCategory = category) }
        applyFilterAndEmit()
    }

    private fun applyFilterAndEmit() {
        val categoryFilter = _uiState.value.filteredCategory
        val filtered = if (categoryFilter.isNullOrBlank()) {
            allNotifications
        } else {
            allNotifications.filter { it.category.equals(categoryFilter, ignoreCase = true) }
        }
        val unread = allNotifications.count { !it.isRead }
        _uiState.update {
            it.copy(
                isLoading = false,
                notifications = filtered,
                unreadCount = unread
            )
        }
    }

    fun markAsRead(notification: NotificationItem) {
        val userId = currentUserId()
        viewModelScope.launch {
            notificationRepository.markAsRead(userId, notification.id)
        }
    }

    fun markAllAsRead() {
        val userId = currentUserId()
        viewModelScope.launch {
            notificationRepository.markAllAsRead(userId)
        }
    }

    fun clearAll() {
        val userId = currentUserId()
        viewModelScope.launch {
            notificationRepository.clearAllNotifications(userId)
        }
    }

    fun onNotificationClicked(notification: NotificationItem, onNavigate: (String) -> Unit) {
        markAsRead(notification)
        notification.destinationRoute?.let { route ->
            onNavigate(route)
        }
    }

    fun loadReminderSettings() {
        val (cHour, cMin) = preferenceRepository.getReminderTime(NotificationItem.CATEGORY_CHECKIN)
        val (hHour, hMin) = preferenceRepository.getReminderTime(NotificationItem.CATEGORY_HYDRATION)
        val (wHour, wMin) = preferenceRepository.getReminderTime(NotificationItem.CATEGORY_WELLNESS)

        _settingsState.update {
            it.copy(
                checkinEnabled = preferenceRepository.isReminderEnabled(NotificationItem.CATEGORY_CHECKIN),
                checkinHour = cHour,
                checkinMinute = cMin,
                hydrationEnabled = preferenceRepository.isReminderEnabled(NotificationItem.CATEGORY_HYDRATION),
                hydrationHour = hHour,
                hydrationMinute = hMin,
                wellnessEnabled = preferenceRepository.isReminderEnabled(NotificationItem.CATEGORY_WELLNESS),
                wellnessHour = wHour,
                wellnessMinute = wMin
            )
        }
    }

    fun toggleReminder(category: String, enabled: Boolean) {
        preferenceRepository.setReminderEnabled(category, enabled)
        reminderManager.scheduleDailyReminders()
        loadReminderSettings()
        _settingsState.update { it.copy(successMessage = "Reminder setting updated") }
    }

    fun updateReminderTime(category: String, hour: Int, minute: Int) {
        if (hour !in 0..23 || minute !in 0..59) {
            _settingsState.update { it.copy(error = "Invalid time selected") }
            return
        }
        preferenceRepository.setReminderTime(category, hour, minute)
        reminderManager.scheduleDailyReminders()
        loadReminderSettings()
        _settingsState.update { it.copy(successMessage = "Reminder time updated") }
    }

    fun clearSettingsMessages() {
        _settingsState.update { it.copy(successMessage = null, error = null) }
    }
}

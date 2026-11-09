package cit.edu.primera.feature.notification.ui

import cit.edu.primera.feature.notification.domain.NotificationItem

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val notifications: List<NotificationItem> = emptyList(),
    val filteredCategory: String? = null,
    val unreadCount: Int = 0,
    val error: String? = null
)

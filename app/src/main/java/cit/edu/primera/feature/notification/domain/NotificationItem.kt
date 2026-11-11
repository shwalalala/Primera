
package cit.edu.primera.feature.notification.domain

import java.util.UUID

data class NotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = CATEGORY_SYSTEM,
    val destinationRoute: String? = null,
    val isRead: Boolean = false
) {
    companion object {
        // Existing teammates' categories
        const val CATEGORY_CHECKIN = "checkin"
        const val CATEGORY_HYDRATION = "hydration"
        const val CATEGORY_WELLNESS = "wellness"
        const val CATEGORY_SMARTWATCH = "smartwatch"
        const val CATEGORY_SYSTEM = "system"

        // New PRIMERA reminder categories
        const val CATEGORY_MISSED_CHECKIN = "missed_checkin"
        const val CATEGORY_PREGNANCY_MILESTONE = "pregnancy_milestone"
        const val CATEGORY_APPOINTMENT = "appointment"
    }
}

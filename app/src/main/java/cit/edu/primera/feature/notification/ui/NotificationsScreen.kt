
package cit.edu.primera.feature.notification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cit.edu.primera.core.di.ViewModelProvider
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.LogPain
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.PrimeraLogoStart
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.SurfaceWhite
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.feature.notification.domain.NotificationItem
import cit.edu.primera.ui.components.FeatureTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ====================================================
// NEW NOTIFICATION CATEGORIES
//
// These identifiers will also be used by our new
// notification scheduling implementation.
// ====================================================

private const val CATEGORY_MISSED_CHECKIN = "missed_checkin"
private const val CATEGORY_PREGNANCY_MILESTONE = "pregnancy_milestone"
private const val CATEGORY_APPOINTMENT = "appointment"

// ====================================================
// LEGACY SYMPTOM NOTIFICATION FILTER
//
// Hide the old recurring and persistent warnings
// without affecting actual check-in history.
//
// Stored legacy notifications will be cleaned up
// separately through the repository.
// ====================================================

private fun isLegacySymptomWarning(
    notification: NotificationItem
): Boolean {
    return notification.id.startsWith(
        "checkin_symptom_"
    ) || notification.title.equals(
        "Recurring Symptom Detected",
        ignoreCase = true
    ) || notification.title.equals(
        "Persistent Symptom Detected",
        ignoreCase = true
    )
}

// ====================================================
// MAIN NOTIFICATIONS SCREEN
// ====================================================

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: NotificationsViewModel = viewModel(
        factory = ViewModelProvider.Factory
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedNotification by remember {
        mutableStateOf<NotificationItem?>(null)
    }

    NotificationsContent(
        state = state,
        onBack = onBack,
        onSelectCategory = viewModel::filterByCategory,
        onMarkAllRead = viewModel::markAllAsRead,
        onClearAll = viewModel::clearAll,
        onNotificationClick = { notification ->
            viewModel.markAsRead(notification)
            selectedNotification = notification
        },
        onOpenSettings = onOpenSettings,
        onRetry = viewModel::loadNotifications
    )

    selectedNotification?.let { notification ->
        NotificationDetailsDialog(
            notification = notification,
            onDismiss = {
                selectedNotification = null
            },
            onOpenRelatedScreen = {
                selectedNotification = null

                notification.destinationRoute?.let { route ->
                    onNavigate(route)
                }
            }
        )
    }
}

// ====================================================
// NOTIFICATIONS CONTENT
// ====================================================

@Composable
fun NotificationsContent(
    state: NotificationsUiState,
    onBack: () -> Unit,
    onSelectCategory: (String?) -> Unit,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit,
    onNotificationClick: (NotificationItem) -> Unit,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Legacy alerts remain hidden even if they still
    // exist in historical Firestore notification data.
    val visibleNotifications = state.notifications.filterNot {
        isLegacySymptomWarning(it)
    }

    val visibleUnreadCount = visibleNotifications.count {
        !it.isRead
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundCream,
                        PrimeraLilac.copy(alpha = 0.45f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            FeatureTopBar(
                title = "Notifications",
                onBack = onBack,
                onSettings = onOpenSettings
            )

            // ----------------------------------------
            // FILTER HEADER AND ACTIONS
            // ----------------------------------------

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )

                        if (visibleUnreadCount > 0) {
                            Spacer(
                                modifier = Modifier.width(8.dp)
                            )

                            Badge(
                                containerColor = LogPain,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = "$visibleUnreadCount unread",
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(
                                        horizontal = 4.dp
                                    )
                                )
                            }
                        }
                    }

                    Row {
                        if (visibleUnreadCount > 0) {
                            IconButton(
                                onClick = onMarkAllRead,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Mark all read",
                                    tint = PrimeraViolet
                                )
                            }
                        }

                        if (visibleNotifications.isNotEmpty()) {
                            IconButton(
                                onClick = onClearAll,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear all",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                // Preserve existing filters and add
                // categories for the planned reminders.

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(
                            rememberScrollState()
                        ),
                    horizontalArrangement = Arrangement.spacedBy(
                        8.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val categories = listOf(
                        null to "All",
                        NotificationItem.CATEGORY_CHECKIN to "Check-ins",
                        CATEGORY_MISSED_CHECKIN to "Missed Days",
                        CATEGORY_PREGNANCY_MILESTONE to "Pregnancy",
                        CATEGORY_APPOINTMENT to "Appointments",
                        NotificationItem.CATEGORY_HYDRATION to "Hydration",
                        NotificationItem.CATEGORY_WELLNESS to "Wellness",
                        NotificationItem.CATEGORY_SMARTWATCH to "Smartwatch"
                    )

                    categories.forEach { (category, label) ->
                        val isSelected =
                            state.filteredCategory == category

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onSelectCategory(category)
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    color = if (isSelected) {
                                        Color.White
                                    } else {
                                        TextPrimary
                                    },
                                    fontWeight = if (isSelected) {
                                        FontWeight.SemiBold
                                    } else {
                                        FontWeight.Medium
                                    }
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimeraLogoStart,
                                selectedLabelColor = Color.White,
                                containerColor = SurfaceWhite,
                                labelColor = TextPrimary
                            )
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // ----------------------------------------
            // NOTIFICATION LIST
            // ----------------------------------------

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(
                                Alignment.Center
                            ),
                            color = PrimeraViolet
                        )
                    }

                    state.error != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.error
                                    ?: "Failed to load notifications",
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            OutlinedButton(
                                onClick = onRetry
                            ) {
                                Text("Retry")
                            }
                        }
                    }

                    visibleNotifications.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = PrimeraViolet.copy(
                                    alpha = 0.4f
                                )
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Text(
                                text = "No notifications available",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text = "You're all caught up on maternal wellness updates!",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(
                                10.dp
                            )
                        ) {
                            items(
                                items = visibleNotifications,
                                key = { it.id }
                            ) { item ->
                                NotificationCard(
                                    item = item,
                                    onClick = {
                                        onNotificationClick(item)
                                    }
                                )
                            }

                            item {
                                Spacer(
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ====================================================
// COMPACT NOTIFICATION CARD
// ====================================================

@Composable
fun NotificationCard(
    item: NotificationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (categoryIcon, iconColor) =
        getCategoryIconAndColor(item.category)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isRead) {
                SurfaceWhite
            } else {
                PrimeraLilac.copy(alpha = 0.30f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.14f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(
                    3.dp
                )
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (item.isRead) {
                            FontWeight.Medium
                        } else {
                            FontWeight.Bold
                        },
                        color = TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = compactNotificationSummary(item),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formatTimestamp(item.timestamp),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!item.isRead) {
                    Surface(
                        shape = CircleShape,
                        color = LogPain,
                        modifier = Modifier.size(8.dp)
                    ) {}

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View notification",
                    tint = PrimeraViolet,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ====================================================
// GENERAL NOTIFICATION DETAILS
//
// No symptom analysis, medical warning,
// or Firestore symptom occurrence lookups.
// ====================================================

@Composable
private fun NotificationDetailsDialog(
    notification: NotificationItem,
    onDismiss: () -> Unit,
    onOpenRelatedScreen: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp),
            shape = RoundedCornerShape(22.dp),
            color = BackgroundCream
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // HEADER

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 18.dp,
                            end = 12.dp,
                            top = 14.dp,
                            bottom = 12.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = notification.title,
                            fontSize = 14.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(
                            modifier = Modifier.height(3.dp)
                        )

                        Text(
                            text = formatFullTimestamp(
                                notification.timestamp
                            ),
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Surface(
                        shape = CircleShape,
                        color = PrimeraLilac.copy(
                            alpha = 0.50f
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close preview",
                                tint = TextPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = PrimeraViolet.copy(
                        alpha = 0.10f
                    )
                )

                // SCROLLABLE NOTIFICATION MESSAGE

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(
                            rememberScrollState()
                        )
                        .padding(
                            horizontal = 18.dp,
                            vertical = 14.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(
                        12.dp
                    )
                ) {
                    val (categoryIcon, iconColor) =
                        getCategoryIconAndColor(
                            notification.category
                        )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = PrimeraLilac.copy(
                            alpha = 0.38f
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 13.dp,
                                vertical = 11.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(19.dp)
                            )

                            Spacer(
                                modifier = Modifier.width(10.dp)
                            )

                            Text(
                                text = getCategoryLabel(
                                    notification.category
                                ),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }

                    Text(
                        text = "MESSAGE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    Text(
                        text = notification.message,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = TextPrimary
                    )
                }

                // FIXED FOOTER
                // Navigation is available for notifications
                // that include a destination route.

                if (notification.destinationRoute != null) {
                    HorizontalDivider(
                        color = PrimeraViolet.copy(
                            alpha = 0.10f
                        )
                    )

                    Button(
                        onClick = onOpenRelatedScreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 18.dp,
                                vertical = 12.dp
                            )
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimeraLogoStart,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = getDestinationLabel(
                                notification.category
                            ),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ====================================================
// NOTIFICATION SUMMARY
// ====================================================

private fun compactNotificationSummary(
    notification: NotificationItem
): String {
    return notification.message
        .replace("\n", " ")
        .trim()
}

// ====================================================
// CATEGORY LABELS
// ====================================================

private fun getCategoryLabel(
    category: String
): String {
    return when (category.lowercase(Locale.ROOT)) {
        NotificationItem.CATEGORY_CHECKIN ->
            "Daily Check-In"

        CATEGORY_MISSED_CHECKIN ->
            "Missed Check-In Reminder"

        CATEGORY_PREGNANCY_MILESTONE ->
            "Pregnancy Milestone"

        CATEGORY_APPOINTMENT ->
            "Prenatal Appointment"

        NotificationItem.CATEGORY_HYDRATION ->
            "Hydration Reminder"

        NotificationItem.CATEGORY_WELLNESS ->
            "Wellness Reminder"

        NotificationItem.CATEGORY_SMARTWATCH ->
            "Smartwatch"

        else ->
            "PRIMERA Update"
    }
}

// ====================================================
// RELATED SCREEN BUTTON LABEL
// ====================================================

private fun getDestinationLabel(
    category: String
): String {
    return when (category.lowercase(Locale.ROOT)) {
        NotificationItem.CATEGORY_CHECKIN,
        CATEGORY_MISSED_CHECKIN ->
            "Open Daily Check-In"

        CATEGORY_APPOINTMENT ->
            "View Prenatal Checkups"

        CATEGORY_PREGNANCY_MILESTONE ->
            "View Pregnancy Progress"

        else ->
            "Open Related Screen"
    }
}

// ====================================================
// CATEGORY ICONS
// ====================================================

private fun getCategoryIconAndColor(
    category: String
): Pair<ImageVector, Color> {
    return when (category.lowercase(Locale.ROOT)) {
        NotificationItem.CATEGORY_CHECKIN ->
            Icons.Default.Favorite to LogPain

        CATEGORY_MISSED_CHECKIN ->
            Icons.Default.DateRange to PrimeraViolet

        CATEGORY_PREGNANCY_MILESTONE ->
            Icons.Default.Favorite to PrimeraLogoStart

        CATEGORY_APPOINTMENT ->
            Icons.Default.DateRange to PrimeraViolet

        NotificationItem.CATEGORY_HYDRATION ->
            Icons.Default.LocalDrink to Color(0xFF42A5F5)

        NotificationItem.CATEGORY_WELLNESS ->
            Icons.Default.CheckCircle to PrimeraLogoStart

        NotificationItem.CATEGORY_SMARTWATCH ->
            Icons.Default.Watch to Color(0xFFFF9800)

        else ->
            Icons.Default.Info to Color(0xFF78909C)
    }
}

// ====================================================
// RELATIVE TIMESTAMP
// ====================================================

private fun formatTimestamp(
    timestamp: Long
): String {
    val diff = (
            System.currentTimeMillis() - timestamp
            ).coerceAtLeast(0L)

    val minutes = diff / 60000L
    val hours = minutes / 60L
    val days = hours / 24L

    return when {
        minutes < 1 -> "Just now"

        minutes < 60 -> "${minutes}m ago"

        hours < 24 -> "${hours}h ago"

        days < 7 -> "${days}d ago"

        else -> SimpleDateFormat(
            "MMM d, h:mm a",
            Locale.getDefault()
        ).format(Date(timestamp))
    }
}

// ====================================================
// FULL TIMESTAMP
// ====================================================

private fun formatFullTimestamp(
    timestamp: Long
): String {
    return SimpleDateFormat(
        "MMMM d, yyyy 'at' h:mm a",
        Locale.getDefault()
    ).format(Date(timestamp))
}

package cit.edu.primera.feature.notification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.util.Locale

@Composable
fun ReminderSettingsScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val state by viewModel.settingsState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSettingsMessages()
        }
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSettingsMessages()
        }
    }

    ReminderSettingsContent(
        state = state,
        onBack = onBack,
        onToggleReminder = viewModel::toggleReminder,
        onUpdateTime = viewModel::updateReminderTime,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsContent(
    state: ReminderSettingsUiState,
    onBack: () -> Unit,
    onToggleReminder: (String, Boolean) -> Unit,
    onUpdateTime: (String, Int, Int) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    var activeCategoryForTimePicker by remember { mutableStateOf<String?>(null) }
    var initialHour by remember { mutableStateOf(9) }
    var initialMinute by remember { mutableStateOf(0) }

    if (activeCategoryForTimePicker != null) {
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { activeCategoryForTimePicker = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        activeCategoryForTimePicker?.let { cat ->
                            onUpdateTime(cat, timePickerState.hour, timePickerState.minute)
                        }
                        activeCategoryForTimePicker = null
                    }
                ) {
                    Text("Save", color = PrimeraLogoStart, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeCategoryForTimePicker = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            title = {
                Text(
                    text = "Select Reminder Time",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundCream,
                        PrimeraLilac.copy(alpha = 0.45f)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FeatureTopBar(
                title = "Reminder Settings",
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Configure your daily notification preferences to keep up with maternal wellness check-ins, hydration, and rest.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                    lineHeight = 20.sp
                )

                // 1. Morning Check-in
                ReminderSettingCard(
                    title = "Morning Check-in",
                    description = "Daily morning reminder to log symptoms and check maternal health.",
                    icon = Icons.Default.Favorite,
                    iconBg = LogPain,
                    isEnabled = state.checkinEnabled,
                    hour = state.checkinHour,
                    minute = state.checkinMinute,
                    onToggle = { onToggleReminder(NotificationItem.CATEGORY_CHECKIN, it) },
                    onEditTime = {
                        initialHour = state.checkinHour
                        initialMinute = state.checkinMinute
                        activeCategoryForTimePicker = NotificationItem.CATEGORY_CHECKIN
                    }
                )

                // 2. Hydration Reminder
                ReminderSettingCard(
                    title = "Hydration Reminder",
                    description = "Afternoon reminder to drink water and maintain optimal hydration.",
                    icon = Icons.Default.LocalDrink,
                    iconBg = Color(0xFF42A5F5),
                    isEnabled = state.hydrationEnabled,
                    hour = state.hydrationHour,
                    minute = state.hydrationMinute,
                    onToggle = { onToggleReminder(NotificationItem.CATEGORY_HYDRATION, it) },
                    onEditTime = {
                        initialHour = state.hydrationHour
                        initialMinute = state.hydrationMinute
                        activeCategoryForTimePicker = NotificationItem.CATEGORY_HYDRATION
                    }
                )

                // 3. Evening Wind-down
                ReminderSettingCard(
                    title = "Evening Wind-down",
                    description = "Evening reminder to reflect, review symptoms, and rest well.",
                    icon = Icons.Default.Nightlight,
                    iconBg = PrimeraLogoStart,
                    isEnabled = state.wellnessEnabled,
                    hour = state.wellnessHour,
                    minute = state.wellnessMinute,
                    onToggle = { onToggleReminder(NotificationItem.CATEGORY_WELLNESS, it) },
                    onEditTime = {
                        initialHour = state.wellnessHour
                        initialMinute = state.wellnessMinute
                        activeCategoryForTimePicker = NotificationItem.CATEGORY_WELLNESS
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
fun ReminderSettingCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconBg: Color,
    isEnabled: Boolean,
    hour: Int,
    minute: Int,
    onToggle: (Boolean) -> Unit,
    onEditTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = iconBg,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimeraLogoStart,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.LightGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )

            if (isEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = PrimeraViolet,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scheduled at ${formatTime(hour, minute)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = onEditTime,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Change Time", fontSize = 12.sp, color = PrimeraLogoStart)
                    }
                }
            }
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm)
}

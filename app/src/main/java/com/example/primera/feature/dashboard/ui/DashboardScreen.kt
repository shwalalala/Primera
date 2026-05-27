package com.example.primera.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.primera.R
import com.example.primera.core.di.ViewModelProvider
import com.example.primera.ui.components.*
import com.example.primera.core.theme.*

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        (BackgroundCream),
                        PrimeraLilac.copy(alpha = 0.45f)
                    )
                )
            )
    ) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> FullScreenLoadingOverlay()
            is DashboardUiState.Error -> ErrorContent(state.message)
            is DashboardUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    DashboardTopBar(state.data.userName, onLogout)
                    DashboardContent(
                        state = state.data,
                        onViewAllLogs = viewModel::onViewAllLogs,
                        onAddLog = viewModel::onAddLog,
                        onInputManually = viewModel::onInputManually
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    state: DashboardUiModel,
    onViewAllLogs: () -> Unit,
    onAddLog: () -> Unit,
    onInputManually: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        WeekCalendarRow(state.weekDays)
        Spacer(Modifier.height(8.dp))
        DashboardGreeting(
            userName = state.userName,
            timeOfDay = state.timeOfDay,
            trimesterText = "You're in your ${state.trimester}"
        )
        Spacer(Modifier.height(4.dp))
        BabyRingCard(state)
        Spacer(Modifier.height(20.dp))
        StatsGrid(state, onInputManually)
        Spacer(Modifier.height(24.dp))
        RecentHealthLogsSection(state.recentLogs, onViewAllLogs, onAddLog)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DashboardTopBar(userName: String, onLogout: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MenuButtonBg)
                    .border(1.dp, MenuButtonBorder, CircleShape)
                    .minimumInteractiveComponentSize()
                    .clickable { showMenu = true },
                contentAlignment = Alignment.Center
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(Modifier.size(width = 16.dp, height = 2.dp).background(TextSecondary))
                    Box(Modifier.size(width = 16.dp, height = 2.dp).background(TextSecondary))
                    Box(Modifier.size(width = 10.dp, height = 2.dp).background(TextSecondary))
                }
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier
                    .background(SurfaceWhite)
                    .border(1.dp, MenuButtonBorder, RoundedCornerShape(12.dp))
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Logout",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = PrimeraViolet,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        showMenu = false
                        onLogout()
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PrimeraViolet),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.take(1).uppercase(),
                color = SurfaceWhite,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WeekCalendarRow(days: List<DashboardWeekDayItem>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                DayChip(
                    initial = day.initial,
                    date = day.date,
                    isSelected = day.isSelected
                )
            }
        }
    }
}

@Composable
private fun BabyRingCard(state: DashboardUiModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularPregnancyRing(
            weekNumber = state.weekNumber,
            dayNumber = state.dayNumber,
            daysLeft = state.daysLeft,
            babyEmoji = "🥬"
        )

        Spacer(Modifier.height(16.dp))

        TrimesterBadge(
            weekNumber = state.weekNumber,
            dayNumber = state.dayNumber
        )
        Spacer(Modifier.height(4.dp))
        BabySizeText(babySize = state.babySize)
    }
}

@Composable
private fun StatsGrid(state: DashboardUiModel, onInputManually: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HealthStatCard(
                title = "Heart Rate",
                value = state.heartRateBpm.toString(),
                unit = "bpm",
                icon = painterResource(R.drawable.heart),
                iconBgColor = HeartRateBg,
                trendText = "${if (state.heartRateTrendingUp) "▲" else "▼"} ${state.heartRateVsLastWeek}% vs last wk",
                trendColor = if (state.heartRateTrendingUp) TrendGreen else TrendRed,
                modifier = Modifier.weight(1f)
            )
            HealthStatCard(
                title = "Steps",
                value = "%,d".format(state.steps),
                unit = "steps",
                icon = painterResource(R.drawable.steps),
                iconBgColor = StepsBg,
                trendText = "Goal: %,d".format(state.stepsGoal),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HealthStatCard(
                title = "Sleep",
                value = "${state.sleepHours}h ${state.sleepMinutes}m",
                unit = "",
                icon = painterResource(R.drawable.sleep),
                iconBgColor = SleepBg,
                trendText = state.sleepQuality.ifBlank { "—" },
                trendColor = TrendGreen,
                modifier = Modifier.weight(1f)
            )
            InputManuallyCard(onClick = onInputManually, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RecentHealthLogsSection(
    logs: List<DashboardLogUiItem>,
    onViewAll: () -> Unit,
    onAdd: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        DashboardSectionHeader(
            title = "Recent Health Logs",
            onViewAll = onViewAll,
            onAdd = onAdd
        )

        Spacer(Modifier.height(16.dp))

        logs.forEach { log ->
            LogCard(
                category = log.category,
                time = log.time,
                description = log.description,
                accentColor = log.accentColor
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun ErrorContent(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = ErrorRed)
    }
}

@Preview(name = "Compact Phone", device = Devices.PHONE)
@Preview(name = "Small Phone", widthDp = 360, heightDp = 640)
@Preview(name = "Large Phone", widthDp = 480, heightDp = 960)
@Composable
private fun DashboardScreenPreview() {
    PrimeraTheme {
        DashboardContent(
            state = DashboardUiModel(
                userName = "Sarah",
                timeOfDay = "Good morning",
                trimester = "Third Trimester",
                weekNumber = 28,
                dayNumber = 3,
                daysLeft = 88,
                babySize = "Size of a Cabbage",
                heartRateBpm = 72,
                heartRateTrendingUp = true,
                heartRateVsLastWeek = 5,
                steps = 6432,
                stepsGoal = 8000,
                sleepHours = 7,
                sleepMinutes = 45,
                sleepQuality = "Good",
                recentLogs = listOf(
                    DashboardLogUiItem("Back Pain", "8:32 AM", "Felt mild lower back pain", LogPain),
                    DashboardLogUiItem("Nutrition", "12:15 PM", "Healthy lunch", LogNutrition)
                ),
                weekDays = listOf(
                    DashboardWeekDayItem("S", 22, false),
                    DashboardWeekDayItem("M", 23, true),
                    DashboardWeekDayItem("T", 24, false),
                    DashboardWeekDayItem("W", 25, false),
                    DashboardWeekDayItem("T", 26, false),
                    DashboardWeekDayItem("F", 27, false),
                    DashboardWeekDayItem("S", 28, false)
                )
            ),
            onViewAllLogs = {},
            onAddLog = {},
            onInputManually = {}
        )
    }
}

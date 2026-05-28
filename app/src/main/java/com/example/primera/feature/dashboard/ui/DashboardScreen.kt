package com.example.primera.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
                        onInputManually = viewModel::onInputManually,
                        onSyncWatch = { /* Trigger watch sync logic */ }
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
    onSyncWatch: () -> Unit,
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
        BabyRingCard(
            weekNumber = state.weekNumber,
            dayNumber = state.dayNumber,
            daysLeft = state.daysLeft,
            babySize = state.babySize,
            babyEmoji = state.babyEmoji
        )
        Spacer(Modifier.height(20.dp))
        StatsGrid(state, onInputManually, onSyncWatch)
        Spacer(Modifier.height(24.dp))
        RecentHealthLogsSection(state.recentLogs, onViewAllLogs, onAddLog)
        Spacer(Modifier.height(8.dp))
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
                babySize = "Large eggplant",
                babyEmoji = "🍆",
                heartRateBpm = 72,
                heartRateTrendingUp = true,
                heartRateVsLastWeek = 5,
                steps = 6432,
                stepsGoal = 8000,
                sleepHours = 7,
                sleepMinutes = 45,
                sleepQuality = "Good quality",
                isWatchSynced = true,
                recentLogs = listOf(
                    DashboardLogUiItem("Back Pain", "Felt mild lower back pain", "8:32 AM", LogPain),
                    DashboardLogUiItem("Nutrition", "Healthy lunch", "12:15 PM", LogNutrition)
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
            onInputManually = {},
            onSyncWatch = {}
        )
    }
}

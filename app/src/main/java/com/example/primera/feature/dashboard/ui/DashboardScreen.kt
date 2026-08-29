package com.example.primera.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.primera.core.di.ViewModelProvider
import com.example.primera.core.theme.BackgroundCream
import com.example.primera.core.theme.ErrorRed
import com.example.primera.core.theme.PrimeraLilac
import com.example.primera.core.theme.PrimeraTheme
import com.example.primera.ui.components.FullScreenLoadingOverlay
import com.example.primera.ui.components.OfflineBanner

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    onLogClick: (DashboardLogUiItem) -> Unit,
    onViewAllLogs: () -> Unit,
    onAddLog: () -> Unit,
    onInputManually: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

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
                    DashboardTopBar(state.data.userName, onLogout, onProfileClick)
                    
                    if (!isOnline) {
                        OfflineBanner()
                    }

                    DashboardContent(
                        state = state.data,
                        onViewAllLogs = onViewAllLogs,
                        onAddLog = onAddLog,
                        onInputManually = onInputManually,
                        onLogClick = onLogClick,
                        onSyncWatch = { viewModel.onSyncWatch() },
                        onProfileClick = onProfileClick
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
    onLogClick: (DashboardLogUiItem) -> Unit,
    onSyncWatch: () -> Unit,
    onProfileClick: () -> Unit,
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
            babyEmoji = state.babyEmoji,
            babyIllustration = state.babyIllustration
        )
        Spacer(Modifier.height(20.dp))
        StatsGrid(
            state = state,
            onInputManually = onInputManually,
            onSyncWatch = onSyncWatch
        )
        Spacer(Modifier.height(24.dp))
        RecentHealthLogsSection(state.recentLogs, onViewAllLogs, onAddLog, onLogClick)
        Spacer(Modifier.height(16.dp))
        BabyDevelopmentSection(state.milestones, state.symptoms)
        Spacer(Modifier.height(8.dp))
        EducationalArticlesSection(state.articles)
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
                heartRateBpm = 0,
                heartRateTrendingUp = false,
                heartRateVsLastWeek = 0,
                steps = 0,
                stepsGoal = 8000,
                sleepHours = 7,
                sleepMinutes = 45,
                sleepQuality = "Good quality",
                spO2 = 98,
                isWatchSynced = true,
                recentLogs = emptyList(),
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
            onLogClick = {},
            onSyncWatch = {},
            onProfileClick = {}
        )
    }
}

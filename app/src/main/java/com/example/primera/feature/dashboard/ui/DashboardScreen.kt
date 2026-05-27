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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.primera.R
import com.example.primera.ui.components.*
import com.example.primera.core.theme.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCream)
        ) {
            DashboardTopBar(state.userName, onLogout)
            DashboardContent(
                state = state,
                onViewAllLogs = viewModel::onViewAllLogs,
                onAddLog = viewModel::onAddLog,
                onInputManually = viewModel::onInputManually
            )
        }

        if (state.isLoading) {
            FullScreenLoadingOverlay()
        }
    }
}

@Composable
fun DashboardContent(
    state: DashboardUiState,
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
        WeekCalendarRow(defaultWeekDays)
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
        Spacer(Modifier.height(24.dp))
        WellnessGoalsSection(state.wellnessGoals)
        Spacer(Modifier.height(24.dp))
        RecentSymptomsSection(defaultSymptoms)
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
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WeekCalendarRow(days: List<WeekDayItem>) {
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
private fun BabyRingCard(state: DashboardUiState) {
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
private fun StatsGrid(state: DashboardUiState, onInputManually: () -> Unit) {
    val stats = state.healthStats
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HealthStatCard(
                title = "Heart Rate",
                value = stats?.heartRateBpm?.toString() ?: "0",
                unit = "bpm",
                icon = painterResource(R.drawable.heart),
                iconBgColor = HeartRateBg,
                trendText = "▲ 0% vs last wk",
                trendColor = TrendGreen,
                modifier = Modifier.weight(1f)
            )
            HealthStatCard(
                title = "Steps",
                value = "%,d".format(stats?.steps ?: 0),
                unit = "steps",
                icon = painterResource(R.drawable.steps),
                iconBgColor = StepsBg,
                trendText = "Goal: %,d".format(stats?.stepsGoal ?: 8000),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HealthStatCard(
                title = "Sleep",
                value = "${stats?.sleepHours ?: 0}h ${stats?.sleepMinutes ?: 0}m",
                unit = "",
                icon = painterResource(R.drawable.sleep),
                iconBgColor = SleepBg,
                trendText = state.sleepQuality,
                trendColor = TrendGreen,
                modifier = Modifier.weight(1f)
            )
            InputManuallyCard(onClick = onInputManually, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RecentHealthLogsSection(
    logs: List<DashboardLogUiModel>,
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
                description = log.message,
                accentColor = log.color
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun WellnessGoalsSection(goals: List<DashboardWellnessGoalUiModel>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        DashboardSectionHeader(title = "Wellness Goals")
        
        Spacer(Modifier.height(16.dp))
        
        goals.forEach { goal ->
            WellnessGoalCard(
                label = goal.label,
                emoji = goal.emoji,
                current = goal.current,
                target = goal.target,
                unit = goal.unit,
                barColor = goal.color
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun RecentSymptomsSection(symptoms: List<SymptomItem>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        DashboardSectionHeader(title = "Recent Symptoms")
        
        Spacer(Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            symptoms.forEach { symptom ->
                SymptomChip(
                    label = symptom.label,
                    backgroundColor = symptom.backgroundColor,
                    indicatorColor = symptom.textColor
                )
            }
        }
    }
}

data class WeekDayItem(
    val initial: String,
    val date: Int,
    val isSelected: Boolean = false
)

data class SymptomItem(
    val label: String,
    val backgroundColor: Color,
    val textColor: Color
)

val defaultWeekDays = listOf(
    WeekDayItem("S", 22),
    WeekDayItem("M", 23, isSelected = true),
    WeekDayItem("T", 24),
    WeekDayItem("W", 25),
    WeekDayItem("T", 26),
    WeekDayItem("F", 27),
    WeekDayItem("S", 28)
)

val defaultSymptoms = listOf(
    SymptomItem("Mild Nausea", Color(0xFFFFF3E0), Color(0xFFE65100)),
    SymptomItem("Back Pain",   Color(0xFFE8EAF6), Color(0xFF3949AB)),
    SymptomItem("Fatigue",     Color(0xFFF3E5F5), Color(0xFF7B1FA2))
)

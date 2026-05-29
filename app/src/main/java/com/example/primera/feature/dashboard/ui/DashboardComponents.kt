package com.example.primera.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.R
import com.example.primera.core.theme.*
import com.example.primera.ui.components.*
import com.example.primera.feature.dashboard.ui.DashboardLogUiItem
import com.example.primera.feature.dashboard.ui.DashboardUiModel
import com.example.primera.feature.dashboard.ui.DashboardWeekDayItem
import java.util.*

@Composable
fun DashboardTopBar(userName: String, onLogout: () -> Unit) {
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
fun WeekCalendarRow(days: List<DashboardWeekDayItem>) {
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
fun BabyRingCard(
    weekNumber: Int,
    dayNumber: Int,
    daysLeft: Int,
    babySize: String,
    babyEmoji: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularPregnancyRing(
            weekNumber = weekNumber,
            dayNumber = dayNumber,
            daysLeft = daysLeft,
            babyEmoji = babyEmoji
        )

        Spacer(Modifier.height(16.dp))

        TrimesterBadge(
            weekNumber = weekNumber,
            dayNumber = dayNumber
        )
        Spacer(Modifier.height(4.dp))
        BabySizeText(babySize = babySize)
    }
}

@Composable
fun SyncWatchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PrimeraGradientButton(
        text = "Sync Watch Now",
        onClick = onClick,
        modifier = modifier.padding(horizontal = 20.dp)
    )
}

@Composable
fun StatsGrid(
    state: DashboardUiModel,
    onInputManually: () -> Unit,
    onSyncWatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!state.isWatchSynced) {
            SyncWatchButton(onClick = onSyncWatch, modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HealthStatCard(
                title = "Heart Rate",
                value = if (state.isWatchSynced) state.heartRateBpm.toString() else "--",
                unit = "bpm",
                icon = painterResource(R.drawable.heart),
                iconBgColor = HeartRateBg,
                trendText = if (state.isWatchSynced) "${if (state.heartRateTrendingUp) "▲" else "▼"} ${state.heartRateVsLastWeek}% vs last wk" else null,
                trendColor = if (state.heartRateTrendingUp) TrendGreen else TrendRed,
                modifier = Modifier.weight(1f)
            )
            HealthStatCard(
                title = "Steps",
                value = if (state.isWatchSynced) "%,d".format(state.steps) else "--",
                unit = "steps",
                icon = painterResource(R.drawable.steps),
                iconBgColor = StepsBg,
                trendText = if (state.isWatchSynced) "Goal: %,d".format(state.stepsGoal) else "Goal: --",
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HealthStatCard(
                title = "Sleep",
                value = if (state.isWatchSynced) "${state.sleepHours}h ${state.sleepMinutes}m" else "--",
                unit = "",
                icon = painterResource(R.drawable.sleep),
                iconBgColor = SleepBg,
                trendText = if (state.isWatchSynced) state.sleepQuality.ifBlank { "—" } else "--",
                trendColor = TrendGreen,
                modifier = Modifier.weight(1f)
            )
            HealthStatCard(
                title = "SpO₂",
                value = if (state.isWatchSynced) (state.spO2?.toString() ?: "--") else "--",
                unit = "%",
                icon = painterResource(R.drawable.sp02),
                iconBgColor = Color(0xFFFFEBEE),
                trendText = if (state.isWatchSynced) "Normal" else null,
                trendColor = TrendGreen,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RecentHealthLogsSection(
    logs: List<DashboardLogUiItem>,
    onViewAll: () -> Unit,
    onAdd: () -> Unit,
    onLogClick: (DashboardLogUiItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        DashboardSectionHeader(
            title = "Recent Health Logs",
            onViewAll = onViewAll,
            onAdd = onAdd
        )

        Spacer(Modifier.height(16.dp))

        if (logs.isEmpty()) {
            Text(
                text = "No logs yet. Add your first health log!",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            logs.forEach { log ->
                LogCard(
                    category = log.category,
                    time = log.time,
                    description = log.description,
                    accentColor = log.accentColor,
                    onClick = { onLogClick(log) }
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

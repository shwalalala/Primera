package com.example.primera.frontend.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.primera.frontend.common.theme.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.primera.R


// ---------------------------------------------------------------------------
// Entry point — ViewModel wired
// ---------------------------------------------------------------------------

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { DashboardTopBar() },
        bottomBar = { DashboardBottomBar() },
        containerColor = BackgroundCream,
        modifier = modifier
    ) { innerPadding ->
        DashboardContent(
            state           = state,
            onViewAllLogs   = viewModel::onViewAllLogs,
            onAddLog        = viewModel::onAddLog,
            onInputManually = viewModel::onInputManually,
            modifier        = Modifier.padding(innerPadding)
        )
    }
}

// ---------------------------------------------------------------------------
// Stateless content — previewable without ViewModel
// ---------------------------------------------------------------------------

@Composable
fun DashboardContent(
    state: DashboardState   = DashboardState(),
    onViewAllLogs: () -> Unit = {},
    onAddLog: () -> Unit      = {},
    onInputManually: () -> Unit = {},
    modifier: Modifier        = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        WeekCalendarRow(state.weekDays)
        Spacer(Modifier.height(8.dp))
        GreetingSection(state)
        Spacer(Modifier.height(4.dp))
        BabyRingCard(state)
        Spacer(Modifier.height(20.dp))
        StatsGrid(state, onInputManually)
        Spacer(Modifier.height(24.dp))
        RecentHealthLogs(state.recentLogs, onViewAllLogs, onAddLog)
        Spacer(Modifier.height(24.dp))
        WellnessGoalsSection(state.wellnessGoals)
        Spacer(Modifier.height(24.dp))
        RecentSymptomsSection(state.recentSymptoms)
        Spacer(Modifier.height(8.dp))
    }
}

// ---------------------------------------------------------------------------
// TopBar & BottomBar
// ---------------------------------------------------------------------------

@Composable
private fun DashboardTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 20.dp)
            .padding( horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Menu Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5ECEC))
                .border(1.dp, Color(0xFFF0F0F0), CircleShape)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(Modifier.size(width = 16.dp, height = 2.dp).background(TextSecondary))
                Box(Modifier.size(width = 16.dp, height = 2.dp).background(TextSecondary))
                Box(Modifier.size(width = 10.dp, height = 2.dp).background(TextSecondary))
            }
        }

        // Profile Picture
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5ECEC))
        ) {

        }
    }
}

// Call our bottom nav
@Composable
private fun DashboardBottomBar() {

}

@Composable
private fun BottomNavItem(icon: String, label: String, isSelected: Boolean) {

}

// ---------------------------------------------------------------------------
// Week calendar strip
// ---------------------------------------------------------------------------

@Composable
private fun WeekCalendarRow(days: List<WeekDayItem>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { DayChip(it) }
        }
    }
}

@Composable
private fun DayChip(day: WeekDayItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(40.dp)
    ) {
        Text(
            text = day.initial,
            fontSize = 11.sp,
            color = if (day.isSelected) Color(0xFFB8A8D9) else TextSecondary,
            fontWeight = if (day.isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
        Spacer(Modifier.height(4.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFFDFD))
                .then(
                    if (day.isSelected) {
                        Modifier.border(1.5.dp, Color(0xFFB8A8D9), CircleShape)
                    } else Modifier
                )
        ) {
            Text(
                text = day.date.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (day.isSelected) Color(0xFFB8A8D9) else TextPrimary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Greeting
// ---------------------------------------------------------------------------

@Composable
private fun GreetingSection(state: DashboardState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello, ${state.userName}",
            fontSize = 13.sp,
            color = TextSecondary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = state.timeOfDay.ifBlank { "Good morning" },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "You're in your ${state.trimester}",
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}

// ---------------------------------------------------------------------------
// Baby circular ring card
// ---------------------------------------------------------------------------

@Composable
private fun BabyRingCard(state: DashboardState) {
    val totalDays   = 280f
    val elapsed     = (totalDays - state.daysLeft).coerceIn(0f, totalDays)
    val sweepAngle  = (elapsed / totalDays) * 300f

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(240.dp) // 20dp bigger than ring
                .shadow(
                    elevation = 18.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.12f)
                )
                .background(
                    color = Color.White,
                    shape = CircleShape
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .drawBehind {
                        val strokeWidth = 14.dp.toPx()
                        val inset = strokeWidth / 2f
                        val arcSize = Size(
                            size.width - strokeWidth,
                            size.height - strokeWidth
                        )
                        val topLeft = Offset(inset, inset)
                        val startAngle = 120f

                        drawArc(
                            color = Color(0x4DDEDEDE),
                            startAngle = startAngle,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )

                        drawArc(
                            color = Color(0xFFC5B4E3),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )
                    }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🥬", fontSize = 72.sp)

                    Spacer(Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFF5F0FF)
                    ) {
                        Text(
                            text = "${state.daysLeft} Days Left",
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 4.dp
                            ),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimeraViolet
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text       = "Week ${state.weekNumber}, Day ${state.dayNumber}",
            fontSize   = 24.sp,
            fontWeight = FontWeight.Bold,
            color      = TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text     = "Your baby is the size of a ${state.babySize}!",
            fontSize = 14.sp,
            color    = TextSecondary
        )
    }
}

// ---------------------------------------------------------------------------
// Stats — 2×2 grid
// ---------------------------------------------------------------------------

@Composable
private fun StatsGrid(state: DashboardState, onInputManually: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HeartRateCard(state, Modifier.weight(1f))
            StepsCard(state, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SleepCard(state, Modifier.weight(1f))
            InputManuallyCard(onInputManually, Modifier.weight(1f))
        }
    }
}

@Composable
private fun HeartRateCard(state: DashboardState, modifier: Modifier) {
    StatCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEBEE))
            ) {
                Image(
                    painter = painterResource(R.drawable.heart),
                    contentDescription = "Heart",
                    modifier = Modifier.size(13.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                "Heart Rate",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text       = state.heartRateBpm.toString(),
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text     = "bpm",
                fontSize = 12.sp,
                color    = TextSecondary,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text     = "${if (state.heartRateTrendingUp) "▲" else "▼"} ${state.heartRateVsLastWeek}% vs last wk",
            fontSize = 11.sp,
            color    = if (state.heartRateTrendingUp) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}

@Composable
private fun StepsCard(state: DashboardState, modifier: Modifier) {
        StatCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F8E9))
            ) {
                Image(
                    painter = painterResource(R.drawable.steps),
                    contentDescription = "Heart",
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Steps",
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text       = "%,d".format(state.steps),
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text     = "steps",
                fontSize = 12.sp,
                color    = TextSecondary,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text     = "Goal: %,d".format(state.stepsGoal),
            fontSize = 11.sp,
            color    = TextSecondary
        )
    }
}

@Composable
private fun SleepCard(state: DashboardState, modifier: Modifier) {
    StatCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3E5F5))
            ) {
                Image(
                painter = painterResource(R.drawable.sleep),
                contentDescription = "Heart",
                modifier = Modifier.size(13.dp)
            ) }
            Spacer(Modifier.width(8.dp))
            Text(
                "Sleep",
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text       = "${state.sleepHours}h ${state.sleepMinutes}m",
            fontSize   = 24.sp,
            fontWeight = FontWeight.Bold,
            color      = TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text     = state.sleepQuality.ifBlank { "—" },
            fontSize = 11.sp,
            color    = Color(0xFF4CAF50)
        )
    }
}

@Composable
private fun InputManuallyCard(onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier  = modifier
            .height(110.dp)
            .clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(
            modifier              = Modifier.fillMaxSize(),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Text(
                text     = "+",
                fontSize = 32.sp,
                color    = TextSecondary,
                fontWeight = FontWeight.Light
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text       = "Input Manually",
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
                textAlign  = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = modifier.height(110.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) { content() }
    }
}

// ---------------------------------------------------------------------------
// Recent health logs
// ---------------------------------------------------------------------------

@Composable
private fun RecentHealthLogs(
    logs: List<HealthLogItem>,
    onViewAll: () -> Unit,
    onAdd: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Recent Health Logs",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "View All",
                    fontSize   = 12.sp,
                    color      = TextSecondary,
                    modifier   = Modifier.clickable { onViewAll() }
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFFDDDDDD), CircleShape)
                        .clickable { onAdd() }
                ) {
                    Text("+", fontSize = 14.sp, color = TextSecondary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        logs.forEach { log ->
            LogCard(log)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LogCard(log: HealthLogItem) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(log.accentColor)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = log.category.uppercase(),
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text     = log.time.ifBlank { "—" },
                    fontSize = 11.sp,
                    color    = TextSecondary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text       = log.description.ifBlank { "No description." },
                fontSize   = 13.sp,
                color      = TextPrimary,
                lineHeight = 18.sp
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Wellness goals
// ---------------------------------------------------------------------------

@Composable
private fun WellnessGoalsSection(goals: List<WellnessGoalItem>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            "Wellness Goals",
            fontSize   = 16.sp,
            fontWeight = FontWeight.Bold,
            color      = TextPrimary
        )
        Spacer(Modifier.height(16.dp))
        goals.forEach { goal ->
            WellnessGoalCard(goal)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun WellnessGoalCard(goal: WellnessGoalItem) {
    val progress = if (goal.target > 0f) (goal.current / goal.target).coerceIn(0f, 1f) else 0f
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF5F5F5))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(goal.barColor.copy(alpha = 0.1f))
                    ) {
                        Text(goal.emoji, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text       = goal.label,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary
                    )
                }
                Text(
                    text     = "${goal.current} / ${goal.target} ${goal.unit}",
                    fontSize = 11.sp,
                    color    = TextSecondary
                )
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress  = { progress },
                modifier  = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color      = goal.barColor,
                trackColor = Color(0xFFF5F5F5)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Recent symptoms chips
// ---------------------------------------------------------------------------

@Composable
private fun RecentSymptomsSection(symptoms: List<SymptomItem>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            "Recent Symptoms",
            fontSize   = 16.sp,
            fontWeight = FontWeight.Bold,
            color      = TextPrimary
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            symptoms.forEach { SymptomChip(it) }
        }
    }
}

@Composable
private fun SymptomChip(symptom: SymptomItem) {
    Surface(
        shape = RoundedCornerShape(50),
        color = symptom.backgroundColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(symptom.textColor)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text     = symptom.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color    = TextPrimary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun DashboardScreenPreview() {
    PrimeraTheme {
        DashboardScreen()
    }
}

package cit.edu.primera.feature.insights.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cit.edu.primera.core.di.ViewModelProvider
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.ErrorRed
import cit.edu.primera.core.theme.Good
import cit.edu.primera.core.theme.HeartRateBg
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.SleepBg
import cit.edu.primera.core.theme.SurfaceWhite
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.feature.goals.data.GoalDto
import cit.edu.primera.feature.goals.ui.AddGoalDialog
import cit.edu.primera.ui.components.ChartContainer
import cit.edu.primera.ui.components.DashboardSectionHeader
import cit.edu.primera.ui.components.FeatureTopBar
import cit.edu.primera.ui.components.FullScreenLoadingOverlay
import cit.edu.primera.ui.components.GoalItemCard
import cit.edu.primera.ui.components.InsightTipCard
import cit.edu.primera.ui.components.MonitoringDisclaimerFooter
import cit.edu.primera.ui.components.MoodLineChart
import cit.edu.primera.ui.components.OfflineBanner
import cit.edu.primera.ui.components.SimpleBarChart

@Composable
fun InsightsScreen(
    onBack: () -> Unit,
    viewModel: InsightsViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activePeriod by viewModel.activePeriod.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    var showAddGoal by remember { mutableStateOf(value = false) }
    var editingGoal by remember { mutableStateOf<GoalDto?>(null) }
    val context = LocalContext.current

    InsightsScreenContent(
        uiState = uiState,
        activePeriod = activePeriod,
        isOnline = isOnline,
        showAddGoal = showAddGoal,
        editingGoal = editingGoal,
        onBack = onBack,
        onPeriodSelected = viewModel::onPeriodSelected,
        onExport = { viewModel.onExportSummaryPdf(context) },
        onAddGoalClick = { showAddGoal = true },
        onGoalClick = { goalId ->
            if (uiState is InsightsUiState.Success) {
                editingGoal = (uiState as InsightsUiState.Success).data.rawGoals.find { it.id == goalId }
            }
        },
        onDeleteGoal = viewModel::deleteGoal,
        onDismissGoalDialog = {
            showAddGoal = false
            editingGoal = null
        },
        onSaveGoal = { goal ->
            viewModel.saveGoal(goal)
            showAddGoal = false
            editingGoal = null
        },
        onWeightPrevious = viewModel::onWeightPrevious,
        onWeightNext = viewModel::onWeightNext,
        onWeightBarClick = viewModel::onWeightBarClick,
        onMoodPrevious = viewModel::onMoodPrevious,
        onMoodNext = viewModel::onMoodNext,
        onActivityPrevious = viewModel::onActivityPrevious,
        onActivityNext = viewModel::onActivityNext,
        onActivityBarClick = viewModel::onActivityBarClick,
        onHrPeriodSelected = viewModel::onHrPeriodSelected,
        onHrPrevious = viewModel::onHrPrevious,
        onHrNext = viewModel::onHrNext,
        onHrBarClick = viewModel::onHrBarClick,
        onSleepPeriodSelected = viewModel::onSleepPeriodSelected,
        onSleepPrevious = viewModel::onSleepPrevious,
        onSleepNext = viewModel::onSleepNext,
        onSleepBarClick = viewModel::onSleepBarClick,
        onSpO2PeriodSelected = viewModel::onSpO2PeriodSelected,
        onSpO2Previous = viewModel::onSpO2Previous,
        onSpO2Next = viewModel::onSpO2Next,
        onSpO2BarClick = viewModel::onSpO2BarClick
    )
}

@Composable
fun InsightsScreenContent(
    uiState: InsightsUiState,
    activePeriod: String,
    isOnline: Boolean,
    showAddGoal: Boolean,
    editingGoal: GoalDto?,
    onBack: () -> Unit,
    onPeriodSelected: (String) -> Unit,
    onExport: () -> Unit,
    onAddGoalClick: () -> Unit,
    onGoalClick: (String) -> Unit,
    onDeleteGoal: (String) -> Unit,
    onDismissGoalDialog: () -> Unit,
    onSaveGoal: (GoalDto) -> Unit,
    modifier: Modifier = Modifier,
    onWeightPrevious: () -> Unit = {},
    onWeightNext: () -> Unit = {},
    onWeightBarClick: (Int) -> Unit = {},
    onMoodPrevious: () -> Unit = {},
    onMoodNext: () -> Unit = {},
    onActivityPrevious: () -> Unit = {},
    onActivityNext: () -> Unit = {},
    onActivityBarClick: (Int) -> Unit = {},
    onHrPeriodSelected: (String) -> Unit = {},
    onHrPrevious: () -> Unit = {},
    onHrNext: () -> Unit = {},
    onHrBarClick: (Int) -> Unit = {},
    onSleepPeriodSelected: (String) -> Unit = {},
    onSleepPrevious: () -> Unit = {},
    onSleepNext: () -> Unit = {},
    onSleepBarClick: (Int) -> Unit = {},
    onSpO2PeriodSelected: (String) -> Unit = {},
    onSpO2Previous: () -> Unit = {},
    onSpO2Next: () -> Unit = {},
    onSpO2BarClick: (Int) -> Unit = {}
) {
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
                title = "Insights", 
                onBack = onBack,
                selectedPeriod = activePeriod,
                onPeriodSelected = onPeriodSelected,
                onExport = onExport
            )

            if (!isOnline) {
                OfflineBanner()
            }

            when (val state = uiState) {
                is InsightsUiState.Loading -> FullScreenLoadingOverlay()
                is InsightsUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = ErrorRed)
                }
                is InsightsUiState.Success -> {
                    InsightsContent(
                        data = state.data,
                        activePeriod = activePeriod,
                        onPeriodSelected = onPeriodSelected,
                        onAddGoal = onAddGoalClick,
                        onGoalClick = onGoalClick,
                        onDeleteGoal = onDeleteGoal,
                        onWeightPrevious = onWeightPrevious,
                        onWeightNext = onWeightNext,
                        onWeightBarClick = onWeightBarClick,
                        onMoodPrevious = onMoodPrevious,
                        onMoodNext = onMoodNext,
                        onActivityPrevious = onActivityPrevious,
                        onActivityNext = onActivityNext,
                        onActivityBarClick = onActivityBarClick,
                        onHrPeriodSelected = onHrPeriodSelected,
                        onHrPrevious = onHrPrevious,
                        onHrNext = onHrNext,
                        onHrBarClick = onHrBarClick,
                        onSleepPeriodSelected = onSleepPeriodSelected,
                        onSleepPrevious = onSleepPrevious,
                        onSleepNext = onSleepNext,
                        onSleepBarClick = onSleepBarClick,
                        onSpO2PeriodSelected = onSpO2PeriodSelected,
                        onSpO2Previous = onSpO2Previous,
                        onSpO2Next = onSpO2Next,
                        onSpO2BarClick = onSpO2BarClick
                    )
                }
            }
        }

        if (showAddGoal || editingGoal != null) {
            AddGoalDialog(
                onDismiss = onDismissGoalDialog,
                editingGoal = editingGoal,
                initialPeriod = activePeriod,
                onConfirm = onSaveGoal
            )
        }
    }
}

@Composable
fun FeatureChartHeader(
    title: String,
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showMenu = true }
            ) {
                Text(selectedPeriod, fontSize = 12.sp, color = PrimeraViolet)
                Icon(Icons.Default.ArrowDropDown, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(SurfaceWhite)
            ) {
                listOf("Daily", "Weekly", "Monthly", "Yearly").forEach { period ->
                    DropdownMenuItem(
                        text = { Text(period, color = TextPrimary) },
                        onClick = {
                            onPeriodSelected(period)
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InsightsContent(
    data: InsightsUiModel,
    activePeriod: String,
    onPeriodSelected: (String) -> Unit,
    onAddGoal: () -> Unit,
    onGoalClick: (String) -> Unit,
    onDeleteGoal: (String) -> Unit,
    modifier: Modifier = Modifier,
    onWeightPrevious: () -> Unit = {},
    onWeightNext: () -> Unit = {},
    onWeightBarClick: (Int) -> Unit = {},
    onMoodPrevious: () -> Unit = {},
    onMoodNext: () -> Unit = {},
    onActivityPrevious: () -> Unit = {},
    onActivityNext: () -> Unit = {},
    onActivityBarClick: (Int) -> Unit = {},
    onHrPeriodSelected: (String) -> Unit = {},
    onHrPrevious: () -> Unit = {},
    onHrNext: () -> Unit = {},
    onHrBarClick: (Int) -> Unit = {},
    onSleepPeriodSelected: (String) -> Unit = {},
    onSleepPrevious: () -> Unit = {},
    onSleepNext: () -> Unit = {},
    onSleepBarClick: (Int) -> Unit = {},
    onSpO2PeriodSelected: (String) -> Unit = {},
    onSpO2Previous: () -> Unit = {},
    onSpO2Next: () -> Unit = {},
    onSpO2BarClick: (Int) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Pregnancy & Goal Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Pregnancy & Goal Progress",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(Modifier.height(16.dp))

                Text(
                    "CURRENT GESTATIONAL AGE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${data.gestationalAgeWeeks} weeks, ${data.gestationalAgeDays} days",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimeraViolet
                    )
                    if (data.gestationalAgeWeeks >= 40) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ErrorRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Post-term",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Pregnancy Timeline Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pregnancy Progress", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    Text("${(data.pregnancyProgress * 100).toInt()}%", fontSize = 12.sp, color = PrimeraViolet, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { data.pregnancyProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PrimeraViolet,
                    trackColor = PrimeraLilac.copy(alpha = 0.3f)
                )

                Spacer(Modifier.height(16.dp))

                // Goal Completion Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active Goals (${data.completedGoalsCount}/${data.totalGoalsCount})", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    Text("${(data.goalCompletionRatio * 100).toInt()}%", fontSize = 12.sp, color = Good, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { data.goalCompletionRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Good,
                    trackColor = Good.copy(alpha = 0.15f)
                )
            }
        }

        // 2. Summary Tips & Warnings
        InsightTipCard(
            text = data.completedGoalsText + " " + data.goalsCompletionTip,
            backgroundColor = data.goalsCompletionTipColor,
            contentColor = TextPrimary.copy(alpha = 0.8f)
        )

        data.healthWarningTip?.let {
            InsightTipCard(
                text = it,
                backgroundColor = data.healthWarningTipColor,
                contentColor = SurfaceWhite
            )
        }

        // 3. Wellness Goals Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                DashboardSectionHeader(
                    title = "$activePeriod Wellness Goals",
                    onAdd = onAddGoal
                )
                
                Spacer(Modifier.height(12.dp))

                if (data.wellnessGoals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No wellness goals set for $activePeriod. Tap + to add one!",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    data.wellnessGoals.forEach { goal ->
                        GoalItemCard(
                            icon = goal.icon,
                            title = goal.title,
                            currentValue = goal.current,
                            targetValue = goal.target,
                            progress = goal.progress,
                            accentColor = goal.color,
                            onClick = { onGoalClick(goal.id) },
                            onDelete = if (goal.isDeletable) {
                                { onDeleteGoal(goal.id) }
                            } else null
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        // 4. Health Insights (Charts) Section
        DashboardSectionHeader(title = "Health Insights")

        // Weight Chart
        ChartContainer(
            title = {
                FeatureChartHeader(
                    title = "Weight (kg)",
                    selectedPeriod = activePeriod,
                    onPeriodSelected = onPeriodSelected
                )
            },
            dateRange = data.weightTrend.dateRange,
            onPrevious = onWeightPrevious,
            onNext = onWeightNext
        ) {
            SimpleBarChart(
                data = data.weightTrend.values,
                labels = data.weightTrend.labels,
                highlightedIndex = data.weightTrend.highlightedIndex,
                onBarClick = onWeightBarClick
            )
        }

        // Mood Chart
        ChartContainer(
            title = {
                FeatureChartHeader(
                    title = "Mood Trend",
                    selectedPeriod = activePeriod,
                    onPeriodSelected = onPeriodSelected
                )
            },
            dateRange = data.moodTrend.dateRange,
            onPrevious = onMoodPrevious,
            onNext = onMoodNext
        ) {
            MoodLineChart(
                data = data.moodTrend.values,
                labels = data.moodTrend.labels
            )
        }

        // Activity Chart
        ChartContainer(
            title = {
                FeatureChartHeader(
                    title = "Movement (Steps)",
                    selectedPeriod = activePeriod,
                    onPeriodSelected = onPeriodSelected
                )
            },
            dateRange = data.activityTrend.dateRange,
            onPrevious = onActivityPrevious,
            onNext = onActivityNext
        ) {
            SimpleBarChart(
                data = data.activityTrend.values,
                labels = data.activityTrend.labels,
                highlightedIndex = data.activityTrend.highlightedIndex,
                onBarClick = onActivityBarClick
            )
        }

        // Heart Rate Chart
        ChartContainer(
            title = {
                FeatureChartHeader(
                    title = "Heart Rate (BPM)",
                    selectedPeriod = activePeriod,
                    onPeriodSelected = onHrPeriodSelected
                )
            },
            dateRange = data.heartRateTrend.dateRange,
            onPrevious = onHrPrevious,
            onNext = onHrNext
        ) {
            SimpleBarChart(
                data = data.heartRateTrend.values,
                labels = data.heartRateTrend.labels,
                highlightedIndex = data.heartRateTrend.highlightedIndex,
                onBarClick = onHrBarClick,
                barColor = HeartRateBg
            )
        }

        // Sleep Chart
        ChartContainer(
            title = {
                FeatureChartHeader(
                    title = "Sleep (Hours)",
                    selectedPeriod = activePeriod,
                    onPeriodSelected = onSleepPeriodSelected
                )
            },
            dateRange = data.sleepTrend.dateRange,
            onPrevious = onSleepPrevious,
            onNext = onSleepNext
        ) {
            SimpleBarChart(
                data = data.sleepTrend.values,
                labels = data.sleepTrend.labels,
                highlightedIndex = data.sleepTrend.highlightedIndex,
                onBarClick = onSleepBarClick,
                barColor = SleepBg
            )
        }

        // SpO2 Chart
        ChartContainer(
            title = {
                FeatureChartHeader(
                    title = "SpO2 (%)",
                    selectedPeriod = activePeriod,
                    onPeriodSelected = onSpO2PeriodSelected
                )
            },
            dateRange = data.spO2Trend.dateRange,
            onPrevious = onSpO2Previous,
            onNext = onSpO2Next
        ) {
            SimpleBarChart(
                data = data.spO2Trend.values,
                labels = data.spO2Trend.labels,
                highlightedIndex = data.spO2Trend.highlightedIndex,
                onBarClick = onSpO2BarClick,
                barColor = Color(0xFFFFEBEE)
            )
        }

        Spacer(Modifier.height(8.dp))

        // 5. Highlighted Footer with Legal Disclaimer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                MonitoringDisclaimerFooter()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightsPreview() {
    PrimeraTheme {
        InsightsScreenContent(
            uiState = InsightsUiState.Success(
                InsightsUiModel(
                    gestationalAgeWeeks = 28,
                    gestationalAgeDays = 3,
                    gestationalAgeText = "28 weeks, 3 days",
                    pregnancyProgress = 0.7f,
                    goalCompletionRatio = 0.8f,
                    completedGoalsCount = 4,
                    totalGoalsCount = 5,
                    activePeriod = "Weekly",
                    completedGoalsText = "You've completed 4 of 5 weekly goals!",
                    goalsCompletionTip = "Keep up the great work!",
                    wellnessGoals = listOf(
                        InsightGoalUiItem(
                            id = "1",
                            icon = "water",
                            title = "Hydration",
                            current = "2.0 L",
                            target = "2.5 L",
                            progress = 0.8f,
                            color = Color(0xFF93C5FD)
                        )
                    ),
                    weightTrend = ChartData(
                        title = "Weight (kg)",
                        dateRange = "Oct 12 - Oct 18",
                        values = listOf(65.0f, 65.2f, 65.5f, 65.4f, 65.6f, 65.8f, 66.0f),
                        labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                        highlightedIndex = 6
                    ),
                    moodTrend = ChartData(
                        title = "Mood Trend",
                        dateRange = "Oct 12 - Oct 18",
                        values = listOf(3f, 4f, 5f, 4f, 5f, 5f, 4f),
                        labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    ),
                    activityTrend = ChartData(
                        title = "Movement (Steps)",
                        dateRange = "Oct 12 - Oct 18",
                        values = listOf(5000f, 7000f, 6500f, 8000f, 7200f, 9000f, 8500f),
                        labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                        highlightedIndex = 5
                    ),
                    heartRateTrend = ChartData(
                        title = "Heart Rate (BPM)",
                        dateRange = "Oct 12 - Oct 18",
                        values = listOf(72f, 75f, 74f, 78f, 76f, 80f, 75f),
                        labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    ),
                    sleepTrend = ChartData(
                        title = "Sleep (Hours)",
                        dateRange = "Oct 12 - Oct 18",
                        values = listOf(7.5f, 8.0f, 7.0f, 6.5f, 8.5f, 8.0f, 7.5f),
                        labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    ),
                    spO2Trend = ChartData(
                        title = "SpO2 (%)",
                        dateRange = "Oct 12 - Oct 18",
                        values = listOf(98f, 99f, 98f, 97f, 99f, 98f, 98f),
                        labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    )
                )
            ),
            activePeriod = "Weekly",
            isOnline = true,
            showAddGoal = false,
            editingGoal = null,
            onBack = {},
            onPeriodSelected = {},
            onExport = {},
            onAddGoalClick = {},
            onGoalClick = {},
            onDeleteGoal = {},
            onDismissGoalDialog = {},
            onSaveGoal = {}
        )
    }
}

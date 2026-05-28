package com.example.primera.feature.insights.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.primera.core.di.ViewModelProvider
import com.example.primera.core.theme.*
import com.example.primera.feature.goals.data.GoalsRepository
import com.example.primera.feature.goals.ui.AddGoalDialog
import com.example.primera.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun InsightsScreen(
    onBack: () -> Unit,
    viewModel: InsightsViewModel = viewModel(factory = ViewModelProvider.Factory),
    goalsRepository: GoalsRepository? = null 
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activePeriod by viewModel.activePeriod.collectAsStateWithLifecycle()
    var showAddGoal by remember { mutableStateOf(value = false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
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
                onPeriodSelected = viewModel::onPeriodSelected
            )

            when (val state = uiState) {
                is InsightsUiState.Loading -> FullScreenLoadingOverlay()
                is InsightsUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = ErrorRed)
                }
                is InsightsUiState.Success -> {
                    InsightsContent(
                        data = state.data,
                        activePeriod = activePeriod,
                        onPeriodSelected = viewModel::onPeriodSelected,
                        onAddGoal = { showAddGoal = true },
                        onPreviousRange = viewModel::onPreviousRange,
                        onNextRange = viewModel::onNextRange
                    )
                }
            }
        }

        if (showAddGoal) {
            AddGoalDialog(
                onDismiss = { showAddGoal = false }
            ) { goal ->
                scope.launch {
                    goalsRepository?.addGoal(goal)
                    showAddGoal = false
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
    onPreviousRange: () -> Unit,
    onNextRange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Purple Background Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(BackgroundSection)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Your Physique Today",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Spacer(Modifier.height(16.dp))

                InsightCircularProgress(
                    progress = data.physiqueProgress,
                    progressColor = data.progressColor,
                    centerContent = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                data.physiqueSubtitle,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = data.progressColor
                            )
                            Text(
                                data.physiqueStatus,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    }
                )

                Spacer(Modifier.height(24.dp))

                // Tips
                InsightTipCard(
                    text = data.completedGoalsText + " " + data.goalsCompletionTip,
                    backgroundColor = data.goalsCompletionTipColor,
                    contentColor = TextPrimary.copy(alpha = 0.8f)
                )

                Spacer(Modifier.height(12.dp))

                data.healthWarningTip?.let {
                    InsightTipCard(
                        text = it,
                        backgroundColor = data.healthWarningTipColor,
                        contentColor = SurfaceWhite
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Goals Section
                DashboardSectionHeader(
                    title = "$activePeriod Wellness Goals",
                    onAdd = onAddGoal
                )
                
                Spacer(Modifier.height(8.dp))

                data.wellnessGoals.forEach { goal ->
                    GoalItemCard(
                        icon = goal.icon,
                        title = goal.title,
                        currentValue = goal.current,
                        targetValue = goal.target,
                        progress = goal.progress,
                        accentColor = goal.color
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Health Insights (Charts)
                DashboardSectionHeader(title = "Health Insights")
                Spacer(Modifier.height(8.dp))

                ChartContainer(
                    title = data.weightTrend.title,
                    dateRange = data.weightTrend.dateRange,
                    onPrevious = onPreviousRange,
                    onNext = onNextRange
                ) {
                    SimpleBarChart(
                        data = data.weightTrend.values,
                        highlightedIndex = data.weightTrend.highlightedIndex
                    )
                }

                Spacer(Modifier.height(16.dp))

                ChartContainer(
                    title = data.moodTrend.title,
                    dateRange = data.moodTrend.dateRange,
                    onPrevious = onPreviousRange,
                    onNext = onNextRange
                ) {
                    SimpleLineChart(data = data.moodTrend.values)
                }

                Spacer(Modifier.height(16.dp))

                ChartContainer(
                    title = data.activityTrend.title,
                    dateRange = data.activityTrend.dateRange,
                    onPrevious = onPreviousRange,
                    onNext = onNextRange
                ) {
                    SimpleBarChart(
                        data = data.activityTrend.values,
                        highlightedIndex = data.activityTrend.highlightedIndex
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightsPreview() {
    PrimeraTheme {
        InsightsContent(
            data = InsightsUiModel(
                physiqueProgress = 0.6f,
                physiqueSubtitle = "60%",
                physiqueStatus = "Healthy",
                progressColor = Color(0xFFA1D386),
                completedGoalsText = "This week, you've completed 5/24 goals.",
                goalsCompletionTip = "Try focusing on one small goal each day this week.",
                wellnessGoals = listOf(
                    InsightGoalUiItem("1", "💧", "Hydration", "1.8", "2.5 L", 0.72f, Color(0xFF64B5F6))
                ),
                weightTrend = ChartData(
                    title = "Weight(kg)",
                    dateRange = "March 10 - March 16, 2024",
                    values = listOf(45f, 48f, 52f, 60f, 55f, 40f, 35f),
                    highlightedIndex = 3
                ),
                moodTrend = ChartData(
                    title = "Mood Trend",
                    dateRange = "March 10 - March 16, 2024",
                    values = listOf(3f, 4f, 2f, 5f, 4f, 3f, 4f)
                ),
                activityTrend = ChartData(
                    title = "This week's activity",
                    dateRange = "March 10 - March 16, 2024",
                    values = listOf(2000f, 4000f, 6000f, 8000f, 7000f, 5000f, 6432f),
                    highlightedIndex = 6
                )
            ),
            activePeriod = "Weekly",
            onPeriodSelected = {},
            onAddGoal = {},
            onPreviousRange = {},
            onNextRange = {}
        )
    }
}

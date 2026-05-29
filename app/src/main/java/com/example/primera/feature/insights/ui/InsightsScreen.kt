package com.example.primera.feature.insights.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.example.primera.feature.goals.data.GoalDto
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
    var editingGoal by remember { mutableStateOf<GoalDto?>(null) }
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
                        onGoalClick = { goalId ->
                            editingGoal = state.data.rawGoals.find { it.id == goalId }
                        },
                        viewModel = viewModel
                    )
                }
            }
        }

        if (showAddGoal || editingGoal != null) {
            AddGoalDialog(
                onDismiss = { 
                    showAddGoal = false
                    editingGoal = null
                },
                editingGoal = editingGoal
            ) { goal ->
                scope.launch {
                    if (goal.id != null) {
                        goalsRepository?.updateGoal(goal)
                    } else {
                        goalsRepository?.addGoal(goal)
                    }
                    showAddGoal = false
                    editingGoal = null
                }
            }
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
    viewModel: InsightsViewModel, // Pass viewModel to handle individual chart actions
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
                        accentColor = goal.color,
                        onClick = { onGoalClick(goal.id) }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Health Insights (Charts)
                DashboardSectionHeader(title = "Health Insights")
                Spacer(Modifier.height(8.dp))

                // BMI Insight Card
                if (data.bmiValue != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceWhite)
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    "Your BMI",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    "Based on latest check-in",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "%.1f".format(data.bmiValue),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimeraViolet
                                )
                                Spacer(Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when(data.bmiStatus) {
                                                "Healthy" -> Color(0xFFA1D386).copy(alpha = 0.2f)
                                                "Underweight" -> Color(0xFF64B5F6).copy(alpha = 0.2f)
                                                else -> Color(0xFFF28B82).copy(alpha = 0.2f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = data.bmiStatus ?: "",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when(data.bmiStatus) {
                                            "Healthy" -> Color(0xFF689F38)
                                            "Underweight" -> Color(0xFF1976D2)
                                            else -> Color(0xFFD32F2F)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Weight Chart
                ChartContainer(
                    title = {
                        FeatureChartHeader(
                            title = "Weight(kg)",
                            selectedPeriod = data.weightTrend.title.substringAfter("(").substringBefore(")"),
                            onPeriodSelected = viewModel::onWeightPeriodSelected
                        )
                    },
                    dateRange = data.weightTrend.dateRange,
                    onPrevious = viewModel::onWeightPrevious,
                    onNext = viewModel::onWeightNext
                ) {
                    SimpleBarChart(
                        data = data.weightTrend.values,
                        labels = data.weightTrend.labels,
                        highlightedIndex = data.weightTrend.highlightedIndex,
                        onBarClick = viewModel::onWeightBarClick
                    )
                }
                Spacer(Modifier.height(16.dp))

                // Mood Chart
                ChartContainer(
                    title = {
                        FeatureChartHeader(
                            title = "Mood Trend",
                            selectedPeriod = data.moodTrend.title.substringAfter("(").substringBefore(")"),
                            onPeriodSelected = viewModel::onMoodPeriodSelected
                        )
                    },
                    dateRange = data.moodTrend.dateRange,
                    onPrevious = viewModel::onMoodPrevious,
                    onNext = viewModel::onMoodNext
                ) {
                    SimpleBarChart(
                        data = data.moodTrend.values,
                        labels = data.moodTrend.labels,
                        highlightedIndex = data.moodTrend.highlightedIndex,
                        onBarClick = viewModel::onMoodBarClick
                    )
                }
                Spacer(Modifier.height(16.dp))

                // Activity Chart
                ChartContainer(
                    title = {
                        FeatureChartHeader(
                            title = "Activity",
                            selectedPeriod = data.activityTrend.title.substringAfter("(").substringBefore(")"),
                            onPeriodSelected = viewModel::onActivityPeriodSelected
                        )
                    },
                    dateRange = data.activityTrend.dateRange,
                    onPrevious = viewModel::onActivityPrevious,
                    onNext = viewModel::onActivityNext
                ) {
                    SimpleBarChart(
                        data = data.activityTrend.values,
                        labels = data.activityTrend.labels,
                        highlightedIndex = data.activityTrend.highlightedIndex,
                        onBarClick = viewModel::onActivityBarClick
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightsPreview() {
    // Note: This preview is broken in simple IDE view due to viewModel parameter, 
    // but the actual app will work correctly.
}

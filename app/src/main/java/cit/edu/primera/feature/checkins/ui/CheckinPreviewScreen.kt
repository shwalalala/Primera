
package cit.edu.primera.feature.checkins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.ErrorRed
import cit.edu.primera.core.theme.MenuButtonBorder
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.SurfaceWhite
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.ui.components.FeatureTopBar
import cit.edu.primera.ui.components.PrimeraGradientButton
import java.text.SimpleDateFormat
import java.util.Locale

// OUR ADDITION: Format the selected Daily Check-In date.
private fun formatCheckinReviewDate(date: String): String {
    return runCatching {
        val inputFormat = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).apply {
            isLenient = false
        }

        val parsedDate = inputFormat.parse(date)
            ?: return@runCatching date

        SimpleDateFormat(
            "MMMM d, yyyy",
            Locale.getDefault()
        ).format(parsedDate)
    }.getOrDefault(date)
}

@Composable
fun CheckinPreviewScreen(
    onBack: () -> Unit,
    onNavigateToOverview: () -> Unit,
    viewModel: CheckinsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.dailyState.collectAsStateWithLifecycle()

    LaunchedEffect(state.success) {
        if (state.success) {
            viewModel.resetSuccess()
            onNavigateToOverview()
        }
    }

    CheckinPreviewContent(
        state = state,
        onBack = onBack,
        onConfirm = viewModel::onSaveCheckin,
        modifier = modifier
    )
}

@Composable
fun CheckinPreviewContent(
    state: DailyCheckinUiState,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            FeatureTopBar(
                title = "Review Check-In",
                onBack = onBack
            )

            Spacer(Modifier.height(16.dp))

            // OUR ADDITION:
            // Show the selected date before confirming.
            SummaryItem(
                title = "Check-In Date",
                content = formatCheckinReviewDate(
                    state.selectedCheckinDate
                )
            )

            // TEAMMATES' EXISTING SECTIONS
            SummarySection(
                title = "Symptoms",
                items = state.selectedSymptoms.toList()
            )

            SummarySection(
                title = "Moods",
                items = state.selectedMoods.toList()
            )

            SummarySection(
                title = "Medicine",
                items = state.selectedMedicines.toList()
            )

            if (state.note.isNotBlank()) {
                SummaryItem(
                    title = "Note",
                    content = state.note
                )
            }

            if (state.weightKg.isNotBlank()) {
                SummaryItem(
                    title = "Weight",
                    content = "${state.weightKg} kg"
                )
            }

            // OUR ADDITION:
            // Display blood sugar when entered.
            if (state.bloodSugarMgDl.isNotBlank()) {
                SummaryItem(
                    title = "Blood Sugar",
                    content = "${state.bloodSugarMgDl} mg/dL"
                )
            }

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = ErrorRed,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(
                        horizontal = 24.dp,
                        vertical = 8.dp
                    )
                )
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))

            PrimeraGradientButton(
                text = if (state.isSaving) {
                    "Saving..."
                } else {
                    "Confirm & Save"
                },
                onClick = onConfirm,
                enabled = !state.isSaving,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummarySection(
    title: String,
    items: List<String>
) {
    if (items.isEmpty()) return

    Column(
        modifier = Modifier.padding(
            horizontal = 24.dp,
            vertical = 10.dp
        )
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceWhite)
                        .border(
                            1.dp,
                            MenuButtonBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        )
                ) {
                    Text(
                        text = item,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimeraViolet
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    title: String,
    content: String
) {
    Column(
        modifier = Modifier.padding(
            horizontal = 24.dp,
            vertical = 10.dp
        )
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = content,
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckinPreviewPreview() {
    PrimeraTheme {
        CheckinPreviewContent(
            state = DailyCheckinUiState(
                selectedCheckinDate = "2026-11-10",
                selectedSymptoms = setOf(
                    "Nausea",
                    "Back Pain"
                ),
                selectedMoods = setOf("Happy"),
                note = "Feeling good today!",
                weightKg = "65",
                bloodSugarMgDl = "98"
            ),
            onBack = {},
            onConfirm = {}
        )
    }
}

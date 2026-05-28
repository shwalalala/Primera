package com.example.primera.feature.checkins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.primera.core.theme.*
import com.example.primera.ui.components.PrimeraGradientButton

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
            Spacer(Modifier.statusBarsPadding())
            PreviewTopBar(onBack)

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Review Your Check-In",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(24.dp))

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

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))

            PrimeraGradientButton(
                text = if (state.isSaving) "Saving..." else "Confirm & Save",
                onClick = onConfirm,
                enabled = !state.isSaving,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun PreviewTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceWhite)
        ) {
            Icon(
                Icons.Default.ArrowBackIosNew,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp),
                tint = TextPrimary
            )
        }
    }
}

@Composable
private fun SummarySection(
    title: String,
    items: List<String>
) {
    if (items.isEmpty()) return

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimeraViolet.copy(alpha = 0.1f))
                        .border(1.dp, PrimeraViolet.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = item,
                        fontSize = 14.sp,
                        color = TextPrimary
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
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = content,
            fontSize = 16.sp,
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
                selectedSymptoms = setOf("Nausea", "Back Pain"),
                selectedMoods = setOf("Happy"),
                note = "Feeling good today!",
                weightKg = "65"
            ),
            onBack = {},
            onConfirm = {}
        )
    }
}

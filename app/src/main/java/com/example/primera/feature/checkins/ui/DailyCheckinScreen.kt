package com.example.primera.feature.checkins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
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
import com.example.primera.core.theme.*
import com.example.primera.feature.checkins.ui.CheckinItemChip
import com.example.primera.feature.dashboard.ui.DashboardWeekDayItem
import com.example.primera.feature.dashboard.ui.WeekCalendarRow
import com.example.primera.ui.components.*

@Composable
fun DailyCheckinScreen(
    onBack: () -> Unit,
    onReview: () -> Unit,
    viewModel: CheckinsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.dailyState.collectAsStateWithLifecycle()

    LaunchedEffect(state.success) {
        if (state.success) {
            viewModel.resetSuccess()
            onReview()
        }
    }

    DailyCheckinContent(
        state = state,
        onBack = onBack,
        onClear = viewModel::prepareNewCheckin,
        onSubmit = viewModel::onSaveCheckin,
        onSymptomToggle = viewModel::onSymptomToggle,
        onMoodToggle = viewModel::onMoodToggle,
        onMedicineToggle = viewModel::onMedicineToggle,
        onNoteChange = viewModel::onNoteChange,
        onWeightChange = viewModel::onWeightChange,
        onAddCustomOption = viewModel::onAddCustomOption,
        modifier = modifier
    )
}

@Composable
fun DailyCheckinContent(
    state: DailyCheckinUiState,
    onBack: () -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    onSymptomToggle: (String) -> Unit,
    onMoodToggle: (String) -> Unit,
    onMedicineToggle: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onAddCustomOption: (String, String) -> Unit,
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
            DailyCheckinTopBar(
                onBack = onBack,
                onClear = onClear,
                isEditing = state.editingId != null
            )
            
            WeekCalendarRow(state.weekDays)
            
            Spacer(Modifier.height(16.dp))
            
            CheckinSection(
                title = "Symptoms",
                options = state.availableSymptoms,
                selectedOptions = state.selectedSymptoms,
                onOptionToggle = onSymptomToggle,
                onAddCustom = { label -> onAddCustomOption("Symptom", label) }
            )
            
            Spacer(Modifier.height(24.dp))
            
            CheckinSection(
                title = "Moods",
                options = state.availableMoods,
                selectedOptions = state.selectedMoods,
                onOptionToggle = onMoodToggle,
                onAddCustom = { label -> onAddCustomOption("Mood", label) }
            )
            
            Spacer(Modifier.height(24.dp))
            
            CheckinSection(
                title = "Medicine",
                options = state.availableMedicines,
                selectedOptions = state.selectedMedicines,
                onOptionToggle = onMedicineToggle,
                onAddCustom = { label -> onAddCustomOption("Medicine", label) }
            )
            
            Spacer(Modifier.height(24.dp))
            
            NoteSection(
                note = state.note,
                onNoteChange = onNoteChange
            )
            
            Spacer(Modifier.height(24.dp))
            
            WeightSection(
                weight = state.weightKg,
                lastUpdated = state.lastWeightUpdateDate,
                shouldAlert = state.shouldShowWeightUpdateAlert,
                onWeightChange = onWeightChange
            )
            
            Spacer(Modifier.height(32.dp))
            
            PrimeraGradientButton(
                text = if (state.editingId != null) "Update Check-in" else "Submit Check-in",
                onClick = onSubmit,
                isLoading = state.isSaving,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    color = ErrorRed,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun DailyCheckinTopBar(
    onBack: () -> Unit,
    onClear: () -> Unit,
    isEditing: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack) {
            Text("Cancel", color = TextSecondary)
        }
        
        Text(
            text = if (isEditing) "Edit Check-In" else "Daily Check-In",
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        TextButton(onClick = onClear) {
            Text("Clear", color = PrimeraViolet)
        }
    }
}

@Composable
private fun CheckinSection(
    title: String,
    options: List<CheckinOption>,
    selectedOptions: Set<String>,
    onOptionToggle: (String) -> Unit,
    onAddCustom: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var customLabel by remember { mutableStateOf("") }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            IconButton(onClick = { showAddDialog = true }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add custom $title",
                    modifier = Modifier.size(20.dp),
                    tint = PrimeraViolet
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(options) { option ->
                CheckinItemChip(
                    label = option.label,
                    emoji = option.emoji,
                    isSelected = selectedOptions.contains(option.label),
                    onClick = { onOptionToggle(option.label) }
                )
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Custom $title") },
            text = {
                OutlinedTextField(
                    value = customLabel,
                    onValueChange = { customLabel = it },
                    label = { Text("Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customLabel.isNotBlank()) {
                            onAddCustom(customLabel)
                            customLabel = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun NoteSection(
    note: String,
    onNoteChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceWhite)
                .border(1.dp, DashboardLogBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Note",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = note,
                    onValueChange = onNoteChange,
                    placeholder = { Text("Add your note here...", color = TextHint) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
private fun WeightSection(
    weight: String,
    lastUpdated: String?,
    shouldAlert: Boolean,
    onWeightChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceWhite)
                .border(1.dp, DashboardLogBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weight",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (lastUpdated != null) {
                        Text(
                            text = "Last updated: $lastUpdated",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                if (shouldAlert) {
                    Text(
                        text = "Please update your weight (it's been > 2 weeks)",
                        fontSize = 12.sp,
                        color = ErrorRed,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = weight,
                    onValueChange = onWeightChange,
                    placeholder = { Text("Add your weight here...", color = TextHint) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DailyCheckinPreview() {
    PrimeraTheme {
        DailyCheckinContent(
            state = DailyCheckinUiState(
                editingId = null,
                weekDays = listOf(
                    DashboardWeekDayItem("S", 22, false),
                    DashboardWeekDayItem("M", 23, true),
                    DashboardWeekDayItem("T", 24, false),
                    DashboardWeekDayItem("W", 25, false),
                    DashboardWeekDayItem("T", 26, false),
                    DashboardWeekDayItem("F", 27, false),
                    DashboardWeekDayItem("S", 28, false)
                ),
                selectedSymptoms = setOf("Nausea"),
                lastWeightUpdateDate = "May 14, 2026",
                shouldShowWeightUpdateAlert = true
            ),
            onBack = {},
            onClear = {},
            onSubmit = {},
            onSymptomToggle = {},
            onMoodToggle = {},
            onMedicineToggle = {},
            onNoteChange = {},
            onWeightChange = {},
            onAddCustomOption = { _, _ -> }
        )
    }
}

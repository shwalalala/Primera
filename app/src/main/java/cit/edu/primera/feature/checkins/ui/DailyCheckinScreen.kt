
package cit.edu.primera.feature.checkins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.DashboardLogBorder
import cit.edu.primera.core.theme.ErrorRed
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.SurfaceWhite
import cit.edu.primera.core.theme.TextHint
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.ui.components.PrimeraGradientButton

// ==================================================
// DAILY CHECK-IN SCREEN
// ==================================================

@Composable
fun DailyCheckinScreen(
    onBack: () -> Unit,
    onReview: () -> Unit,
    viewModel: CheckinsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.dailyState.collectAsStateWithLifecycle()

    DailyCheckinContent(
        state = state,
        onBack = onBack,
        onClear = viewModel::prepareNewCheckin,
        onSubmit = onReview,
        onSymptomToggle = viewModel::onSymptomToggle,
        onMoodToggle = viewModel::onMoodToggle,
        onMedicineToggle = viewModel::onMedicineToggle,
        onNoteChange = viewModel::onNoteChange,
        onWeightChange = viewModel::onWeightChange,
        onBloodSugarChange = viewModel::onBloodSugarChange,
        onCheckinDateSelected = viewModel::onCheckinDateSelected,
        onAddCustomOption = viewModel::onAddCustomOption,
        modifier = modifier
    )
}

// ==================================================
// DAILY CHECK-IN CONTENT
// ==================================================

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
    onBloodSugarChange: (String) -> Unit,
    onCheckinDateSelected: (String) -> Unit,
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
                .padding(bottom = 24.dp)
        ) {
            Spacer(Modifier.statusBarsPadding())

            // ----------------------------------
            // COMPACT HEADER
            // ----------------------------------

            DailyCheckinTopBar(
                onBack = onBack,
                onClear = onClear,
                isEditing = state.editingId != null
            )

            // ----------------------------------
            // EXISTING COMPACT CALENDAR
            // ----------------------------------

            SelectableCheckinCalendar(
                selectedDate = state.selectedCheckinDate,
                onDateSelected = onCheckinDateSelected,
                compact = true
            )

            Spacer(Modifier.height(8.dp))

            // ----------------------------------
            // SYMPTOMS
            // ----------------------------------

            CheckinSection(
                title = "Symptoms",
                options = state.availableSymptoms,
                selectedOptions = state.selectedSymptoms,
                onOptionToggle = onSymptomToggle,
                onAddCustom = { label ->
                    onAddCustomOption("Symptom", label)
                }
            )

            Spacer(Modifier.height(12.dp))

            // ----------------------------------
            // MOODS
            // ----------------------------------

            CheckinSection(
                title = "Moods",
                options = state.availableMoods,
                selectedOptions = state.selectedMoods,
                onOptionToggle = onMoodToggle,
                onAddCustom = { label ->
                    onAddCustomOption("Mood", label)
                }
            )

            Spacer(Modifier.height(12.dp))

            // ----------------------------------
            // MEDICINE
            // ----------------------------------

            CheckinSection(
                title = "Medicine",
                options = state.availableMedicines,
                selectedOptions = state.selectedMedicines,
                onOptionToggle = onMedicineToggle,
                onAddCustom = { label ->
                    onAddCustomOption("Medicine", label)
                }
            )

            Spacer(Modifier.height(14.dp))

            // ----------------------------------
            // NOTE
            // ----------------------------------

            NoteSection(
                note = state.note,
                onNoteChange = onNoteChange
            )

            Spacer(Modifier.height(12.dp))

            // ----------------------------------
            // WEIGHT
            // ----------------------------------

            WeightSection(
                weight = state.weightKg,
                lastUpdated = state.lastWeightUpdateDate,
                shouldAlert = state.shouldShowWeightUpdateAlert,
                onWeightChange = onWeightChange
            )

            Spacer(Modifier.height(12.dp))

            // ----------------------------------
            // BLOOD SUGAR
            // ----------------------------------

            BloodSugarSection(
                bloodSugar = state.bloodSugarMgDl,
                onBloodSugarChange = onBloodSugarChange
            )

            Spacer(Modifier.height(20.dp))

            // ----------------------------------
            // EXISTING REVIEW ACTION
            // ----------------------------------

            PrimeraGradientButton(
                text = "Review Check-in",
                onClick = onSubmit,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    color = ErrorRed,
                    modifier = Modifier.padding(
                        horizontal = 20.dp,
                        vertical = 8.dp
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ==================================================
// COMPACT TOP BAR
// ==================================================

@Composable
private fun DailyCheckinTopBar(
    onBack: () -> Unit,
    onClear: () -> Unit,
    isEditing: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 12.dp,
                top = 4.dp,
                bottom = 4.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = PrimeraViolet,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = if (isEditing) {
                "Edit Check-In"
            } else {
                "Daily Check-In"
            },
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        TextButton(
            onClick = onClear,
            contentPadding = PaddingValues(
                horizontal = 8.dp,
                vertical = 4.dp
            )
        ) {
            Text(
                text = "Clear",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = PrimeraViolet
            )
        }
    }
}

// ==================================================
// SYMPTOMS / MOODS / MEDICINE
// ==================================================

@Composable
private fun CheckinSection(
    title: String,
    options: List<CheckinOption>,
    selectedOptions: Set<String>,
    onOptionToggle: (String) -> Unit,
    onAddCustom: (String) -> Unit
) {
    var showAddDialog by remember {
        mutableStateOf(false)
    }

    var customLabel by remember {
        mutableStateOf("")
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            IconButton(
                onClick = {
                    showAddDialog = true
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add custom $title",
                    modifier = Modifier.size(20.dp),
                    tint = PrimeraViolet
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                horizontal = 20.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(options) { option ->
                CheckinItemChip(
                    label = option.label,
                    isSelected = selectedOptions.contains(
                        option.label
                    ),
                    onClick = {
                        onOptionToggle(option.label)
                    }
                )
            }
        }
    }

    // ------------------------------------------
    // EXISTING CUSTOM OPTION DIALOG
    // ------------------------------------------

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
            },
            title = {
                Text("Add Custom $title")
            },
            text = {
                OutlinedTextField(
                    value = customLabel,
                    onValueChange = {
                        customLabel = it
                    },
                    label = {
                        Text("Label")
                    },
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
                    Text(
                        text = "Add",
                        color = PrimeraViolet
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                    }
                ) {
                    Text(
                        text = "Cancel",
                        color = TextSecondary
                    )
                }
            }
        )
    }
}

// ==================================================
// REUSABLE COMPACT INPUT CARD
// ==================================================

@Composable
private fun CompactCheckinCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .border(
                width = 1.dp,
                color = DashboardLogBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(
                horizontal = 14.dp,
                vertical = 10.dp
            ),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        content()
    }
}

// ==================================================
// NOTE
// ==================================================

@Composable
private fun NoteSection(
    note: String,
    onNoteChange: (String) -> Unit
) {
    CompactCheckinCard(
        title = "Note",
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        TextField(
            value = note,
            onValueChange = onNoteChange,
            placeholder = {
                Text(
                    text = "Add your note here...",
                    fontSize = 14.sp,
                    color = TextHint
                )
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                color = TextPrimary
            ),
            minLines = 1,
            maxLines = 4,
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

// ==================================================
// WEIGHT
// ==================================================

@Composable
private fun WeightSection(
    weight: String,
    lastUpdated: String?,
    shouldAlert: Boolean,
    onWeightChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .border(
                width = 1.dp,
                color = DashboardLogBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(
                horizontal = 14.dp,
                vertical = 10.dp
            ),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Weight",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(Modifier.weight(1f))

            if (lastUpdated != null) {
                Text(
                    text = "Updated: $lastUpdated",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (shouldAlert) {
            Text(
                text = "Please update your weight (it's been > 2 weeks)",
                fontSize = 11.sp,
                color = ErrorRed
            )
        }

        TextField(
            value = weight,
            onValueChange = onWeightChange,
            placeholder = {
                Text(
                    text = "Add your weight here...",
                    fontSize = 14.sp,
                    color = TextHint
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                color = TextPrimary
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

// ==================================================
// BLOOD SUGAR
// ==================================================

@Composable
private fun BloodSugarSection(
    bloodSugar: String,
    onBloodSugarChange: (String) -> Unit
) {
    CompactCheckinCard(
        title = "Blood Sugar",
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        TextField(
            value = bloodSugar,
            onValueChange = onBloodSugarChange,
            placeholder = {
                Text(
                    text = "Add blood sugar (mg/dL)...",
                    fontSize = 14.sp,
                    color = TextHint
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                color = TextPrimary
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

// ==================================================
// COMPOSE PREVIEW
// ==================================================

@Preview(showBackground = true)
@Composable
private fun DailyCheckinPreview() {
    PrimeraTheme {
        DailyCheckinContent(
            state = DailyCheckinUiState(
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
            onBloodSugarChange = {},
            onCheckinDateSelected = {},
            onAddCustomOption = { _, _ -> }
        )
    }
}

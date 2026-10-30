package cit.edu.primera.feature.goals.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.toColorInt
import cit.edu.primera.core.theme.InputBorder
import cit.edu.primera.core.theme.NunitoSans
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.SurfaceWhite
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.feature.goals.data.GoalDto
import cit.edu.primera.ui.components.GoalIcon
import cit.edu.primera.ui.components.PrimeraGradientButton
import cit.edu.primera.ui.components.PrimeraOutlinedButton
import java.util.Date
import java.util.Locale
import kotlin.math.max

private data class GoalOption(
    val title: String,
    val unit: String,
    val defaultTarget: Double,
    val icon: String,
    val colorHex: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    editingGoal: GoalDto? = null,
    initialPeriod: String = "Daily",
    onConfirm: (GoalDto) -> Unit
) {
    val goalOptions = remember {
        listOf(
            GoalOption("Hydration", "L", 2.5, "R.drawable.water", "#93C5FD"),
            GoalOption("Movement", "min", 30.0, "R.drawable.steps", "#AED581"),
            GoalOption("Yoga", "min", 20.0, "🧘", "#A1D386"),
            GoalOption("Walking", "steps", 6000.0, "R.drawable.steps", "#FFAB91"),
            GoalOption("Pelvic Floor Exercise", "reps", 15.0, "R.drawable.heart", "#CE93D8"),
            GoalOption("Prenatal Vitamins", "count", 1.0, "R.drawable.log", "#FFF176"),
            GoalOption("Meditation", "min", 15.0, "🧘", "#81C784"),
            GoalOption("Fruits & Veggies", "servings", 5.0, "R.drawable.apple", "#FF8A65"),
            GoalOption("Journaling", "pages", 1.0, "R.drawable.log", "#BA68C8"),
            GoalOption("Stretching", "min", 15.0, "🧘", "#4DB6AC"),
            GoalOption("Nap / Rest Time", "hours", 1.0, "R.drawable.sleep", "#9575CD"),
            GoalOption("Reading", "pages", 10.0, "R.drawable.log", "#F06292"),
            GoalOption("Deep Breathing", "min", 10.0, "R.drawable.heart", "#4DD0E1"),
            GoalOption("Foot Care", "min", 15.0, "R.drawable.log", "#FFD54F"),
            GoalOption("Belly Massage", "min", 10.0, "R.drawable.heart", "#F48FB1"),
            GoalOption("Custom Goal", "times", 1.0, "🎯", "#64B5F6")
        )
    }

    val availableColors = listOf(
        "#64B5F6", "#A1D386", "#FFAB91", "#CE93D8", 
        "#FFF176", "#81C784", "#FF8A65", "#BA68C8",
        "#4DB6AC", "#9575CD", "#F06292", "#4DD0E1",
        "#FFD54F", "#F48FB1", "#93C5FD"
    )

    var goalTitle by remember {
        mutableStateOf(editingGoal?.title ?: "Hydration")
    }

    var goalUnit by remember {
        mutableStateOf(editingGoal?.unit ?: "L")
    }

    var iconString by remember {
        mutableStateOf(editingGoal?.icon ?: "R.drawable.water")
    }

    var selectedPeriod by remember {
        mutableStateOf(editingGoal?.period ?: initialPeriod)
    }

    var targetValueText by remember {
        val initial = editingGoal?.targetValue
        mutableStateOf(
            if (initial != null) {
                if (initial % 1.0 == 0.0) initial.toInt().toString() else initial.toString()
            } else "2.5"
        )
    }

    var currentValueText by remember {
        val initial = editingGoal?.currentValue
        mutableStateOf(
            if (initial != null) {
                if (initial % 1.0 == 0.0) initial.toInt().toString() else initial.toString()
            } else "0"
        )
    }
    
    var selectedColorHex by remember {
        mutableStateOf(editingGoal?.accentColorHex ?: "#93C5FD") 
    }

    var presetDropdownExpanded by remember { mutableStateOf(false) }
    var periodDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = SurfaceWhite,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .heightIn(max = 580.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = if (editingGoal == null) "Add New Goal" else "Edit Goal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoSans,
                    color = PrimeraViolet
                )

                // Goal Preset Dropdown / Title Input
                ExposedDropdownMenuBox(
                    expanded = presetDropdownExpanded,
                    onExpandedChange = { presetDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        label = { Text("Goal Title", fontFamily = NunitoSans) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = presetDropdownExpanded) },
                        leadingIcon = {
                            GoalIcon(
                                icon = iconString,
                                tint = try {
                                    Color(selectedColorHex.toColorInt())
                                } catch (_: Exception) {
                                    PrimeraViolet
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                ExposedDropdownMenuAnchorType.PrimaryEditable,
                                enabled = true
                            ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimeraViolet,
                            unfocusedBorderColor = InputBorder,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = presetDropdownExpanded,
                        onDismissRequest = { presetDropdownExpanded = false },
                        modifier = Modifier
                            .background(SurfaceWhite)
                            .border(1.dp, InputBorder, RoundedCornerShape(12.dp))
                    ) {
                        goalOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        GoalIcon(
                                            icon = option.icon,
                                            tint = try {
                                                Color(option.colorHex.toColorInt())
                                            } catch (_: Exception) {
                                                PrimeraViolet
                                            },
                                            modifier = Modifier
                                                .padding(end = 12.dp)
                                                .size(20.dp)
                                        )
                                        Text(
                                            text = option.title,
                                            fontFamily = NunitoSans,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                },
                                onClick = {
                                    goalTitle = option.title
                                    goalUnit = option.unit
                                    iconString = option.icon
                                    selectedColorHex = option.colorHex
                                    targetValueText =
                                        if (option.defaultTarget % 1.0 == 0.0) option.defaultTarget.toInt()
                                            .toString() else option.defaultTarget.toString()
                                    presetDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Period Selection & Unit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Period dropdown
                    ExposedDropdownMenuBox(
                        expanded = periodDropdownExpanded,
                        onExpandedChange = { periodDropdownExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedPeriod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Period", fontFamily = NunitoSans) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodDropdownExpanded) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(
                                    ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                    enabled = true
                                ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimeraViolet,
                                unfocusedBorderColor = InputBorder
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = periodDropdownExpanded,
                            onDismissRequest = { periodDropdownExpanded = false },
                            modifier = Modifier.background(SurfaceWhite)
                        ) {
                            listOf("Daily", "Weekly", "Monthly").forEach { period ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            period,
                                            fontFamily = NunitoSans,
                                            color = TextPrimary
                                        )
                                    },
                                    onClick = {
                                        selectedPeriod = period
                                        periodDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Unit text field
                    OutlinedTextField(
                        value = goalUnit,
                        onValueChange = { goalUnit = it },
                        label = { Text("Unit", fontFamily = NunitoSans) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimeraViolet,
                            unfocusedBorderColor = InputBorder
                        )
                    )
                }

                // Target Input
                OutlinedTextField(
                    value = targetValueText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() || char == '.' }) targetValueText = it
                    },
                    label = { Text("Target Goal", fontFamily = NunitoSans) },
                    modifier = Modifier.fillMaxWidth(),
                    suffix = { Text(goalUnit, fontFamily = NunitoSans, color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimeraViolet,
                        unfocusedBorderColor = InputBorder
                    )
                )

                // Accomplished Slider
                val targetFloat = targetValueText.toFloatOrNull() ?: 1f
                val currentFloat =
                    (currentValueText.toFloatOrNull() ?: 0f).coerceIn(0f, max(1f, targetFloat))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Current Progress",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = NunitoSans,
                            color = TextSecondary
                        )
                        Text(
                            text = "$currentValueText $goalUnit",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = NunitoSans,
                            color = PrimeraViolet
                        )
                    }
                    Slider(
                        value = currentFloat,
                        onValueChange = { valVal ->
                            currentValueText =
                                if (valVal % 1f == 0f) valVal.toInt().toString() else String.format(
                                    Locale.US,
                                    "%.1f",
                                    valVal
                                )
                        },
                        valueRange = 0f..max(1f, targetFloat),
                        colors = SliderDefaults.colors(
                            thumbColor = PrimeraViolet,
                            activeTrackColor = PrimeraViolet,
                            inactiveTrackColor = PrimeraViolet.copy(alpha = 0.2f)
                        )
                    )
                }

                // Accent Color Selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Accent Color",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = NunitoSans,
                        color = TextSecondary
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(availableColors) { hex ->
                            val color = try {
                                Color(hex.toColorInt())
                            } catch (_: Exception) {
                                PrimeraViolet
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selectedColorHex.equals(
                                                hex,
                                                ignoreCase = true
                                            )
                                        ) 3.dp else 0.dp,
                                        color = if (selectedColorHex.equals(
                                                hex,
                                                ignoreCase = true
                                            )
                                        ) PrimeraViolet else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = hex }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                val isValid =
                    goalTitle.isNotBlank() && (targetValueText.toDoubleOrNull() ?: 0.0) > 0.0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrimeraOutlinedButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    PrimeraGradientButton(
                        text = if (editingGoal == null) "Add Goal" else "Save Changes",
                        onClick = {
                            val target = targetValueText.toDoubleOrNull() ?: 0.0
                            val current = currentValueText.toDoubleOrNull() ?: 0.0
                            if (target > 0) {
                                onConfirm(
                                    GoalDto(
                                        id = editingGoal?.id,
                                        userId = editingGoal?.userId,
                                        title = goalTitle.trim(),
                                        targetValue = target,
                                        unit = goalUnit.ifBlank { "unit" },
                                        icon = iconString,
                                        category = editingGoal?.category ?: "Wellness",
                                        period = selectedPeriod,
                                        accentColorHex = selectedColorHex,
                                        currentValue = current,
                                        timestamp = editingGoal?.timestamp ?: Date()
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1.2f),
                        enabled = isValid
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Add Goal Dialog",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
private fun AddGoalDialogPreview() {
    PrimeraTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            AddGoalDialog(
                onDismiss = {},
                onConfirm = {}
            )
        }
    }
}

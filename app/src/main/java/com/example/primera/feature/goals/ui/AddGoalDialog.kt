package com.example.primera.feature.goals.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.primera.core.theme.NunitoSans
import com.example.primera.core.theme.PrimeraViolet
import com.example.primera.core.theme.SurfaceWhite
import com.example.primera.feature.goals.data.GoalDto
import com.example.primera.ui.components.GoalIcon
import java.util.Date

private data class GoalOption(
    val title: String,
    val unit: String,
    val icon: String,
    val colorHex: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    editingGoal: GoalDto? = null,
    onConfirm: (GoalDto) -> Unit
) {
    val goalOptions = remember {
        listOf(
            GoalOption("Hydration", "ml", "R.drawable.water", "#93C5FD"),
            GoalOption("Yoga", "min", "R.drawable.yoga", "#A1D386"),
            GoalOption("Walking", "steps", "R.drawable.steps", "#FFAB91"),
            GoalOption("Pelvic Floor Exercise", "reps", "R.drawable.heart", "#CE93D8"),
            GoalOption("Vitamins", "count", "R.drawable.log", "#FFF176"),
            GoalOption("Meditation", "min", "R.drawable.yoga", "#81C784"),
            GoalOption("Fruits/Veg", "servings", "R.drawable.apple", "#FF8A65"),
            GoalOption("Journaling", "pages", "R.drawable.log", "#BA68C8"),
            GoalOption("Stretching", "min", "R.drawable.yoga", "#4DB6AC"),
            GoalOption("Nap Time", "hours", "R.drawable.sleep", "#9575CD"),
            GoalOption("Reading", "pages", "R.drawable.log", "#F06292"),
            GoalOption("Deep Breathing", "min", "R.drawable.heart", "#4DD0E1"),
            GoalOption("Foot Care", "min", "R.drawable.log", "#FFD54F"),
            GoalOption("Belly Massage", "min", "R.drawable.heart", "#F48FB1")
        )
    }

    val availableColors = listOf(
        "#A1D386", "#FFAB91", "#64B5F6", "#CE93D8", 
        "#FFF176", "#81C784", "#FF8A65", "#BA68C8",
        "#4DB6AC", "#9575CD", "#F06292", "#4DD0E1",
        "#FFD54F", "#F48FB1", "#93C5FD"
    )

    var selectedOption by remember { 
        mutableStateOf(
            goalOptions.find { it.title == editingGoal?.title } ?: goalOptions[0]
        ) 
    }
    
    var targetValue by remember { 
        mutableStateOf(editingGoal?.targetValue?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") 
    }

    var accomplishedValue by remember {
        mutableStateOf(editingGoal?.currentValue?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "0")
    }
    
    var selectedColorHex by remember { 
        mutableStateOf(editingGoal?.accentColorHex ?: selectedOption.colorHex) 
    }
    
    var titleExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceWhite,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = if (editingGoal == null) "Add New Goal" else "Edit Goal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoSans,
                    color = PrimeraViolet
                )

                // Dropdown for Goal Title
                ExposedDropdownMenuBox(
                    expanded = titleExpanded,
                    onExpandedChange = { if (editingGoal == null) titleExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedOption.title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Goal", fontFamily = NunitoSans) },
                        trailingIcon = { 
                            if (editingGoal == null) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = titleExpanded) 
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimeraViolet,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        leadingIcon = {
                            GoalIcon(
                                icon = selectedOption.icon,
                                tint = Color(selectedColorHex.toColorInt()),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )

                    if (editingGoal == null) {
                        ExposedDropdownMenu(
                            expanded = titleExpanded,
                            onDismissRequest = { titleExpanded = false },
                            modifier = Modifier
                                .background(SurfaceWhite)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        ) {
                            goalOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            GoalIcon(
                                                icon = option.icon,
                                                tint = Color(option.colorHex.toColorInt()),
                                                modifier = Modifier.padding(end = 8.dp).size(20.dp)
                                            )
                                            Text(
                                                text = option.title, 
                                                fontFamily = NunitoSans,
                                                color = Color.Black
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedOption = option
                                        selectedColorHex = option.colorHex
                                        titleExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Target Input
                OutlinedTextField(
                    value = targetValue,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) targetValue = it },
                    label = { Text("Target Goal", fontFamily = NunitoSans) },
                    modifier = Modifier.fillMaxWidth(),
                    suffix = { Text(selectedOption.unit, fontFamily = NunitoSans) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimeraViolet,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                // Accomplished Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Accomplished",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = NunitoSans,
                            color = Color.Gray
                        )
                        Text(
                            text = "$accomplishedValue ${selectedOption.unit}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = NunitoSans,
                            color = PrimeraViolet
                        )
                    }
                    Slider(
                        value = accomplishedValue.toFloatOrNull() ?: 0f,
                        onValueChange = { 
                            accomplishedValue = if (it % 1f == 0f) it.toInt().toString() else "%.1f".format(it)
                        },
                        valueRange = 0f..(targetValue.toFloatOrNull() ?: 100f).coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = PrimeraViolet,
                            activeTrackColor = PrimeraViolet,
                            inactiveTrackColor = PrimeraViolet.copy(alpha = 0.24f)
                        )
                    )
                }

                // Color Picker
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Accent Color",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = NunitoSans,
                        color = Color.Gray
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(availableColors) { hex ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(hex.toColorInt()))
                                    .border(
                                        width = if (selectedColorHex == hex) 3.dp else 0.dp,
                                        color = if (selectedColorHex == hex) PrimeraViolet else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = hex }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        val target = targetValue.toDoubleOrNull() ?: 0.0
                        val current = accomplishedValue.toDoubleOrNull() ?: 0.0
                        if (target > 0) {
                            onConfirm(
                                GoalDto(
                                    id = editingGoal?.id,
                                    userId = editingGoal?.userId,
                                    title = selectedOption.title,
                                    targetValue = target,
                                    unit = selectedOption.unit,
                                    icon = selectedOption.icon,
                                    category = editingGoal?.category ?: "Wellness",
                                    period = editingGoal?.period ?: "Daily",
                                    accentColorHex = selectedColorHex,
                                    currentValue = current,
                                    timestamp = editingGoal?.timestamp ?: Date()
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimeraViolet
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = targetValue.isNotBlank()
                ) {
                    Text(
                        text = if (editingGoal == null) "Add Goal" else "Save Changes",
                        color = Color.White,
                        fontFamily = NunitoSans,
                        fontWeight = FontWeight.SemiBold
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
package com.example.primera.feature.goals.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.primera.core.theme.NunitoSans
import com.example.primera.core.theme.PrimeraViolet
import com.example.primera.core.theme.SurfaceWhite
import com.example.primera.feature.goals.data.GoalDto
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (GoalDto) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetValue by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var unitExpanded by remember { mutableStateOf(false) }

    // Define available units
    val units = listOf("km", "miles", "steps", "minutes", "hours", "sessions", "kg", "lbs", "reps")

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
                    text = "Add New Goal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoSans,
                    color = PrimeraViolet
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(
                            "Goal Title",
                            fontFamily = NunitoSans
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = targetValue,
                        onValueChange = { targetValue = it },
                        label = {
                            Text(
                                "Target",
                                fontFamily = NunitoSans
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Dropdown for Unit
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = {
                                Text(
                                    "Unit",
                                    fontFamily = NunitoSans
                                )
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            units.forEach { unitOption ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            unitOption,
                                            fontFamily = NunitoSans
                                        )
                                    },
                                    onClick = {
                                        unit = unitOption
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        val target = targetValue.toDoubleOrNull() ?: 0.0

                        if (title.isNotBlank() && target > 0 && unit.isNotBlank()) {

                            onConfirm(
                                GoalDto(
                                    title = title,
                                    targetValue = target,
                                    unit = unit,
                                    icon = "",
                                    category = "Wellness",
                                    period = "Weekly",
                                    accentColorHex = "#9C62C0",
                                    timestamp = Date()
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimeraViolet
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Add Goal",
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
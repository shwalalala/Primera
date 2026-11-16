package cit.edu.primera.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.primera.core.theme.ErrorRed
import cit.edu.primera.core.theme.InputBorder
import cit.edu.primera.core.theme.NunitoSans
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.SurfaceWhite
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.feature.smartwatchconnection.domain.SmartwatchHealth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class HealthAttribute(
    val title: String,
    val unit: String,
    val placeholder: String
) {
    HEART_RATE("Heart Rate", "BPM", "e.g., 72"),
    STEPS("Daily Steps", "steps", "e.g., 8000"),
    SLEEP("Sleep Duration", "", "7"),
    SPO2("Blood Oxygen (SpO₂)", "%", "e.g., 98")
}

@Composable
fun DatePickerField(
    label: String,
    dateText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = NunitoSans),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            OutlinedTextField(
                value = dateText,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                singleLine = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        tint = PrimeraViolet,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledContainerColor = SurfaceWhite,
                    disabledBorderColor = InputBorder,
                    disabledTextColor = TextPrimary,
                    disabledTrailingIconColor = PrimeraViolet
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
            )
        }
    }
}

@Composable
fun SingleAttributeEntryDialog(
    attribute: HealthAttribute,
    currentValue: String = "",
    errorMessage: String? = null,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (date: Date, time: String, value: String, extraValue: String?) -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    val currentDate = remember { calendar.time }
    val currentTimeStr =
        remember { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(currentDate) }

    var valueInput by remember { mutableStateOf(currentValue) }
    var extraInput by remember { mutableStateOf("") }

    val dateTimeFormatted = remember {
        SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(currentDate)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Record ${attribute.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = "$dateTimeFormatted (Auto-recorded)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = ErrorRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                if (attribute == HealthAttribute.SLEEP) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LabeledField(
                            label = "Sleep Hours",
                            value = valueInput,
                            onValueChange = { valueInput = it },
                            placeholder = "7",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                        LabeledField(
                            label = "Sleep Mins",
                            value = extraInput,
                            onValueChange = { extraInput = it },
                            placeholder = "30",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    LabeledField(
                        label = "${attribute.title} ${if (attribute.unit.isNotBlank()) "(${attribute.unit})" else ""}",
                        value = valueInput,
                        onValueChange = { valueInput = it },
                        placeholder = attribute.placeholder,
                        keyboardType = KeyboardType.Number
                    )
                }

                Text(
                    text = "Saved as self-reported manual data with current timestamp.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            PrimeraGradientButton(
                text = "Save",
                isLoading = isLoading,
                onClick = {
                    onSave(
                        currentDate,
                        currentTimeStr,
                        valueInput,
                        extraInput.takeIf { attribute == HealthAttribute.SLEEP })
                }
            )
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel", color = TextPrimary)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InlineManualEntryForm(
    initialHealth: SmartwatchHealth? = null,
    errorMessage: String? = null,
    isLoading: Boolean = false,
    onSave: (date: Date, time: String, heartRate: Long?, spO2: Double?, steps: Long, sleepHours: Long, sleepMinutes: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val calendar = remember { Calendar.getInstance() }
    var selectedDate by remember { mutableStateOf(calendar.time) }
    val defaultTimeStr =
        remember { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time) }
    var selectedTimeStr by remember {
        mutableStateOf(initialHealth?.time?.takeIf { it.isNotBlank() } ?: defaultTimeStr)
    }

    var heartRate by remember { mutableStateOf(initialHealth?.currentHeartRate?.toString() ?: "") }
    var spO2 by remember { mutableStateOf(initialHealth?.spO2?.toInt()?.toString() ?: "") }
    var steps by remember {
        mutableStateOf(
            initialHealth?.steps?.takeIf { it > 0 }?.toString() ?: ""
        )
    }
    var sleepHours by remember {
        mutableStateOf((initialHealth?.sleepMinutes?.div(60))?.takeIf { it > 0 }?.toString() ?: "")
    }
    var sleepMinutes by remember {
        mutableStateOf((initialHealth?.sleepMinutes?.rem(60))?.takeIf { it > 0 }?.toString() ?: "")
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceWhite,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, InputBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimeraLilac.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = PrimeraViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        "Manual Health Data Entry",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "Self-reported measurements for health history",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        errorMessage,
                        color = ErrorRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DatePickerField(
                    label = "Date",
                    dateText = dateFormat.format(selectedDate),
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1.2f)
                )

                Column(modifier = Modifier.weight(0.8f)) {
                    LabeledField(
                        label = "Time",
                        value = selectedTimeStr,
                        onValueChange = { selectedTimeStr = it },
                        placeholder = "08:30 AM"
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    LabeledField(
                        label = "Heart Rate (BPM)",
                        value = heartRate,
                        onValueChange = { heartRate = it },
                        placeholder = "72",
                        keyboardType = KeyboardType.Number
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    LabeledField(
                        label = "SpO₂ (%)",
                        value = spO2,
                        onValueChange = { spO2 = it },
                        placeholder = "98",
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    LabeledField(
                        label = "Daily Steps",
                        value = steps,
                        onValueChange = { steps = it },
                        placeholder = "8000",
                        keyboardType = KeyboardType.Number
                    )
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        LabeledField(
                            label = "Sleep (h)",
                            value = sleepHours,
                            onValueChange = { sleepHours = it },
                            placeholder = "7",
                            keyboardType = KeyboardType.Number
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        LabeledField(
                            label = "Sleep (m)",
                            value = sleepMinutes,
                            onValueChange = { sleepMinutes = it },
                            placeholder = "30",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }

            PrimeraGradientButton(
                text = "Save Manual Entry",
                isLoading = isLoading,
                onClick = {
                    val hrVal = heartRate.trim().takeIf { it.isNotBlank() }?.toLongOrNull()
                    val spO2Val = spO2.trim().takeIf { it.isNotBlank() }?.toDoubleOrNull()
                    val stepsVal = steps.trim().takeIf { it.isNotBlank() }?.toLongOrNull() ?: 0L
                    val sHoursVal =
                        sleepHours.trim().takeIf { it.isNotBlank() }?.toLongOrNull() ?: 0L
                    val sMinsVal =
                        sleepMinutes.trim().takeIf { it.isNotBlank() }?.toLongOrNull() ?: 0L
                    onSave(
                        selectedDate,
                        selectedTimeStr,
                        hrVal,
                        spO2Val,
                        stepsVal,
                        sHoursVal,
                        sMinsVal
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.time)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = Date(it) }
                    showDatePicker = false
                }) { Text("OK", color = PrimeraViolet, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(
                        "Cancel",
                        color = TextSecondary
                    )
                }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun ManualHealthEntryDialog(
    initialHealth: SmartwatchHealth? = null,
    errorMessage: String? = null,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (date: Date, time: String, heartRate: Long?, spO2: Double?, steps: Long, sleepHours: Long, sleepMinutes: Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        text = {
            InlineManualEntryForm(
                initialHealth = initialHealth,
                errorMessage = errorMessage,
                isLoading = isLoading,
                onSave = { date, time, hr, spO2, steps, sleepH, sleepM ->
                    onSave(date, time, hr, spO2, steps, sleepH, sleepM)
                }
            )
        }
    )
}


package cit.edu.primera.feature.checkins.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cit.edu.primera.R
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.DashboardLogBorder
import cit.edu.primera.core.theme.ErrorRed
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.SurfaceWhite
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.feature.checkins.data.PrenatalRecordDto
import cit.edu.primera.ui.components.PrimeraGradientButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ==================================================
// PRENATAL FORM MODE
// ==================================================

private enum class PrenatalFormMode {
    APPOINTMENT,
    RESULTS
}

// ==================================================
// DATE HELPERS
// ==================================================

private fun prenatalToday(): String {
    return SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    ).format(Date())
}

private fun prenatalDisplayDate(date: String): String {
    return runCatching {
        val parsed = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).apply {
            isLenient = false
        }.parse(date)

        SimpleDateFormat(
            "MMMM d, yyyy",
            Locale.getDefault()
        ).format(parsed ?: Date())
    }.getOrDefault(date)
}

// ==================================================
// PRENATAL CHECKUP SCREEN
// ==================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrenatalCheckupScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PrenatalViewModel = viewModel(),
    selectedRecordId: String? = null
) {
    val state by viewModel.uiState
        .collectAsStateWithLifecycle()

    val today = prenatalToday()

    var selectedDate by remember {
        mutableStateOf(today)
    }

    var formMode by remember {
        mutableStateOf<PrenatalFormMode?>(null)
    }

    var editingRecord by remember {
        mutableStateOf<PrenatalRecordDto?>(null)
    }

    var viewingRecordId by remember {
        mutableStateOf<String?>(null)
    }

    var recordPendingCancellation by remember {
        mutableStateOf<PrenatalRecordDto?>(null)
    }

    var openedLinkedRecord by remember(selectedRecordId) {
        mutableStateOf(false)
    }

    var awaitingSave by remember {
        mutableStateOf(false)
    }

    var formError by remember {
        mutableStateOf<String?>(null)
    }

    val fields = remember {
        mutableStateMapOf<String, String>()
    }

    // ------------------------------------------
    // OPEN LINKED PRENATAL RECORD
    // ------------------------------------------

    LaunchedEffect(
        selectedRecordId,
        state.records,
        state.isLoading
    ) {
        if (
            !selectedRecordId.isNullOrBlank() &&
            !openedLinkedRecord
        ) {
            val matchingRecord = state.records.firstOrNull {
                it.id == selectedRecordId
            }

            if (matchingRecord != null) {
                selectedDate = matchingRecord.visitDate
                viewingRecordId = matchingRecord.id
                openedLinkedRecord = true
            }
        }
    }

    // ------------------------------------------
    // FORM ACTIONS
    // ------------------------------------------

    fun openForm(
        mode: PrenatalFormMode,
        record: PrenatalRecordDto? = null
    ) {
        viewModel.clearMessages()
        fields.clear()

        if (record != null) {
            fields.putAll(record.details)
            selectedDate = record.visitDate
        }

        viewingRecordId = null
        editingRecord = record
        formError = null
        awaitingSave = false
        formMode = mode
    }

    fun closeForm() {
        if (!state.isSaving) {
            formMode = null
            editingRecord = null
            formError = null
            awaitingSave = false
        }
    }

    // ------------------------------------------
    // FIREBASE SAVE RESULT
    // ------------------------------------------

    LaunchedEffect(
        state.successMessage,
        state.errorMessage,
        awaitingSave
    ) {
        if (awaitingSave) {
            when {
                state.successMessage != null -> {
                    formMode = null
                    editingRecord = null
                    fields.clear()
                    formError = null
                    awaitingSave = false
                }

                state.errorMessage != null -> {
                    awaitingSave = false
                }
            }
        }
    }

    // ------------------------------------------
    // CALENDAR DATA
    // ------------------------------------------

    val scheduledDates = state.records
        .filter { it.status == "scheduled" }
        .map { it.visitDate }
        .toSet()

    val completedDates = state.records
        .filter { it.status == "completed" }
        .map { it.visitDate }
        .toSet()

    val selectedEntries = state.records
        .filter {
            it.visitDate == selectedDate &&
                    it.status != "cancelled"
        }
        .sortedWith(
            compareBy<PrenatalRecordDto> {
                it.status != "scheduled"
            }.thenBy {
                it.details["time"].orEmpty()
            }
        )

    val viewingRecord = state.records.firstOrNull {
        it.id == viewingRecordId
    }

    // ==========================================
    // MAIN SCREEN
    // ==========================================

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
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

            // ==================================
            // COMPACT TOP BAR
            // ==================================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 4.dp
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
                    text = "Prenatal Checkup",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 40.dp),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ==================================
            // EXISTING SHARED CALENDAR
            // ==================================

            SelectableCheckinCalendar(
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    selectedDate = date
                    viewingRecordId = null
                    viewModel.clearMessages()
                },
                allowFutureDates = true,
                scheduledDates = scheduledDates,
                completedDates = completedDates
            )

            Spacer(Modifier.height(16.dp))

            // ----------------------------------
            // LOADING INDICATOR
            // ----------------------------------

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PrimeraViolet
                    )
                }
            }

            // ----------------------------------
            // FIREBASE FEEDBACK
            // ----------------------------------

            if (
                state.successMessage != null &&
                formMode == null
            ) {
                Text(
                    text = state.successMessage.orEmpty(),
                    color = Color(0xFF43866F),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(
                        horizontal = 24.dp,
                        vertical = 8.dp
                    )
                )
            }

            if (
                state.errorMessage != null &&
                formMode == null
            ) {
                Text(
                    text = state.errorMessage.orEmpty(),
                    color = ErrorRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(
                        horizontal = 24.dp,
                        vertical = 8.dp
                    )
                )
            }

            // ==================================
            // VISITS AND APPOINTMENTS
            // ==================================

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Visits & Appointments",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = prenatalDisplayDate(selectedDate),
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                if (!state.isLoading) {
                    if (selectedEntries.isEmpty()) {
                        PrenatalCard {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = PrimeraViolet
                            )

                            Text(
                                text = "No prenatal entries",
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )

                            Text(
                                text = "Choose a date to schedule an " +
                                        "appointment or record results " +
                                        "from a prenatal visit.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    } else {
                        selectedEntries.forEach { record ->
                            PrenatalEntryCard(
                                record = record,
                                onView = {
                                    viewingRecordId = record.id
                                },
                                onEdit = {
                                    openForm(
                                        mode = if (
                                            record.status == "completed"
                                        ) {
                                            PrenatalFormMode.RESULTS
                                        } else {
                                            PrenatalFormMode.APPOINTMENT
                                        },
                                        record = record
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ==================================
            // ADD PRENATAL ENTRY
            // ==================================

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Prenatal Entry",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                if (selectedDate >= today) {
                    PrimeraGradientButton(
                        text = "Schedule Appointment",
                        onClick = {
                            openForm(PrenatalFormMode.APPOINTMENT)
                        }
                    )
                }

                if (selectedDate <= today) {
                    Button(
                        onClick = {
                            openForm(PrenatalFormMode.RESULTS)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceWhite,
                            contentColor = TextPrimary
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            PrimeraViolet
                        )
                    ) {
                        Text(
                            text = "Record Prenatal Results",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }

                Text(
                    text = "Appointments can be scheduled for " +
                            "today or future dates. Completed results " +
                            "can be recorded for today or past visits.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }

    // ==========================================
    // VIEW SPECIFIC RECORD BOTTOM SHEET
    // ==========================================

    if (viewingRecord != null && formMode == null) {
        val detailSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        ModalBottomSheet(
            onDismissRequest = {
                viewingRecordId = null
            },
            sheetState = detailSheetState,
            containerColor = BackgroundCream
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 32.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (
                        viewingRecord.status == "completed"
                    ) {
                        "Completed Prenatal Visit"
                    } else if (
                        viewingRecord.status == "scheduled"
                    ) {
                        "Scheduled Appointment"
                    } else {
                        "Cancelled Appointment"
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = prenatalDisplayDate(
                        viewingRecord.visitDate
                    ),
                    color = PrimeraViolet,
                    fontWeight = FontWeight.SemiBold
                )

                PrenatalCard {
                    PrenatalFormTitle("Record Details")

                    val labels = listOf(
                        "time" to "Appointment Time",
                        "clinic" to "Clinic / Hospital",
                        "provider" to "Healthcare Provider",
                        "aogWeeks" to "AOG - Weeks",
                        "aogDays" to "AOG - Additional Days",
                        "weight" to "Weight (kg)",
                        "bloodPressure" to "Blood Pressure (mmHg)",
                        "fundalHeight" to "Fundal Height (cm)",
                        "fetalHeartRate" to "Fetal Heart Rate (bpm)",
                        "presentation" to "Fetal Presentation",
                        "efw" to "Estimated Fetal Weight (g)",
                        "ieFindings" to "IE Findings",
                        "medicines" to "Medicines & Supplements",
                        "instructions" to "Doctor's Instructions",
                        "nextVisit" to "Next Visit Date",
                        "notes" to "Appointment Notes"
                    )

                    var hasDetails = false

                    labels.forEach { (key, label) ->
                        val value = viewingRecord.details[key]
                            .orEmpty()

                        if (value.isNotBlank()) {
                            hasDetails = true

                            Column(
                                verticalArrangement =
                                    Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )

                                Text(
                                    text = value,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                            }

                            HorizontalDivider(
                                color = DashboardLogBorder
                            )
                        }
                    }

                    if (!hasDetails) {
                        Text(
                            text = "No additional information recorded.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Existing editing functionality.
                PrimeraGradientButton(
                    text = "Edit Prenatal Record",
                    onClick = {
                        val record = viewingRecord

                        openForm(
                            mode = if (
                                record.status == "completed"
                            ) {
                                PrenatalFormMode.RESULTS
                            } else {
                                PrenatalFormMode.APPOINTMENT
                            },
                            record = record
                        )
                    }
                )

                // Existing cancellation functionality.
                if (viewingRecord.status == "scheduled") {
                    Button(
                        onClick = {
                            recordPendingCancellation = viewingRecord
                        },
                        enabled = !state.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceWhite,
                            contentColor = ErrorRed
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            ErrorRed
                        )
                    ) {
                        Text(
                            text = "Cancel Appointment",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ErrorRed
                        )
                    }
                }

                TextButton(
                    onClick = {
                        viewingRecordId = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Close",
                        color = TextSecondary
                    )
                }
            }
        }
    }

    // ==========================================
    // APPOINTMENT CANCELLATION DIALOG
    // ==========================================

    val pendingCancellation = recordPendingCancellation

    if (pendingCancellation != null) {
        AlertDialog(
            onDismissRequest = {
                if (!state.isSaving) {
                    recordPendingCancellation = null
                }
            },
            title = {
                Text("Cancel Appointment?")
            },
            text = {
                Text(
                    "Are you sure you want to cancel this prenatal " +
                            "appointment? Its upcoming reminders will " +
                            "also be cancelled."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearMessages()

                        viewModel.updateRecord(
                            pendingCancellation.copy(
                                status = "cancelled"
                            )
                        )

                        recordPendingCancellation = null
                        viewingRecordId = null
                    },
                    enabled = !state.isSaving
                ) {
                    Text(
                        text = "Cancel Appointment",
                        color = ErrorRed
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        recordPendingCancellation = null
                    },
                    enabled = !state.isSaving
                ) {
                    Text(
                        text = "Keep Appointment",
                        color = PrimeraViolet
                    )
                }
            },
            containerColor = BackgroundCream
        )
    }

    // ==========================================
    // APPOINTMENT / RESULTS FORM BOTTOM SHEET
    // ==========================================

    val activeMode = formMode

    if (activeMode != null) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        ModalBottomSheet(
            onDismissRequest = {
                closeForm()
            },
            sheetState = sheetState,
            containerColor = BackgroundCream
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 32.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = when {
                        editingRecord != null ->
                            "Edit Prenatal Record"

                        activeMode == PrenatalFormMode.APPOINTMENT ->
                            "Schedule Appointment"

                        else ->
                            "Record Prenatal Results"
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = prenatalDisplayDate(selectedDate),
                    color = PrimeraViolet,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Visit date selected from the calendar.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                if (activeMode == PrenatalFormMode.APPOINTMENT) {
                    PrenatalCard {
                        PrenatalFormTitle("Appointment Details")

                        // Existing violet-themed time picker.
                        PrenatalTimePickerField(
                            value = fields["time"].orEmpty(),
                            onTimeSelected = { selectedTime ->
                                fields["time"] = selectedTime
                            }
                        )

                        PrenatalFormField(
                            label = "Clinic / Hospital",
                            value = fields["clinic"].orEmpty(),
                            onValueChange = {
                                fields["clinic"] = it
                            },
                            placeholder = "Optional"
                        )

                        PrenatalFormField(
                            label = "Healthcare Provider",
                            value = fields["provider"].orEmpty(),
                            onValueChange = {
                                fields["provider"] = it
                            },
                            placeholder = "Doctor or midwife"
                        )

                        PrenatalFormField(
                            label = "Notes",
                            value = fields["notes"].orEmpty(),
                            onValueChange = {
                                fields["notes"] = it
                            },
                            placeholder = "Things to remember",
                            singleLine = false
                        )
                    }
                } else {
                    PrenatalResultsForm(fields = fields)
                }

                if (formError != null) {
                    Text(
                        text = formError.orEmpty(),
                        fontSize = 12.sp,
                        color = ErrorRed
                    )
                }

                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage.orEmpty(),
                        fontSize = 12.sp,
                        color = ErrorRed
                    )
                }

                if (state.isSaving) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = PrimeraViolet
                        )

                        Text(
                            text = "Saving to Firebase...",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                } else {
                    PrimeraGradientButton(
                        text = when {
                            editingRecord != null ->
                                "Update Prenatal Record"

                            activeMode == PrenatalFormMode.APPOINTMENT ->
                                "Save Appointment"

                            else ->
                                "Save Prenatal Results"
                        },
                        onClick = {
                            formError = null

                            val details = fields.toMap()
                            val existing = editingRecord

                            when {
                                activeMode ==
                                        PrenatalFormMode.APPOINTMENT &&
                                        selectedDate < prenatalToday() -> {

                                    formError =
                                        "Choose today or a future date."
                                }

                                activeMode ==
                                        PrenatalFormMode.APPOINTMENT &&
                                        fields["time"].isNullOrBlank() -> {

                                    formError =
                                        "Select an appointment time."
                                }

                                activeMode ==
                                        PrenatalFormMode.RESULTS &&
                                        selectedDate > prenatalToday() -> {

                                    formError =
                                        "Results cannot be recorded " +
                                                "for a future date."
                                }

                                activeMode ==
                                        PrenatalFormMode.RESULTS &&
                                        details.values.all {
                                            it.isBlank()
                                        } -> {

                                    formError =
                                        "Enter at least one finding " +
                                                "from the prenatal visit."
                                }

                                existing != null -> {
                                    awaitingSave = true

                                    viewModel.updateRecord(
                                        existing.copy(
                                            visitDate = selectedDate,
                                            details = details
                                        )
                                    )
                                }

                                activeMode ==
                                        PrenatalFormMode.APPOINTMENT -> {

                                    awaitingSave = true

                                    viewModel.saveAppointment(
                                        visitDate = selectedDate,
                                        details = details
                                    )
                                }

                                else -> {
                                    awaitingSave = true

                                    viewModel.savePrenatalResults(
                                        visitDate = selectedDate,
                                        details = details
                                    )
                                }
                            }
                        }
                    )
                }

                Text(
                    text = "Information entered here is " +
                            "patient-reported. Copy clinical findings " +
                            "from your Mother's Book or healthcare " +
                            "provider's records.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                TextButton(
                    onClick = {
                        closeForm()
                    },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel",
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

// ==================================================
// COMPACT PRENATAL ENTRY CARD
// ==================================================

@Composable
private fun PrenatalEntryCard(
    record: PrenatalRecordDto,
    onView: () -> Unit,
    onEdit: () -> Unit
) {
    val completed = record.status == "completed"
    val time = record.details["time"].orEmpty()
    val clinic = record.details["clinic"].orEmpty()
    val provider = record.details["provider"].orEmpty()

    val iconColor = if (completed) {
        Color(0xFF79B49E)
    } else {
        PrimeraViolet
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .border(
                width = 1.dp,
                color = DashboardLogBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onView)
            .padding(
                horizontal = 14.dp,
                vertical = 11.dp
            ),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Small appointment icon.
            Icon(
                imageVector = if (completed) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.DateRange
                },
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(21.dp)
            )

            // Compact title and time.
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = if (completed) {
                        "Completed Prenatal Visit"
                    } else {
                        "Scheduled Appointment"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (time.isNotBlank()) {
                    Text(
                        text = time,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            // Existing edit functionality.
            TextButton(
                onClick = onEdit
            ) {
                Text(
                    text = "Edit",
                    color = PrimeraViolet,
                    fontSize = 12.sp
                )
            }
        }

        // Clinic / healthcare provider displayed
        // without adding excessive vertical space.
        if (clinic.isNotBlank() || provider.isNotBlank()) {
            val secondaryLine = listOf(
                clinic,
                provider
            )
                .filter { it.isNotBlank() }
                .joinToString(" • ")

            Text(
                text = secondaryLine,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 31.dp)
            )
        }

        // Preserve completed prenatal measurements.
        if (completed) {
            val displayedFields = listOf(
                "Gestational Age" to
                        record.details["aogWeeks"].orEmpty()
                            .takeIf { it.isNotBlank() }
                            ?.let { weeks ->
                                val days = record.details["aogDays"]
                                    .orEmpty()

                                if (days.isNotBlank()) {
                                    "$weeks weeks, $days days"
                                } else {
                                    "$weeks weeks"
                                }
                            }.orEmpty(),

                "Weight" to
                        record.details["weight"].orEmpty()
                            .takeIf { it.isNotBlank() }
                            ?.let { "$it kg" }.orEmpty(),

                "Blood Pressure" to
                        record.details["bloodPressure"].orEmpty()
                            .takeIf { it.isNotBlank() }
                            ?.let { "$it mmHg" }.orEmpty(),

                "Fetal Heart Rate" to
                        record.details["fetalHeartRate"].orEmpty()
                            .takeIf { it.isNotBlank() }
                            ?.let { "$it bpm" }.orEmpty()
            )

            val nonEmptyFields = displayedFields.filter {
                it.second.isNotBlank()
            }

            if (nonEmptyFields.isNotEmpty()) {
                HorizontalDivider(
                    color = DashboardLogBorder,
                    modifier = Modifier.padding(vertical = 3.dp)
                )

                nonEmptyFields.forEach { (label, value) ->
                    Text(
                        text = "$label: $value",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

// ==================================================
// PRENATAL RESULTS FORM
// ==================================================

@Composable
private fun PrenatalResultsForm(
    fields: MutableMap<String, String>
) {
    var showClinical by remember {
        mutableStateOf(false)
    }

    var showMedicines by remember {
        mutableStateOf(false)
    }

    PrenatalCard {
        PrenatalFormTitle("Visit Details")

        PrenatalFormField(
            label = "AOG - Weeks",
            value = fields["aogWeeks"].orEmpty(),
            onValueChange = {
                fields["aogWeeks"] = it
            },
            placeholder = "e.g. 32",
            keyboardType = KeyboardType.Number
        )

        PrenatalFormField(
            label = "AOG - Additional Days",
            value = fields["aogDays"].orEmpty(),
            onValueChange = {
                fields["aogDays"] = it
            },
            placeholder = "0-6",
            keyboardType = KeyboardType.Number
        )

        PrenatalFormField(
            label = "Clinic / Hospital",
            value = fields["clinic"].orEmpty(),
            onValueChange = {
                fields["clinic"] = it
            },
            placeholder = "Optional"
        )

        PrenatalFormField(
            label = "Healthcare Provider",
            value = fields["provider"].orEmpty(),
            onValueChange = {
                fields["provider"] = it
            },
            placeholder = "Doctor or midwife"
        )
    }

    PrenatalCard {
        PrenatalFormTitle("Maternal Measurements")

        PrenatalFormField(
            label = "Weight (kg)",
            value = fields["weight"].orEmpty(),
            onValueChange = {
                fields["weight"] = it
            },
            placeholder = "e.g. 62.5",
            keyboardType = KeyboardType.Decimal
        )

        PrenatalFormField(
            label = "Blood Pressure (mmHg)",
            value = fields["bloodPressure"].orEmpty(),
            onValueChange = {
                fields["bloodPressure"] = it
            },
            placeholder = "e.g. 120/80"
        )
    }

    PrenatalCard {
        PrenatalFormTitle("Fetal Measurements")

        PrenatalFormField(
            label = "Fundal Height (cm)",
            value = fields["fundalHeight"].orEmpty(),
            onValueChange = {
                fields["fundalHeight"] = it
            },
            placeholder = "Optional",
            keyboardType = KeyboardType.Decimal
        )

        PrenatalFormField(
            label = "Fetal Heart Rate (bpm)",
            value = fields["fetalHeartRate"].orEmpty(),
            onValueChange = {
                fields["fetalHeartRate"] = it
            },
            placeholder = "Optional",
            keyboardType = KeyboardType.Number
        )
    }

    PrenatalCard {
        PrenatalExpandableHeader(
            title = "Additional Clinical Findings",
            expanded = showClinical,
            onClick = {
                showClinical = !showClinical
            }
        )

        if (showClinical) {
            PrenatalFormField(
                label = "Fetal Presentation",
                value = fields["presentation"].orEmpty(),
                onValueChange = {
                    fields["presentation"] = it
                },
                placeholder = "As recorded by clinician"
            )

            PrenatalFormField(
                label = "Estimated Fetal Weight (g)",
                value = fields["efw"].orEmpty(),
                onValueChange = {
                    fields["efw"] = it
                },
                placeholder = "Optional",
                keyboardType = KeyboardType.Number
            )

            PrenatalFormField(
                label = "IE Findings",
                value = fields["ieFindings"].orEmpty(),
                onValueChange = {
                    fields["ieFindings"] = it
                },
                placeholder = "Copy clinician's findings",
                singleLine = false
            )
        }
    }

    PrenatalCard {
        PrenatalExpandableHeader(
            title = "Medicines & Supplements",
            expanded = showMedicines,
            onClick = {
                showMedicines = !showMedicines
            }
        )

        if (showMedicines) {
            PrenatalFormField(
                label = "Prescribed Medicines / Supplements",
                value = fields["medicines"].orEmpty(),
                onValueChange = {
                    fields["medicines"] = it
                },
                placeholder = "As instructed by clinician",
                singleLine = false
            )
        }
    }

    PrenatalCard {
        PrenatalFormTitle(
            "Doctor's Instructions & Follow-up"
        )

        PrenatalFormField(
            label = "Remarks / Instructions",
            value = fields["instructions"].orEmpty(),
            onValueChange = {
                fields["instructions"] = it
            },
            placeholder = "Advice from your prenatal visit",
            singleLine = false
        )

        PrenatalFormField(
            label = "Next Visit Date",
            value = fields["nextVisit"].orEmpty(),
            onValueChange = {
                fields["nextVisit"] = it
            },
            placeholder = "YYYY-MM-DD (optional)"
        )

        Text(
            text = "A next visit date can be scheduled " +
                    "as an appointment separately.",
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

// ==================================================
// REUSABLE PRENATAL CARD
// ==================================================

@Composable
private fun PrenatalCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .border(
                width = 1.dp,
                color = DashboardLogBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

// ==================================================
// FORM TITLE
// ==================================================

@Composable
private fun PrenatalFormTitle(
    title: String
) {
    Text(
        text = title,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    )
}

// ==================================================
// EXPANDABLE FORM HEADER
// ==================================================

@Composable
private fun PrenatalExpandableHeader(
    title: String,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Icon(
            imageVector = if (expanded) {
                Icons.Default.ArrowDropUp
            } else {
                Icons.Default.ArrowDropDown
            },
            contentDescription = if (expanded) {
                "Collapse"
            } else {
                "Expand"
            },
            tint = PrimeraViolet
        )
    }
}

// ==================================================
// FORM INPUT FIELD
// ==================================================

@Composable
private fun PrenatalFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            },
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 2,
            maxLines = if (singleLine) 1 else 4,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

// ==================================================
// PRIMERA-THEMED APPOINTMENT TIME PICKER
// ==================================================

@Composable
private fun PrenatalTimePickerField(
    value: String,
    onTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = "Appointment Time",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Select appointment time",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Choose time",
                        tint = PrimeraViolet
                    )
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .semantics {
                        contentDescription = "Choose appointment time"
                    }
                    .clickable {
                        val startTime = Calendar.getInstance()

                        val parsedTime = runCatching {
                            SimpleDateFormat(
                                "h:mm a",
                                Locale.US
                            ).apply {
                                isLenient = false
                            }.parse(value)
                        }.getOrNull()

                        if (parsedTime != null) {
                            val existing = Calendar.getInstance().apply {
                                time = parsedTime
                            }

                            startTime.set(
                                Calendar.HOUR_OF_DAY,
                                existing.get(Calendar.HOUR_OF_DAY)
                            )

                            startTime.set(
                                Calendar.MINUTE,
                                existing.get(Calendar.MINUTE)
                            )
                        }

                        TimePickerDialog(
                            context,
                            R.style.PrimeraTimePickerDialogTheme,
                            { _, selectedHour, selectedMinute ->
                                val hour12 = when (
                                    val hour = selectedHour % 12
                                ) {
                                    0 -> 12
                                    else -> hour
                                }

                                val period = if (
                                    selectedHour < 12
                                ) {
                                    "AM"
                                } else {
                                    "PM"
                                }

                                val formattedTime = String.format(
                                    Locale.US,
                                    "%d:%02d %s",
                                    hour12,
                                    selectedMinute,
                                    period
                                )

                                onTimeSelected(formattedTime)
                            },
                            startTime.get(Calendar.HOUR_OF_DAY),
                            startTime.get(Calendar.MINUTE),
                            false
                        ).show()
                    }
            )
        }
    }
}

// ==================================================
// COMPOSE PREVIEW
// ==================================================

@Preview(showBackground = true)
@Composable
private fun PrenatalCheckupPreview() {
    PrimeraTheme {
        PrenatalCheckupScreen(
            onBack = {}
        )
    }
}

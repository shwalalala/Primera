
package cit.edu.primera.feature.checkins.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cit.edu.primera.core.theme.LogBaby
import cit.edu.primera.core.theme.LogNutrition
import cit.edu.primera.core.theme.LogOther
import cit.edu.primera.core.theme.LogPain
import cit.edu.primera.feature.checkins.data.CheckinLogDto
import cit.edu.primera.feature.checkins.data.CheckinsRepository
import cit.edu.primera.feature.dashboard.ui.DashboardLogUiItem
import cit.edu.primera.feature.dashboard.ui.DashboardWeekDayItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class CheckinsViewModel(
    private val repository: CheckinsRepository,
    private val symptomExtractor: cit.edu.primera.feature.transcription.domain.SymptomExtractor
) : ViewModel() {

    private val _overviewState = MutableStateFlow(CheckinsOverviewUiState())
    val overviewState: StateFlow<CheckinsOverviewUiState> =
        _overviewState.asStateFlow()

    private val _dailyState = MutableStateFlow(DailyCheckinUiState())
    val dailyState: StateFlow<DailyCheckinUiState> =
        _dailyState.asStateFlow()

    // Added: preserve the original DTOs for date-aware editing.
    // This avoids changing teammates' dashboard model.
    private var cachedLogs: List<CheckinLogDto> = emptyList()

    // Keep teammates' option definitions and constructor ordering.
    private val defaultSymptoms = listOf(
        CheckinOption("Aching Head", "Symptom"),
        CheckinOption("Nausea", "Symptom"),
        CheckinOption("Back Pain", "Symptom"),
        CheckinOption("Fatigue", "Symptom")
    )

    private val defaultMoods = listOf(
        CheckinOption("Normal", "Mood"),
        CheckinOption("Angry", "Mood"),
        CheckinOption("Happy", "Mood"),
        CheckinOption("Sad", "Mood")
    )

    private val defaultMedicines = listOf(
        CheckinOption("Med Pills", "Medicine"),
        CheckinOption("Gauze", "Medicine"),
        CheckinOption("Syringe", "Medicine")
    )

    init {
        observeLogs()
        observeWeight()
        updateWeekDays()
        loadOptions()
    }

    // -------------------------------------------------
    // DATE HELPERS - OUR ADDITION
    // -------------------------------------------------

    private fun createDateFormat(): SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
        }

    private fun normalizeCheckinDate(
        value: String?,
        fallback: Date?
    ): String {
        val formatter = createDateFormat()

        if (!value.isNullOrBlank()) {
            val parsed = runCatching {
                formatter.parse(value)
            }.getOrNull()

            if (parsed != null && formatter.format(parsed) == value) {
                return value
            }
        }

        return formatter.format(fallback ?: Date())
    }

    // -------------------------------------------------
    // TEAMMATES' EXISTING CUSTOM OPTIONS
    // -------------------------------------------------

    private fun loadOptions() {
        val customSymptoms = repository.getCustomOptions("Symptom")
            .map { CheckinOption(it, "Symptom") }

        val customMoods = repository.getCustomOptions("Mood")
            .map { CheckinOption(it, "Mood") }

        val customMedicines = repository.getCustomOptions("Medicine")
            .map { CheckinOption(it, "Medicine") }

        _dailyState.update {
            it.copy(
                availableSymptoms = customSymptoms + defaultSymptoms,
                availableMoods = customMoods + defaultMoods,
                availableMedicines = customMedicines + defaultMedicines
            )
        }
    }

    // -------------------------------------------------
    // OBSERVE CHECK-IN HISTORY
    // -------------------------------------------------

    private fun observeLogs() {
        repository.observeLogs()
            .onEach { logs ->
                // Added: retain DTOs for restoring edit dates.
                cachedLogs = logs

                _overviewState.update {
                    it.copy(
                        logs = mapLogs(logs),
                        isLoading = false
                    )
                }
            }
            .catch { e ->
                _overviewState.update {
                    it.copy(
                        errorMessage = e.message,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    // -------------------------------------------------
    // TEAMMATES' WEIGHT OBSERVER
    // With edit protection from our earlier version.
    // -------------------------------------------------

    private fun observeWeight() {
        repository.observeUserWeight()
            .onEach { userDto ->
                if (userDto != null) {
                    val lastUpdate = userDto.weightUpdatedAt

                    val shouldAlert = if (lastUpdate != null) {
                        val diff = Date().time - lastUpdate.time
                        diff > 14L * 24 * 60 * 60 * 1000
                    } else {
                        true
                    }

                    _dailyState.update { state ->
                        state.copy(
                            weightKg = if (state.editingId == null) {
                                userDto.weightKg?.toString() ?: ""
                            } else {
                                // Do not overwrite a record being edited.
                                state.weightKg
                            },
                            lastWeightUpdateDate = lastUpdate?.let { date ->
                                SimpleDateFormat(
                                    "MMM d, yyyy",
                                    Locale.getDefault()
                                ).format(date)
                            },
                            shouldShowWeightUpdateAlert = shouldAlert
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    // -------------------------------------------------
    // CHECK-IN HISTORY
    // Added date-aware sorting while retaining
    // teammates' DashboardLogUiItem structure.
    // -------------------------------------------------

    private fun mapLogs(
        logs: List<CheckinLogDto>
    ): List<DashboardLogUiItem> {
        val dateFormat = createDateFormat()

        val timeFormat = SimpleDateFormat(
            "h:mm a",
            Locale.getDefault()
        )

        val displayFormat = SimpleDateFormat(
            "MMM d, yyyy",
            Locale.getDefault()
        )

        val todayString = dateFormat.format(Date())

        val yesterdayString = dateFormat.format(
            Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }.time
        )

        return logs.sortedWith(
            compareByDescending<CheckinLogDto> {
                normalizeCheckinDate(
                    it.checkinDate,
                    it.timestamp
                )
            }.thenByDescending {
                it.timestamp?.time ?: 0L
            }
        ).map { log ->
            val actualDate = normalizeCheckinDate(
                log.checkinDate,
                log.timestamp
            )

            val timestamp = log.timestamp

            val timeText = when (actualDate) {
                todayString -> {
                    if (timestamp != null) {
                        timeFormat.format(timestamp)
                    } else {
                        "Today"
                    }
                }

                yesterdayString -> "Yesterday"

                else -> {
                    val parsed = dateFormat.parse(actualDate)

                    if (parsed != null) {
                        displayFormat.format(parsed)
                    } else {
                        actualDate
                    }
                }
            }

            DashboardLogUiItem(
                id = log.id,
                category = log.category ?: "Other",
                description = log.description ?: "",
                time = timeText,
                accentColor = getCategoryColor(log.category ?: "")
            )
        }
    }

    private fun getCategoryColor(category: String): Color {
        return when (category.lowercase()) {
            "back pain", "pain", "nausea", "symptom" -> LogPain
            "nutrition", "food" -> LogNutrition
            "fetal movement", "baby" -> LogBaby
            else -> LogOther
        }
    }

    // -------------------------------------------------
    // CALENDAR DATE MANAGEMENT
    // -------------------------------------------------

    private fun updateWeekDays() {
        val selectedDate = _dailyState.value.selectedCheckinDate
        val dateFormat = createDateFormat()

        val selected = runCatching {
            dateFormat.parse(selectedDate)
        }.getOrNull() ?: return

        val calendar = Calendar.getInstance().apply {
            time = selected
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }

        val dayInitials = listOf(
            "S", "M", "T", "W", "T", "F", "S"
        )

        val days = mutableListOf<DashboardWeekDayItem>()

        for (i in 0..6) {
            days.add(
                DashboardWeekDayItem(
                    initial = dayInitials[i],
                    date = calendar.get(Calendar.DAY_OF_MONTH),
                    isSelected = dateFormat.format(calendar.time) ==
                            selectedDate
                )
            )

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        _dailyState.update {
            it.copy(weekDays = days)
        }
    }

    // Added: called by SelectableCheckinCalendar.
    fun onCheckinDateSelected(date: String) {
        val dateFormat = createDateFormat()

        val selectedDate = runCatching {
            dateFormat.parse(date)
        }.getOrNull() ?: return

        // Reject malformed dates.
        if (dateFormat.format(selectedDate) != date) {
            return
        }

        val today = dateFormat.parse(
            dateFormat.format(Date())
        ) ?: return

        // Daily check-ins are not allowed for future dates.
        if (selectedDate.after(today)) {
            return
        }

        _dailyState.update {
            it.copy(
                selectedCheckinDate = date,
                errorMessage = null
            )
        }

        updateWeekDays()
    }

    // -------------------------------------------------
    // TEAMMATES' SEARCH AND FILTER
    // -------------------------------------------------

    fun onSearchQueryChange(query: String) {
        _overviewState.update {
            it.copy(searchQuery = query)
        }
    }

    fun onFilterChange(filter: String) {
        _overviewState.update {
            it.copy(selectedFilter = filter)
        }
    }

    // -------------------------------------------------
    // TEAMMATES' SYMPTOM, MOOD AND MEDICINE TOGGLES
    // -------------------------------------------------

    fun onSymptomToggle(symptom: String) {
        _dailyState.update { state ->
            val newSet = if (state.selectedSymptoms.contains(symptom)) {
                state.selectedSymptoms - symptom
            } else {
                state.selectedSymptoms + symptom
            }

            state.copy(selectedSymptoms = newSet)
        }
    }

    fun onMoodToggle(mood: String) {
        _dailyState.update { state ->
            val newSet = if (state.selectedMoods.contains(mood)) {
                state.selectedMoods - mood
            } else {
                state.selectedMoods + mood
            }

            state.copy(selectedMoods = newSet)
        }
    }

    fun onMedicineToggle(medicine: String) {
        _dailyState.update { state ->
            val newSet = if (state.selectedMedicines.contains(medicine)) {
                state.selectedMedicines - medicine
            } else {
                state.selectedMedicines + medicine
            }

            state.copy(selectedMedicines = newSet)
        }
    }

    fun onNoteChange(note: String) {
        _dailyState.update {
            it.copy(note = note)
        }
    }

    // -------------------------------------------------
    // MEASUREMENTS
    // -------------------------------------------------

    fun onWeightChange(weight: String) {
        if (
            weight.isEmpty() ||
            weight.matches(Regex("\\d{0,3}(\\.\\d{0,2})?"))
        ) {
            _dailyState.update {
                it.copy(weightKg = weight)
            }
        }
    }

    // Added: blood sugar handling.
    fun onBloodSugarChange(bloodSugar: String) {
        if (
            bloodSugar.isEmpty() ||
            bloodSugar.matches(Regex("\\d{0,3}"))
        ) {
            _dailyState.update {
                it.copy(bloodSugarMgDl = bloodSugar)
            }
        }
    }

    // -------------------------------------------------
    // TEAMMATES' CUSTOM OPTIONS
    // -------------------------------------------------

    fun onAddCustomOption(category: String, label: String) {
        if (label.isBlank()) return

        repository.addCustomOption(category, label)
        loadOptions()

        when (category.lowercase()) {
            "symptom" -> onSymptomToggle(label)
            "mood" -> onMoodToggle(label)
            "medicine" -> onMedicineToggle(label)
        }
    }

    // -------------------------------------------------
    // TEAMMATES' VOICE INPUT AND SYMPTOM EXTRACTION
    // -------------------------------------------------

    fun applyVoiceInputToCheckin(
        transcribedText: String
    ) {
        viewModelScope.launch {
            val detectedSymptoms = symptomExtractor.extract(transcribedText)

            val mappedSymptoms = detectedSymptoms
                .mapNotNull { mapDetectedSymptomToCheckinOption(it) }
                .distinct()

            mappedSymptoms.forEach { symptomLabel ->
                val alreadyAvailable =
                    _dailyState.value.availableSymptoms.any { option ->
                        option.label.equals(
                            symptomLabel,
                            ignoreCase = true
                        )
                    }

                if (!alreadyAvailable) {
                    repository.addCustomOption("Symptom", symptomLabel)
                }
            }

            loadOptions()

            _dailyState.update { state ->
                val updatedNote = buildString {
                    if (state.note.isNotBlank()) {
                        append(state.note.trim())
                        append("\n\n")
                    }

                    append("Voice input: ")
                    append(transcribedText.trim())
                }

                state.copy(
                    selectedSymptoms =
                        state.selectedSymptoms + mappedSymptoms,
                    note = updatedNote,
                    success = false,
                    errorMessage = null
                )
            }
        }
    }

    private fun mapDetectedSymptomToCheckinOption(
        symptom: String
    ): String? {
        return when (symptom.lowercase().trim()) {
            "headache" -> "Aching Head"
            "dizziness" -> "Dizziness"
            "nausea" -> "Nausea"
            "fatigue" -> "Fatigue"
            "back pain" -> "Back Pain"
            "abdominal pain" -> "Abdominal Pain"
            "chest pain" -> "Chest Pain"
            "shortness of breath" -> "Shortness of Breath"
            "fever" -> "Fever"
            "cough" -> "Cough"
            "swelling" -> "Swelling"

            else -> symptom.replaceFirstChar { char ->
                if (char.isLowerCase()) {
                    char.titlecase(Locale.getDefault())
                } else {
                    char.toString()
                }
            }
        }
    }

    // -------------------------------------------------
    // SAVE CHECK-IN
    // Date-aware saving and blood sugar restored.
    // Existing repository.saveLog() is preserved.
    // -------------------------------------------------

    fun onSaveCheckin() {
        if (_dailyState.value.isSaving) return

        viewModelScope.launch {
            _dailyState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null
                )
            }

            try {
                val state = _dailyState.value
                val dateFormat = createDateFormat()

                val selectedDate = dateFormat.parse(
                    state.selectedCheckinDate
                ) ?: error("Invalid check-in date")

                require(
                    dateFormat.format(selectedDate) ==
                            state.selectedCheckinDate
                ) {
                    "Invalid check-in date"
                }

                val today = dateFormat.parse(
                    dateFormat.format(Date())
                ) ?: error("Invalid current date")

                require(!selectedDate.after(today)) {
                    "You cannot record a check-in for a future date"
                }

                val descriptionParts = mutableListOf<String>()

                if (state.weightKg.isNotBlank()) {
                    descriptionParts.add(
                        "Weight: ${state.weightKg} kg"
                    )
                }

                if (state.bloodSugarMgDl.isNotBlank()) {
                    descriptionParts.add(
                        "Blood Sugar: ${state.bloodSugarMgDl} mg/dL"
                    )
                }

                if (state.selectedSymptoms.isNotEmpty()) {
                    descriptionParts.add(
                        "Symptoms: ${state.selectedSymptoms.joinToString(", ")}"
                    )
                }

                if (state.selectedMoods.isNotEmpty()) {
                    descriptionParts.add(
                        "Mood: ${state.selectedMoods.joinToString(", ")}"
                    )
                }

                if (state.selectedMedicines.isNotEmpty()) {
                    descriptionParts.add(
                        "Medicine: ${state.selectedMedicines.joinToString(", ")}"
                    )
                }

                if (state.note.isNotBlank()) {
                    descriptionParts.add(state.note)
                }

                if (descriptionParts.isNotEmpty()) {
                    val category = when {
                        state.selectedSymptoms.isNotEmpty() -> "Symptom"
                        state.selectedMoods.isNotEmpty() -> "Mood"
                        state.selectedMedicines.isNotEmpty() -> "Medicine"
                        else -> "Other"
                    }

                    repository.saveLog(
                        CheckinLogDto(
                            id = state.editingId,
                            type = "Check-in",
                            category = category,
                            description = descriptionParts.joinToString("; "),
                            timestamp = state.originalTimestamp ?: Date(),
                            checkinDate = state.selectedCheckinDate
                        )
                    ).getOrThrow()

                    // Do not overwrite today's profile weight
                    // when saving a historical check-in.
                    val isToday =
                        state.selectedCheckinDate == dateFormat.format(Date())

                    if (isToday) {
                        val weight = state.weightKg.toDoubleOrNull()

                        if (weight != null && weight > 0) {
                            repository.updateUserWeight(
                                weight.roundToInt()
                            ).getOrThrow()
                        }
                    }
                }

                _dailyState.update {
                    it.copy(
                        isSaving = false,
                        success = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _dailyState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage =
                            e.message ?: "Failed to save check-in"
                    )
                }
            }
        }
    }

    // -------------------------------------------------
    // RESET FOR NEW CHECK-IN
    // -------------------------------------------------

    fun prepareNewCheckin() {
        _dailyState.update { state ->
            DailyCheckinUiState(
                availableSymptoms = state.availableSymptoms,
                availableMoods = state.availableMoods,
                availableMedicines = state.availableMedicines,
                weightKg = state.weightKg,
                lastWeightUpdateDate = state.lastWeightUpdateDate,
                shouldShowWeightUpdateAlert =
                    state.shouldShowWeightUpdateAlert
            )
        }

        updateWeekDays()
    }

    // -------------------------------------------------
    // LOAD EXISTING CHECK-IN FOR EDITING
    // Restore the original date, timestamp,
    // and blood sugar without changing dashboard DTOs.
    // -------------------------------------------------

    fun loadCheckinForEdit(log: DashboardLogUiItem) {
        val originalLog = cachedLogs.firstOrNull {
            it.id == log.id
        }

        val description = originalLog?.description ?: log.description

        val symptoms = parseSection(description, "Symptoms:")
        val moods = parseSection(description, "Mood:")
        val medicines = parseSection(description, "Medicine:")

        val weight = parseSection(description, "Weight:")
            .firstOrNull()
            ?.removeSuffix(" kg")
            ?.trim()

        val bloodSugar = parseSection(description, "Blood Sugar:")
            .firstOrNull()
            ?.removeSuffix(" mg/dL")
            ?.trim()

        var note = description

        listOf(
            "Symptoms:",
            "Mood:",
            "Medicine:",
            "Weight:",
            "Blood Sugar:"
        ).forEach { label ->
            val index = note.indexOf(label)

            if (index != -1) {
                val end = note.indexOf(";", index).let {
                    if (it == -1) note.length else it + 1
                }

                note = note.removeRange(index, end).trim()
            }
        }

        note = note.trimStart(';').trim()

        val restoredDate = normalizeCheckinDate(
            originalLog?.checkinDate,
            originalLog?.timestamp
        )

        _dailyState.update { current ->
            current.copy(
                editingId = log.id,
                originalTimestamp = originalLog?.timestamp,
                selectedCheckinDate = restoredDate,
                selectedSymptoms = symptoms.toSet(),
                selectedMoods = moods.toSet(),
                selectedMedicines = medicines.toSet(),
                weightKg = weight ?: "",
                bloodSugarMgDl = bloodSugar ?: "",
                note = note,
                success = false,
                errorMessage = null
            )
        }

        updateWeekDays()
    }

    // -------------------------------------------------
    // TEAMMATES' DESCRIPTION PARSING
    // -------------------------------------------------

    private fun parseSection(
        description: String,
        label: String
    ): List<String> {
        val start = description.indexOf(label)
        if (start == -1) return emptyList()

        val contentStart = start + label.length
        val end = description.indexOf(";", contentStart).let {
            if (it == -1) description.length else it
        }

        return description.substring(contentStart, end)
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    fun resetSuccess() {
        _dailyState.update {
            it.copy(success = false)
        }
    }
}

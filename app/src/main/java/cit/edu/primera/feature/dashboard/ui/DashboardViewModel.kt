
package cit.edu.primera.feature.dashboard.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cit.edu.primera.core.theme.LogBaby
import cit.edu.primera.core.theme.LogNutrition
import cit.edu.primera.core.theme.LogOther
import cit.edu.primera.core.theme.LogPain
import cit.edu.primera.core.util.DataExportHelper
import cit.edu.primera.core.util.HealthSummaryDataLoader
import cit.edu.primera.core.util.HealthSummaryPdfRenderer
import cit.edu.primera.feature.dashboard.data.DashboardRepository
import cit.edu.primera.feature.dashboard.domain.DashboardBusinessLogic
import cit.edu.primera.feature.dashboard.domain.DashboardData
import cit.edu.primera.feature.smartwatchconnection.data.HealthConnectManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardViewModel(
    private val repository: DashboardRepository,
    private val goalsRepository: cit.edu.primera.feature.goals.data.GoalsRepository,
    private val healthConnectManager: HealthConnectManager,
    private val preferenceRepository: cit.edu.primera.core.data.PreferenceRepository,
    private val networkMonitor: cit.edu.primera.core.util.NetworkMonitor,
    private val geminiContentRepository:
    cit.edu.primera.feature.dashboard.data.GeminiContentRepository? = null,
    private val healthSummaryDataLoader: HealthSummaryDataLoader,
    private val validationLogger: cit.edu.primera.core.logging.SystemValidationLogger? = null
) : ViewModel() {

    private val _weeklyContent =
        MutableStateFlow<cit.edu.primera.feature.dashboard.domain.WeeklyInfo?>(null)

    private var currentLoadedWeek: Int? = null

    private val _showMicCoachmark = MutableStateFlow(false)
    val showMicCoachmark: StateFlow<Boolean> =
        _showMicCoachmark.asStateFlow()

    private val _showManualEntryDialog = MutableStateFlow(false)
    val showManualEntryDialog: StateFlow<Boolean> = _showManualEntryDialog.asStateFlow()

    private val _selectedAttribute =
        MutableStateFlow<cit.edu.primera.ui.components.HealthAttribute?>(null)
    val selectedAttribute: StateFlow<cit.edu.primera.ui.components.HealthAttribute?> =
        _selectedAttribute.asStateFlow()

    private val _manualEntryError = MutableStateFlow<String?>(null)
    val manualEntryError: StateFlow<String?> = _manualEntryError.asStateFlow()

    fun onOpenManualEntryDialog() {
        _manualEntryError.value = null
        _showManualEntryDialog.value = true
    }

    fun onDismissManualEntryDialog() {
        _manualEntryError.value = null
        _showManualEntryDialog.value = false
    }

    fun onOpenAttributeEntryDialog(attribute: cit.edu.primera.ui.components.HealthAttribute) {
        _manualEntryError.value = null
        _selectedAttribute.value = attribute
    }

    fun onDismissAttributeEntryDialog() {
        _manualEntryError.value = null
        _selectedAttribute.value = null
    }

    fun saveAttributeHealthData(
        attribute: cit.edu.primera.ui.components.HealthAttribute,
        date: Date,
        timeStr: String,
        valueStr: String,
        extraValueStr: String?
    ) {
        var heartRate: Long? = null
        var steps: Long = 0L
        var sleepHours: Long = 0L
        var sleepMinutes: Long = 0L
        var spO2: Double? = null

        val currentDashboardData = (uiState.value as? DashboardUiState.Success)?.data

        when (attribute) {
            cit.edu.primera.ui.components.HealthAttribute.HEART_RATE -> {
                val parsed = valueStr.trim().toLongOrNull()
                if (parsed == null || parsed < 30 || parsed > 250) {
                    _manualEntryError.value = "Heart rate must be between 30 and 250 bpm."
                    return
                }
                heartRate = parsed
                steps = currentDashboardData?.steps?.toLong() ?: 0L
                sleepHours = currentDashboardData?.sleepHours?.toLong() ?: 0L
                sleepMinutes = currentDashboardData?.sleepMinutes?.toLong() ?: 0L
                spO2 = currentDashboardData?.spO2?.toDouble()
            }

            cit.edu.primera.ui.components.HealthAttribute.STEPS -> {
                val parsed = valueStr.trim().toLongOrNull()
                if (parsed == null || parsed < 0) {
                    _manualEntryError.value = "Steps count must be 0 or greater."
                    return
                }
                steps = parsed
                heartRate = currentDashboardData?.heartRateBpm?.toLong()?.takeIf { it > 0 }
                sleepHours = currentDashboardData?.sleepHours?.toLong() ?: 0L
                sleepMinutes = currentDashboardData?.sleepMinutes?.toLong() ?: 0L
                spO2 = currentDashboardData?.spO2?.toDouble()
            }

            cit.edu.primera.ui.components.HealthAttribute.SLEEP -> {
                val parsedHours = valueStr.trim().toLongOrNull() ?: 0L
                val parsedMins = extraValueStr?.trim()?.toLongOrNull() ?: 0L
                if (parsedHours < 0 || parsedHours > 24) {
                    _manualEntryError.value = "Sleep hours must be between 0 and 24."
                    return
                }
                if (parsedMins < 0 || parsedMins >= 60) {
                    _manualEntryError.value = "Sleep minutes must be between 0 and 59."
                    return
                }
                sleepHours = parsedHours
                sleepMinutes = parsedMins
                heartRate = currentDashboardData?.heartRateBpm?.toLong()?.takeIf { it > 0 }
                steps = currentDashboardData?.steps?.toLong() ?: 0L
                spO2 = currentDashboardData?.spO2?.toDouble()
            }

            cit.edu.primera.ui.components.HealthAttribute.SPO2 -> {
                val parsed = valueStr.trim().toDoubleOrNull()
                if (parsed == null || parsed < 50.0 || parsed > 100.0) {
                    _manualEntryError.value = "SpO₂ percentage must be between 50% and 100%."
                    return
                }
                spO2 = parsed
                heartRate = currentDashboardData?.heartRateBpm?.toLong()?.takeIf { it > 0 }
                steps = currentDashboardData?.steps?.toLong() ?: 0L
                sleepHours = currentDashboardData?.sleepHours?.toLong() ?: 0L
                sleepMinutes = currentDashboardData?.sleepMinutes?.toLong() ?: 0L
            }
        }

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
        val totalSleepMinutes = sleepHours * 60 + sleepMinutes

        val record = cit.edu.primera.feature.smartwatchconnection.domain.SmartwatchHealth(
            date = dateStr,
            time = timeStr,
            steps = steps,
            currentHeartRate = heartRate,
            averageHeartRate = heartRate,
            minimumHeartRate = heartRate,
            maximumHeartRate = heartRate,
            spO2 = spO2,
            sleepMinutes = totalSleepMinutes,
            syncedAt = date.time,
            isManualEntry = true,
            source = "manual"
        )

        viewModelScope.launch {
            try {
                repository.saveHistoricalRecord(record)
                repository.updateHealthData(
                    steps = steps,
                    heartRate = heartRate ?: 0L,
                    sleepHours = sleepHours,
                    sleepMinutes = sleepMinutes,
                    spO2 = spO2?.toLong()
                )
                _selectedAttribute.value = null
                _manualEntryError.value = null
            } catch (e: Exception) {
                _manualEntryError.value = "Failed to save: ${e.localizedMessage}"
            }
        }
    }

    fun saveManualHealthData(
        date: Date,
        timeStr: String,
        heartRate: Long?,
        spO2: Double?,
        steps: Long,
        sleepHours: Long,
        sleepMinutes: Long
    ) {
        if (heartRate != null && (heartRate < 30 || heartRate > 250)) {
            _manualEntryError.value = "Heart rate must be between 30 and 250 bpm."
            return
        }
        if (steps < 0) {
            _manualEntryError.value = "Steps count must be 0 or greater."
            return
        }
        if (sleepHours < 0 || sleepHours > 24) {
            _manualEntryError.value = "Sleep hours must be between 0 and 24."
            return
        }
        if (sleepMinutes < 0 || sleepMinutes >= 60) {
            _manualEntryError.value = "Sleep minutes must be between 0 and 59."
            return
        }
        if (spO2 != null && (spO2 < 50.0 || spO2 > 100.0)) {
            _manualEntryError.value = "SpO₂ percentage must be between 50% and 100%."
            return
        }
        if (heartRate == null && spO2 == null && steps == 0L && sleepHours == 0L && sleepMinutes == 0L) {
            _manualEntryError.value = "Please enter at least one health measurement."
            return
        }

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
        val totalSleepMinutes = sleepHours * 60 + sleepMinutes

        val record = cit.edu.primera.feature.smartwatchconnection.domain.SmartwatchHealth(
            date = dateStr,
            time = timeStr,
            steps = steps,
            currentHeartRate = heartRate,
            averageHeartRate = heartRate,
            minimumHeartRate = heartRate,
            maximumHeartRate = heartRate,
            spO2 = spO2,
            sleepMinutes = totalSleepMinutes,
            syncedAt = date.time,
            isManualEntry = true,
            source = "manual"
        )

        viewModelScope.launch {
            try {
                repository.saveHistoricalRecord(record)
                repository.updateHealthData(
                    steps = steps,
                    heartRate = heartRate ?: 0L,
                    sleepHours = sleepHours,
                    sleepMinutes = sleepMinutes,
                    spO2 = spO2?.toLong()
                )
                _showManualEntryDialog.value = false
                _manualEntryError.value = null
            } catch (e: Exception) {
                _manualEntryError.value = "Failed to save: ${e.localizedMessage}"
            }
        }
    }

    init {
        viewModelScope.launch {
            goalsRepository.ensureMandatoryGoals()
            checkMicCoachmark()
        }
    }

    fun checkMicCoachmark() {
        viewModelScope.launch {
            val userId =
                com.google.firebase.auth.FirebaseAuth.getInstance()
                    .currentUser?.uid ?: ""

            val hasSeen =
                preferenceRepository.hasSeenMicCoachmark(userId)

            _showMicCoachmark.value = !hasSeen
        }
    }

    fun dismissMicCoachmark() {
        viewModelScope.launch {
            val userId =
                com.google.firebase.auth.FirebaseAuth.getInstance()
                    .currentUser?.uid ?: ""

            preferenceRepository.setMicCoachmarkDismissed(userId)
            _showMicCoachmark.value = false
        }
    }

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    // Existing QR summary text remains unchanged.
    val summaryQrContent: StateFlow<String> = combine(
        repository.observeHealthRecords(),
        repository.observeDashboardData()
    ) { healthRecords, dashboardData ->
        DataExportHelper.generateSummaryText(
            healthRecords,
            dashboardData
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "PRIMERA HEALTH SUMMARY\nGenerated by Primera App"
    )

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeDashboardData(),
        repository.observeHealthRecords(),
        _weeklyContent
    ) { data, healthRecords, weeklyContent ->
        if (data == null) {
            DashboardUiState.Success(
                createDefaultUiModel(weeklyContent)
            )
        } else {
            val week =
                DashboardBusinessLogic.getWeekNumber(data.dueDate)

            ensureWeeklyContent(week)

            DashboardUiState.Success(
                mapToUiModel(
                    data,
                    healthRecords,
                    weeklyContent,
                    week
                )
            )
        } as DashboardUiState
    }
        .onStart {
            // Existing loading behavior preserved.
        }
        .catch { e ->
            if (e.message?.contains("index") == true) {
                emit(
                    DashboardUiState.Success(
                        createDefaultUiModel(null)
                    )
                )
            } else {
                emit(
                    DashboardUiState.Error(
                        e.message ?: "An unexpected error occurred"
                    )
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState.Loading
        )

    private fun ensureWeeklyContent(week: Int) {
        if (currentLoadedWeek != week) {
            currentLoadedWeek = week

            viewModelScope.launch {
                val content =
                    if (isOnline.value && geminiContentRepository != null) {
                        geminiContentRepository.getWeeklyContent(week)
                    } else {
                        cit.edu.primera.feature.dashboard.domain.BabyDevelopmentData
                            .getInfoForWeek(week)
                    }

                _weeklyContent.value = content
            }
        }
    }

    private fun createDefaultUiModel(
        weeklyContent: cit.edu.primera.feature.dashboard.domain.WeeklyInfo?
    ): DashboardUiModel {
        val info = weeklyContent
            ?: cit.edu.primera.feature.dashboard.domain.BabyDevelopmentData
                .getInfoForWeek(1)

        return DashboardUiModel(
            userName = "Guest",
            timeOfDay = DashboardBusinessLogic.getTimeOfDay(),
            trimester = DashboardBusinessLogic.getTrimester(1),
            weekNumber = 1,
            dayNumber = 0,
            daysLeft = 280,
            babySize = DashboardBusinessLogic.getBabySize(1),
            babyEmoji = DashboardBusinessLogic.getBabyEmoji(1),
            babyIllustration = DashboardBusinessLogic.getBabyIllustration(1),
            heartRateBpm = 0,
            heartRateTrendingUp = false,
            heartRateVsLastWeek = 0,
            steps = 0,
            stepsGoal = 8000,
            sleepHours = 0,
            sleepMinutes = 0,
            sleepQuality = DashboardBusinessLogic.getSleepQuality(0, 0),
            spO2 = null,
            isWatchSynced = false,
            isGestationalAgeTentative = false,
            inaccuracyWarning = null,
            recentLogs = emptyList(),
            weekDays = getCurrentWeekDays(),
            milestones = info.milestones,
            symptoms = info.symptoms,
            articles = info.articles
        )
    }

    private fun mapToUiModel(
        data: DashboardData,
        healthRecords: List<cit.edu.primera.feature.smartwatchconnection.domain.SmartwatchHealth>,
        weeklyContent: cit.edu.primera.feature.dashboard.domain.WeeklyInfo?,
        week: Int
    ): DashboardUiModel {
        val userId =
            com.google.firebase.auth.FirebaseAuth.getInstance()
                .currentUser?.uid ?: ""

        val info = weeklyContent
            ?: cit.edu.primera.feature.dashboard.domain.BabyDevelopmentData
                .getInfoForWeek(week)

        val isWatchSynced =
            preferenceRepository.isWatchSyncEnabled(userId)

        val isTentative =
            data.isCycleRegular == false &&
                    data.hasHadUltrasound == false

        val warning = if (isTentative) {
            "Gestational age may not be accurate. Please have an ultrasound as soon as possible for a confirmed due date."
        } else {
            null
        }

        val avgHrLastWeek = healthRecords
            .take(7)
            .mapNotNull { it.averageHeartRate }
            .average()

        val latestHealthRecord = healthRecords.firstOrNull()
        val effectiveHr = if (data.heartRateBpm > 0) data.heartRateBpm else (latestHealthRecord?.currentHeartRate ?: latestHealthRecord?.averageHeartRate)?.toInt() ?: 0
        val effectiveSteps = if (data.steps > 0) data.steps else latestHealthRecord?.steps?.toInt() ?: 0
        val effectiveSleepTotal = if (data.sleepHours > 0 || data.sleepMinutes > 0) {
            data.sleepHours * 60 + data.sleepMinutes
        } else {
            latestHealthRecord?.sleepMinutes?.toInt() ?: 0
        }
        val effectiveSleepHours = effectiveSleepTotal / 60
        val effectiveSleepMinutes = effectiveSleepTotal % 60
        val effectiveSpO2 = data.spO2 ?: latestHealthRecord?.spO2?.toInt()

        val hrVsLastWeek =
            if (!avgHrLastWeek.isNaN() && effectiveHr > 0) {
                (effectiveHr - avgHrLastWeek).toInt()
            } else {
                0
            }

        return DashboardUiModel(
            userName = data.userName,
            timeOfDay = DashboardBusinessLogic.getTimeOfDay(),
            trimester = DashboardBusinessLogic.getTrimester(week),
            weekNumber = week,
            dayNumber = DashboardBusinessLogic.getDayNumber(data.dueDate),
            daysLeft = DashboardBusinessLogic.getDaysLeft(data.dueDate),
            babySize = DashboardBusinessLogic.getBabySize(week),
            babyEmoji = DashboardBusinessLogic.getBabyEmoji(week),
            babyIllustration = DashboardBusinessLogic.getBabyIllustration(week),
            heartRateBpm = effectiveHr,
            heartRateTrendingUp = hrVsLastWeek > 0,
            heartRateVsLastWeek = hrVsLastWeek,
            steps = effectiveSteps,
            stepsGoal = data.stepsGoal,
            sleepHours = effectiveSleepHours,
            sleepMinutes = effectiveSleepMinutes,
            sleepQuality = DashboardBusinessLogic.getSleepQuality(
                effectiveSleepHours,
                effectiveSleepMinutes
            ),
            spO2 = effectiveSpO2,
            isWatchSynced = isWatchSynced,
            isGestationalAgeTentative = isTentative,
            inaccuracyWarning = warning,
            recentLogs = data.recentLogs.map { log ->
                val sdf = SimpleDateFormat(
                    "h:mm a",
                    Locale.getDefault()
                )

                val timeText = if (isToday(log.timestamp)) {
                    "Today, ${sdf.format(log.timestamp)}"
                } else if (isYesterday(log.timestamp)) {
                    "Yesterday, ${sdf.format(log.timestamp)}"
                } else {
                    SimpleDateFormat(
                        "MMM d, h:mm a",
                        Locale.getDefault()
                    ).format(log.timestamp)
                }

                DashboardLogUiItem(
                    id = log.id,
                    category = log.category,
                    description = log.description,
                    time = timeText,
                    accentColor = getCategoryColor(log.category)
                )
            },
            weekDays = getCurrentWeekDays(),
            upcomingReminders = getPrenatalReminders(week),
            milestones = info.milestones,
            symptoms = info.symptoms,
            articles = info.articles
        )
    }

    private fun getPrenatalReminders(
        week: Int
    ): List<PrenatalReminderUiItem> {
        val reminders = mutableListOf<PrenatalReminderUiItem>()

        when {
            week in 1..13 -> {
                reminders.add(
                    PrenatalReminderUiItem(
                        title = "First-Trimester Prenatal Check-in",
                        description = "Confirm gestational age, review blood tests, and discuss early symptoms with your clinician.",
                        dueDateText = "Weeks 8 - 12",
                        isUrgent = false
                    )
                )

                reminders.add(
                    PrenatalReminderUiItem(
                        title = "Dating Ultrasound Scan",
                        description = "Ultrasound to confirm EDD and check early fetal development.",
                        dueDateText = "Recommended soon",
                        isUrgent = true
                    )
                )
            }

            week in 14..27 -> {
                reminders.add(
                    PrenatalReminderUiItem(
                        title = "Anatomy / Ultrasound Check-in",
                        description = "Detailed anomaly scan to evaluate fetal growth, placenta, and anatomy.",
                        dueDateText = "Weeks 18 - 22",
                        isUrgent = false
                    )
                )

                reminders.add(
                    PrenatalReminderUiItem(
                        title = "Glucose Screening (GDM Test)",
                        description = "Screening for Gestational Diabetes Mellitus.",
                        dueDateText = "Weeks 24 - 28",
                        isUrgent = false
                    )
                )
            }

            else -> {
                reminders.add(
                    PrenatalReminderUiItem(
                        title = "Third-Trimester Routine Check-in",
                        description = "Monitor blood pressure, fetal heart rate, fundal height, and kick count trends.",
                        dueDateText = "Bi-weekly / Weekly",
                        isUrgent = false
                    )
                )

                reminders.add(
                    PrenatalReminderUiItem(
                        title = "Group B Strep (GBS) Screening",
                        description = "Routine swab test prior to delivery.",
                        dueDateText = "Weeks 35 - 37",
                        isUrgent = false
                    )
                )
            }
        }

        return reminders
    }

    private fun isToday(date: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance().apply {
            time = date
        }

        return cal1[Calendar.YEAR] == cal2[Calendar.YEAR] &&
                cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]
    }

    private fun isYesterday(date: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.add(Calendar.DAY_OF_YEAR, -1)

        val cal2 = Calendar.getInstance().apply {
            time = date
        }

        return cal1[Calendar.YEAR] == cal2[Calendar.YEAR] &&
                cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]
    }

    private fun getCategoryColor(category: String): Color {
        return when (category.lowercase()) {
            "back pain", "pain" -> LogPain
            "nutrition", "food" -> LogNutrition
            "fetal movement", "baby" -> LogBaby
            else -> LogOther
        }
    }

    private fun getCurrentWeekDays(): List<DashboardWeekDayItem> {
        val calendar = Calendar.getInstance()
        val today = calendar[Calendar.DAY_OF_YEAR]

        calendar[Calendar.DAY_OF_WEEK] =
            calendar.firstDayOfWeek

        val days = mutableListOf<DashboardWeekDayItem>()
        val dayInitials = listOf(
            "S", "M", "T", "W", "T", "F", "S"
        )

        val monthFormat = SimpleDateFormat(
            "MMM",
            Locale.getDefault()
        )

        for (i in 0..6) {
            val monthStr =
                monthFormat.format(calendar.time)

            days.add(
                DashboardWeekDayItem(
                    initial = dayInitials[i],
                    date = calendar[Calendar.DAY_OF_MONTH],
                    isSelected = calendar[Calendar.DAY_OF_YEAR] == today,
                    month = monthStr
                )
            )

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return days
    }

    // ---------------------------------------------
    // HEALTH SUMMARY PDF EXPORT
    // ---------------------------------------------

    fun onExportSummaryPdf(context: android.content.Context) {
        onShareSummaryPdf(context)
    }

    // Existing QR upload remains unchanged for now.
    // Do not use it to share real patient data until
    // controlled-access PDF sharing is implemented.
    suspend fun getSummaryPdfUrl(
        context: android.content.Context
    ): String {
        val startTime = System.currentTimeMillis()
        return try {
            val summary = healthSummaryDataLoader.load()
            val url = DataExportHelper.uploadSummaryPdfAndGetUrl(
                context,
                summary.smartwatchRecords,
                summary.patient
            )
            val duration = System.currentTimeMillis() - startTime
            validationLogger?.logHealthSummary(
                "successful",
                duration,
                qrGenerationStatus = "successful"
            )
            validationLogger?.logPerformanceEvent("qr_generation", "successful", duration)
            url
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            validationLogger?.logHealthSummary("failed", duration, qrGenerationStatus = "failed")
            validationLogger?.logPerformanceEvent(
                "qr_generation",
                "failed",
                duration,
                failureReason = e.message
            )
            throw e
        }
    }

    fun onSaveSummaryPdf(context: android.content.Context) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            try {
                val summary = healthSummaryDataLoader.load()
                val pdfFile = HealthSummaryPdfRenderer.create(
                    context,
                    summary
                )
                DataExportHelper.savePdfToDownloads(
                    context,
                    pdfFile
                )
                val duration = System.currentTimeMillis() - startTime
                validationLogger?.logHealthSummary(
                    "successful",
                    duration,
                    qrGenerationStatus = "n/a",
                    printGenerationStatus = "n/a"
                )
                validationLogger?.logPerformanceEvent("health_summary_save", "successful", duration)
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                validationLogger?.logHealthSummary("failed", duration)
                validationLogger?.logPerformanceEvent(
                    "health_summary_save",
                    "failed",
                    duration,
                    failureReason = e.message
                )
                android.util.Log.e(
                    "DashboardViewModel",
                    "Failed to save health summary PDF",
                    e
                )

                android.widget.Toast.makeText(
                    context,
                    "Unable to generate health summary",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun onPrintSummaryPdf(context: android.content.Context) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            try {
                val summary = healthSummaryDataLoader.load()
                val pdfFile = HealthSummaryPdfRenderer.create(
                    context,
                    summary
                )
                DataExportHelper.printPdfFile(
                    context,
                    pdfFile
                )
                val duration = System.currentTimeMillis() - startTime
                validationLogger?.logHealthSummary(
                    "successful",
                    duration,
                    qrGenerationStatus = "n/a",
                    printGenerationStatus = "successful"
                )
                validationLogger?.logPerformanceEvent("print_generation", "successful", duration)
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                validationLogger?.logHealthSummary(
                    "failed",
                    duration,
                    printGenerationStatus = "failed"
                )
                validationLogger?.logPerformanceEvent(
                    "print_generation",
                    "failed",
                    duration,
                    failureReason = e.message
                )
                android.util.Log.e(
                    "DashboardViewModel",
                    "Failed to print health summary PDF",
                    e
                )

                android.widget.Toast.makeText(
                    context,
                    "Unable to prepare health summary for printing",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    fun onShareSummaryPdf(context: android.content.Context) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            try {
                val summary = healthSummaryDataLoader.load()
                val pdfFile = HealthSummaryPdfRenderer.create(
                    context,
                    summary
                )
                DataExportHelper.sharePdfFile(
                    context,
                    pdfFile
                )
                val duration = System.currentTimeMillis() - startTime
                validationLogger?.logHealthSummary("successful", duration)
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                validationLogger?.logHealthSummary("failed", duration)
                validationLogger?.logPerformanceEvent(
                    "health_summary_share",
                    "failed",
                    duration,
                    failureReason = e.message
                )
                android.util.Log.e(
                    "DashboardViewModel",
                    "Failed to share health summary PDF",
                    e
                )

                android.widget.Toast.makeText(
                    context,
                    "Unable to prepare health summary for sharing",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    // ---------------------------------------------
    // SMARTWATCH SYNC
    // ---------------------------------------------

    fun onSyncWatch() {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            try {
                if (
                    healthConnectManager.isAvailable() &&
                    healthConnectManager.hasAllPermissions()
                ) {
                    val healthData =
                        healthConnectManager.readTodaySmartwatchHealth()

                    val effectiveHeartRate = (healthData.currentHeartRate ?: healthData.averageHeartRate ?: 0L).coerceAtLeast(0L)
                    repository.updateHealthData(
                        steps = healthData.steps,
                        heartRate = effectiveHeartRate,
                        sleepHours = healthData.sleepMinutes / 60,
                        sleepMinutes = healthData.sleepMinutes % 60,
                        spO2 = healthData.spO2?.toLong()
                    )

                    repository.saveHistoricalRecord(healthData)

                    val userId =
                        com.google.firebase.auth.FirebaseAuth.getInstance()
                            .currentUser?.uid ?: ""

                    preferenceRepository.setWatchSyncEnabled(
                        userId,
                        enabled = true
                    )

                    val duration = System.currentTimeMillis() - startTime
                    val hrSync =
                        if ((healthData.currentHeartRate ?: 0L) > 0) "successful" else "missing"
                    val stepsSync = if (healthData.steps > 0) "successful" else "missing"
                    val sleepSync = if (healthData.sleepMinutes > 0) "successful" else "missing"

                    validationLogger?.logSmartwatchSync(
                        syncAttempt = 1,
                        syncStatus = "successful",
                        syncDurationMs = duration,
                        heartRateSync = hrSync,
                        stepsSync = stepsSync,
                        sleepSync = sleepSync,
                        dashboardUpdate = "successful"
                    )
                }
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                validationLogger?.logSmartwatchSync(
                    syncAttempt = 1,
                    syncStatus = "failed",
                    syncDurationMs = duration,
                    heartRateSync = "failed",
                    stepsSync = "failed",
                    sleepSync = "failed",
                    dashboardUpdate = "failed",
                    failureReason = e.message
                )
            }
        }
    }

    // ---------------------------------------------
    // STEPS GOAL
    // ---------------------------------------------

    fun onUpdateStepsGoal(goal: Long) {
        viewModelScope.launch {
            repository.updateStepsGoal(goal)
        }
    }

    // ---------------------------------------------
    // PROFILE PICTURE
    // ---------------------------------------------

    fun onPhotoUriSelected(photoUri: String) {
        viewModelScope.launch {
            val userId =
                com.google.firebase.auth.FirebaseAuth.getInstance()
                    .currentUser?.uid ?: ""

            preferenceRepository.setProfilePictureUri(
                userId,
                photoUri
            )

            repository.updatePhotoUri(photoUri)
        }
    }
}

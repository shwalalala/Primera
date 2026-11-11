
package cit.edu.primera.core.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import cit.edu.primera.feature.dashboard.domain.DashboardBusinessLogic
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * PRIMERA Health Summary PDF.
 *
 * All insights are descriptive calculations.
 * No AI-generated diagnoses, warnings, or advice.
 */
object HealthSummaryPdfRenderer {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val LEFT = 43f
    private const val RIGHT = 552f
    private const val BOTTOM = 782f
    private const val RECENT_CHECKINS = 5

    private val purple = Color.rgb(106, 76, 147)
    private val dark = Color.rgb(42, 42, 42)
    private val gray = Color.rgb(105, 105, 105)

    private fun makePaint(
        colorValue: Int,
        size: Float,
        bold: Boolean = false
    ): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorValue
        textSize = size
        isFakeBoldText = bold
    }

    private fun formatDate(date: Date): String =
        SimpleDateFormat(
            "MMM dd, yyyy",
            Locale.getDefault()
        ).format(date)

    private fun formatDateTime(date: Date): String =
        SimpleDateFormat(
            "MMM dd, yyyy - hh:mm a",
            Locale.getDefault()
        ).format(date)

    private fun validText(value: String?): String? =
        value?.trim()?.takeIf {
            it.isNotEmpty()
        }

    private fun number(value: Double): String =
        String.format(
            Locale.US,
            "%.1f",
            value
        ).removeSuffix(".0")

    fun create(
        context: Context,
        summary: HealthSummaryData
    ): File {

        val insights =
            HealthSummaryInsightsCalculator.calculate(summary)

        val document = PdfDocument()

        val titlePaint = makePaint(
            Color.WHITE, 21f, true
        )
        val subtitlePaint = makePaint(
            Color.WHITE, 9.5f
        )
        val sectionPaint = makePaint(
            purple, 14f, true
        )
        val headingPaint = makePaint(
            purple, 11f, true
        )
        val bodyPaint = makePaint(
            dark, 10f
        )
        val footerPaint = makePaint(
            gray, 8.5f
        )
        val rulePaint = makePaint(
            Color.rgb(225, 218, 234), 1f
        )

        var pageNumber = 0
        lateinit var page: PdfDocument.Page
        lateinit var canvas: Canvas
        var y = 0f

        fun startPage() {
            pageNumber++

            page = document.startPage(
                PdfDocument.PageInfo.Builder(
                    PAGE_WIDTH,
                    PAGE_HEIGHT,
                    pageNumber
                ).create()
            )

            canvas = page.canvas
            canvas.drawColor(Color.WHITE)

            canvas.drawRect(
                0f,
                0f,
                PAGE_WIDTH.toFloat(),
                89f,
                makePaint(purple, 10f)
            )

            canvas.drawText(
                "PRIMERA Health Summary",
                LEFT,
                39f,
                titlePaint
            )

            canvas.drawText(
                "Generated: ${formatDateTime(Date())}",
                LEFT,
                62f,
                subtitlePaint
            )

            canvas.drawText(
                "Page $pageNumber",
                PAGE_WIDTH / 2f - 15f,
                PAGE_HEIGHT - 26f,
                footerPaint
            )

            y = 115f
        }

        fun ensureSpace(height: Float) {
            if (y + height > BOTTOM) {
                document.finishPage(page)
                startPage()
            }
        }

        fun drawText(
            text: String,
            paint: Paint = bodyPaint,
            lineHeight: Float = 15.5f
        ) {
            val availableWidth = RIGHT - LEFT

            for (paragraph in text.split("\n")) {
                if (paragraph.isEmpty()) {
                    ensureSpace(lineHeight)
                    y += lineHeight
                    continue
                }

                var remaining = paragraph.trim()

                while (remaining.isNotEmpty()) {
                    var count = paint.breakText(
                        remaining,
                        true,
                        availableWidth,
                        null
                    )

                    if (count <= 0) break

                    if (count < remaining.length) {
                        val lastSpace = remaining
                            .substring(0, count)
                            .lastIndexOf(' ')

                        if (lastSpace > 0) {
                            count = lastSpace
                        }
                    }

                    val line = remaining
                        .substring(0, count)
                        .trimEnd()

                    ensureSpace(lineHeight)

                    canvas.drawText(
                        line,
                        LEFT,
                        y,
                        paint
                    )

                    y += lineHeight

                    remaining = remaining
                        .substring(count)
                        .trimStart()
                }
            }
        }

        fun section(title: String) {
            ensureSpace(55f)
            y += 14f

            drawText(
                title,
                sectionPaint,
                23f
            )

            canvas.drawLine(
                LEFT,
                y - 10f,
                RIGHT,
                y - 10f,
                rulePaint
            )

            y += 5f
        }

        fun heading(text: String) {
            ensureSpace(43f)
            y += 8f

            drawText(
                text,
                headingPaint,
                20f
            )
        }

        fun field(
            label: String,
            value: String?,
            showMissing: Boolean = false
        ) {
            val actual = validText(value)

            if (actual != null) {
                drawText("$label: $actual")
            } else if (showMissing) {
                drawText("$label: Not recorded")
            }
        }

        fun separator() {
            y += 7f
        }

        try {
            startPage()

            // ======================================
            // 1. PATIENT PROFILE
            // ======================================

            section("1. Patient & Pregnancy Profile")

            val patient = summary.patient

            if (patient == null) {
                drawText("Patient profile unavailable.")
            } else {
                val fullName = listOfNotNull(
                    validText(patient.firstName),
                    validText(patient.middleName),
                    validText(patient.lastName)
                ).joinToString(" ")
                    .ifBlank {
                        patient.userName
                    }

                field("Patient Name", fullName)

                field(
                    "Date of Birth",
                    patient.birthday?.let(::formatDate)
                )

                field(
                    "Last Menstrual Period",
                    patient.lmpDate?.let(::formatDate)
                )

                field(
                    "Estimated Due Date",
                    patient.dueDate?.let(::formatDate)
                )

                field(
                    "Height",
                    patient.heightCm
                        ?.takeIf { it > 0 }
                        ?.let { "$it cm" },
                    showMissing = true
                )

                field(
                    "Recorded Profile Weight",
                    patient.weightKg
                        ?.takeIf { it > 0 }
                        ?.let { "$it kg" }
                )

                field(
                    "First Pregnancy",
                    when (patient.isFirstPregnancy) {
                        true -> "Yes"
                        false -> "No"
                        null -> null
                    }
                )

                field(
                    "Previous Pregnancy Records",
                    patient.pregnancyHistories.size.toString()
                )
            }

            // ======================================
            // 2. PREGNANCY PROGRESS
            // ======================================

            section("2. Pregnancy & Baby Progress")

            val dueDate = patient?.dueDate

            if (dueDate == null) {
                drawText(
                    "Pregnancy progress unavailable because " +
                            "an estimated due date is not recorded."
                )
            } else {
                val week = DashboardBusinessLogic
                    .getWeekNumber(dueDate)

                val day = DashboardBusinessLogic
                    .getDayNumber(dueDate)

                val daysLeft = DashboardBusinessLogic
                    .getDaysLeft(dueDate)

                field(
                    "Gestational Age",
                    "$week weeks, $day days"
                )

                field(
                    "Trimester",
                    DashboardBusinessLogic
                        .getTrimester(week)
                        .replaceFirstChar {
                            it.uppercase()
                        }
                )

                if (week in 1..40) {
                    field(
                        "Approximate Baby Size Comparison",
                        DashboardBusinessLogic
                            .getBabySize(week)
                    )
                }

                if (daysLeft >= 0) {
                    field(
                        "Days Until Estimated Due Date",
                        daysLeft.toString()
                    )
                }

                drawText(
                    "Baby size is a general week-based " +
                            "comparison, not an ultrasound measurement."
                )

                if (
                    patient.isCycleRegular == false &&
                    patient.hasHadUltrasound == false
                ) {
                    drawText(
                        "Pregnancy dating is based on " +
                                "information recorded in PRIMERA " +
                                "and has not been confirmed by ultrasound."
                    )
                }
            }

            // ======================================
            // 3. HEALTH INSIGHTS OVERVIEW
            // ======================================

            section("3. Health Insights Overview")

            drawText(
                "The following information summarizes " +
                        "recorded values and their frequency. " +
                        "It does not interpret their medical meaning."
            )

            heading("Weight Monitoring")

            val initialWeight = insights.earliestWeightKg
            val latestWeight = insights.latestWeightKg
            val weightChange = insights.weightChangeKg

            if (
                initialWeight == null ||
                latestWeight == null
            ) {
                drawText(
                    "No recorded daily check-in weights available."
                )
            } else {
                field(
                    "Earliest Recorded Weight",
                    "${number(initialWeight)} kg" +
                            (
                                    insights.earliestWeightDate
                                        ?.let { " ($it)" }
                                        ?: ""
                                    )
                )

                field(
                    "Latest Recorded Weight",
                    "${number(latestWeight)} kg" +
                            (
                                    insights.latestWeightDate
                                        ?.let { " ($it)" }
                                        ?: ""
                                    )
                )

                if (weightChange != null) {
                    val changeText = when {
                        weightChange > 0 ->
                            "Increased by " +
                                    "${number(weightChange)} kg"

                        weightChange < 0 ->
                            "Decreased by " +
                                    "${number(abs(weightChange))} kg"

                        else ->
                            "No change between recorded measurements"
                    }

                    field(
                        "Recorded Weight Change",
                        changeText
                    )
                } else {
                    drawText(
                        "Only one weight measurement is available."
                    )
                }
            }

            heading("Blood Sugar Monitoring")

            if (insights.bloodSugarReadingCount == 0) {
                drawText(
                    "No blood sugar readings recorded " +
                            "in mg/dL."
                )
            } else {
                field(
                    "Number of Readings",
                    insights.bloodSugarReadingCount.toString()
                )

                field(
                    "Latest Reading",
                    insights.latestBloodSugarMgDl
                        ?.let {
                            "${number(it)} mg/dL"
                        }
                )

                field(
                    "Lowest Recorded Reading",
                    insights.lowestBloodSugarMgDl
                        ?.let {
                            "${number(it)} mg/dL"
                        }
                )

                field(
                    "Highest Recorded Reading",
                    insights.highestBloodSugarMgDl
                        ?.let {
                            "${number(it)} mg/dL"
                        }
                )

                drawText(
                    "These are recorded values only; " +
                            "fasting or meal timing is not specified."
                )
            }

            heading("Recent Symptom Summary")

            if (insights.recentCheckinCount == 0) {
                drawText(
                    "No recent daily check-ins recorded."
                )
            } else if (
                insights.mostFrequentSymptoms.isEmpty()
            ) {
                drawText(
                    "No symptoms logged in the " +
                            "${insights.recentCheckinCount} most recent check-ins."
                )
            } else {
                val symptomNames =
                    insights.mostFrequentSymptoms.joinToString(", ")

                val count = insights.mostFrequentSymptomCount
                val total = insights.recentCheckinCount

                if (insights.mostFrequentSymptoms.size == 1) {
                    drawText(
                        "The most frequently logged symptom " +
                                "was $symptomNames, recorded in " +
                                "$count of the $total most recent check-ins."
                    )
                } else {
                    drawText(
                        "The most frequently logged symptoms " +
                                "were $symptomNames, each recorded " +
                                "in $count of the $total most recent check-ins."
                    )
                }

                insights.symptomCounts
                    .take(5)
                    .forEach { (symptom, occurrences) ->
                        field(
                            symptom,
                            "$occurrences check-in " +
                                    if (occurrences == 1) {
                                        "entry"
                                    } else {
                                        "entries"
                                    }
                        )
                    }
            }

            heading("Prenatal Record Summary")

            field(
                "Completed Prenatal Checkups",
                insights.completedPrenatalCount.toString()
            )

            field(
                "Latest Completed Visit",
                insights.latestCompletedPrenatalDate
            )

            heading("Connected Device Summary")

            if (insights.smartwatchRecordCount == 0) {
                drawText(
                    "No historical smartwatch measurements available."
                )
            } else {
                field(
                    "Saved Smartwatch Records",
                    insights.smartwatchRecordCount.toString()
                )
            }

            // Goal completion cannot be calculated
            // without loading actual GoalsRepository data.
            // Do not invent a goals-achieved count.

            // ======================================
            // 4. RECENT DAILY CHECK-INS
            // ======================================

            section("4. Recent Daily Check-ins")

            val checkins =
                HealthSummaryInsightsCalculator.newestFirst(
                    summary.dailyCheckins
                ).take(RECENT_CHECKINS)

            if (checkins.isEmpty()) {
                drawText("No daily check-ins recorded.")
            } else {
                checkins.forEachIndexed { index, checkin ->
                    ensureSpace(65f)

                    val date = checkin.checkinDate
                        ?.takeIf { it.isNotBlank() }
                        ?: checkin.timestamp?.let(::formatDate)
                        ?: "Date unavailable"

                    heading("${index + 1}. $date")

                    val description =
                        validText(checkin.description)

                    val message =
                        validText(checkin.message)

                    if (description != null) {
                        description
                            .split(";")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .forEach { recordedField ->
                                drawText(recordedField)
                            }
                    }

                    if (
                        message != null &&
                        message != description &&
                        (
                                description == null ||
                                        !description.contains(
                                            message,
                                            ignoreCase = true
                                        )
                                )
                    ) {
                        field("Notes", message)
                    }

                    if (
                        description == null &&
                        message == null
                    ) {
                        field(
                            "Category",
                            checkin.category
                        )
                    }

                    // No app-generated observations,
                    // warnings, or medical advice.
                    separator()
                }
            }

            // ======================================
            // 5. SMARTWATCH HISTORY
            // ======================================

            section("5. Connected Smartwatch Measurements")

            val watchRecords =
                summary.smartwatchRecords
                    .sortedByDescending { it.date }
                    .take(7)

            if (watchRecords.isEmpty()) {
                drawText(
                    "No synchronized smartwatch history available."
                )
            } else {
                drawText(
                    "Recorded measurements from the " +
                            "connected device, where available."
                )

                watchRecords.forEach { record ->
                    ensureSpace(80f)

                    heading(
                        "Measurement Date: " +
                                record.date.ifBlank {
                                    "Date unavailable"
                                }
                    )

                    field(
                        "Average Heart Rate",
                        record.averageHeartRate
                            ?.takeIf { it > 0 }
                            ?.let { "$it bpm" }
                    )

                    field(
                        "Minimum Heart Rate",
                        record.minimumHeartRate
                            ?.takeIf { it > 0 }
                            ?.let { "$it bpm" }
                    )

                    field(
                        "Maximum Heart Rate",
                        record.maximumHeartRate
                            ?.takeIf { it > 0 }
                            ?.let { "$it bpm" }
                    )

                    field(
                        "Blood Oxygen (SpO2)",
                        record.spO2
                            ?.takeIf { it > 0 }
                            ?.let {
                                String.format(
                                    Locale.US,
                                    "%.1f%%",
                                    it
                                )
                            }
                    )

                    field(
                        "Sleep Duration",
                        record.sleepMinutes
                            .takeIf { it > 0 }
                            ?.let {
                                "${it / 60}h ${it % 60}m"
                            }
                    )

                    field(
                        "Steps",
                        record.steps
                            .takeIf { it > 0 }
                            ?.toString()
                    )

                    separator()
                }
            }

            // ======================================
            // 6. COMPLETED PRENATAL RESULTS
            // ======================================

            section("6. Completed Prenatal Checkups")

            val completedCheckups =
                summary.prenatalRecords
                    .filter {
                        it.status.equals(
                            "completed",
                            ignoreCase = true
                        )
                    }
                    .sortedByDescending {
                        it.visitDate
                    }
                    .take(20)

            if (completedCheckups.isEmpty()) {
                drawText(
                    "No completed prenatal checkups recorded."
                )
            } else {
                completedCheckups.forEachIndexed {
                        index, record ->

                    ensureSpace(95f)

                    heading(
                        "${index + 1}. ${record.visitDate}"
                    )

                    val details = record.details

                    val fields = listOf(
                        Triple(
                            "clinic",
                            "Clinic / Hospital",
                            ""
                        ),
                        Triple(
                            "provider",
                            "Healthcare Provider",
                            ""
                        ),
                        Triple(
                            "aogWeeks",
                            "Gestational Age",
                            " weeks"
                        ),
                        Triple(
                            "aogDays",
                            "Additional Gestational Days",
                            " days"
                        ),
                        Triple(
                            "weight",
                            "Maternal Weight",
                            " kg"
                        ),
                        Triple(
                            "bloodPressure",
                            "Blood Pressure",
                            " mmHg"
                        ),
                        Triple(
                            "fundalHeight",
                            "Fundal Height",
                            " cm"
                        ),
                        Triple(
                            "fetalHeartRate",
                            "Fetal Heart Rate",
                            " bpm"
                        ),
                        Triple(
                            "presentation",
                            "Fetal Presentation",
                            ""
                        ),
                        Triple(
                            "efw",
                            "Estimated Fetal Weight",
                            " g"
                        ),
                        Triple(
                            "ieFindings",
                            "IE Findings",
                            ""
                        ),
                        Triple(
                            "medicines",
                            "Recorded Medicines & Supplements",
                            ""
                        ),
                        Triple(
                            "instructions",
                            "Recorded Instructions",
                            ""
                        ),
                        Triple(
                            "notes",
                            "Consultation Notes",
                            ""
                        )
                    )

                    fields.forEach {
                            (key, label, suffix) ->

                        val value =
                            validText(details[key])

                        if (value != null) {
                            field(
                                label,
                                "$value$suffix"
                            )
                        }
                    }

                    // Cancelled and scheduled visits
                    // are deliberately excluded.
                    separator()
                }
            }

            // ======================================
            // 7. REPORT INFORMATION
            // ======================================

            section("7. About This Report")

            drawText(
                "This report contains patient-entered " +
                        "information, saved prenatal checkup " +
                        "records, and available connected-device " +
                        "measurements. Calculated summaries " +
                        "describe recorded values and frequencies."
            )

            y += 8f

            drawText(
                "PRIMERA does not provide diagnoses, " +
                        "medical advice, or risk assessments. " +
                        "Baby-size comparisons are illustrative. " +
                        "The report does not replace clinical records."
            )

            document.finishPage(page)

            val file = File(
                context.cacheDir,
                "Primera_Health_Summary_" +
                        System.currentTimeMillis() +
                        ".pdf"
            )

            FileOutputStream(file).use {
                document.writeTo(it)
            }

            return file

        } finally {
            document.close()
        }
    }
}

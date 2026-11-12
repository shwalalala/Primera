
package cit.edu.primera.core.util

import cit.edu.primera.feature.checkins.data.CheckinLogDto
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Factual summaries of recorded PRIMERA information.
 *
 * No AI, diagnosis, risk assessment, or medical advice.
 */
data class HealthSummaryInsights(
    val recentCheckinCount: Int,
    val mostFrequentSymptoms: List<String>,
    val mostFrequentSymptomCount: Int,
    val symptomCounts: List<Pair<String, Int>>,
    val earliestWeightKg: Double?,
    val latestWeightKg: Double?,
    val weightChangeKg: Double?,
    val earliestWeightDate: String?,
    val latestWeightDate: String?,
    val bloodSugarReadingCount: Int,
    val latestBloodSugarMgDl: Double?,
    val lowestBloodSugarMgDl: Double?,
    val highestBloodSugarMgDl: Double?,
    val latestBloodSugarDate: String?,
    val completedPrenatalCount: Int,
    val latestCompletedPrenatalDate: String?,
    val smartwatchRecordCount: Int
)

object HealthSummaryInsightsCalculator {

    private const val RECENT_CHECKIN_LIMIT = 5

    private val weightPattern = Regex(
        """^Weight\s*:\s*(\d+(?:\.\d+)?)\s*kg\b""",
        RegexOption.IGNORE_CASE
    )

    private val bloodSugarPattern = Regex(
        """^Blood\s+Sugar\s*:\s*(\d+(?:\.\d+)?)\s*mg/dL\b""",
        RegexOption.IGNORE_CASE
    )

    private val symptomsPattern = Regex(
        """^Symptoms?\s*:\s*(.+)$""",
        RegexOption.IGNORE_CASE
    )

    /**
     * Sorts by the explicitly selected check-in date,
     * then by the saved timestamp.
     */
    fun newestFirst(
        checkins: List<CheckinLogDto>
    ): List<CheckinLogDto> {

        fun dateKey(log: CheckinLogDto): String {
            val selectedDate = log.checkinDate
                ?.takeIf { it.isNotBlank() }

            if (selectedDate != null) {
                return selectedDate
            }

            val timestamp = log.timestamp
                ?: return ""

            return SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            ).format(timestamp)
        }

        return checkins.sortedWith(
            compareByDescending<CheckinLogDto> {
                dateKey(it)
            }.thenByDescending {
                it.timestamp?.time ?: 0L
            }
        )
    }

    private fun recordedFields(
        log: CheckinLogDto
    ): List<String> {
        return log.description
            .orEmpty()
            .split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun recordedValue(
        log: CheckinLogDto,
        pattern: Regex
    ): Double? {
        return recordedFields(log)
            .firstNotNullOfOrNull { field ->
                pattern.find(field)
                    ?.groupValues
                    ?.getOrNull(1)
                    ?.toDoubleOrNull()
            }
            ?.takeIf {
                it > 0.0 && it.isFinite()
            }
    }

    private fun recordedSymptoms(
        log: CheckinLogDto
    ): List<String> {
        return recordedFields(log)
            .mapNotNull { field ->
                symptomsPattern.find(field)
                    ?.groupValues
                    ?.getOrNull(1)
            }
            .flatMap { text ->
                text.split(",")
            }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy {
                it.lowercase(Locale.ROOT)
            }
    }

    private fun recordDate(
        log: CheckinLogDto
    ): String? {
        return log.checkinDate
            ?.takeIf { it.isNotBlank() }
            ?: log.timestamp?.let {
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                ).format(it)
            }
    }

    fun calculate(
        summary: HealthSummaryData
    ): HealthSummaryInsights {

        val allCheckins = newestFirst(
            summary.dailyCheckins
        )

        val recentCheckins = allCheckins.take(
            RECENT_CHECKIN_LIMIT
        )

        // One symptom is counted at most once
        // per check-in, even if repeated in text.
        val counts = linkedMapOf<String, Int>()
        val symptomNames = linkedMapOf<String, String>()

        recentCheckins.forEach { checkin ->
            recordedSymptoms(checkin).forEach { symptom ->
                val key = symptom.lowercase(Locale.ROOT)

                symptomNames.putIfAbsent(
                    key,
                    symptom
                )

                counts[key] = (counts[key] ?: 0) + 1
            }
        }

        val sortedSymptoms = counts.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> {
                    it.value
                }.thenBy {
                    it.key
                }
            )
            .map { (key, count) ->
                (symptomNames[key] ?: key) to count
            }

        val topCount = sortedSymptoms
            .firstOrNull()
            ?.second ?: 0

        val topSymptoms = sortedSymptoms
            .filter {
                it.second == topCount
            }
            .map {
                it.first
            }

        // All available daily check-ins are used
        // for the earliest-to-latest weight comparison.
        val recordedWeights = allCheckins.mapNotNull { log ->
            recordedValue(
                log,
                weightPattern
            )?.let { value ->
                Triple(
                    log,
                    value,
                    recordDate(log)
                )
            }
        }

        val latestWeight = recordedWeights.firstOrNull()
        val earliestWeight = recordedWeights.lastOrNull()

        val weightChange =
            if (
                latestWeight != null &&
                earliestWeight != null &&
                latestWeight !== earliestWeight
            ) {
                latestWeight.second -
                        earliestWeight.second
            } else {
                null
            }

        // Use all available check-ins for the
        // blood sugar reading summary.
        // Only readings explicitly in mg/dL count.
        val bloodSugarReadings = allCheckins.mapNotNull { log ->
            recordedValue(
                log,
                bloodSugarPattern
            )?.let { value ->
                Triple(
                    log,
                    value,
                    recordDate(log)
                )
            }
        }

        val completedPrenatal = summary.prenatalRecords
            .filter {
                it.status.equals(
                    "completed",
                    ignoreCase = true
                )
            }
            .sortedByDescending {
                it.visitDate
            }

        return HealthSummaryInsights(
            recentCheckinCount = recentCheckins.size,
            mostFrequentSymptoms = topSymptoms,
            mostFrequentSymptomCount = topCount,
            symptomCounts = sortedSymptoms,
            earliestWeightKg = earliestWeight?.second,
            latestWeightKg = latestWeight?.second,
            weightChangeKg = weightChange,
            earliestWeightDate = earliestWeight?.third,
            latestWeightDate = latestWeight?.third,
            bloodSugarReadingCount = bloodSugarReadings.size,
            latestBloodSugarMgDl =
                bloodSugarReadings.firstOrNull()?.second,
            lowestBloodSugarMgDl =
                bloodSugarReadings.minOfOrNull { it.second },
            highestBloodSugarMgDl =
                bloodSugarReadings.maxOfOrNull { it.second },
            latestBloodSugarDate =
                bloodSugarReadings.firstOrNull()?.third,
            completedPrenatalCount = completedPrenatal.size,
            latestCompletedPrenatalDate =
                completedPrenatal.firstOrNull()?.visitDate,
            smartwatchRecordCount =
                summary.smartwatchRecords.size
        )
    }
}

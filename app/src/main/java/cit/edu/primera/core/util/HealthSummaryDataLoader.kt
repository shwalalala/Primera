
package cit.edu.primera.core.util

import cit.edu.primera.feature.checkins.data.CheckinLogDto
import cit.edu.primera.feature.checkins.data.CheckinsRepository
import cit.edu.primera.feature.checkins.data.PrenatalDataSource
import cit.edu.primera.feature.checkins.data.PrenatalRecordDto
import cit.edu.primera.feature.dashboard.data.DashboardRepository
import cit.edu.primera.feature.dashboard.domain.DashboardData
import cit.edu.primera.feature.smartwatchconnection.domain.SmartwatchHealth
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first

/**
 * Information collected for the PRIMERA Health Summary PDF.
 *
 * Data is retrieved from existing repositories.
 * No new Firestore collections are created.
 */
data class HealthSummaryData(
    val patient: DashboardData?,
    val smartwatchRecords: List<SmartwatchHealth>,
    val dailyCheckins: List<CheckinLogDto>,
    val prenatalRecords: List<PrenatalRecordDto>
)

/**
 * Retrieves the current patient's health information.
 *
 * Results are intended for PDF generation and should
 * not be logged or uploaded to public file hosts.
 */
class HealthSummaryDataLoader(
    private val dashboardRepository: DashboardRepository,
    private val checkinsRepository: CheckinsRepository,
    private val prenatalDataSource: PrenatalDataSource
) {

    suspend fun load(): HealthSummaryData {
        val userId = FirebaseAuth.getInstance()
            .currentUser
            ?.uid
            ?: throw IllegalStateException(
                "You must be signed in to generate a health summary."
            )

        val patient = dashboardRepository
            .observeDashboardData()
            .first()

        val smartwatchRecords = dashboardRepository
            .observeHealthRecords()
            .first()
            .sortedByDescending { record ->
                record.date
            }

        val dailyCheckins = checkinsRepository
            .observeLogs()
            .first()
            .sortedWith(
                compareByDescending<CheckinLogDto> {
                    it.checkinDate.orEmpty()
                }.thenByDescending {
                    it.timestamp?.time ?: 0L
                }
            )

        val prenatalRecords = prenatalDataSource
            .observeRecords()
            .first()
            .filter { record ->
                record.userId == userId
            }
            .sortedByDescending { record ->
                record.visitDate
            }

        check(
            FirebaseAuth.getInstance().currentUser?.uid == userId
        ) {
            "The signed-in account changed during summary generation."
        }

        return HealthSummaryData(
            patient = patient,
            smartwatchRecords = smartwatchRecords,
            dailyCheckins = dailyCheckins,
            prenatalRecords = prenatalRecords
        )
    }
}


package cit.edu.primera.feature.checkins.data

import java.util.Date

/**
 * A prenatal appointment or completed checkup.
 *
 * Records are stored separately from Daily Check-Ins
 * in the Firestore "prenatal_checkups" collection.
 *
 * status:
 * - "scheduled"
 * - "completed"
 * - "cancelled"
 *
 * Dates use yyyy-MM-dd format.
 */
data class PrenatalRecordDto(
    val id: String? = null,
    val userId: String = "",

    // Date of the appointment or actual checkup
    val visitDate: String = "",

    // scheduled, completed, or cancelled
    val status: String = "scheduled",

    // Appointment information and clinical findings
    val details: Map<String, String> = emptyMap(),

    // Actual time the record was created
    val createdAt: Date? = null,

    // Last modification time
    val updatedAt: Date? = null
)

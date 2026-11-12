package cit.edu.primera.feature.vaccination.domain

data class VaccineDetail(
    val id: String,
    val title: String,
    val timing: String,
    val description: String,
    val colorHex: Long,
    val iconType: VaccineIconType,
)

enum class VaccineIconType {
    HEALTH_SAFETY,
    SHIELD,
    MEDICAL_SERVICES,
    VACCINES
}

data class VaccineScheduleRow(
    val name: String,
    val timeframeLabel: String,
    val barColorHex: Long,
    val spanFirst: Boolean,
    val spanSecond: Boolean,
    val spanThird: Boolean,
    val customNote: String? = null,
)

data class VaccinationSourceInfo(
    val title: String = "Maternal Immunization Schedule",
    val providerTitle: String = "Information provided by our Obstetrician",
    val doctorRole: String = "Obstetrics & Gynecology (OB-GYN) Clinical Guidance",
    val references: List<String> = listOf(
        "POGS (Philippine Obstetrical and Gynecological Society) Practice Bulletin No. 1",
        "PIDSOG (Philippine Infectious Diseases Society for Obstetrics and Gynecology) Guidelines",
        "ACOG (American College of Obstetricians and Gynecologists) Immunization Guidelines",
        "CDC Maternal Vaccination Recommendations"
    ),
    val verificationNote: String = "Reviewed and validated for expecting mothers to protect mother and child against preventable maternal and infant infections."
)

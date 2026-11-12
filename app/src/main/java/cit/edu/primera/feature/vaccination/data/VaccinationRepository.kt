package cit.edu.primera.feature.vaccination.data

import cit.edu.primera.feature.vaccination.domain.VaccinationSourceInfo
import cit.edu.primera.feature.vaccination.domain.VaccineDetail
import cit.edu.primera.feature.vaccination.domain.VaccineIconType
import cit.edu.primera.feature.vaccination.domain.VaccineScheduleRow

interface VaccinationRepository {
    fun getVaccineDetails(): List<VaccineDetail>
    fun getScheduleRows(): List<VaccineScheduleRow>
    fun getSourceInfo(): VaccinationSourceInfo
}

class VaccinationRepositoryImpl : VaccinationRepository {

    override fun getVaccineDetails(): List<VaccineDetail> {
        return listOf(
            VaccineDetail(
                id = "flu",
                title = "Influenza (Flu)",
                timing = "Anytime during pregnancy",
                description = "Flu vaccination reduces the risk of severe respiratory illness in pregnant mothers and protects newborn babies from flu in their first months.",
                colorHex = 0xFF127E36, // TrendGreen
                iconType = VaccineIconType.HEALTH_SAFETY
            ),
            VaccineDetail(
                id = "covid19",
                title = "COVID-19",
                timing = "Anytime during pregnancy",
                description = "Helps prevent severe COVID-19 complications during pregnancy and transfers protective antibodies across the placenta to the baby.",
                colorHex = 0xFF127E36, // TrendGreen
                iconType = VaccineIconType.SHIELD
            ),
            VaccineDetail(
                id = "tdap",
                title = "Tdap (Tetanus, Diphtheria & Pertussis)",
                timing = "27 – 36 Weeks Gestation",
                description = "Protects your newborn from whooping cough (pertussis), a life-threatening infection in infants, by maximizing antibody transfer before delivery.",
                colorHex = 0xFF834ECE, // PrimeraViolet
                iconType = VaccineIconType.MEDICAL_SERVICES
            ),
            VaccineDetail(
                id = "rsv",
                title = "RSV (Respiratory Syncytial Virus)",
                timing = "24 – 36 Weeks Gestation*",
                description = "Immunization (e.g. ABRYSVO®) provides maternal antibodies against RSV, shielding infants against severe lower respiratory tract disease.",
                colorHex = 0xFFD32F2F, // Red
                iconType = VaccineIconType.VACCINES
            )
        )
    }

    override fun getScheduleRows(): List<VaccineScheduleRow> {
        return listOf(
            VaccineScheduleRow(
                name = "Influenza (flu)",
                timeframeLabel = "ANYTIME DURING PREGNANCY",
                barColorHex = 0xFFD32F2F,
                spanFirst = true,
                spanSecond = true,
                spanThird = true
            ),
            VaccineScheduleRow(
                name = "*COVID-19",
                timeframeLabel = "ANYTIME DURING PREGNANCY",
                barColorHex = 0xFFD32F2F,
                spanFirst = true,
                spanSecond = true,
                spanThird = true
            ),
            VaccineScheduleRow(
                name = "Tdap",
                timeframeLabel = "27-36 WEEKS",
                barColorHex = 0xFFC2185B,
                spanFirst = false,
                spanSecond = false,
                spanThird = true,
                customNote = "27-36 WEEKS"
            ),
            VaccineScheduleRow(
                name = "RSV",
                timeframeLabel = "24-36 WEEKS*",
                barColorHex = 0xFFB71C1C,
                spanFirst = false,
                spanSecond = true,
                spanThird = true,
                customNote = "24-36 WEEKS*"
            )
        )
    }

    override fun getSourceInfo(): VaccinationSourceInfo {
        return VaccinationSourceInfo()
    }
}

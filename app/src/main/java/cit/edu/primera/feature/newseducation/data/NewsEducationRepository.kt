package cit.edu.primera.feature.newseducation.data

import cit.edu.primera.feature.newseducation.domain.EducationalResource

/**
 * Repository interface for retrieving educational resources and links.
 */
interface NewsEducationRepository {
    fun getEducationalResources(): List<EducationalResource>
}

/**
 * Concrete implementation of [NewsEducationRepository].
 * All educational links are defined here in a single collection, making it simple to add or update links.
 */
class NewsEducationRepositoryImpl : NewsEducationRepository {

    override fun getEducationalResources(): List<EducationalResource> {
        return listOf(
            EducationalResource(
                id = "cleveland_pregnancy_overview",
                title = "Pregnancy: Overview & What to Expect",
                description = "Comprehensive medical article from Cleveland Clinic covering symptoms, body changes, timelines, and prenatal healthcare guidelines.",
                url = "https://my.clevelandclinic.org/health/articles/pregnancy",
                sourceName = "Cleveland Clinic",
                category = "General Guidance",
                tags = listOf("Overview", "Pregnancy", "Symptoms", "Health", "Cleveland Clinic"),
            ),
            EducationalResource(
                id = "mayo_prenatal_care",
                title = "Prenatal Care: 1st Trimester Visits",
                description = "In-depth guide by Mayo Clinic explaining what to expect during initial doctor appointments, lab tests, and early prenatal care visits.",
                url = "https://www.mayoclinic.org/healthy-lifestyle/pregnancy-week-by-week/in-depth/prenatal-care/art-20044882",
                sourceName = "Mayo Clinic",
                category = "Prenatal Care",
                tags = listOf("Prenatal", "First Trimester", "Doctor Visits", "Mayo Clinic", "Checkups")
            ),
            EducationalResource(
                id = "who_pregnancy_handbook",
                title = "Pregnancy & Family Health Handbook",
                description = "Official WHO Philippines handbook for maternal and family health, offering essential care, nutrition, and child safety recommendations.",
                url = "https://cdn.who.int/media/docs/default-source/wpro---documents/countries/philippines/reports/phfphandbook-compressed.pdf?sfvrsn=1b2e1b63_1&download=true",
                sourceName = "World Health Organization (WHO)",
                category = "Official Handbook (PDF)",
                tags = listOf("WHO", "Handbook", "PDF", "Maternal Health", "Philippines", "Family Health")
            ),
            EducationalResource(
                id = "makati_prenatal_guide",
                title = "Prenatal Care: Delivering a Healthy Baby",
                description = "Practical guide from Makati Medical Center featuring tips on nutrition, trimester checkups, and key steps for delivering a healthy baby.",
                url = "https://www.makatimed.net.ph/blogs/prenatal-care-a-handy-guide-to-delivering-a-healthy-baby/",
                sourceName = "Makati Medical Center",
                category = "Maternal Health",
                tags = listOf("MakatiMed", "Prenatal Care", "Delivery", "Healthy Baby", "Nutrition")
            ),
            EducationalResource(
                id = "cleveland_second_trimester",
                title = "Second Trimester of Pregnancy",
                description = "Detailed article by Cleveland Clinic highlighting physical changes, baby growth, and health checkups for weeks 13 through 27.",
                url = "https://my.clevelandclinic.org/health/articles/16092-pregnancy-second-trimester",
                sourceName = "Cleveland Clinic",
                category = "Trimester Guide",
                tags = listOf("Second Trimester", "Weeks 13-27", "Fetal Growth", "Cleveland Clinic", "Milestones")
            )
        )
    }
}

package cit.edu.primera.feature.newseducation.domain

/**
 * Domain model representing an educational resource or article link.
 *
 * @param id Unique identifier for the resource.
 * @param title Display title of the educational resource.
 * @param description Brief summary or description of the content.
 * @param url The external URL that redirects to the educational content.
 * @param sourceName Name of the publisher/organization (e.g., Cleveland Clinic, WHO, Mayo Clinic).
 * @param category Category or tag for grouping (e.g., General Guidance, Prenatal Care, Official Handbook).
 * @param tags List of keywords used for searching and filtering.
 */
data class EducationalResource(
    val id: String,
    val title: String,
    val description: String,
    val url: String,
    val sourceName: String,
    val category: String,
    val tags: List<String> = emptyList()
)

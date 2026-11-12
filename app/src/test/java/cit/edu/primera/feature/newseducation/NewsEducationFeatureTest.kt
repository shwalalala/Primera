package cit.edu.primera.feature.newseducation

import cit.edu.primera.feature.newseducation.data.NewsEducationRepository
import cit.edu.primera.feature.newseducation.data.NewsEducationRepositoryImpl
import cit.edu.primera.feature.newseducation.ui.NewsEducationViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NewsEducationFeatureTest {

    private lateinit var repository: NewsEducationRepository
    private lateinit var viewModel: NewsEducationViewModel

    @Before
    fun setUp() {
        repository = NewsEducationRepositoryImpl()
        viewModel = NewsEducationViewModel(repository)
    }

    @Test
    fun repository_returnsExpectedInitialEducationalLinks() {
        val resources = repository.getEducationalResources()

        assertEquals(5, resources.size)

        val urls = resources.map { it.url }
        assertTrue(urls.contains("https://my.clevelandclinic.org/health/articles/pregnancy"))
        assertTrue(urls.contains("https://www.mayoclinic.org/healthy-lifestyle/pregnancy-week-by-week/in-depth/prenatal-care/art-20044882"))
        assertTrue(urls.contains("https://cdn.who.int/media/docs/default-source/wpro---documents/countries/philippines/reports/phfphandbook-compressed.pdf?sfvrsn=1b2e1b63_1&download=true"))
        assertTrue(urls.contains("https://www.makatimed.net.ph/blogs/prenatal-care-a-handy-guide-to-delivering-a-healthy-baby/"))
        assertTrue(urls.contains("https://my.clevelandclinic.org/health/articles/16092-pregnancy-second-trimester"))
    }

    @Test
    fun viewModel_initialState_loadsAllResources() {
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals(5, state.allResources.size)
        assertEquals(5, state.filteredResources.size)
    }

    @Test
    fun viewModel_onSearchQueryChanged_filtersByTitle() {
        viewModel.onSearchQueryChanged("Cleveland")

        val state = viewModel.uiState.value
        assertEquals("Cleveland", state.searchQuery)
        assertEquals(2, state.filteredResources.size)
        assertTrue(state.filteredResources.all { it.sourceName == "Cleveland Clinic" })
    }

    @Test
    fun viewModel_onSearchQueryChanged_filtersByPdfCategory() {
        viewModel.onSearchQueryChanged("PDF")

        val state = viewModel.uiState.value
        assertEquals(1, state.filteredResources.size)
        assertEquals("Pregnancy & Family Health Handbook", state.filteredResources.first().title)
    }

    @Test
    fun viewModel_onClearSearch_resetsFilteredList() {
        viewModel.onSearchQueryChanged("Mayo")
        assertEquals(1, viewModel.uiState.value.filteredResources.size)

        viewModel.onClearSearch()
        assertEquals("", viewModel.uiState.value.searchQuery)
        assertEquals(5, viewModel.uiState.value.filteredResources.size)
    }
}

package cit.edu.primera.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cit.edu.primera.core.di.ViewModelProvider
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.feature.dashboard.domain.ArticleInfo
import cit.edu.primera.ui.components.FeatureTopBar
import cit.edu.primera.ui.components.FullScreenLoadingOverlay

@Composable
fun NewsEducationScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedArticle by remember { mutableStateOf<ArticleInfo?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundCream,
                        PrimeraLilac.copy(alpha = 0.45f)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FeatureTopBar(
                title = "News & Education",
                onBack = onBack,
                onPrintQrClick = { viewModel.onExportSummaryPdf(context) }
            )

            when (val state = uiState) {
                is DashboardUiState.Loading -> FullScreenLoadingOverlay()
                is DashboardUiState.Error -> ErrorContent(state.message)
                is DashboardUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        EducationalArticlesSection(
                            articles = state.data.articles,
                            onArticleClick = { selectedArticle = it }
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    selectedArticle?.let { article ->
        ArticleDetailDialog(
            article = article,
            onDismiss = { selectedArticle = null }
        )
    }
}

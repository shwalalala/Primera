package cit.edu.primera.feature.welcome.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cit.edu.primera.R
import cit.edu.primera.core.di.ViewModelProvider
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.TextPrimary
import kotlinx.coroutines.launch

/**
 * Actual main features of Primera present in the codebase:
 * 1. Pregnancy & Baby Progress (week by week fruit sizes, trimester milestones, days left)
 * 2. Daily Health & Voice Logging (symptom tracker, mood, hydration, AI speech-to-text logging)
 * 3. Smartwatch Sync & Insights (steps, heart rate, sleep via Health Connect, wellness goals)
 */
val defaultWelcomePages = listOf(
    WelcomePageData(
        imageRes = R.drawable.welcome_1,
        title = "Track Your Pregnancy",
        description = "Follow baby growth week by week with fruit size comparisons, trimester milestones, and progress updates."
    ),
    WelcomePageData(
        imageRes = R.drawable.welcome_3,
        title = "Daily Health & Voice Logging",
        description = "Log symptoms, mood, sleep, and hydration effortlessly with AI-assisted voice check-ins."
    ),
    WelcomePageData(
        imageRes = R.drawable.welcome_2,
        title = "Smartwatch Sync & Insights",
        description = "Sync heart rate, steps, and sleep from Health Connect, view health trends, and set personal goals."
    )
)

/**
 * Stateful entry point for WelcomeScreen that manages ViewModel effects and navigation.
 */
@Composable
fun WelcomeScreen(
    onNavigateToRegister: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToAuth: () -> Unit = onNavigateToRegister,
    viewModel: WelcomeViewModel = viewModel(factory = ViewModelProvider.Factory),
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WelcomeEffect.NavigateToRegister -> onNavigateToRegister()
                is WelcomeEffect.NavigateToLogin -> onNavigateToLogin()
                is WelcomeEffect.NavigateToAuth -> onNavigateToAuth()
            }
        }
    }

    val pagerState = rememberPagerState(pageCount = { defaultWelcomePages.size })
    val coroutineScope = rememberCoroutineScope()

    WelcomeScreenContent(
        pages = defaultWelcomePages,
        pagerState = pagerState,
        onContinueClick = {
            keyboardController?.hide()
            if (pagerState.currentPage < (defaultWelcomePages.size - 1)) {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            } else {
                viewModel.onGetStarted()
            }
        },
        onSkipClick = {
            keyboardController?.hide()
            viewModel.onSkip()
        },
        onLoginClick = {
            keyboardController?.hide()
            viewModel.onLoginClicked()
        }
    )
}

/**
 * Minimalist stateless screen layout component.
 */
@Composable
fun WelcomeScreenContent(
    pages: List<WelcomePageData>,
    pagerState: PagerState,
    onContinueClick: () -> Unit,
    onSkipClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLastPage = pagerState.currentPage == (pages.size - 1)

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
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Skip Button
            WelcomeTopBar(
                onSkipClick = onSkipClick,
                showSkip = !isLastPage
            )

            // Horizontal Pager for Feature Pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                WelcomePageCard(data = pages[page])
            }

            // Minimalist Page Indicator Dots
            WelcomePagerIndicator(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom CTA Section
            WelcomeBottomActions(
                isLastPage = isLastPage,
                onContinueClick = onContinueClick,
                onLoginClick = onLoginClick
            )
        }
    }
}

// ---------------------------------------------------------------------------
// PREVIEWS - Show individual slides and a combined preview of ALL slides at once
// ---------------------------------------------------------------------------

@Preview(showBackground = true, name = "ALL SLIDES AT A GLANCE", widthDp = 380, heightDp = 1800)
@Composable
private fun WelcomeScreen_AllPagesPreview() {
    PrimeraTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCream)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            defaultWelcomePages.forEachIndexed { index, page ->
                Text(
                    text = "Slide ${index + 1} of ${defaultWelcomePages.size}: ${page.title}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(520.dp)
                ) {
                    WelcomeScreenContent(
                        pages = defaultWelcomePages,
                        pagerState = rememberPagerState(initialPage = index, pageCount = { defaultWelcomePages.size }),
                        onContinueClick = {},
                        onSkipClick = {},
                        onLoginClick = {}
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Slide 1: Track Your Pregnancy")
@Composable
private fun WelcomeScreenPreview1() {
    PrimeraTheme {
        WelcomeScreenContent(
            pages = defaultWelcomePages,
            pagerState = rememberPagerState(initialPage = 0, pageCount = { defaultWelcomePages.size }),
            onContinueClick = {},
            onSkipClick = {},
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Slide 2: Health & Voice Logging")
@Composable
private fun WelcomeScreenPreview2() {
    PrimeraTheme {
        WelcomeScreenContent(
            pages = defaultWelcomePages,
            pagerState = rememberPagerState(initialPage = 1, pageCount = { defaultWelcomePages.size }),
            onContinueClick = {},
            onSkipClick = {},
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Slide 3: Smartwatch Sync & Insights")
@Composable
private fun WelcomeScreenPreview3() {
    PrimeraTheme {
        WelcomeScreenContent(
            pages = defaultWelcomePages,
            pagerState = rememberPagerState(initialPage = 2, pageCount = { defaultWelcomePages.size }),
            onContinueClick = {},
            onSkipClick = {},
            onLoginClick = {}
        )
    }
}

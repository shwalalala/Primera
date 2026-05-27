package com.example.primera.feature.welcome.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.primera.R
import com.example.primera.core.di.ViewModelProvider
import com.example.primera.core.theme.BackgroundCream
import com.example.primera.core.theme.PrimeraLilac
import com.example.primera.core.theme.PrimeraTheme
import com.example.primera.core.theme.PrimeraViolet
import com.example.primera.core.theme.PagerIndicatorInactive
import com.example.primera.core.theme.TextPrimary
import com.example.primera.core.theme.TextSecondary
import com.example.primera.ui.components.PrimeraGradientButton
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    viewModel: WelcomeViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Listen for navigation effect
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WelcomeEffect.NavigateToAuth -> onGetStarted()
            }
        }
    }

    WelcomeScreenContent(
        uiState = uiState,
        onPreviousPage = { viewModel.previousPage() },
        onNextPage = { viewModel.nextPage() },
        onSkip = { viewModel.onSkip() },
        onGetStarted = { viewModel.onGetStarted() }
    )
}

@Composable
private fun WelcomeScreenContent(
    uiState: WelcomeUiState,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onSkip: () -> Unit,
    onGetStarted: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = uiState.currentPage, pageCount = { uiState.totalPages })
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    val onboardingPages = listOf(
        WelcomePageData(
            imageRes = R.drawable.welcome_1,
            title = "Welcome to Primera",
            description = "Your personalized companion for a healthy and happy pregnancy journey."
        ),
        WelcomePageData(
            imageRes = R.drawable.welcome_2,
            title = "Track Your Progress",
            description = "Easily monitor your baby's growth and your health milestones every step of the way."
        ),
        WelcomePageData(
            imageRes = R.drawable.welcome_3,
            title = "Personalized Insights",
            description = "Get expert advice and tips tailored to your specific stage of pregnancy."
        )
    )

    Box(
        modifier = Modifier
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
        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "Skip",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnboardingPage(data = onboardingPages[page])
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Page Indicators
            Row(
                Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(onboardingPages.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) PrimeraViolet else PagerIndicatorInactive
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation Button
            PrimeraGradientButton(
                text = if (pagerState.currentPage == uiState.totalPages - 1) "Get Started" else "Continue",
                onClick = {
                    if (pagerState.currentPage < uiState.totalPages - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            onNextPage()
                        }
                    } else {
                        onGetStarted()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .navigationBarsPadding()
                    .padding(bottom = 64.dp)
            )
        }
    }
}

@Composable
private fun OnboardingPage(data: WelcomePageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = data.imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(1f),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(68.dp))

        Text(
            text = data.title,
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = data.description,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Preview(name = "Compact Phone", device = Devices.PHONE)
@Preview(name = "Small Phone", widthDp = 360, heightDp = 640)
@Preview(name = "Large Phone", widthDp = 480, heightDp = 960)
@Composable
private fun WelcomeScreenPreview() {
    PrimeraTheme {
        WelcomeScreenContent(
            uiState = WelcomeUiState(),
            onPreviousPage = {},
            onNextPage = {},
            onSkip = {},
            onGetStarted = {}
        )
    }
}

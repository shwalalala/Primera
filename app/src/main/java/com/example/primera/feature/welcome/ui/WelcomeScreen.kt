package com.example.primera.feature.welcome.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.primera.R
import com.example.primera.core.di.ViewModelProvider
import com.example.primera.ui.components.PrimeraGradientButton
import com.example.primera.core.theme.*
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    onNavigateToAuth: () -> Unit,
    viewModel: WelcomeViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WelcomeEffect.NavigateToAuth -> onNavigateToAuth()
            }
        }
    }

    val onboardingPages = listOf(
        OnboardingPageData(
            imageRes = R.drawable.welcome_1,
            title = "Welcome to Primera",
            description = "Your personalized companion for a healthy and happy pregnancy journey."
        ),
        OnboardingPageData(
            imageRes = R.drawable.welcome_2,
            title = "Track Your Progress",
            description = "Easily monitor your baby's growth and your health milestones every step of the way."
        ),
        OnboardingPageData(
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
        // Skip Button
        TextButton(
            onClick = { viewModel.onSkip() },
            modifier = Modifier
                .align(Alignment.TopEnd)
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
                repeat(3) { iteration ->
                    val color = if (pagerState.currentPage == iteration) PrimeraViolet else Color.LightGray
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
                text = if (pagerState.currentPage == 2) "Get Started" else "Continue",
                onClick = {
                    keyboardController?.hide()
                    if (pagerState.currentPage < 2) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        viewModel.onGetStarted()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 64.dp)
            )
        }
    }
}

@Composable
fun OnboardingPage(data: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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

data class OnboardingPageData(
    val imageRes: Int,
    val title: String,
    val description: String
)

@Preview(showBackground = true, name = "Welcome Screen - Page 1")
@Composable
private fun WelcomeScreenPreview1() {
    PrimeraTheme {
        WelcomeScreen(onNavigateToAuth = {})
    }
}

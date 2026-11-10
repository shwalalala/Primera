package cit.edu.primera.feature.welcome.ui

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.primera.R
import cit.edu.primera.core.theme.PagerIndicatorInactive
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.ui.components.PrimeraGradientButton
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest

/**
 * Minimalist onboarding page layout with clear typography hierarchy and clean spacing.
 */
@Composable
fun WelcomePageCard(
    data: WelcomePageData,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Crisp Hero Image
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .heightIn(max = 200.dp)
                .aspectRatio(1.1f)
                .padding(vertical = 8.dp)
        ) {
            val context = LocalContext.current
            val imageRequest = remember(data.imageRes) {
                ImageRequest.Builder(context)
                    .data(data.imageRes)
                    .decoderFactory(
                        if (Build.VERSION.SDK_INT >= 28) {
                            ImageDecoderDecoder.Factory()
                        } else {
                            GifDecoder.Factory()
                        }
                    )
                    .build()
            }
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier.fillMaxHeight(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large, Commanding Headline
        Text(
            text = data.title,
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 32.sp
            ),
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtle, Readable Body Text
        Text(
            text = data.description,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Normal
            ),
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Sleek, minimalist pill page indicator.
 */
@Composable
fun WelcomePagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(20.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { pageIndex ->
            val isSelected = currentPage == pageIndex
            val width by animateDpAsState(
                targetValue = if (isSelected) 20.dp else 6.dp,
                animationSpec = tween(durationMillis = 250),
                label = "indicatorWidth"
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) PrimeraViolet else PagerIndicatorInactive,
                animationSpec = tween(durationMillis = 250),
                label = "indicatorColor"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(6.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

/**
 * Top bar with discreet skip button.
 */
@Composable
fun WelcomeTopBar(
    onSkipClick: () -> Unit,
    showSkip: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp)
    ) {
        if (showSkip) {
            TextButton(
                onClick = onSkipClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * Minimalist bottom action section with primary button and login link.
 */
@Composable
fun WelcomeBottomActions(
    isLastPage: Boolean,
    onContinueClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PrimeraGradientButton(
            text = if (isLastPage) "Get Started" else "Continue",
            onClick = onContinueClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = TextSecondary
            )
            Text(
                text = "Log In",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = PrimeraViolet,
                modifier = Modifier.clickable { onLoginClick() }
            )
        }
    }
}

@Preview(showBackground = true, name = "Welcome Page Minimalist Card")
@Composable
private fun WelcomePageCardPreview() {
    PrimeraTheme {
        WelcomePageCard(
            data = WelcomePageData(
                imageRes = R.drawable.welcome_1,
                title = "Track Your Pregnancy",
                description = "Follow baby growth week by week with size comparisons, trimester milestones, and progress updates."
            )
        )
    }
}

package cit.edu.primera.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.primera.R
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.SurfaceWhite
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.core.utils.clickableNoRipple

/**
 * Reusable Onboarding Coachmark / Tooltip component.
 *
 * Darkens the screen background behind it, displays a rounded dialog box with a downward-pointing
 * arrow tip, highlights an optional target component (e.g. FAB), and provides "Skip" & primary action buttons.
 */
@Composable
fun CoachmarkTooltip(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Voice Check-in",
    text: String = "Tap to record your daily check-in thoughts or symptoms with your voice!",
    primaryActionLabel: String = "Got it",
    skipActionLabel: String = "Skip",
    onPrimaryAction: () -> Unit = onDismiss,
    targetContent: (@Composable () -> Unit)? = null
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickableNoRipple { /* Intercept clicks on dark backdrop */ }
        ) {
            // Floating Tooltip Dialog Card + Arrow Pointer above target
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 92.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PrimeraLilac.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(R.drawable.mic),
                                        contentDescription = null,
                                        tint = PrimeraViolet,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = text,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 19.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text(
                                    text = skipActionLabel,
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = onPrimaryAction,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimeraViolet)
                            ) {
                                Text(
                                    text = primaryActionLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Downward pointing arrow tip pointing at the target element below
                TooltipArrowDown(
                    color = SurfaceWhite,
                    modifier = Modifier.offset(y = (-1).dp)
                )
            }

            // Highlighted target rendered clearly over the dimmed background
            if (targetContent != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                ) {
                    targetContent()
                }
            }
        }
    }
}

/**
 * Small triangle Canvas path pointing downwards.
 */
@Composable
private fun TooltipArrowDown(
    modifier: Modifier = Modifier,
    color: Color = SurfaceWhite
) {
    Canvas(modifier = modifier.size(width = 20.dp, height = 12.dp)) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width / 2, size.height)
            lineTo(size.width, 0f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

@Preview(showBackground = true)
@Composable
private fun CoachmarkTooltipPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            CoachmarkTooltip(
                visible = true,
                onDismiss = {},
                title = "Voice Check-in",
                text = "Tap to record your daily check-in thoughts or symptoms with your voice!",
                primaryActionLabel = "Got it",
                skipActionLabel = "Skip"
            )
        }
    }
}

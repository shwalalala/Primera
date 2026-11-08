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

enum class ArrowPosition {
    BOTTOM,
    TOP,
    NONE
}

/**
 * Reusable Onboarding Coachmark / Tooltip component.
 *
 * Darkens the screen background behind it, displays a rounded dialog box with an arrow tip,
 * highlights an optional target component, and provides "Skip" & primary action buttons.
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
    stepText: String? = null,
    iconRes: Int? = R.drawable.mic,
    iconEmoji: String? = null,
    arrowPosition: ArrowPosition = ArrowPosition.BOTTOM,
    cardAlignment: Alignment = Alignment.BottomCenter,
    targetAlignment: Alignment = Alignment.BottomCenter,
    cardModifier: Modifier = Modifier.padding(bottom = 92.dp, start = 24.dp, end = 24.dp),
    targetModifier: Modifier = Modifier.padding(bottom = 12.dp),
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
            // Floating Tooltip Dialog Card + Arrow Pointer
            Column(
                modifier = cardModifier.align(cardAlignment),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (arrowPosition == ArrowPosition.TOP) {
                    TooltipArrowUp(
                        color = SurfaceWhite,
                        modifier = Modifier.offset(y = 1.dp)
                    )
                }

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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = PrimeraLilac.copy(alpha = 0.5f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (iconEmoji != null) {
                                            Text(text = iconEmoji, fontSize = 20.sp)
                                        } else if (iconRes != null) {
                                            Icon(
                                                painter = painterResource(iconRes),
                                                contentDescription = null,
                                                tint = PrimeraViolet,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            if (stepText != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PrimeraLilac.copy(alpha = 0.35f)
                                ) {
                                    Text(
                                        text = stepText,
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        ),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimeraViolet
                                    )
                                }
                            }
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

                if (arrowPosition == ArrowPosition.BOTTOM) {
                    TooltipArrowDown(
                        color = SurfaceWhite,
                        modifier = Modifier.offset(y = (-1).dp)
                    )
                }
            }

            // Highlighted target rendered clearly over the dimmed background
            if (targetContent != null) {
                Box(
                    modifier = targetModifier.align(targetAlignment)
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

/**
 * Small triangle Canvas path pointing upwards.
 */
@Composable
private fun TooltipArrowUp(
    modifier: Modifier = Modifier,
    color: Color = SurfaceWhite
) {
    Canvas(modifier = modifier.size(width = 20.dp, height = 12.dp)) {
        val path = Path().apply {
            moveTo(size.width / 2, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
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
                skipActionLabel = "Skip",
                stepText = "1 of 4"
            )
        }
    }
}

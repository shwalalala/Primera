package com.example.primera.frontend.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.frontend.common.theme.*

@Composable
fun CircularPregnancyRing(
    weekNumber: Int,
    dayNumber: Int,
    daysLeft: Int,
    babyEmoji: String,
    modifier: Modifier = Modifier
) {
    val totalWeeks = 40f
    val completedWeeks = (weekNumber - 1) + (dayNumber / 7f)
    val progress = (completedWeeks / totalWeeks).coerceIn(0f, 1f)
    val sweepAngle = progress * 300f

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(1f)
                .shadow(
                    elevation = 18.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.12f)
                )
                .background(
                    color = SurfaceWhite,
                    shape = CircleShape
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize(0.92f)
                    .drawBehind {
                        val strokeWidth = 14.dp.toPx()
                        val inset = strokeWidth / 2f
                        val arcSize = Size(
                            size.width - strokeWidth,
                            size.height - strokeWidth
                        )
                        val topLeft = Offset(inset, inset)
                        val startAngle = 120f

                        drawArc(
                            color = RingTrackColor,
                            startAngle = startAngle,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )

                        drawArc(
                            color = RingProgressColor,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )
                    }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(babyEmoji, fontSize = 72.sp)

                    Spacer(Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = DaysLeftBg
                    ) {
                        Text(
                            text = "$daysLeft Days Left",
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 4.dp
                            ),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimeraViolet
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressIndicatorsPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CircularPregnancyRing(
                weekNumber = 28,
                dayNumber = 3,
                daysLeft = 88,
                babyEmoji = "🥬"
            )
        }
    }
}

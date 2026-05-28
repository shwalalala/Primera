package com.example.primera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.primera.core.theme.DaysLeftBg
import com.example.primera.core.theme.PrimeraTheme
import com.example.primera.core.theme.PrimeraViolet
import com.example.primera.core.theme.RingProgressColor
import com.example.primera.core.theme.RingTrackColor
import com.example.primera.core.theme.SurfaceWhite

@Composable
fun CircularPregnancyRing(
    weekNumber: Int,
    dayNumber: Int,
    daysLeft: Int,
    babyEmoji: String,
    modifier: Modifier = Modifier
) {
    InsightCircularProgress(
        progress = ((weekNumber - 1) + (dayNumber / 7f)) / 40f,
        centerContent = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(babyEmoji, fontSize = 72.sp)

                Spacer(Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = DaysLeftBg
                ) {
                    Text(
                        text = "$daysLeft Days Left",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimeraViolet
                    )
                }
            }
        },
        modifier = modifier
    )
}

@Composable
fun InsightCircularProgress(
    progress: Float,
    centerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    progressColor: Color = RingProgressColor,
    trackColor: Color = RingTrackColor
) {
    val sweepAngle = (progress.coerceIn(0f, 1f)) * 300f

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
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val topLeft = Offset(inset, inset)
                        val startAngle = 120f

                        drawArc(
                            color = trackColor,
                            startAngle = startAngle,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        drawArc(
                            color = progressColor,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
            ) {
                centerContent()
            }
        }
    }
}

@Composable
fun GoalProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = PrimeraViolet
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressIndicatorsPreview() {
    PrimeraTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularPregnancyRing(
                weekNumber = 28,
                dayNumber = 3,
                daysLeft = 88,
                babyEmoji = "🥬"
            )
            
            GoalProgressBar(progress = 0.6f)
        }
    }
}


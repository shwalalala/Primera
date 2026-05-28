package com.example.primera.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.core.theme.PrimeraTheme
import com.example.primera.core.theme.SurfaceWhite
import com.example.primera.core.theme.TextPrimary
import com.example.primera.core.theme.TextSecondary

@Composable
fun ChartContainer(
    title: String,
    dateRange: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceWhite)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevious, modifier = Modifier.size(24.dp)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous", tint = TextSecondary)
            }
            Text(dateRange, fontSize = 12.sp, color = TextSecondary)
            IconButton(onClick = onNext, modifier = Modifier.size(24.dp)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next", tint = TextSecondary)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        content()
    }
}

@Composable
fun SimpleBarChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFFAED581), // Light green
    highlightedIndex: Int = -1,
    highlightColor: Color = Color(0xFF8BC34A) // Darker green
) {
    if (data.isEmpty()) {
        EmptyChartPlaceholder(modifier = modifier)
        return
    }

    val maxValue = data.maxOrNull() ?: 1f
    
    Canvas(modifier = modifier.fillMaxWidth().height(150.dp)) {
        val spacing = 20.dp.toPx()
        val barWidth = (size.width - (data.size - 1) * spacing) / data.size
        
        data.forEachIndexed { index, value ->
            val barHeight = (value / maxValue) * size.height
            val x = index * (barWidth + spacing)
            val y = size.height - barHeight
            
            drawRoundRect(
                color = if (index == highlightedIndex) highlightColor else barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            
            if (index == highlightedIndex) {
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(x + barWidth / 2f, y + 8.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun SimpleLineChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFFCE93D8) // Lilac
) {
    if (data.isEmpty()) {
        EmptyChartPlaceholder(modifier = modifier)
        return
    }

    val maxValue = (data.maxOrNull() ?: 1f).coerceAtLeast(1f)
    val minValue = data.minOrNull() ?: 0f
    val range = if (maxValue == minValue) 1f else maxValue - minValue

    Canvas(modifier = modifier.fillMaxWidth().height(150.dp)) {
        if (data.size < 2) {
            // Can't draw a line with one point, just draw a circle
            val x = size.width / 2f
            val y = size.height / 2f
            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(x, y))
            return@Canvas
        }

        val spacing = size.width / (data.size - 1)
        val path = Path()
        
        data.forEachIndexed { index, value ->
            val x = index * spacing
            val y = size.height - ((value - minValue) / range * size.height)
            
            if (index == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
            
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }
        
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun EmptyChartPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No data recorded yet",
            color = TextSecondary.copy(alpha = 0.5f),
            fontSize = 12.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChartsPreview() {
    PrimeraTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ChartContainer(
                title = "Weight(kg)",
                dateRange = "March 10 - March 16, 2024",
                onPrevious = {},
                onNext = {}
            ) {
                SimpleBarChart(
                    data = listOf(45f, 48f, 52f, 60f, 55f, 40f, 35f),
                    highlightedIndex = 3
                )
            }
            
            ChartContainer(
                title = "Mood Trend",
                dateRange = "March 10 - March 16, 2024",
                onPrevious = {},
                onNext = {}
            ) {
                SimpleLineChart(
                    data = listOf(3f, 4f, 2f, 5f, 4f, 3f, 4f)
                )
            }
        }
    }
}

package com.example.primera.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.core.theme.PrimeraTheme
import com.example.primera.core.theme.SurfaceWhite
import com.example.primera.core.theme.TextPrimary
import com.example.primera.core.theme.TextSecondary
import com.example.primera.core.theme.PrimeraViolet

@Composable
fun ChartContainer(
    title: @Composable () -> Unit,
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
        title()
        
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
    labels: List<String> = emptyList(),
    barColor: Color = PrimeraViolet,
    highlightedIndex: Int = -1,
    onBarClick: (Int) -> Unit = {}
) {
    if (data.isEmpty()) {
        EmptyChartPlaceholder(modifier = modifier)
        return
    }

    val maxValue = (data.maxOrNull() ?: 1f).coerceAtLeast(1f)
    val textMeasurer = rememberTextMeasurer()
    val valueTextStyle = TextStyle(
        color = TextPrimary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    val labelTextStyle = TextStyle(
        color = TextSecondary,
        fontSize = 10.sp,
        textAlign = TextAlign.Center
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val spacing = 16.dp.toPx()
                        val barWidth = (size.width - (data.size + 1) * spacing) / data.size
                        data.forEachIndexed { index, _ ->
                            val xStart = spacing + index * (barWidth + spacing)
                            if (offset.x >= xStart && offset.x <= xStart + barWidth) {
                                onBarClick(index)
                            }
                        }
                    }
                }
        ) {
            val spacing = 16.dp.toPx()
            val availableWidth = size.width - (data.size + 1) * spacing
            val barWidth = availableWidth / data.size
            
            // Draw from bottom, leave space for labels
            val bottomPadding = 25.dp.toPx()
            val topPadding = 25.dp.toPx()
            val chartHeight = size.height - bottomPadding - topPadding

            data.forEachIndexed { index, value ->
                val barHeight = (value / maxValue) * chartHeight
                val x = spacing + index * (barWidth + spacing)
                val y = size.height - bottomPadding - barHeight
                
                // Draw Bar
                drawRoundRect(
                    color = if (index == highlightedIndex) barColor else barColor.copy(alpha = 0.6f),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                // Draw Value above bar
                val valueText = if (value % 1f == 0f) value.toInt().toString() else "%.1f".format(value)
                val measuredValue = textMeasurer.measure(valueText, valueTextStyle)
                drawText(
                    textLayoutResult = measuredValue,
                    topLeft = Offset(x + (barWidth - measuredValue.size.width) / 2f, y - measuredValue.size.height - 4.dp.toPx())
                )

                // Draw Label below bar
                if (index < labels.size) {
                    val labelText = labels[index]
                    val measuredLabel = textMeasurer.measure(labelText, labelTextStyle)
                    drawText(
                        textLayoutResult = measuredLabel,
                        topLeft = Offset(x + (barWidth - measuredLabel.size.width) / 2f, size.height - bottomPadding + 4.dp.toPx())
                    )
                }
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
fun MoodLineChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    labels: List<String> = emptyList(),
    lineColor: Color = PrimeraViolet
) {
    if (data.isEmpty()) {
        EmptyChartPlaceholder(modifier = modifier)
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val labelTextStyle = TextStyle(
        color = TextSecondary,
        fontSize = 10.sp,
        textAlign = TextAlign.Center
    )
    val emojiStyle = TextStyle(fontSize = 14.sp)
    val moods = listOf("😄", "🙂", "😐", "😕", "☹️") // Corresponds to values 5 down to 1

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val leftPadding = 35.dp.toPx()
            val bottomPadding = 25.dp.toPx()
            val topPadding = 15.dp.toPx()
            val chartWidth = size.width - leftPadding - 16.dp.toPx()
            val chartHeight = size.height - bottomPadding - topPadding

            // Draw Y-axis emojis and horizontal lines
            moods.forEachIndexed { index, emoji ->
                val y = topPadding + (index.toFloat() / (moods.size - 1)) * chartHeight
                
                // Draw Emoji
                val measuredEmoji = textMeasurer.measure(emoji, emojiStyle)
                drawText(
                    textLayoutResult = measuredEmoji,
                    topLeft = Offset(8.dp.toPx(), y - measuredEmoji.size.height / 2f)
                )
                
                // Draw Horizontal Guide Line
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    start = Offset(leftPadding, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (data.isNotEmpty()) {
                val spacing = if (data.size > 1) chartWidth / (data.size - 1) else 0f
                val path = Path()

                data.forEachIndexed { index, value ->
                    // Value is 1 (Angry) to 5 (Happy). 
                    // Mapping: 5 -> top (index 0), 1 -> bottom (index 4)
                    val normalizedValue = (5f - value).coerceIn(0f, 4f) / 4f
                    val x = leftPadding + (if (data.size > 1) index * spacing else chartWidth / 2f)
                    val y = topPadding + normalizedValue * chartHeight

                    if (index == 0) path.moveTo(x, y)
                    else path.lineTo(x, y)

                    // Draw Point
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                    
                    // Draw Label below X-axis
                    if (index < labels.size) {
                        val measuredLabel = textMeasurer.measure(labels[index], labelTextStyle)
                        drawText(
                            textLayoutResult = measuredLabel,
                            topLeft = Offset(x - measuredLabel.size.width / 2f, size.height - bottomPadding + 6.dp.toPx())
                        )
                    }
                }

                if (data.size > 1) {
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
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
                title = { Text("Weight(kg)", fontWeight = FontWeight.SemiBold) },
                dateRange = "March 10 - March 16, 2024",
                onPrevious = {},
                onNext = {}
            ) {
                SimpleBarChart(
                    data = listOf(45f, 48f, 52f, 60f, 55f, 40f, 35f),
                    labels = listOf("11.02", "12.02", "13.02", "14.02", "15.02", "16.02", "17.02"),
                    highlightedIndex = 3
                )
            }
        }
    }
}

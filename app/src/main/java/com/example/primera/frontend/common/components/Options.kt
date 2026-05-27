package com.example.primera.frontend.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.frontend.common.theme.*

@Composable
fun PrimeraOptionButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(if (isSelected) PrimeraViolet else SurfaceWhite)
            .border(
                width = 1.dp,
                color = if (isSelected) PrimeraViolet else InputBorder,
                shape = RoundedCornerShape(28.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) SurfaceWhite else TextPrimary,
            fontSize = 18.sp,
            fontFamily = NunitoSans,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PrimeraOptionCard(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .border(
                width = 1.dp,
                color = if (isSelected) PrimeraViolet else InputBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = if (isSelected) PrimeraViolet else TextPrimary,
            fontSize = 18.sp,
            fontFamily = NunitoSans,
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun PrimeraOptionChip(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) PrimeraViolet else SurfaceWhite)
            .border(
                width = 1.dp,
                color = if (isSelected) PrimeraViolet else InputBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) SurfaceWhite else TextPrimary,
            fontSize = 18.sp,
            fontFamily = NunitoSans,
            fontWeight = FontWeight.Normal
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1FB)
@Composable
private fun OptionsPreview() {
    PrimeraTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PrimeraOptionButton(text = "Option Button", isSelected = true) {}
            PrimeraOptionButton(text = "Option Button", isSelected = false) {}
            
            PrimeraOptionCard(text = "Option Card Selection", isSelected = true) {}
            PrimeraOptionCard(text = "Option Card Selection", isSelected = false) {}
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimeraOptionChip(text = "Chip 1", isSelected = true) {}
                PrimeraOptionChip(text = "Chip 2", isSelected = false) {}
            }
        }
    }
}

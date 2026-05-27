package com.example.primera.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.core.theme.*

@Composable
fun PrimeraTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 30.sp,
            fontWeight = fontWeight,
            color = color,
            textAlign = textAlign
        )
    )
}

@Composable
fun PrimeraHeader(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.SemiBold
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = 24.sp,
            fontWeight = fontWeight,
            color = color,
            textAlign = textAlign
        )
    )
}

@Composable
fun PrimeraSubheader(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary,
    textAlign: TextAlign = TextAlign.Start,
    fontFamily: FontFamily = Quicksand,
    fontWeight: FontWeight = FontWeight.Medium
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge.copy(
            fontSize = 20.sp,
            fontFamily = Quicksand,
            fontWeight = fontWeight,
            color = color,
            textAlign = textAlign
        )
    )
}

@Composable
fun PrimeraLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary,
    fontFamily: FontFamily = Quicksand,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.Medium,
    paddingValues: PaddingValues = PaddingValues(bottom = 20.dp),
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 18.sp,
            fontWeight = fontWeight,
            color = color,
            textAlign = textAlign,
            fontFamily = Quicksand
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun TypographyPreview() {
    PrimeraTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PrimeraTitle(text = "Title 30sp")
            PrimeraHeader(text = "Header 24sp")
            PrimeraSubheader(text = "Subheader 20sp")
            PrimeraLabel(text = "Label 14sp")
        }
    }
}

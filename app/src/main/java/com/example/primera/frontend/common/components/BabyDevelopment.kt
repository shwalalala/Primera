package com.example.primera.frontend.common.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.primera.frontend.common.theme.*

@Composable
fun BabySizeText(
    babySize: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Your baby is the size of a $babySize!",
        fontSize = 14.sp,
        color = TextSecondary,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun BabyDevelopmentPreview() {
    PrimeraTheme {
        BabySizeText(babySize = "Large eggplant")
    }
}

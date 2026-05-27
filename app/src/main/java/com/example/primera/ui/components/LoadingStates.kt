package com.example.primera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.primera.core.theme.PrimeraTheme
import com.example.primera.core.theme.PrimeraViolet

@Composable
fun FullScreenLoadingOverlay(
    modifier: Modifier = Modifier,
    color: Color = PrimeraViolet
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = color)
    }
}

@Preview
@Composable
private fun LoadingStatesPreview() {
    PrimeraTheme {
        FullScreenLoadingOverlay()
    }
}


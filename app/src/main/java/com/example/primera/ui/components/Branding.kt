package com.example.primera.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.primera.R

@Composable
fun PrimeraLogoBubble(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.primera_logo),
        contentDescription = "Primera Logo",
        modifier = modifier.size(120.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun BrandingPreview() {
    PrimeraLogoBubble()
}


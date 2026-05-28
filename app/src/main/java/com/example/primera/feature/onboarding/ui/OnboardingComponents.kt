package com.example.primera.feature.onboarding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primera.core.theme.InputBorder
import com.example.primera.core.theme.PrimeraTheme
import com.example.primera.core.theme.PrimeraViolet
import com.example.primera.core.theme.SurfaceWhite
import com.example.primera.core.theme.TextPrimary
import com.example.primera.core.theme.TextSecondary
import com.example.primera.ui.components.PrimeraGradientButton

@Composable
fun OnboardingLayout(
    title: String,
    subtitle: String? = null,
    onContinue: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            ),
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(48.dp))
        content()
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        PrimeraGradientButton(text = "Continue", onClick = onContinue)
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun OnboardingButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(if (isSelected) PrimeraViolet else SurfaceWhite)
            .border(1.dp, if (isSelected) PrimeraViolet else InputBorder, RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) SurfaceWhite else TextPrimary
        )
    }
}

@Composable
fun OnboardingChip(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) PrimeraViolet else SurfaceWhite)
            .border(1.dp, if (isSelected) PrimeraViolet else InputBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) SurfaceWhite else TextPrimary, fontSize = 14.sp)
    }
}

@Composable
fun OnboardingSelectionCard(
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
            .border(1.dp, if (isSelected) PrimeraViolet else InputBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = if (isSelected) PrimeraViolet else TextPrimary,
            fontSize = 14.sp,
            textAlign = TextAlign.Start
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingComponentsPreview() {
    PrimeraTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OnboardingButton(text = "Yes", isSelected = true, onClick = {})
            OnboardingButton(text = "No", isSelected = false, onClick = {})
            OnboardingChip(text = "Vaginal", isSelected = true, onClick = {})
            OnboardingChip(text = "C-section", isSelected = false, onClick = {})
            OnboardingSelectionCard(
                text = "Pregnancy induced hypertension",
                isSelected = true,
                onClick = {}
            )
        }
    }
}







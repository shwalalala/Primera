package com.example.primera.frontend.features.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.primera.frontend.common.components.*
import com.example.primera.frontend.common.theme.*
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun OnboardingHostScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.isCompleted) {
        LaunchedEffect(Unit) {
            onOnboardingComplete()
        }
    }

    Scaffold(
        topBar = {
            if (state.currentStep != OnboardingStep.PREPARING) {
                OnboardingTopBar(
                    onBackClick = { viewModel.previousStep() },
                    currentStep = state.currentStep.ordinal + 1,
                    totalSteps = OnboardingStep.entries.size
                )
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            (BackgroundCream),
                            PrimeraLilac.copy(alpha = 0.45f)
                        )
                    )
                )
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Crossfade(targetState = state.currentStep, label = "OnboardingStep") { step ->
                when (step) {
                    OnboardingStep.NAME -> NameStep(state, viewModel)
                    OnboardingStep.BIRTHDAY -> BirthdayStep(state, viewModel)
                    OnboardingStep.WEIGHT -> WeightStep(state, viewModel)
                    OnboardingStep.HEIGHT -> HeightStep(state, viewModel)
                    OnboardingStep.LMP -> LmpStep(state, viewModel)
                    OnboardingStep.EDD -> EddStep(state, viewModel)
                    OnboardingStep.FIRST_PREGNANCY -> FirstPregnancyStep(state, viewModel)
                    OnboardingStep.PREGNANCY_HISTORY -> PregnancyHistoryStep(state, viewModel)
                    OnboardingStep.PREPARING -> PreparingStep(state)
                }
            }
        }
    }
}

@Composable
fun OnboardingTopBar(onBackClick: () -> Unit, currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(SurfaceWhite)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary
            )
        }
        Text(
            text = "$currentStep / $totalSteps",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

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
fun NameStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Tell Us Your Name",
        onContinue = { viewModel.nextStep() }
    ) {
        Box(
            modifier = Modifier.shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
        ) {
            PrimeraTextField(
                value = state.firstName,
                onValueChange = viewModel::onFirstNameChange,
                placeholder = "Firstname"
            )
        }
        Spacer(Modifier.height(16.dp))
        PrimeraTextField(
            value = state.lastName,
            onValueChange = viewModel::onLastNameChange,
            placeholder = "Lastname"
        )
        Spacer(Modifier.height(16.dp))
        PrimeraTextField(
            value = state.middleName,
            onValueChange = viewModel::onMiddleNameChange,
            placeholder = "Middlename"
        )
    }
}

@Composable
fun BirthdayStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Tell Us Your Birthday",
        onContinue = { viewModel.nextStep() }
    ) {
        // Simple Placeholder for Date Picker Wheel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceWhite)
                .border(1.dp, InputBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.birthday?.let { SimpleDateFormat("MMM dd, yyyy", Locale.US).format(it) } ?: "Select Date",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun WeightStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Tell Us Your Weight",
        onContinue = { viewModel.nextStep() }
    ) {
        // Ruler Placeholder
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${state.weightKg}kg",
                style = MaterialTheme.typography.displayMedium,
                color = PrimeraViolet
            )
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(SurfaceWhite, RoundedCornerShape(8.dp))
                    .border(1.dp, InputBorder, RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
fun HeightStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Tell Us Your Height",
        onContinue = { viewModel.nextStep() }
    ) {
        // Ruler Placeholder
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${state.heightCm}cm",
                style = MaterialTheme.typography.displayMedium,
                color = PrimeraViolet
            )
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(SurfaceWhite, RoundedCornerShape(8.dp))
                    .border(1.dp, InputBorder, RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
fun LmpStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Enter the Start Date of Your Last Period?",
        onContinue = { viewModel.nextStep() }
    ) {
        // Calendar Placeholder (using state)
        val dummy = state.lmpDate.hashCode()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(SurfaceWhite, RoundedCornerShape(16.dp))
                .border(1.dp, InputBorder, RoundedCornerShape(16.dp))
        )
    }
}

@Composable
fun EddStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "When is your Expected Date of Delivery?",
        onContinue = { viewModel.nextStep() }
    ) {
        // Calendar Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(SurfaceWhite, RoundedCornerShape(16.dp))
                .border(1.dp, InputBorder, RoundedCornerShape(16.dp))
        )
    }
}

@Composable
fun FirstPregnancyStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Is this your first pregnancy?",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))
        
        OnboardingButton(
            text = "Yes",
            isSelected = state.isFirstPregnancy == true,
            onClick = { viewModel.onIsFirstPregnancyChange(true) }
        )
        Spacer(Modifier.height(16.dp))
        OnboardingButton(
            text = "No",
            isSelected = state.isFirstPregnancy == false,
            onClick = { viewModel.onIsFirstPregnancyChange(false) }
        )
        
        Spacer(Modifier.height(48.dp))
        if (state.isFirstPregnancy != null) {
            PrimeraGradientButton(text = "Continue", onClick = { viewModel.nextStep() })
        }
    }
}

@Composable
fun OnboardingButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
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
fun PregnancyHistoryStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Pregnancy History",
        onContinue = { viewModel.nextStep() }
    ) {
        Text(
            text = "Pregnancy Number",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.Start)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..5).forEach { num ->
                val isSel = state.pregnancyNumber == num
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isSel) PrimeraViolet else SurfaceWhite)
                        .border(1.dp, if (isSel) PrimeraViolet else InputBorder, CircleShape)
                        .clickable { viewModel.onPregnancyNumberChange(num) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(num.toString(), color = if (isSel) SurfaceWhite else TextPrimary)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        
        LabeledField(
            label = "Date of Delivery",
            value = state.historyDeliveryDate?.toString() ?: "",
            onValueChange = {},
            placeholder = "Select Date"
        )
        
        Spacer(Modifier.height(16.dp))
        Text("Type of Delivery", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.Start))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OnboardingChip("Vaginal", state.deliveryType == "Vaginal") { viewModel.onDeliveryTypeChange("Vaginal") }
            OnboardingChip("C-section", state.deliveryType == "C-section") { viewModel.onDeliveryTypeChange("C-section") }
        }
        
        Spacer(Modifier.height(16.dp))
        Text("No. of Child/Children Delivered", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.Start))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OnboardingChip("Single", state.childrenDelivered == "Single") { viewModel.onChildrenDeliveredChange("Single") }
            OnboardingChip("Twins", state.childrenDelivered == "Twins") { viewModel.onChildrenDeliveredChange("Twins") }
            OnboardingChip("Multiple", state.childrenDelivered == "Multiple") { viewModel.onChildrenDeliveredChange("Multiple") }
        }

        Spacer(Modifier.height(16.dp))
        Text("Pregnancy-related Complications/Conditions:", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.Start))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val complications = listOf(
                "Pregnancy induced hypertension",
                "Preeclampsia / Eclampsia",
                "Bleeding during pregnancy or after delivery"
            )
            complications.forEach { comp ->
                OnboardingSelectionCard(
                    text = comp,
                    isSelected = state.complications.contains(comp),
                    onClick = { viewModel.toggleComplication(comp) }
                )
            }
        }
    }
}

@Composable
fun OnboardingSelectionCard(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
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

@Composable
fun OnboardingChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(40.dp)
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
fun PreparingStep(state: OnboardingState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Preparing your\npersonal calendar...",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(64.dp))
        
        Box(contentAlignment = Alignment.Center) {
            val progress = state.preparationProgress
            val strokeWidth = with(LocalDensity.current) { 12.dp.toPx() }
            Canvas(modifier = Modifier.size(200.dp)) {
                drawArc(
                    color = Color(0xFFE0E0E0),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = PrimeraViolet,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.displayLarge,
                color = PrimeraViolet
            )
        }
    }
}

@Preview(widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    PrimeraTheme {
        OnboardingHostScreen(
            onOnboardingComplete = { }
        )
    }
}


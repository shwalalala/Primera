package com.example.primera.feature.onboarding.ui

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.primera.core.di.ViewModelProvider
import com.example.primera.core.theme.*
import com.example.primera.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OnboardingHostScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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

// ...existing OnboardingLayout is now part of ui/components/OnboardingComponents.kt

@Composable
fun NameStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
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
fun BirthdayStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Tell Us Your Birthday",
        onContinue = { viewModel.nextStep() }
    ) {
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
fun WeightStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Tell Us Your Weight",
        onContinue = { viewModel.nextStep() }
    ) {
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
fun HeightStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Tell Us Your Height",
        onContinue = { viewModel.nextStep() }
    ) {
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
fun LmpStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Enter the Start Date of Your Last Period?",
        onContinue = { viewModel.nextStep() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(SurfaceWhite, RoundedCornerShape(16.dp))
                .border(1.dp, InputBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.lmpDate?.let { SimpleDateFormat("MMM dd, yyyy", Locale.US).format(it) } ?: "Select Date",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun EddStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "When is your Expected Date of Delivery?",
        onContinue = { viewModel.nextStep() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(SurfaceWhite, RoundedCornerShape(16.dp))
                .border(1.dp, InputBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.eddDate?.let { SimpleDateFormat("MMM dd, yyyy", Locale.US).format(it) } ?: "Select Date",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun FirstPregnancyStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
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

// OnboardingButton is now extracted to ui/components/OnboardingComponents.kt

@Composable
fun PregnancyHistoryStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
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

// OnboardingSelectionCard is now extracted to ui/components/OnboardingComponents.kt

// OnboardingChip is now extracted to ui/components/OnboardingComponents.kt

@Composable
fun PreparingStep(state: OnboardingUiState) {
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
                    color = OnboardingRingTrack,
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

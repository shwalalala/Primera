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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.primera.core.di.ViewModelProvider
import com.example.primera.core.theme.*
import com.example.primera.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
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
                            BackgroundCream,
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceWhite)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(totalSteps) { index ->
                val isCompleted = index < currentStep
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isCompleted) PrimeraViolet else SurfaceWhite)
                )
            }
        }

        Text(
            text = "$currentStep / $totalSteps",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            ),
            color = TextSecondary
        )
    }
}

@Composable
fun OnboardingLayout(
    title: String,
    subtitle: String? = null,
    onContinue: () -> Unit,
    isContinueEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        PrimeraTitle(
            text = title,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(12.dp))
            PrimeraLabel(
                text = subtitle,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(48.dp))
        content()
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        PrimeraGradientButton(
            text = "Continue",
            enabled = isContinueEnabled,
            onClick = {
                keyboardController?.hide()
                onContinue()
            }
        )
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun NameStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Tell Us Your Name",
        onContinue = { viewModel.nextStep() },
        isContinueEnabled = state.firstName.isNotBlank() && state.lastName.isNotBlank()
    ) {
        LabeledField(
            label = "First Name",
            value = state.firstName,
            onValueChange = { viewModel.onFirstNameChange(it) },
            placeholder = "Firstname"
        )
        Spacer(Modifier.height(16.dp))
        LabeledField(
            label = "Last Name",
            value = state.lastName,
            onValueChange = { viewModel.onLastNameChange(it) },
            placeholder = "Lastname"
        )
        Spacer(Modifier.height(16.dp))
        LabeledField(
            label = "Middle Name (Optional)",
            value = state.middleName,
            onValueChange = { viewModel.onMiddleNameChange(it) },
            placeholder = "Middlename"
        )
    }
}

@Composable
fun BirthdayStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Tell Us Your Birthday",
        onContinue = { viewModel.nextStep() },
        isContinueEnabled = state.birthday != null
    ) {
        PrimeraDatePicker(
            selectedDate = state.birthday,
            onDateSelected = { viewModel.onBirthdayChange(it) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun WeightStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Tell Us Your Weight",
        onContinue = { viewModel.nextStep() },
        isContinueEnabled = state.weightKg > 0
    ) {
        LabeledField(
            label = "Weight (kg)",
            value = if (state.weightKg == 0) "" else state.weightKg.toString(),
            onValueChange = { value -> 
                val weight = value.toIntOrNull() ?: 0
                viewModel.onWeightChange(weight) 
            },
            placeholder = "Enter weight in kg",
            keyboardType = KeyboardType.Number
        )
    }
}

@Composable
fun HeightStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Tell Us Your Height",
        onContinue = { viewModel.nextStep() },
        isContinueEnabled = state.heightCm > 0
    ) {
        LabeledField(
            label = "Height (cm)",
            value = if (state.heightCm == 0) "" else state.heightCm.toString(),
            onValueChange = { value -> 
                val height = value.toIntOrNull() ?: 0
                viewModel.onHeightChange(height)
            },
            placeholder = "Enter height in cm",
            keyboardType = KeyboardType.Number
        )
    }
}

@Composable
fun LmpStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Enter the Start Date of Your Last Period?",
        onContinue = { viewModel.nextStep() },
        isContinueEnabled = state.lmpDate != null
    ) {
        PrimeraDatePicker(
            selectedDate = state.lmpDate,
            onDateSelected = { viewModel.onLmpDateChange(it) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EddStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "When is your Expected Date of Delivery?",
        onContinue = { viewModel.nextStep() },
        isContinueEnabled = state.eddDate != null
    ) {
        PrimeraDatePicker(
            selectedDate = state.eddDate,
            onDateSelected = { viewModel.onEddDateChange(it) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FirstPregnancyStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PrimeraTitle(
            text = "Is this your first pregnancy?",
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))
        
        PrimeraOptionButton(
            text = "Yes",
            isSelected = state.isFirstPregnancy == true,
            onClick = { viewModel.onIsFirstPregnancyChange(true) }
        )
        Spacer(Modifier.height(16.dp))
        PrimeraOptionButton(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyHistoryStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.historyDeliveryDate?.time ?: System.currentTimeMillis()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onHistoryDeliveryDateChange(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK", color = PrimeraViolet) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    OnboardingLayout(
        title = "Pregnancy History",
        onContinue = { viewModel.nextStep() },
        isContinueEnabled = state.historyDeliveryDate != null && 
                          state.deliveryType.isNotBlank() && 
                          state.childrenDelivered.isNotBlank()
    ) {
        PrimeraSubheader(
            text = "Pregnancy Number",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
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
        Spacer(Modifier.height(20.dp))
        
        PrimeraLabel(
            text = "Date of Delivery",
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp).align(Alignment.Start)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceWhite)
                .border(1.dp, InputBorder, RoundedCornerShape(24.dp))
                .clickable { showDatePicker = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = state.historyDeliveryDate?.let { SimpleDateFormat("MMM dd, yyyy", Locale.US).format(it) } ?: "Select Date",
                color = if (state.historyDeliveryDate == null) TextHint else TextPrimary
            )
        }
        
        Spacer(Modifier.height(40.dp))
        PrimeraLabel("Type of Delivery", modifier = Modifier.align(Alignment.Start))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimeraOptionChip("Vaginal", state.deliveryType == "Vaginal") { viewModel.onDeliveryTypeChange("Vaginal") }
            PrimeraOptionChip("C-section", state.deliveryType == "C-section") { viewModel.onDeliveryTypeChange("C-section") }
        }

        Spacer(Modifier.height(40.dp))
        PrimeraLabel("No. of Child/Children Delivered", modifier = Modifier.align(Alignment.Start))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimeraOptionChip("Single", state.childrenDelivered == "Single") { viewModel.onChildrenDeliveredChange("Single") }
            PrimeraOptionChip("Twins", state.childrenDelivered == "Twins") { viewModel.onChildrenDeliveredChange("Twins") }
            PrimeraOptionChip("Multiple", state.childrenDelivered == "Multiple") { viewModel.onChildrenDeliveredChange("Multiple") }
        }

        Spacer(Modifier.height(40.dp))
        PrimeraLabel("Pregnancy-related Complications/Conditions:", modifier = Modifier.align(Alignment.Start))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val complications = listOf(
                "Pregnancy induced hypertension",
                "Preeclampsia / Eclampsia",
                "Bleeding during pregnancy or after delivery"
            )
            complications.forEach { comp ->
                PrimeraOptionCard(
                    text = comp,
                    isSelected = state.complications.contains(comp),
                    onClick = { viewModel.toggleComplication(comp) }
                )
            }
        }
    }
}

@Composable
fun PreparingStep(state: OnboardingState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PrimeraTitle(
            text = "Preparing your\npersonal calendar...",
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

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding - Name Step")
@Composable
private fun NameStepPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.background(BackgroundCream)) {
            NameStep(
                state = OnboardingState(currentStep = OnboardingStep.NAME),
                viewModel = viewModel()
            )
        }
    }
}

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding - Birthday Step")
@Composable
private fun BirthdayStepPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.background(BackgroundCream)) {
            BirthdayStep(
                state = OnboardingState(currentStep = OnboardingStep.BIRTHDAY),
                viewModel = viewModel()
            )
        }
    }
}

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding - Weight Step")
@Composable
private fun WeightStepPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.background(BackgroundCream)) {
            WeightStep(
                state = OnboardingState(currentStep = OnboardingStep.WEIGHT),
                viewModel = viewModel()
            )
        }
    }
}

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding - LMP Step")
@Composable
private fun LmpStepPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.background(BackgroundCream)) {
            LmpStep(
                state = OnboardingState(currentStep = OnboardingStep.LMP),
                viewModel = viewModel()
            )
        }
    }
}

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding - Pregnancy History")
@Composable
private fun PregnancyHistoryPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.background(BackgroundCream)) {
            PregnancyHistoryStep(
                state = OnboardingState(currentStep = OnboardingStep.PREGNANCY_HISTORY),
                viewModel = viewModel()
            )
        }
    }
}

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding - Preparing")
@Composable
private fun PreparingStepPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.background(BackgroundCream)) {
            PreparingStep(
                OnboardingState(
                    currentStep = OnboardingStep.PREPARING,
                    preparationProgress = 0.65f
                )
            )
        }
    }
}

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding Host")
@Composable
private fun OnboardingScreenPreview() {
    PrimeraTheme {
        OnboardingHostScreen(
            onOnboardingComplete = { }
        )
    }
}

package cit.edu.primera.feature.onboarding.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Close
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
import cit.edu.primera.core.di.ViewModelProvider
import cit.edu.primera.core.theme.*
import cit.edu.primera.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OnboardingHostScreen(
    onOnboardingComplete: () -> Unit,
    onBack: () -> Unit = {},
    viewModel: OnboardingViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isCompleted) {
        LaunchedEffect(Unit) {
            onOnboardingComplete()
        }
    }

    if (state.showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissConfirmation() },
            title = { Text("Confirm Information") },
            text = { Text("Are you sure all the information provided is correct? You can't change some of these details later.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmAndSave() }) {
                    Text("Confirm", color = PrimeraViolet)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConfirmation() }) {
                    Text("Cancel")
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    OnboardingScaffoldLayout(
        state = state,
        onBack = {
            if (state.currentStep == OnboardingStep.BIRTHDAY) {
                onBack()
            } else {
                viewModel.previousStep()
            }
        },
        stepContent = { step ->
            when (step) {
                OnboardingStep.BIRTHDAY -> BirthdayStep(
                    birthday = state.birthday,
                    onBirthdayChange = { viewModel.onBirthdayChange(it) },
                    onContinue = { viewModel.nextStep() }
                )
                OnboardingStep.WEIGHT -> WeightStep(
                    weightKg = state.weightKg,
                    onWeightChange = { viewModel.onWeightChange(it) },
                    onContinue = { viewModel.nextStep() }
                )
                OnboardingStep.HEIGHT -> HeightStep(
                    heightCm = state.heightCm,
                    onHeightChange = { viewModel.onHeightChange(it) },
                    onContinue = { viewModel.nextStep() }
                )
                OnboardingStep.CYCLE_REGULARITY -> CycleRegularityStep(
                    isCycleRegular = state.isCycleRegular,
                    onIsCycleRegularChange = { viewModel.onIsCycleRegularChange(it) },
                    onContinue = { viewModel.nextStep() }
                )
                OnboardingStep.CYCLE_VARIANCE -> CycleVarianceStep(
                    shortestCycleDays = state.shortestCycleDays,
                    longestCycleDays = state.longestCycleDays,
                    onShortestCycleChange = { viewModel.onShortestCycleChange(it) },
                    onLongestCycleChange = { viewModel.onLongestCycleChange(it) },
                    onContinue = { viewModel.nextStep() }
                )
                OnboardingStep.HAD_ULTRASOUND -> HadUltrasoundStep(
                    hasHadUltrasound = state.hasHadUltrasound,
                    onHasHadUltrasoundChange = { viewModel.onHasHadUltrasoundChange(it) },
                    onContinue = {
                        if (state.hasHadUltrasound == true) viewModel.goToUltrasound()
                        else viewModel.nextStep()
                    }
                )
                OnboardingStep.LMP -> LmpStep(
                    lmpDate = state.lmpDate,
                    onLmpDateChange = { viewModel.onLmpDateChange(it) },
                    onGoToUltrasound = { viewModel.goToUltrasound() },
                    onContinue = { viewModel.nextStep() }
                )
                OnboardingStep.ULTRASOUND -> UltrasoundStep(
                    scanDate = state.scanDate,
                    isRevisedEdd = state.isRevisedEdd,
                    scanWeeks = state.scanWeeks,
                    scanDays = state.scanDays,
                    eddDate = state.eddDate,
                    onScanDateChange = { viewModel.onScanDateChange(it) },
                    onIsRevisedEddChange = { viewModel.onIsRevisedEddChange(it) },
                    onScanWeeksChange = { viewModel.onScanWeeksChange(it) },
                    onScanDaysChange = { viewModel.onScanDaysChange(it) },
                    onEddDateChange = { viewModel.onEddDateChange(it) },
                    onContinue = { viewModel.nextStep() }
                )
                OnboardingStep.EDD -> EddStep(
                    isTentative = state.isCycleRegular == false && state.hasHadUltrasound == false,
                    positiveTestDate = state.positiveTestDate,
                    eddDate = state.eddDate,
                    onPositiveTestDateChange = { viewModel.onPositiveTestDateChange(it) },
                    onEddDateChange = { viewModel.onEddDateChange(it) },
                    onContinue = { viewModel.nextStep() }
                )
                OnboardingStep.FIRST_PREGNANCY -> FirstPregnancyStep(
                    isFirstPregnancy = state.isFirstPregnancy,
                    onIsFirstPregnancyChange = { viewModel.onIsFirstPregnancyChange(it) },
                    onContinue = { viewModel.nextStep() }
                )
                OnboardingStep.PREGNANCY_HISTORY -> PregnancyHistoryStep(
                    state = state,
                    onSelectPregnancy = { viewModel.onSelectPregnancy(it) },
                    onRemovePregnancy = { viewModel.removePregnancy(it) },
                    onAddPregnancy = { viewModel.addPregnancy() },
                    onDeliveryDateChange = { viewModel.onHistoryDeliveryDateChange(it) },
                    onDeliveryTypeChange = { viewModel.onDeliveryTypeChange(it) },
                    onChildrenDeliveredChange = { viewModel.onChildrenDeliveredChange(it) },
                    onAreChildrenLivingChange = { viewModel.onAreChildrenLivingChange(it) },
                    onToggleStructuredComplication = { viewModel.toggleStructuredComplication(it) },
                    onToggleComplication = { viewModel.toggleComplication(it) },
                    onAdditionalComplicationsChange = { viewModel.onAdditionalComplicationsChange(it) },
                    onContinue = { viewModel.nextStep() }
                )
                OnboardingStep.REVIEW_SUMMARY -> ReviewSummaryStep(
                    state = state,
                    onSubmit = { viewModel.submitOnboarding() }
                )
                OnboardingStep.PREPARING -> PreparingStep(
                    progress = state.preparationProgress
                )
            }
        }
    )
}

@Composable
fun OnboardingScaffoldLayout(
    state: OnboardingState,
    onBack: () -> Unit,
    stepContent: @Composable (OnboardingStep) -> Unit
) {
    Box(
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
    ) {
        Scaffold(
            topBar = {
                if (state.currentStep != OnboardingStep.PREPARING) {
                    OnboardingTopBar(
                        onBackClick = onBack,
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
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Crossfade(targetState = state.currentStep, label = "OnboardingStep") { step ->
                    stepContent(step)
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
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
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
            Spacer(Modifier.height(24.dp))
        }

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
fun BirthdayStep(
    birthday: Date?,
    onBirthdayChange: (Date) -> Unit,
    onContinue: () -> Unit
) {
    OnboardingLayout(
        title = "Tell Us Your Birthday",
        onContinue = onContinue,
        isContinueEnabled = birthday != null
    ) {
        val calendar = Calendar.getInstance()
        val minYear = calendar.get(Calendar.YEAR) - 100
        val maxYear = calendar.get(Calendar.YEAR) - 13

        Row(verticalAlignment = Alignment.CenterVertically) {
            PrimeraLabel("Select Date")
            Text(" *", color = ErrorRed, style = MaterialTheme.typography.labelSmall)
        }

        PrimeraDatePicker(
            selectedDate = birthday,
            onDateSelected = onBirthdayChange,
            yearRange = minYear..maxYear,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun WeightStep(
    weightKg: Int,
    onWeightChange: (Int) -> Unit,
    onContinue: () -> Unit
) {
    OnboardingLayout(
        title = "Pre-pregnancy Weight",
        onContinue = onContinue,
        isContinueEnabled = weightKg in 30..250
    ) {
        LabeledField(
            label = "Weight before pregnancy (kg) *",
            value = if (weightKg == 0) "" else weightKg.toString(),
            onValueChange = { value ->
                val weight = value.filter { it.isDigit() }.toIntOrNull() ?: 0
                onWeightChange(weight)
            },
            placeholder = "Enter weight in kg",
            keyboardType = KeyboardType.Number
        )
    }
}

@Composable
fun HeightStep(
    heightCm: Int,
    onHeightChange: (Int) -> Unit,
    onContinue: () -> Unit
) {
    OnboardingLayout(
        title = "Your Height",
        onContinue = onContinue,
        isContinueEnabled = heightCm in 50..250
    ) {
        LabeledField(
            label = "Height (cm) *",
            value = if (heightCm == 0) "" else heightCm.toString(),
            onValueChange = { value ->
                val height = value.filter { it.isDigit() }.toIntOrNull() ?: 0
                onHeightChange(height)
            },
            placeholder = "Enter height in cm",
            keyboardType = KeyboardType.Number
        )
    }
}

@Composable
fun CycleRegularityStep(
    isCycleRegular: Boolean?,
    onIsCycleRegularChange: (Boolean) -> Unit,
    onContinue: () -> Unit
) {
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
            text = "Is your menstrual cycle regular?",
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        PrimeraLabel(
            text = "Regular cycles usually last about the same number of days each month.",
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))

        PrimeraOptionButton(
            text = "Regular",
            isSelected = isCycleRegular == true,
            onClick = { onIsCycleRegularChange(true) }
        )
        Spacer(Modifier.height(16.dp))
        PrimeraOptionButton(
            text = "Irregular",
            isSelected = isCycleRegular == false,
            onClick = { onIsCycleRegularChange(false) }
        )

        Spacer(Modifier.height(48.dp))
        if (isCycleRegular != null) {
            PrimeraGradientButton(text = "Continue", onClick = onContinue)
        }
    }
}

@Composable
fun CycleVarianceStep(
    shortestCycleDays: Int,
    longestCycleDays: Int,
    onShortestCycleChange: (Int) -> Unit,
    onLongestCycleChange: (Int) -> Unit,
    onContinue: () -> Unit
) {
    OnboardingLayout(
        title = "Your Cycle Variance",
        subtitle = "Tell us your shortest and longest cycle lengths over the past 3 to 6 months.",
        onContinue = onContinue,
        isContinueEnabled = shortestCycleDays in 15..50 && longestCycleDays in 15..90
    ) {
        LabeledField(
            label = "Shortest Cycle (days) *",
            value = if (shortestCycleDays == 0) "" else shortestCycleDays.toString(),
            onValueChange = { value ->
                val days = value.filter { it.isDigit() }.toIntOrNull() ?: 0
                onShortestCycleChange(days)
            },
            placeholder = "e.g. 25",
            keyboardType = KeyboardType.Number
        )
        Spacer(Modifier.height(24.dp))
        LabeledField(
            label = "Longest Cycle (days) *",
            value = if (longestCycleDays == 0) "" else longestCycleDays.toString(),
            onValueChange = { value ->
                val days = value.filter { it.isDigit() }.toIntOrNull() ?: 0
                onLongestCycleChange(days)
            },
            placeholder = "e.g. 35",
            keyboardType = KeyboardType.Number
        )
    }
}

@Composable
fun HadUltrasoundStep(
    hasHadUltrasound: Boolean?,
    onHasHadUltrasoundChange: (Boolean) -> Unit,
    onContinue: () -> Unit
) {
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
            text = "Have you had a dating ultrasound yet?",
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        PrimeraLabel(
            text = "For irregular cycles, an early ultrasound is the most accurate way to determine your due date.",
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))

        PrimeraOptionButton(
            text = "Yes, I've had one",
            isSelected = hasHadUltrasound == true,
            onClick = { onHasHadUltrasoundChange(true) }
        )
        Spacer(Modifier.height(16.dp))
        PrimeraOptionButton(
            text = "No, not yet",
            isSelected = hasHadUltrasound == false,
            onClick = { onHasHadUltrasoundChange(false) }
        )

        Spacer(Modifier.height(48.dp))
        if (hasHadUltrasound != null) {
            PrimeraGradientButton(
                text = "Continue",
                onClick = onContinue
            )
        }
    }
}

@Composable
fun LmpStep(
    lmpDate: Date?,
    onLmpDateChange: (Date) -> Unit,
    onGoToUltrasound: () -> Unit,
    onContinue: () -> Unit
) {
    OnboardingLayout(
        title = "When was the First Day of your Last Period?",
        onContinue = onContinue,
        isContinueEnabled = lmpDate != null
    ) {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        Row(verticalAlignment = Alignment.CenterVertically) {
            PrimeraLabel("Select Date")
            Text(" *", color = ErrorRed, style = MaterialTheme.typography.labelSmall)
        }

        PrimeraDatePicker(
            selectedDate = lmpDate,
            onDateSelected = onLmpDateChange,
            yearRange = (currentYear - 1)..currentYear,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        PrimeraOutlinedButton(
            text = "Already have your ultrasound?",
            onClick = onGoToUltrasound
        )
    }
}

@Composable
fun UltrasoundStep(
    scanDate: Date?,
    isRevisedEdd: Boolean,
    scanWeeks: Int,
    scanDays: Int,
    eddDate: Date?,
    onScanDateChange: (Date) -> Unit,
    onIsRevisedEddChange: (Boolean) -> Unit,
    onScanWeeksChange: (Int) -> Unit,
    onScanDaysChange: (Int) -> Unit,
    onEddDateChange: (Date) -> Unit,
    onContinue: () -> Unit
) {
    val isContinueEnabled = if (scanDate != null) {
        if (isRevisedEdd) eddDate != null
        else (scanWeeks > 0 || scanDays > 0)
    } else false

    OnboardingLayout(
        title = "Ultrasound Details",
        onContinue = onContinue,
        isContinueEnabled = isContinueEnabled
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PrimeraLabel("Date the ultrasound was performed")
            Text(" *", color = ErrorRed, style = MaterialTheme.typography.labelSmall)
        }
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        PrimeraDatePicker(
            selectedDate = scanDate,
            onDateSelected = onScanDateChange,
            yearRange = (currentYear - 1)..currentYear,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        Text(
            text = "What information was provided? *",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimeraOptionChip("Gestational Age", !isRevisedEdd) { onIsRevisedEddChange(false) }
            PrimeraOptionChip("Revised Due Date", isRevisedEdd) { onIsRevisedEddChange(true) }
        }

        Spacer(Modifier.height(24.dp))

        if (!isRevisedEdd) {
            PrimeraLabel("Gestational Age at scan *")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LabeledField(
                    label = "Weeks",
                    value = if (scanWeeks == 0) "" else scanWeeks.toString(),
                    onValueChange = { onScanWeeksChange(it.toIntOrNull() ?: 0) },
                    placeholder = "0",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
                LabeledField(
                    label = "Days",
                    value = if (scanDays == 0) "" else scanDays.toString(),
                    onValueChange = { onScanDaysChange(it.toIntOrNull() ?: 0) },
                    placeholder = "0",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
            }
        } else {
            PrimeraLabel("Revised Due Date *")
            PrimeraDatePicker(
                selectedDate = eddDate,
                onDateSelected = onEddDateChange,
                yearRange = currentYear..(currentYear + 1),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun EddStep(
    isTentative: Boolean,
    positiveTestDate: Date?,
    eddDate: Date?,
    onPositiveTestDateChange: (Date) -> Unit,
    onEddDateChange: (Date) -> Unit,
    onContinue: () -> Unit
) {
    OnboardingLayout(
        title = if (isTentative) "Tentative Due Date" else "When is your Expected Date of Delivery?",
        subtitle = if (isTentative) "Since you have irregular cycles and no scan yet, this is a placeholder estimate." else null,
        onContinue = onContinue,
        isContinueEnabled = eddDate != null && (!isTentative || positiveTestDate != null)
    ) {
        if (isTentative) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = PrimeraViolet.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, PrimeraViolet.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = PrimeraViolet,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Your provider will likely update this after your first-trimester scan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                PrimeraLabel("Date of positive pregnancy test")
                Text(" *", color = ErrorRed, style = MaterialTheme.typography.labelSmall)
            }
            val calendar = Calendar.getInstance()
            PrimeraDatePicker(
                selectedDate = positiveTestDate,
                onDateSelected = onPositiveTestDateChange,
                yearRange = (calendar.get(Calendar.YEAR) - 1)..calendar.get(Calendar.YEAR),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            PrimeraLabel(if (isTentative) "Estimated Due Date" else "Expected Date of Delivery")
            Text(" *", color = ErrorRed, style = MaterialTheme.typography.labelSmall)
        }
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        PrimeraDatePicker(
            selectedDate = eddDate,
            onDateSelected = onEddDateChange,
            yearRange = currentYear..(currentYear + 1),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FirstPregnancyStep(
    isFirstPregnancy: Boolean?,
    onIsFirstPregnancyChange: (Boolean) -> Unit,
    onContinue: () -> Unit
) {
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
            isSelected = isFirstPregnancy == true,
            onClick = { onIsFirstPregnancyChange(true) }
        )
        Spacer(Modifier.height(16.dp))
        PrimeraOptionButton(
            text = "No",
            isSelected = isFirstPregnancy == false,
            onClick = { onIsFirstPregnancyChange(false) }
        )

        Spacer(Modifier.height(48.dp))
        if (isFirstPregnancy != null) {
            PrimeraGradientButton(text = "Continue", onClick = onContinue)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyHistoryStep(
    state: OnboardingState,
    onSelectPregnancy: (Int) -> Unit,
    onRemovePregnancy: (Int) -> Unit,
    onAddPregnancy: () -> Unit,
    onDeliveryDateChange: (Date) -> Unit,
    onDeliveryTypeChange: (String) -> Unit,
    onChildrenDeliveredChange: (String) -> Unit,
    onAreChildrenLivingChange: (Boolean) -> Unit,
    onToggleStructuredComplication: (String) -> Unit,
    onToggleComplication: (String) -> Unit,
    onAdditionalComplicationsChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val currentPregnancy = state.currentPregnancy

    if (showDatePicker) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {},
            title = { Text("Select Delivery Date") },
            text = {
                PrimeraDatePicker(
                    selectedDate = currentPregnancy.deliveryDate,
                    onDateSelected = { date ->
                        onDeliveryDateChange(date)
                        showDatePicker = false
                    },
                    yearRange = (currentYear - 20)..currentYear,
                    selectableDates = object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                            return utcTimeMillis <= System.currentTimeMillis()
                        }
                        override fun isSelectableYear(year: Int): Boolean {
                            return year in (currentYear - 20)..currentYear
                        }
                    }
                )
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    OnboardingLayout(
        title = "Pregnancy History",
        onContinue = onContinue,
        isContinueEnabled = state.pregnancyHistories.all { state.isPregnancyComplete(state.pregnancyHistories.indexOf(it)) }
    ) {
        PrimeraSubheader(
            text = "Pregnancy Number",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            state.pregnancyHistories.forEachIndexed { index, history ->
                val isSel = state.selectedPregnancyIndex == index
                val isCompleted = state.isPregnancyComplete(index)

                Box(contentAlignment = Alignment.TopEnd) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSel) PrimeraViolet else if (isCompleted) PrimeraViolet.copy(alpha = 0.2f) else SurfaceWhite)
                            .border(1.dp, if (isSel) PrimeraViolet else InputBorder, CircleShape)
                            .clickable { onSelectPregnancy(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = history.pregnancyNumber.toString(),
                            color = if (isSel) SurfaceWhite else TextPrimary
                        )
                    }

                    if (state.pregnancyHistories.size > 1) {
                        Surface(
                            modifier = Modifier
                                .size(16.dp)
                                .offset(x = 4.dp, y = (-4).dp)
                                .clickable { onRemovePregnancy(index) },
                            shape = CircleShape,
                            color = ErrorRed,
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.padding(2.dp))
                        }
                    }
                }
            }

            val canAdd = state.isPregnancyComplete(state.pregnancyHistories.lastIndex) && state.pregnancyHistories.size < 10
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (canAdd) PrimeraViolet.copy(alpha = 0.1f) else SurfaceWhite.copy(alpha = 0.5f))
                    .border(1.dp, if (canAdd) PrimeraViolet else InputBorder.copy(alpha = 0.5f), CircleShape)
                    .clickable(enabled = canAdd) { onAddPregnancy() },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = if (canAdd) PrimeraViolet else TextHint, fontSize = 20.sp)
            }
        }

        Spacer(Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.Start)) {
            PrimeraLabel(
                text = "Date of Delivery",
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            Text(" *", color = ErrorRed, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 6.dp))
        }
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
                text = currentPregnancy.deliveryDate?.let { SimpleDateFormat("MMM dd, yyyy", Locale.US).format(it) } ?: "Select Date",
                color = if (currentPregnancy.deliveryDate == null) TextHint else TextPrimary
            )
        }

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.Start)) {
            PrimeraLabel("Type of Delivery")
            Text(" *", color = ErrorRed, style = MaterialTheme.typography.labelSmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimeraOptionChip("Vaginal", currentPregnancy.deliveryType == "Vaginal") { onDeliveryTypeChange("Vaginal") }
            PrimeraOptionChip("C-section", currentPregnancy.deliveryType == "C-section") { onDeliveryTypeChange("C-section") }
        }

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.Start)) {
            PrimeraLabel("No. of Child/Children Delivered")
            Text(" *", color = ErrorRed, style = MaterialTheme.typography.labelSmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimeraOptionChip("Single", currentPregnancy.childrenDelivered == "Single") { onChildrenDeliveredChange("Single") }
            PrimeraOptionChip("Twins", currentPregnancy.childrenDelivered == "Twins") { onChildrenDeliveredChange("Twins") }
            PrimeraOptionChip("Multiple", currentPregnancy.childrenDelivered == "Multiple") { onChildrenDeliveredChange("Multiple") }
        }

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.Start)) {
            PrimeraLabel("Are previous children living?")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimeraOptionChip("Yes", currentPregnancy.areChildrenLiving == true) { onAreChildrenLivingChange(true) }
            PrimeraOptionChip("No / Deceased", currentPregnancy.areChildrenLiving == false) { onAreChildrenLivingChange(false) }
        }

        Spacer(Modifier.height(24.dp))
        PrimeraLabel("Pregnancy-related Complications/Conditions:", modifier = Modifier.align(Alignment.Start))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimeraOptionCard(
                text = "Gestational Diabetes (GDM)",
                isSelected = currentPregnancy.hasGdm,
                onClick = { onToggleStructuredComplication("GDM") }
            )
            PrimeraOptionCard(
                text = "Pre-existing Diabetes",
                isSelected = currentPregnancy.hasDiabetes,
                onClick = { onToggleStructuredComplication("Diabetes") }
            )
            PrimeraOptionCard(
                text = "Pre-eclampsia / Eclampsia",
                isSelected = currentPregnancy.hasPreeclampsia,
                onClick = { onToggleStructuredComplication("Pre-eclampsia") }
            )
            PrimeraOptionCard(
                text = "Preterm Delivery",
                isSelected = currentPregnancy.hasPretermDelivery,
                onClick = { onToggleStructuredComplication("Preterm delivery") }
            )
            val legacyComplications = listOf(
                "Pregnancy induced hypertension",
                "Bleeding during pregnancy or after delivery"
            )
            legacyComplications.forEach { comp ->
                PrimeraOptionCard(
                    text = comp,
                    isSelected = currentPregnancy.complications.contains(comp),
                    onClick = { onToggleComplication(comp) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        LabeledField(
            label = "Additional Delivery Complications (Open-ended)",
            value = currentPregnancy.additionalComplications,
            onValueChange = onAdditionalComplicationsChange,
            placeholder = "Specify any other complications or details..."
        )
    }
}

@Composable
fun ReviewSummaryStep(
    state: OnboardingState,
    onSubmit: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(16.dp))
        PrimeraTitle(
            text = "Review Your Details",
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        PrimeraLabel(
            text = "Please review your information before submitting.",
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, InputBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryRow("Birthday", state.birthday?.let { dateFormat.format(it) } ?: "Not set")
                SummaryRow("Pre-pregnancy Weight", if (state.weightKg > 0) "${state.weightKg} kg" else "Not set")
                SummaryRow("Height", if (state.heightCm > 0) "${state.heightCm} cm" else "Not set")
                SummaryRow("Cycle Regularity", when(state.isCycleRegular) { true -> "Regular"; false -> "Irregular"; else -> "Unknown" })
                SummaryRow("Expected Due Date", state.eddDate?.let { dateFormat.format(it) } ?: "Not set")
                SummaryRow("Pregnancy Type", if (state.isFirstPregnancy == true) "First Pregnancy" else "Previous Pregnancies Recorded (${state.pregnancyHistories.size})")
            }
        }

        Spacer(Modifier.height(32.dp))

        PrimeraGradientButton(
            text = "Submit & Start",
            onClick = onSubmit
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PreparingStep(progress: Float) {
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

// ================= PREVIEWS =================

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding - Birthday Step")
@Composable
private fun BirthdayStepPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.background(BackgroundCream)) {
            BirthdayStep(
                birthday = null,
                onBirthdayChange = {},
                onContinue = {}
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
                weightKg = 60,
                onWeightChange = {},
                onContinue = {}
            )
        }
    }
}

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding - Height Step")
@Composable
private fun HeightStepPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.background(BackgroundCream)) {
            HeightStep(
                heightCm = 165,
                onHeightChange = {},
                onContinue = {}
            )
        }
    }
}

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding - Cycle Regularity")
@Composable
private fun CycleRegularityStepPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.background(BackgroundCream)) {
            CycleRegularityStep(
                isCycleRegular = true,
                onIsCycleRegularChange = {},
                onContinue = {}
            )
        }
    }
}

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding - Cycle Variance")
@Composable
private fun CycleVarianceStepPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.background(BackgroundCream)) {
            CycleVarianceStep(
                shortestCycleDays = 28,
                longestCycleDays = 32,
                onShortestCycleChange = {},
                onLongestCycleChange = {},
                onContinue = {}
            )
        }
    }
}

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding - Had Ultrasound")
@Composable
private fun HadUltrasoundStepPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.background(BackgroundCream)) {
            HadUltrasoundStep(
                hasHadUltrasound = true,
                onHasHadUltrasoundChange = {},
                onContinue = {}
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
                lmpDate = Date(),
                onLmpDateChange = {},
                onGoToUltrasound = {},
                onContinue = {}
            )
        }
    }
}

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding - Ultrasound Step")
@Composable
private fun UltrasoundStepPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.background(BackgroundCream)) {
            UltrasoundStep(
                scanDate = Date(),
                isRevisedEdd = false,
                scanWeeks = 8,
                scanDays = 2,
                eddDate = null,
                onScanDateChange = {},
                onIsRevisedEddChange = {},
                onScanWeeksChange = {},
                onScanDaysChange = {},
                onEddDateChange = {},
                onContinue = {}
            )
        }
    }
}

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding - EDD Step")
@Composable
private fun EddStepPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.background(BackgroundCream)) {
            EddStep(
                isTentative = false,
                positiveTestDate = null,
                eddDate = Date(),
                onPositiveTestDateChange = {},
                onEddDateChange = {},
                onContinue = {}
            )
        }
    }
}

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding - First Pregnancy Step")
@Composable
private fun FirstPregnancyStepPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.background(BackgroundCream)) {
            FirstPregnancyStep(
                isFirstPregnancy = false,
                onIsFirstPregnancyChange = {},
                onContinue = {}
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
                onSelectPregnancy = {},
                onRemovePregnancy = {},
                onAddPregnancy = {},
                onDeliveryDateChange = {},
                onDeliveryTypeChange = {},
                onChildrenDeliveredChange = {},
                onAreChildrenLivingChange = {},
                onToggleStructuredComplication = {},
                onToggleComplication = {},
                onAdditionalComplicationsChange = {},
                onContinue = {}
            )
        }
    }
}

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding - Preparing")
@Composable
private fun PreparingStepPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.background(BackgroundCream)) {
            PreparingStep(progress = 0.65f)
        }
    }
}

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding Host Preview")
@Composable
private fun OnboardingScreenPreview() {
    val sampleState = OnboardingState(
        currentStep = OnboardingStep.BIRTHDAY,
        birthday = null
    )
    PrimeraTheme {
        OnboardingScaffoldLayout(
            state = sampleState,
            onBack = {},
            stepContent = {
                BirthdayStep(
                    birthday = null,
                    onBirthdayChange = {},
                    onContinue = {}
                )
            }
        )
    }
}
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

    Scaffold(
        topBar = {
            if (state.currentStep != OnboardingStep.PREPARING) {
                OnboardingTopBar(
                    onBackClick = { viewModel.previousStep() },
                    currentStep = state.currentStep.ordinal + 1,
                    totalSteps = OnboardingStep.entries.size - 1
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
                    OnboardingStep.BIRTHDAY -> BirthdayStep(state, viewModel)
                    OnboardingStep.WEIGHT -> WeightStep(state, viewModel)
                    OnboardingStep.HEIGHT -> HeightStep(state, viewModel)
                    OnboardingStep.CYCLE_REGULARITY -> CycleRegularityStep(state, viewModel)
                    OnboardingStep.CYCLE_VARIANCE -> CycleVarianceStep(state, viewModel)
                    OnboardingStep.HAD_ULTRASOUND -> HadUltrasoundStep(state, viewModel)
                    OnboardingStep.LMP -> LmpStep(state, viewModel)
                    OnboardingStep.ULTRASOUND -> UltrasoundStep(state, viewModel)
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
fun BirthdayStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Tell Us Your Birthday",
        onContinue = { viewModel.nextStep() },
        isContinueEnabled = state.birthday != null
    ) {
        val calendar = Calendar.getInstance()
        val minYear = calendar.get(Calendar.YEAR) - 100
        val maxYear = calendar.get(Calendar.YEAR) - 13
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            PrimeraLabel("Select Date")
            Text(" *", color = ErrorRed, style = MaterialTheme.typography.labelSmall)
        }
        
        PrimeraDatePicker(
            selectedDate = state.birthday,
            onDateSelected = { viewModel.onBirthdayChange(it) },
            yearRange = minYear..maxYear,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun WeightStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Tell Us Your Weight",
        onContinue = { viewModel.nextStep() },
        isContinueEnabled = state.weightKg in 30..250 // Added reasonable range validation
    ) {
        LabeledField(
            label = "Weight (kg) *",
            value = if (state.weightKg == 0) "" else state.weightKg.toString(),
            onValueChange = { value -> 
                val weight = value.filter { it.isDigit() }.toIntOrNull() ?: 0
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
        isContinueEnabled = state.heightCm in 50..250 // Added reasonable range validation
    ) {
        LabeledField(
            label = "Height (cm) *",
            value = if (state.heightCm == 0) "" else state.heightCm.toString(),
            onValueChange = { value -> 
                val height = value.filter { it.isDigit() }.toIntOrNull() ?: 0
                viewModel.onHeightChange(height)
            },
            placeholder = "Enter height in cm",
            keyboardType = KeyboardType.Number
        )
    }
}

@Composable
fun CycleRegularityStep(state: OnboardingState, viewModel: OnboardingViewModel) {
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
            isSelected = state.isCycleRegular == true,
            onClick = { viewModel.onIsCycleRegularChange(true) }
        )
        Spacer(Modifier.height(16.dp))
        PrimeraOptionButton(
            text = "Irregular",
            isSelected = state.isCycleRegular == false,
            onClick = { viewModel.onIsCycleRegularChange(false) }
        )

        Spacer(Modifier.height(48.dp))
        if (state.isCycleRegular != null) {
            PrimeraGradientButton(text = "Continue", onClick = { viewModel.nextStep() })
        }
    }
}

@Composable
fun CycleVarianceStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Your Cycle Variance",
        subtitle = "Tell us your shortest and longest cycle lengths over the past 3 to 6 months.",
        onContinue = { viewModel.nextStep() },
        isContinueEnabled = state.shortestCycleDays in 15..50 && state.longestCycleDays in 15..90
    ) {
        LabeledField(
            label = "Shortest Cycle (days) *",
            value = if (state.shortestCycleDays == 0) "" else state.shortestCycleDays.toString(),
            onValueChange = { value ->
                val days = value.filter { it.isDigit() }.toIntOrNull() ?: 0
                viewModel.onShortestCycleChange(days)
            },
            placeholder = "e.g. 25",
            keyboardType = KeyboardType.Number
        )
        Spacer(Modifier.height(24.dp))
        LabeledField(
            label = "Longest Cycle (days) *",
            value = if (state.longestCycleDays == 0) "" else state.longestCycleDays.toString(),
            onValueChange = { value ->
                val days = value.filter { it.isDigit() }.toIntOrNull() ?: 0
                viewModel.onLongestCycleChange(days)
            },
            placeholder = "e.g. 35",
            keyboardType = KeyboardType.Number
        )
    }
}

@Composable
fun HadUltrasoundStep(state: OnboardingState, viewModel: OnboardingViewModel) {
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
            isSelected = state.hasHadUltrasound == true,
            onClick = { viewModel.onHasHadUltrasoundChange(true) }
        )
        Spacer(Modifier.height(16.dp))
        PrimeraOptionButton(
            text = "No, not yet",
            isSelected = state.hasHadUltrasound == false,
            onClick = { viewModel.onHasHadUltrasoundChange(false) }
        )

        Spacer(Modifier.height(48.dp))
        if (state.hasHadUltrasound != null) {
            PrimeraGradientButton(
                text = "Continue",
                onClick = { 
                    if (state.hasHadUltrasound == true) viewModel.goToUltrasound()
                    else viewModel.nextStep()
                }
            )
        }
    }
}

@Composable
fun LmpStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "When was the First Day of your Last Period?",
        onContinue = { viewModel.nextStep() },
        isContinueEnabled = state.lmpDate != null
    ) {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            PrimeraLabel("Select Date")
            Text(" *", color = ErrorRed, style = MaterialTheme.typography.labelSmall)
        }
        
        PrimeraDatePicker(
            selectedDate = state.lmpDate,
            onDateSelected = { viewModel.onLmpDateChange(it) },
            yearRange = (currentYear - 1)..currentYear,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))
        
        PrimeraOutlinedButton(
            text = "Already have your ultrasound?",
            onClick = { viewModel.goToUltrasound() }
        )
    }
}

@Composable
fun UltrasoundStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    OnboardingLayout(
        title = "Ultrasound Details",
        onContinue = { viewModel.nextStep() },
        isContinueEnabled = if (state.scanDate != null) {
            if (state.isRevisedEdd) state.eddDate != null 
            else (state.scanWeeks > 0 || state.scanDays > 0)
        } else false
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PrimeraLabel("Date the ultrasound was performed")
            Text(" *", color = ErrorRed, style = MaterialTheme.typography.labelSmall)
        }
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        PrimeraDatePicker(
            selectedDate = state.scanDate,
            onDateSelected = { viewModel.onScanDateChange(it) },
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
            PrimeraOptionChip("Gestational Age", !state.isRevisedEdd) { viewModel.onIsRevisedEddChange(false) }
            PrimeraOptionChip("Revised Due Date", state.isRevisedEdd) { viewModel.onIsRevisedEddChange(true) }
        }
        
        Spacer(Modifier.height(24.dp))
        
        if (!state.isRevisedEdd) {
            PrimeraLabel("Gestational Age at scan *")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LabeledField(
                    label = "Weeks",
                    value = if (state.scanWeeks == 0) "" else state.scanWeeks.toString(),
                    onValueChange = { viewModel.onScanWeeksChange(it.toIntOrNull() ?: 0) },
                    placeholder = "0",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
                LabeledField(
                    label = "Days",
                    value = if (state.scanDays == 0) "" else state.scanDays.toString(),
                    onValueChange = { viewModel.onScanDaysChange(it.toIntOrNull() ?: 0) },
                    placeholder = "0",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
            }
        } else {
            PrimeraLabel("Revised Due Date *")
            PrimeraDatePicker(
                selectedDate = state.eddDate,
                onDateSelected = { viewModel.onEddDateChange(it) },
                yearRange = currentYear..(currentYear + 1),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun EddStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    val isTentative = state.isCycleRegular == false && state.hasHadUltrasound == false
    
    OnboardingLayout(
        title = if (isTentative) "Tentative Due Date" else "When is your Expected Date of Delivery?",
        subtitle = if (isTentative) "Since you have irregular cycles and no scan yet, this is a placeholder estimate." else null,
        onContinue = { viewModel.nextStep() },
        isContinueEnabled = state.eddDate != null && (!isTentative || state.positiveTestDate != null)
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
                        imageVector = Icons.Default.Close, // Using Close as a placeholder for Info/Warning
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
                selectedDate = state.positiveTestDate,
                onDateSelected = { viewModel.onPositiveTestDateChange(it) },
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
            selectedDate = state.eddDate,
            onDateSelected = { viewModel.onEddDateChange(it) },
            yearRange = currentYear..(currentYear + 1),
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
    val currentPregnancy = state.currentPregnancy
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentPregnancy.deliveryDate?.time ?: System.currentTimeMillis()
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
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = PrimeraViolet,
                    todayContentColor = PrimeraViolet,
                    todayDateBorderColor = PrimeraViolet
                )
            )
        }
    }

    OnboardingLayout(
        title = "Pregnancy History",
        onContinue = { viewModel.nextStep() },
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
                            .clickable { viewModel.onSelectPregnancy(index) },
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
                                .clickable { viewModel.removePregnancy(index) },
                            shape = CircleShape,
                            color = ErrorRed,
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.padding(2.dp))
                        }
                    }
                }
            }
            
            // Add Button (+), only enabled if the previous pregnancy is complete
            val canAdd = state.isPregnancyComplete(state.pregnancyHistories.lastIndex) && state.pregnancyHistories.size < 10
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (canAdd) PrimeraViolet.copy(alpha = 0.1f) else SurfaceWhite.copy(alpha = 0.5f))
                    .border(1.dp, if (canAdd) PrimeraViolet else InputBorder.copy(alpha = 0.5f), CircleShape)
                    .clickable(enabled = canAdd) { viewModel.addPregnancy() },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = if (canAdd) PrimeraViolet else TextHint, fontSize = 20.sp)
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        // Form for the CURRENT pregnancy
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
            PrimeraOptionChip("Vaginal", currentPregnancy.deliveryType == "Vaginal") { viewModel.onDeliveryTypeChange("Vaginal") }
            PrimeraOptionChip("C-section", currentPregnancy.deliveryType == "C-section") { viewModel.onDeliveryTypeChange("C-section") }
        }

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.Start)) {
            PrimeraLabel("No. of Child/Children Delivered")
            Text(" *", color = ErrorRed, style = MaterialTheme.typography.labelSmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimeraOptionChip("Single", currentPregnancy.childrenDelivered == "Single") { viewModel.onChildrenDeliveredChange("Single") }
            PrimeraOptionChip("Twins", currentPregnancy.childrenDelivered == "Twins") { viewModel.onChildrenDeliveredChange("Twins") }
            PrimeraOptionChip("Multiple", currentPregnancy.childrenDelivered == "Multiple") { viewModel.onChildrenDeliveredChange("Multiple") }
        }

        Spacer(Modifier.height(24.dp))
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
                    isSelected = currentPregnancy.complications.contains(comp),
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

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding - Height Step")
@Composable
private fun HeightStepPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.background(BackgroundCream)) {
            HeightStep(
                state = OnboardingState(currentStep = OnboardingStep.HEIGHT),
                viewModel = viewModel()
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
                state = OnboardingState(currentStep = OnboardingStep.CYCLE_REGULARITY),
                viewModel = viewModel()
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
                state = OnboardingState(currentStep = OnboardingStep.CYCLE_VARIANCE),
                viewModel = viewModel()
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
                state = OnboardingState(currentStep = OnboardingStep.HAD_ULTRASOUND),
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

@Preview(widthDp = 393, heightDp = 852, showBackground = true, name = "Onboarding - Ultrasound Step")
@Composable
private fun UltrasoundStepPreview() {
    PrimeraTheme {
        Box(modifier = Modifier.background(BackgroundCream)) {
            UltrasoundStep(
                state = OnboardingState(currentStep = OnboardingStep.ULTRASOUND),
                viewModel = viewModel()
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
                state = OnboardingState(currentStep = OnboardingStep.EDD),
                viewModel = viewModel()
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
                state = OnboardingState(currentStep = OnboardingStep.FIRST_PREGNANCY),
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

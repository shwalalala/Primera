package cit.edu.primera.feature.transcription.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.InputBorder
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.core.utils.clickableNoRipple
import cit.edu.primera.feature.transcription.domain.VoiceLanguage
import cit.edu.primera.ui.components.FullScreenLoadingOverlay
import cit.edu.primera.ui.components.PrimeraGradientButton

@Composable
fun TranscriptionScreen(
    viewModel: TranscriptionViewModel,
    onBack: () -> Unit,
    onUseInCheckin: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val transcribedText by viewModel.transcribedText.collectAsStateWithLifecycle()
    val rawTranscribedText by viewModel.rawTranscribedText.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val isAiCorrected by viewModel.isAiCorrected.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    TranscriptionContent(
        uiState = uiState,
        transcribedText = transcribedText,
        rawTranscribedText = rawTranscribedText,
        selectedLanguage = selectedLanguage,
        isAiCorrected = isAiCorrected,
        onSelectLanguage = viewModel::selectLanguage,
        onStartListening = {
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                viewModel.startListening()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        onStopListening = viewModel::stopListening,
        onTextEdited = viewModel::onTextEdited,
        onReformatWithAi = viewModel::reformatWithAi,
        onRevertToRaw = viewModel::revertToRawText,
        onSave = viewModel::saveTranscription,
        onUseInCheckin = {
            if (transcribedText.isNotBlank()) {
                onUseInCheckin(transcribedText)
            }
        },
        onDismissError = viewModel::dismissError,
        onStartManualInput = viewModel::startManualInput,
        onBack = onBack
    )
}

@Composable
private fun TranscriptionContent(
    uiState: TranscriptionUiState,
    transcribedText: String,
    rawTranscribedText: String,
    selectedLanguage: VoiceLanguage,
    isAiCorrected: Boolean,
    onSelectLanguage: (VoiceLanguage) -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onTextEdited: (String) -> Unit,
    onReformatWithAi: () -> Unit,
    onRevertToRaw: () -> Unit,
    onSave: () -> Unit,
    onUseInCheckin: () -> Unit,
    onDismissError: () -> Unit,
    onStartManualInput: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

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
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Text(
                    text = "Voice Logging",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(Modifier.width(48.dp))
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Speak your thoughts. AI will add punctuation and formatting automatically.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            LanguageSelector(
                selectedLanguage = selectedLanguage,
                onSelectLanguage = onSelectLanguage,
                enabled = (uiState !is TranscriptionUiState.Listening) &&
                        (uiState !is TranscriptionUiState.Processing) &&
                        (uiState !is TranscriptionUiState.AnalyzingWithAi)
            )

            Spacer(Modifier.height(24.dp))

            MicrophoneButton(
                uiState = uiState,
                onClick = {
                    when (uiState) {
                        is TranscriptionUiState.Listening -> onStopListening()
                        else -> onStartListening()
                    }
                }
            )

            if (uiState !is TranscriptionUiState.Listening &&
                uiState !is TranscriptionUiState.Success &&
                uiState !is TranscriptionUiState.AnalyzingWithAi &&
                uiState !is TranscriptionUiState.Uploading &&
                uiState !is TranscriptionUiState.Uploaded
            ) {
                TextButton(
                    onClick = onStartManualInput,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PrimeraViolet
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Or type your notes manually",
                        color = PrimeraViolet,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (uiState is TranscriptionUiState.Listening) {
                Text(
                    text = "Listening in ${selectedLanguage.displayName}...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = PrimeraViolet
                    )
                )
            }

            if (uiState is TranscriptionUiState.AnalyzingWithAi) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                            color = PrimeraViolet
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "AI is analyzing & correcting...",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = if (selectedLanguage == VoiceLanguage.BISAYA) {
                                    "Recognizing Bisaya speech, correcting words & punctuating"
                                } else {
                                    "Analyzing ${selectedLanguage.shortLabel} speech and formatting punctuation"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (
                uiState is TranscriptionUiState.Success ||
                uiState is TranscriptionUiState.Uploading ||
                uiState is TranscriptionUiState.Uploaded
            ) {
                // AI Badge and Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAiCorrected) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimeraViolet.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PrimeraViolet,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "AI Punctuated & Formatted",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimeraViolet
                                )
                            )
                        }
                    } else {
                        Spacer(Modifier.width(8.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onReformatWithAi) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("AI Polish")
                        }

                        if (rawTranscribedText.isNotBlank() && rawTranscribedText != transcribedText) {
                            TextButton(onClick = onRevertToRaw) {
                                Text("Revert", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = transcribedText,
                    onValueChange = onTextEdited,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp),
                    label = { Text("Voice Log (AI-Corrected)") },
                    trailingIcon = {
                        if (transcribedText.isNotEmpty()) {
                            IconButton(onClick = { onTextEdited("") }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear text"
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = InputBorder,
                        focusedBorderColor = PrimeraViolet
                    )
                )

                if (rawTranscribedText.isNotBlank() && rawTranscribedText != transcribedText) {
                    var showRaw by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        TextButton(onClick = { showRaw = !showRaw }) {
                            Text(
                                if (showRaw) "Hide raw speech" else "View raw speech input",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    AnimatedVisibility(visible = showRaw) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Raw speech: \"$rawTranscribedText\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                if (uiState is TranscriptionUiState.Success) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Language: ${selectedLanguage.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )

                        Text(
                            text = "Confidence: ${(uiState.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                PrimeraGradientButton(
                    text = if (uiState is TranscriptionUiState.Uploaded) {
                        "Saved!"
                    } else {
                        "Save to Firestore"
                    },
                    onClick = onSave,
                    enabled = uiState is TranscriptionUiState.Success,
                    isLoading = uiState is TranscriptionUiState.Uploading
                )

                Spacer(Modifier.height(12.dp))

                PrimeraGradientButton(
                    text = "Use in Check-In",
                    onClick = onUseInCheckin,
                    enabled = transcribedText.isNotBlank() &&
                            (uiState is TranscriptionUiState.Success || uiState is TranscriptionUiState.Uploaded),
                    isLoading = false
                )
            }

            Spacer(Modifier.height(16.dp))
        }

        if (uiState is TranscriptionUiState.Processing) {
            FullScreenLoadingOverlay()
        }

        if (uiState is TranscriptionUiState.Error) {
            AlertDialog(
                onDismissRequest = onDismissError,
                title = { Text("Speech Recognition Notice") },
                text = { Text(uiState.message) },
                confirmButton = {
                    TextButton(onClick = {
                        onDismissError()
                        onStartManualInput()
                    }) {
                        Text("Type Manually")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissError) {
                        Text("Dismiss")
                    }
                }
            )
        }
    }
}

@Composable
private fun LanguageSelector(
    selectedLanguage: VoiceLanguage,
    onSelectLanguage: (VoiceLanguage) -> Unit,
    enabled: Boolean
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Translate,
            contentDescription = "Language",
            tint = PrimeraViolet,
            modifier = Modifier.size(18.dp)
        )

        VoiceLanguage.entries.forEach { language ->
            val isSelected = selectedLanguage == language
            FilterChip(
                selected = isSelected,
                onClick = { onSelectLanguage(language) },
                label = {
                    Text(
                        text = language.displayName,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                enabled = enabled,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimeraViolet.copy(alpha = 0.2f),
                    selectedLabelColor = PrimeraViolet
                )
            )
        }
    }
}

@Composable
private fun MicrophoneButton(
    uiState: TranscriptionUiState,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val buttonScale = if (uiState is TranscriptionUiState.Listening) scale else 1f

    val buttonColor = when (uiState) {
        is TranscriptionUiState.Listening -> Color.Red
        is TranscriptionUiState.Uploaded -> Color.Green
        else -> PrimeraViolet
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(120.dp)
            .scale(buttonScale)
            .clip(CircleShape)
            .background(buttonColor.copy(alpha = 0.2f))
            .padding(16.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        buttonColor,
                        buttonColor.copy(alpha = 0.8f)
                    )
                )
            )
            .clickableNoRipple(onClick = onClick)
    ) {
        Icon(
            imageVector = when (uiState) {
                is TranscriptionUiState.Listening -> Icons.Default.Stop
                is TranscriptionUiState.Uploaded -> Icons.Default.CheckCircle
                else -> Icons.Default.Mic
            },
            contentDescription = "Microphone",
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Preview(name = "Compact Phone", device = Devices.PHONE)
@Composable
private fun TranscriptionScreenPreview() {
    PrimeraTheme {
        TranscriptionContent(
            uiState = TranscriptionUiState.Idle,
            transcribedText = "",
            rawTranscribedText = "",
            selectedLanguage = VoiceLanguage.BISAYA,
            isAiCorrected = false,
            onSelectLanguage = {},
            onStartListening = {},
            onStopListening = {},
            onTextEdited = {},
            onReformatWithAi = {},
            onRevertToRaw = {},
            onSave = {},
            onUseInCheckin = {},
            onDismissError = {},
            onStartManualInput = {},
            onBack = {}
        )
    }
}

@Preview(name = "Success State", device = Devices.PHONE)
@Composable
private fun TranscriptionScreenSuccessPreview() {
    PrimeraTheme {
        TranscriptionContent(
            uiState = TranscriptionUiState.Success(
                text = "Gikapoy ko unya labad akong ulo, unsaon man ni?",
                confidence = 0.95f,
                durationMs = 5000L,
                rawSpokenText = "gikapoy ko unya labad akong ulo unsaon man ni",
                isAiCorrected = true
            ),
            transcribedText = "Gikapoy ko unya labad akong ulo, unsaon man ni?",
            rawTranscribedText = "gikapoy ko unya labad akong ulo unsaon man ni",
            selectedLanguage = VoiceLanguage.BISAYA,
            isAiCorrected = true,
            onSelectLanguage = {},
            onStartListening = {},
            onStopListening = {},
            onTextEdited = {},
            onReformatWithAi = {},
            onRevertToRaw = {},
            onSave = {},
            onUseInCheckin = {},
            onDismissError = {},
            onStartManualInput = {},
            onBack = {}
        )
    }
}

package com.example.primera.feature.profile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun ProfileScreen(
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
        Column(modifier = Modifier.fillMaxSize()) {
            FeatureTopBar(
                title = "My Profile",
                onBack = onBack,
                onSettings = onNavigateToSettings
            )

            if (state.isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimeraViolet)
                }
            } else {
                ProfileContent(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (state.isSaving) {
            FullScreenLoadingOverlay()
        }
    }
    
    // Show success message
    state.successMessage?.let { msg ->
        LaunchedEffect(msg) {
            // In a real app we might use a Snackbar
            viewModel.clearMessages()
        }
    }
}

@Composable
fun ProfileContent(
    state: ProfileUiState,
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.eddDate?.time ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onEddDateChange(Date(it))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Personal Information",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    
                    TextButton(onClick = { 
                        if (state.isEditing) viewModel.saveProfile() else viewModel.toggleEdit() 
                    }) {
                        Text(
                            text = if (state.isEditing) "Save" else "Edit",
                            color = PrimeraViolet,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                ProfileField(
                    label = "First Name",
                    value = state.firstName,
                    isEditing = state.isEditing,
                    onValueChange = viewModel::onFirstNameChange
                )
                
                ProfileField(
                    label = "Last Name",
                    value = state.lastName,
                    isEditing = state.isEditing,
                    onValueChange = viewModel::onLastNameChange
                )

                ProfileField(
                    label = "Middle Name",
                    value = state.middleName,
                    isEditing = state.isEditing,
                    onValueChange = viewModel::onMiddleNameChange
                )

                ProfileField(
                    label = "Email",
                    value = state.email,
                    isEditing = false, // Email usually non-editable in this flow
                    onValueChange = {}
                )

                ProfileField(
                    label = "Weight (kg)",
                    value = state.weightKg.toString(),
                    isEditing = state.isEditing,
                    onValueChange = { viewModel.onWeightChange(it.toIntOrNull() ?: 0) },
                    keyboardType = KeyboardType.Number
                )

                ProfileField(
                    label = "Height (cm)",
                    value = state.heightCm.toString(),
                    isEditing = state.isEditing,
                    onValueChange = { viewModel.onHeightChange(it.toIntOrNull() ?: 0) },
                    keyboardType = KeyboardType.Number
                )
                
                ProfileField(
                    label = "Due Date",
                    value = state.eddDate?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it) } ?: "Not set",
                    isEditing = false,
                    modifier = Modifier.clickable(enabled = state.isEditing) { showDatePicker = true },
                    onValueChange = {}
                )
            }
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    isEditing: Boolean,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimeraViolet,
                    unfocusedBorderColor = InputBorder
                )
            )
        } else {
            Text(
                text = value,
                fontSize = 16.sp,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp), color = InputBorder.copy(alpha = 0.5f))
    }
}

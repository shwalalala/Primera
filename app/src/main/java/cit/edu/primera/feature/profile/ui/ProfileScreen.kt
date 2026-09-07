package cit.edu.primera.feature.profile.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
                    onNavigateToSettings = onNavigateToSettings,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (state.isSaving) {
            FullScreenLoadingOverlay()
        }
    }
    
    // Success message handling
    state.successMessage?.let { msg ->
        LaunchedEffect(msg) {
            viewModel.clearMessages()
        }
    }
}

@Composable
fun ProfileContent(
    state: ProfileUiState,
    viewModel: ProfileViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var datePickerType by remember { mutableStateOf<String?>(null) }
    var historyIndex by remember { mutableIntStateOf(-1) }

    if (datePickerType != null) {
        val initialDate = when (datePickerType) {
            "birthday" -> state.birthday
            "edd" -> state.eddDate
            "lmp" -> state.lmpDate
            "scan" -> state.scanDate
            "test" -> state.positiveTestDate
            "history" -> state.pregnancyHistories.getOrNull(historyIndex)?.deliveryDate
            else -> null
        } ?: Date()

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate.time
        )
        
        DatePickerDialog(
            onDismissRequest = { datePickerType = null },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val d = Date(it)
                        when (datePickerType) {
                            "birthday" -> viewModel.onBirthdayChange(d)
                            "edd" -> viewModel.onEddDateChange(d)
                            "lmp" -> viewModel.onLmpDateChange(d)
                            "scan" -> viewModel.onScanDateChange(d)
                            "test" -> viewModel.onPositiveTestDateChange(d)
                            "history" -> viewModel.onHistoryDeliveryDateChange(historyIndex, d)
                        }
                    }
                    datePickerType = null
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { datePickerType = null }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Edit Mode Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { 
                if (state.isEditing) viewModel.saveProfile() else viewModel.toggleEdit() 
            }) {
                Icon(
                    imageVector = if (state.isEditing) Icons.Default.Save else Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = PrimeraLogoStart
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (state.isEditing) "Save Changes" else "Edit Profile",
                    color = PrimeraLogoStart,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // 1. Personal Information Section
        ProfileSectionCard(
            title = "Personal Information",
            icon = Icons.Default.Person,
            isExpanded = state.expandedSections.contains(ProfileSection.PERSONAL_INFO),
            onToggle = { viewModel.toggleSection(ProfileSection.PERSONAL_INFO) }
        ) {
            ProfileField(
                label = "Email Address",
                value = state.email,
                isEditing = state.isEditing,
                onValueChange = viewModel::onEmailChange
            )
            
            val birthdayStr = state.birthday?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it) } ?: "Not set"
            ProfileField(
                label = "Birthday",
                value = birthdayStr,
                isEditing = false,
                modifier = Modifier.clickable(enabled = state.isEditing) { datePickerType = "birthday" },
                onValueChange = {}
            )
            
            ProfileField(
                label = "Weight (kg)",
                value = if (state.weightKg == 0) "" else state.weightKg.toString(),
                isEditing = state.isEditing,
                onValueChange = { viewModel.onWeightChange(it.toIntOrNull() ?: 0) },
                placeholder = "Enter weight",
                keyboardType = KeyboardType.Number
            )
            ProfileField(
                label = "Height (cm)",
                value = if (state.heightCm == 0) "" else state.heightCm.toString(),
                isEditing = state.isEditing,
                onValueChange = { viewModel.onHeightChange(it.toIntOrNull() ?: 0) },
                placeholder = "Enter height",
                keyboardType = KeyboardType.Number
            )
        }

        // 2. Pregnancy Details Section
        ProfileSectionCard(
            title = "Pregnancy Details",
            icon = Icons.Default.ChildCare,
            isExpanded = state.expandedSections.contains(ProfileSection.PREGNANCY_DETAILS),
            onToggle = { viewModel.toggleSection(ProfileSection.PREGNANCY_DETAILS) }
        ) {
            val eddStr = state.eddDate?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it) } ?: "Not set"
            ProfileField(
                label = "Expected Due Date",
                value = eddStr,
                isEditing = false,
                modifier = Modifier.clickable(enabled = state.isEditing) { datePickerType = "edd" },
                onValueChange = {}
            )
            
            ProfileField(
                label = "First Pregnancy",
                value = if (state.isFirstPregnancy) "Yes" else "No",
                isEditing = false,
                onValueChange = {}
            )

            if (state.isEditing) {
                Text("Cycle Regularity", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimeraOptionChip("Regular", state.isCycleRegular == true) { viewModel.onIsCycleRegularChange(true) }
                    PrimeraOptionChip("Irregular", state.isCycleRegular == false) { viewModel.onIsCycleRegularChange(false) }
                }
            } else {
                ProfileField(
                    label = "Cycle Regularity",
                    value = when (state.isCycleRegular) {
                        true -> "Regular"
                        false -> "Irregular"
                        else -> "Unknown"
                    },
                    isEditing = false,
                    onValueChange = {}
                )
            }

            if (state.isCycleRegular == false) {
                ProfileField(
                    label = "Shortest Cycle (days)",
                    value = state.shortestCycleDays?.toString() ?: "",
                    isEditing = state.isEditing,
                    onValueChange = { viewModel.onShortestCycleChange(it.toIntOrNull() ?: 0) },
                    keyboardType = KeyboardType.Number
                )
                ProfileField(
                    label = "Longest Cycle (days)",
                    value = state.longestCycleDays?.toString() ?: "",
                    isEditing = state.isEditing,
                    onValueChange = { viewModel.onLongestCycleChange(it.toIntOrNull() ?: 0) },
                    keyboardType = KeyboardType.Number
                )
            }

            if (state.isCycleRegular == true) {
                val lmpStr = state.lmpDate?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it) } ?: "Not set"
                ProfileField(
                    label = "Last Menstrual Period (LMP)",
                    value = lmpStr,
                    isEditing = false,
                    modifier = Modifier.clickable(enabled = state.isEditing) { datePickerType = "lmp" },
                    onValueChange = {}
                )
            }

            if (state.isEditing) {
                Text("Dating Ultrasound", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimeraOptionChip("Completed", state.hasHadUltrasound == true) { viewModel.onHasHadUltrasoundChange(true) }
                    PrimeraOptionChip("Not yet", state.hasHadUltrasound == false) { viewModel.onHasHadUltrasoundChange(false) }
                }
            } else {
                ProfileField(
                    label = "Dating Ultrasound",
                    value = if (state.hasHadUltrasound == true) "Completed" else "Not yet",
                    isEditing = false,
                    onValueChange = {}
                )
            }

            if (state.hasHadUltrasound == true) {
                val scanDateStr = state.scanDate?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it) } ?: "Not set"
                ProfileField(
                    label = "Ultrasound Scan Date",
                    value = scanDateStr,
                    isEditing = false,
                    modifier = Modifier.clickable(enabled = state.isEditing) { datePickerType = "scan" },
                    onValueChange = {}
                )
                
                ProfileField(
                    label = "Scan Weeks",
                    value = state.scanWeeks?.toString() ?: "",
                    isEditing = state.isEditing,
                    onValueChange = { viewModel.onScanWeeksChange(it.toIntOrNull() ?: 0) },
                    keyboardType = KeyboardType.Number
                )
                ProfileField(
                    label = "Scan Days",
                    value = state.scanDays?.toString() ?: "",
                    isEditing = state.isEditing,
                    onValueChange = { viewModel.onScanDaysChange(it.toIntOrNull() ?: 0) },
                    keyboardType = KeyboardType.Number
                )
            }

            if (state.hasHadUltrasound == false && state.isCycleRegular == false) {
                val testDateStr = state.positiveTestDate?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it) } ?: "Not set"
                ProfileField(
                    label = "Positive Pregnancy Test Date",
                    value = testDateStr,
                    isEditing = false,
                    modifier = Modifier.clickable(enabled = state.isEditing) { datePickerType = "test" },
                    onValueChange = {}
                )
            }
        }

        // 3. Pregnancy History Section
        if (!state.isFirstPregnancy) {
            ProfileSectionCard(
                title = "Pregnancy History",
                icon = Icons.Default.History,
                isExpanded = state.expandedSections.contains(ProfileSection.PREGNANCY_HISTORY),
                onToggle = { viewModel.toggleSection(ProfileSection.PREGNANCY_HISTORY) }
            ) {
                if (state.pregnancyHistories.isEmpty()) {
                    Text(
                        "No pregnancy history recorded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                } else {
                    state.pregnancyHistories.forEachIndexed { index, history ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(
                                "Pregnancy #${history.pregnancyNumber}",
                                fontWeight = FontWeight.Bold,
                                color = PrimeraLogoStart,
                                fontSize = 14.sp
                            )
                            val deliveryDate = history.deliveryDate?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it) } ?: "Select Date"
                            
                            if (state.isEditing) {
                                Text("Delivery Date", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Box(modifier = Modifier.fillMaxWidth().clickable { 
                                    historyIndex = index
                                    datePickerType = "history"
                                }.padding(vertical = 8.dp)) {
                                    Text(deliveryDate, fontWeight = FontWeight.SemiBold)
                                }
                                
                                Text("Delivery Type", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PrimeraOptionChip("Vaginal", history.deliveryType == "Vaginal") { viewModel.onHistoryDeliveryTypeChange(index, "Vaginal") }
                                    PrimeraOptionChip("C-section", history.deliveryType == "C-section") { viewModel.onHistoryDeliveryTypeChange(index, "C-section") }
                                }
                                
                                ProfileField(
                                    label = "Children Delivered",
                                    value = history.childrenDelivered,
                                    isEditing = true,
                                    onValueChange = { viewModel.onHistoryChildrenChange(index, it) }
                                )
                            } else {
                                Text("Delivered on $deliveryDate", fontSize = 13.sp, color = TextPrimary)
                                Text("${history.childrenDelivered} delivery (${history.deliveryType})", fontSize = 13.sp, color = TextSecondary)
                                if (history.complications.isNotEmpty()) {
                                    Text("Complications: ${history.complications.joinToString()}", fontSize = 12.sp, color = ErrorRed)
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = InputBorder.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }

        // 4. ICE Contact Section
        ProfileSectionCard(
            title = "In Case of Emergency (ICE)",
            icon = Icons.Default.Emergency,
            isExpanded = state.expandedSections.contains(ProfileSection.ICE_CONTACT),
            onToggle = { viewModel.toggleSection(ProfileSection.ICE_CONTACT) },
            headerColor = ErrorRed
        ) {
            ProfileField(
                label = "Contact Name",
                value = state.emergencyContact.name,
                isEditing = state.isEditing,
                onValueChange = viewModel::onIceNameChange,
                placeholder = "Full Name"
            )
            ProfileField(
                label = "Relationship",
                value = state.emergencyContact.relationship,
                isEditing = state.isEditing,
                onValueChange = viewModel::onIceRelationshipChange,
                placeholder = "e.g. Spouse, Parent"
            )
            
            ProfileField(
                label = "Primary Phone",
                value = state.emergencyContact.primaryPhone,
                isEditing = state.isEditing,
                onValueChange = viewModel::onIcePrimaryPhoneChange,
                placeholder = "09XX XXX XXXX",
                keyboardType = KeyboardType.Phone,
                trailingIcon = if (!state.isEditing && state.emergencyContact.primaryPhone.isNotBlank()) {
                    {
                        val context = LocalContext.current
                        IconButton(onClick = { 
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                    data = android.net.Uri.parse("tel:${state.emergencyContact.primaryPhone}")
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.Call, null, tint = Good, modifier = Modifier.size(20.dp))
                        }
                    }
                } else null
            )
            
            ProfileField(
                label = "Secondary Phone",
                value = state.emergencyContact.secondaryPhone,
                isEditing = state.isEditing,
                onValueChange = viewModel::onIceSecondaryPhoneChange,
                placeholder = "Optional",
                keyboardType = KeyboardType.Phone
            )
        }

        // 5. Account Settings Section
        ProfileSectionCard(
            title = "Account Settings",
            icon = Icons.Default.Settings,
            isExpanded = state.expandedSections.contains(ProfileSection.ACCOUNT_SETTINGS),
            onToggle = { viewModel.toggleSection(ProfileSection.ACCOUNT_SETTINGS) }
        ) {
            Text(
                "Manage your security, password, and preferences.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            PrimeraOutlinedButton(
                text = "Open Security Settings",
                onClick = onNavigateToSettings
            )
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun ProfileSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    headerColor: Color = PrimeraLogoStart,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(headerColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = headerColor, modifier = Modifier.size(20.dp))
                }
                
                Spacer(Modifier.width(16.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = InputBorder.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 8.dp))
                    content()
                }
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
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(placeholder, fontSize = 14.sp, color = TextHint) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimeraViolet,
                        unfocusedBorderColor = InputBorder,
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    ),
                    singleLine = true
                )
            } else {
                Text(
                    text = value.ifBlank { "Not set" },
                    fontSize = 16.sp,
                    color = if (value.isBlank()) TextHint else TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (trailingIcon != null) {
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    trailingIcon()
                }
            }
        }
    }
}

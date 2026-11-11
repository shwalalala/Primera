
package cit.edu.primera.feature.checkins.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.DashboardLogBorder
import cit.edu.primera.core.theme.InputBorder
import cit.edu.primera.core.theme.LogPain
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.SurfaceWhite
import cit.edu.primera.core.theme.TextHint
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.feature.checkins.data.PrenatalRecordDto
import cit.edu.primera.feature.dashboard.ui.DashboardLogUiItem
import cit.edu.primera.ui.components.LogCard
import cit.edu.primera.ui.components.PrimeraGradientButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PrenatalCompletedGreen = Color(0xFF79B49E)

// Maximum number of prenatal cards on the main page.
private const val PRENATAL_OVERVIEW_LIMIT = 2

private fun overviewToday(): String =
    SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    ).format(Date())

private fun overviewDisplayDate(date: String): String {
    return runCatching {
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).apply {
            isLenient = false
        }

        val parsed = formatter.parse(date)
            ?: return@runCatching date

        SimpleDateFormat(
            "MMM d, yyyy",
            Locale.getDefault()
        ).format(parsed)
    }.getOrDefault(date)
}

// -------------------------------------------------
// DAILY CHECK-IN PREVIEW
// -------------------------------------------------

private data class DailyPreviewField(
    val label: String,
    val value: String
)

private fun dailyPreviewFields(
    log: DashboardLogUiItem
): List<DailyPreviewField> {
    val knownLabels = listOf(
        "Weight:" to "Weight",
        "Blood Sugar:" to "Blood Sugar",
        "Symptoms:" to "Symptoms",
        "Mood:" to "Mood",
        "Medicine:" to "Medicine"
    )

    val extracted = mutableMapOf<String, String>()
    val noteParts = mutableListOf<String>()

    log.description.split(";").forEach { rawPart ->
        val part = rawPart.trim()

        if (part.isNotBlank()) {
            val found = knownLabels.firstOrNull {
                    (prefix, _) ->
                part.startsWith(prefix, ignoreCase = true)
            }

            if (found != null) {
                val value = part.substring(
                    found.first.length
                ).trim()

                if (value.isNotBlank()) {
                    extracted[found.second] = value
                }
            } else {
                noteParts.add(part)
            }
        }
    }

    val fields = mutableListOf<DailyPreviewField>()

    if (log.time.isNotBlank()) {
        fields.add(
            DailyPreviewField(
                label = "Check-In Date / Time",
                value = log.time
            )
        )
    }

    listOf(
        "Weight",
        "Blood Sugar",
        "Symptoms",
        "Mood",
        "Medicine"
    ).forEach { label ->
        extracted[label]?.let { value ->
            fields.add(
                DailyPreviewField(label, value)
            )
        }
    }

    val notes = noteParts.joinToString("; ").trim()

    if (notes.isNotBlank()) {
        fields.add(
            DailyPreviewField(
                label = "Notes",
                value = notes
            )
        )
    }

    return fields
}

// -------------------------------------------------
// MAIN OVERVIEW SCREEN
// -------------------------------------------------

@Composable
fun CheckinsOverviewScreen(
    onNavigateToDailyCheckin: () -> Unit,
    onLogClick: (DashboardLogUiItem) -> Unit,
    viewModel: CheckinsViewModel,
    onOpenDrawer: () -> Unit = {},
    onPrintQrClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onNavigateToPrenatalCheckup: (() -> Unit)? = null,
    onPrenatalRecordClick: ((String) -> Unit)? = null
) {
    val state by viewModel.overviewState
        .collectAsStateWithLifecycle()

    val prenatalViewModel: PrenatalViewModel = viewModel()

    val prenatalState by prenatalViewModel.uiState
        .collectAsStateWithLifecycle()

    CheckinsOverviewContent(
        state = state,
        prenatalRecords = prenatalState.records,
        prenatalLoading = prenatalState.isLoading,
        prenatalError = prenatalState.errorMessage,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onFilterChange = viewModel::onFilterChange,
        onNavigateToDailyCheckin = onNavigateToDailyCheckin,
        onNavigateToPrenatalCheckup = onNavigateToPrenatalCheckup,
        onPrenatalRecordClick = onPrenatalRecordClick,
        onLogClick = onLogClick,
        onOpenDrawer = onOpenDrawer,
        onPrintQrClick = onPrintQrClick,
        modifier = modifier
    )
}

// -------------------------------------------------
// OVERVIEW CONTENT
// -------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckinsOverviewContent(
    state: CheckinsOverviewUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onNavigateToDailyCheckin: () -> Unit,
    onLogClick: (DashboardLogUiItem) -> Unit,
    onOpenDrawer: () -> Unit = {},
    onPrintQrClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onNavigateToPrenatalCheckup: (() -> Unit)? = null,
    prenatalRecords: List<PrenatalRecordDto> = emptyList(),
    prenatalLoading: Boolean = false,
    prenatalError: String? = null,
    onPrenatalRecordClick: ((String) -> Unit)? = null
) {
    var showFilterSheet by remember {
        mutableStateOf(false)
    }

    var showAddMenu by remember {
        mutableStateOf(false)
    }

    var showPrenatalHistory by remember {
        mutableStateOf(false)
    }

    var selectedDailyLog by remember {
        mutableStateOf<DashboardLogUiItem?>(null)
    }

    val filterSheetState = rememberModalBottomSheetState()

    val today = overviewToday()

    val showPrenatalSection =
        state.selectedFilter == "All" ||
                state.selectedFilter == "Prenatal"

    val showDailySection =
        state.selectedFilter != "Prenatal"

    // Preserve prenatal search support.
    val matchingPrenatal = prenatalRecords.filter { record ->
        if (record.status == "cancelled") {
            false
        } else {
            val query = state.searchQuery.trim()

            query.isBlank() ||
                    record.visitDate.contains(
                        query,
                        ignoreCase = true
                    ) ||
                    overviewDisplayDate(record.visitDate).contains(
                        query,
                        ignoreCase = true
                    ) ||
                    record.status.contains(
                        query,
                        ignoreCase = true
                    ) ||
                    record.details.values.any { value ->
                        value.contains(query, ignoreCase = true)
                    } ||
                    "Prenatal Checkup".contains(
                        query,
                        ignoreCase = true
                    )
        }
    }

    // Scheduled appointments first.
    val scheduledPrenatal = matchingPrenatal
        .filter { it.status == "scheduled" }
        .sortedWith(
            compareBy<PrenatalRecordDto> {
                it.visitDate < today
            }.thenBy {
                it.visitDate
            }
        )

    // Completed visits second.
    val completedPrenatal = matchingPrenatal
        .filter { it.status == "completed" }
        .sortedWith(
            compareByDescending<PrenatalRecordDto> {
                it.visitDate
            }.thenByDescending {
                it.updatedAt?.time ?: 0L
            }
        )

    // All prenatal records in their established order.
    val allPrenatalRecords =
        scheduledPrenatal + completedPrenatal

    // Only two records appear on the main page.
    val overviewPrenatalRecords =
        allPrenatalRecords.take(PRENATAL_OVERVIEW_LIMIT)

    // Preserve teammates' Daily Check-In filtering.
    val filteredLogs = state.logs.filter { log ->
        val matchesSearch =
            state.searchQuery.isBlank() ||
                    log.category.contains(
                        state.searchQuery,
                        ignoreCase = true
                    ) ||
                    log.description.contains(
                        state.searchQuery,
                        ignoreCase = true
                    ) ||
                    log.time.contains(
                        state.searchQuery,
                        ignoreCase = true
                    )

        val matchesFilter =
            state.selectedFilter == "All" ||
                    log.category.equals(
                        state.selectedFilter,
                        ignoreCase = true
                    )

        matchesSearch && matchesFilter
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundCream,
                        PrimeraLilac.copy(alpha = 0.45f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Existing top bar and QR action.
            cit.edu.primera.ui.components.MainTopBar(
                title = "Check-Ins",
                onOpenDrawer = onOpenDrawer,
                onPrintQrClick = onPrintQrClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onFilterClick = {
                        showAddMenu = false
                        showFilterSheet = true
                    }
                )

                Spacer(Modifier.height(20.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = 100.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    if (showPrenatalSection) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween,
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Prenatal Checkups",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                // CHANGED:
                                // Open history overlay instead
                                // of the prenatal calendar.
                                TextButton(
                                    onClick = {
                                        showAddMenu = false
                                        showPrenatalHistory = true
                                    }
                                ) {
                                    Text(
                                        text = "View All",
                                        fontSize = 12.sp,
                                        color = PrimeraViolet
                                    )
                                }
                            }
                        }

                        if (prenatalLoading) {
                            item {
                                Text(
                                    text = "Loading prenatal records...",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        } else if (prenatalError != null) {
                            item {
                                Text(
                                    text = prenatalError,
                                    fontSize = 12.sp,
                                    color = Color(0xFFB3261E)
                                )
                            }
                        } else if (
                            overviewPrenatalRecords.isEmpty()
                        ) {
                            item {
                                Surface(
                                    color = SurfaceWhite,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        DashboardLogBorder
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement =
                                            Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "No prenatal records",
                                            fontSize = 14.sp,
                                            fontWeight =
                                                FontWeight.SemiBold,
                                            color = TextPrimary
                                        )

                                        Text(
                                            text = "Scheduled appointments and completed prenatal visits will appear here.",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // CHANGED:
                        // Show a maximum of two cards.
                        items(
                            items = overviewPrenatalRecords
                        ) { record ->
                            PrenatalHistoryCard(
                                record = record,
                                onClick = {
                                    showAddMenu = false

                                    val recordId = record.id

                                    if (
                                        !recordId.isNullOrBlank() &&
                                        onPrenatalRecordClick != null
                                    ) {
                                        onPrenatalRecordClick(recordId)
                                    } else {
                                        onNavigateToPrenatalCheckup
                                            ?.invoke()
                                    }
                                }
                            )
                        }
                    }

                    // Daily history now appears immediately
                    // after the two prenatal cards.
                    if (showDailySection) {
                        item {
                            Text(
                                text = "Daily Check-Ins",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(
                                    top = if (showPrenatalSection) {
                                        16.dp
                                    } else {
                                        4.dp
                                    },
                                    bottom = 4.dp
                                )
                            )
                        }

                        if (filteredLogs.isEmpty()) {
                            item {
                                Text(
                                    text = "No daily check-ins found.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(
                                        vertical = 8.dp
                                    )
                                )
                            }
                        }

                        items(filteredLogs) { log ->
                            LogCard(
                                category = log.category,
                                time = log.time,
                                description = log.description,
                                accentColor = log.accentColor,
                                onClick = {
                                    showAddMenu = false
                                    selectedDailyLog = log
                                }
                            )
                        }
                    }
                }
            }
        }

        // Existing outside-tap dismissal.
        if (showAddMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null
                    ) {
                        showAddMenu = false
                    }
            )
        }

        // Existing add menu.
        if (showAddMenu) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 24.dp,
                        bottom = 92.dp
                    ),
                shape = RoundedCornerShape(18.dp),
                color = SurfaceWhite,
                shadowElevation = 8.dp,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAddMenu = false
                                onNavigateToDailyCheckin()
                            }
                            .padding(
                                horizontal = 12.dp,
                                vertical = 12.dp
                            ),
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = PrimeraViolet,
                            modifier = Modifier.size(22.dp)
                        )

                        Text(
                            text = "Daily Check-In",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    HorizontalDivider(
                        color = InputBorder.copy(alpha = 0.5f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled =
                                    onNavigateToPrenatalCheckup != null
                            ) {
                                showAddMenu = false
                                onNavigateToPrenatalCheckup?.invoke()
                            }
                            .padding(
                                horizontal = 12.dp,
                                vertical = 12.dp
                            ),
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = PrimeraViolet,
                            modifier = Modifier.size(22.dp)
                        )

                        Text(
                            text = "Prenatal Checkup",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                showAddMenu = !showAddMenu
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 24.dp,
                    bottom = 24.dp
                )
                .size(56.dp),
            shape = CircleShape,
            containerColor = PrimeraViolet,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = if (showAddMenu) {
                    Icons.Default.Close
                } else {
                    Icons.Default.Add
                },
                contentDescription = if (showAddMenu) {
                    "Close add menu"
                } else {
                    "Open add menu"
                }
            )
        }

        // Existing filter bottom sheet.
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showFilterSheet = false
                },
                sheetState = filterSheetState,
                containerColor = SurfaceWhite,
                dragHandle = {
                    BottomSheetDefaults.DragHandle(
                        color = InputBorder
                    )
                }
            ) {
                FilterSheetContent(
                    selectedFilter = state.selectedFilter,
                    onFilterSelected = {
                        onFilterChange(it)
                        showFilterSheet = false
                    }
                )
            }
        }
    }

    // -------------------------------------------------
    // NEW: FULL-SCREEN PRENATAL HISTORY OVERLAY
    // -------------------------------------------------

    if (showPrenatalHistory) {
        Dialog(
            onDismissRequest = {
                showPrenatalHistory = false
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = BackgroundCream
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 20.dp,
                                end = 12.dp,
                                top = 12.dp,
                                bottom = 12.dp
                            ),
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Prenatal Checkup History",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Text(
                                text = "${allPrenatalRecords.size} records",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        IconButton(
                            onClick = {
                                showPrenatalHistory = false
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close history",
                                tint = TextPrimary
                            )
                        }
                    }

                    HorizontalDivider(
                        color = DashboardLogBorder
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 20.dp,
                            bottom = 32.dp
                        ),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        if (prenatalLoading) {
                            item {
                                Text(
                                    text = "Loading prenatal records...",
                                    color = TextSecondary
                                )
                            }
                        } else if (prenatalError != null) {
                            item {
                                Text(
                                    text = prenatalError,
                                    color = ErrorColor()
                                )
                            }
                        } else if (
                            allPrenatalRecords.isEmpty()
                        ) {
                            item {
                                Surface(
                                    color = SurfaceWhite,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        DashboardLogBorder
                                    )
                                ) {
                                    Text(
                                        text = "No prenatal records found.",
                                        color = TextSecondary,
                                        modifier = Modifier.padding(20.dp)
                                    )
                                }
                            }
                        }

                        if (scheduledPrenatal.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Scheduled Appointments",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            items(scheduledPrenatal) { record ->
                                PrenatalHistoryCard(
                                    record = record,
                                    onClick = {
                                        showPrenatalHistory = false

                                        val recordId = record.id

                                        if (
                                            !recordId.isNullOrBlank() &&
                                            onPrenatalRecordClick != null
                                        ) {
                                            onPrenatalRecordClick(recordId)
                                        } else {
                                            onNavigateToPrenatalCheckup
                                                ?.invoke()
                                        }
                                    }
                                )
                            }
                        }

                        if (completedPrenatal.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Completed Prenatal Visits",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(
                                        top = 12.dp
                                    )
                                )
                            }

                            items(completedPrenatal) { record ->
                                PrenatalHistoryCard(
                                    record = record,
                                    onClick = {
                                        showPrenatalHistory = false

                                        val recordId = record.id

                                        if (
                                            !recordId.isNullOrBlank() &&
                                            onPrenatalRecordClick != null
                                        ) {
                                            onPrenatalRecordClick(recordId)
                                        } else {
                                            onNavigateToPrenatalCheckup
                                                ?.invoke()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------
    // EXISTING DAILY CHECK-IN READ-ONLY PREVIEW
    // -------------------------------------------------

    val activeDailyLog = selectedDailyLog

    if (activeDailyLog != null) {
        val dailySheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            )

        ModalBottomSheet(
            onDismissRequest = {
                selectedDailyLog = null
            },
            sheetState = dailySheetState,
            containerColor = BackgroundCream
        ) {
            DailyCheckinDetailsSheet(
                log = activeDailyLog,
                onEdit = {
                    selectedDailyLog = null
                    onLogClick(activeDailyLog)
                },
                onClose = {
                    selectedDailyLog = null
                }
            )
        }
    }
}

// -------------------------------------------------
// DAILY CHECK-IN DETAILS
// -------------------------------------------------

@Composable
private fun DailyCheckinDetailsSheet(
    log: DashboardLogUiItem,
    onEdit: () -> Unit,
    onClose: () -> Unit
) {
    val savedFields = remember(log) {
        dailyPreviewFields(log)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                end = 24.dp,
                bottom = 32.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Daily Check-In Details",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Text(
            text = log.time,
            color = PrimeraViolet,
            fontWeight = FontWeight.SemiBold
        )

        DailyPreviewCard {
            Text(
                text = "Recorded Information",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            savedFields.forEachIndexed { index, field ->
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = field.label,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Text(
                        text = field.value,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }

                if (index < savedFields.lastIndex) {
                    HorizontalDivider(
                        color = DashboardLogBorder
                    )
                }
            }

            if (savedFields.isEmpty()) {
                Text(
                    text = "No additional information recorded.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        PrimeraGradientButton(
            text = "Edit Daily Check-In",
            onClick = onEdit
        )

        TextButton(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Close",
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun DailyPreviewCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SurfaceWhite,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp),
        content = content
    )
}

// -------------------------------------------------
// PRENATAL HISTORY CARD
// -------------------------------------------------

@Composable
private fun PrenatalHistoryCard(
    record: PrenatalRecordDto,
    onClick: () -> Unit
) {
    val completed = record.status == "completed"

    val accentColor = if (completed) {
        PrenatalCompletedGreen
    } else {
        PrimeraViolet
    }

    val clinic = record.details["clinic"].orEmpty()
    val time = record.details["time"].orEmpty()

    val dateAndTime = buildString {
        append(overviewDisplayDate(record.visitDate))

        if (time.isNotBlank()) {
            append(" · ")
            append(time)
        }
    }

    val completedSummary = buildList {
        val weeks = record.details["aogWeeks"].orEmpty()
        val days = record.details["aogDays"].orEmpty()
        val weight = record.details["weight"].orEmpty()
        val bloodPressure =
            record.details["bloodPressure"].orEmpty()

        if (weeks.isNotBlank()) {
            add(
                if (days.isNotBlank()) {
                    "$weeks weeks, $days days"
                } else {
                    "$weeks weeks"
                }
            )
        }

        if (weight.isNotBlank()) {
            add("$weight kg")
        }

        if (bloodPressure.isNotBlank()) {
            add("BP $bloodPressure")
        }
    }.joinToString(" · ")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = SurfaceWhite,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            DashboardLogBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 11.dp
            ),
            verticalArrangement =
                Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (completed) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.DateRange
                    },
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(19.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (completed) {
                            "Completed Prenatal Visit"
                        } else {
                            "Scheduled Appointment"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = dateAndTime,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (completed) {
                            "Completed"
                        } else {
                            "Scheduled"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = accentColor,
                        modifier = Modifier.padding(
                            horizontal = 7.dp,
                            vertical = 4.dp
                        )
                    )
                }
            }

            if (clinic.isNotBlank()) {
                Text(
                    text = clinic,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (completed && completedSummary.isNotBlank()) {
                Text(
                    text = completedSummary,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// -------------------------------------------------
// EXISTING FILTER SHEET
// -------------------------------------------------

@Composable
private fun FilterSheetContent(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf(
        "All",
        "Prenatal",
        "Fetal Movement",
        "Nutrition",
        "Pain",
        "Symptom",
        "Mood"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom = 48.dp,
                start = 24.dp,
                end = 24.dp
            )
    ) {
        Text(
            text = "Filter by Category",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        filters.forEach { filter ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onFilterSelected(filter)
                    }
                    .padding(vertical = 16.dp),
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text = filter,
                    fontSize = 16.sp,
                    color = if (selectedFilter == filter) {
                        PrimeraViolet
                    } else {
                        TextPrimary
                    },
                    fontWeight = if (selectedFilter == filter) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
                )

                if (selectedFilter == filter) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = PrimeraViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (filter != filters.last()) {
                HorizontalDivider(
                    color = InputBorder.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// -------------------------------------------------
// EXISTING SEARCH BAR
// -------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Search in history",
                color = TextHint
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = TextSecondary
            )
        },
        trailingIcon = {
            IconButton(onClick = onFilterClick) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = "Filters",
                    tint = TextPrimary
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = SurfaceWhite,
            focusedContainerColor = SurfaceWhite,
            unfocusedBorderColor = InputBorder,
            focusedBorderColor = PrimeraViolet
        ),
        singleLine = true
    )
}

// Existing error color styling.
private fun ErrorColor(): Color = Color(0xFFB3261E)

// -------------------------------------------------
// COMPOSE PREVIEW
// -------------------------------------------------

@Preview(showBackground = true)
@Composable
private fun CheckinsOverviewPreview() {
    PrimeraTheme {
        CheckinsOverviewContent(
            state = CheckinsOverviewUiState(
                logs = listOf(
                    DashboardLogUiItem(
                        id = "1",
                        category = "Symptom",
                        description =
                            "Weight: 80 kg; Blood Sugar: 95 mg/dL; " +
                                    "Symptoms: Aching Head; Mood: Normal; " +
                                    "Medicine: Syringe",
                        time = "Nov 6, 2026",
                        accentColor = LogPain
                    )
                )
            ),
            prenatalRecords = listOf(
                PrenatalRecordDto(
                    id = "preview1",
                    visitDate = "2026-11-18",
                    status = "scheduled",
                    details = mapOf(
                        "time" to "10:00 AM",
                        "clinic" to "Sample Hospital"
                    )
                ),
                PrenatalRecordDto(
                    id = "preview2",
                    visitDate = "2026-11-09",
                    status = "completed",
                    details = mapOf(
                        "aogWeeks" to "32",
                        "aogDays" to "2",
                        "weight" to "62.5",
                        "bloodPressure" to "110/70"
                    )
                )
            ),
            onSearchQueryChange = {},
            onFilterChange = {},
            onNavigateToDailyCheckin = {},
            onNavigateToPrenatalCheckup = {},
            onPrenatalRecordClick = {},
            onLogClick = {}
        )
    }
}

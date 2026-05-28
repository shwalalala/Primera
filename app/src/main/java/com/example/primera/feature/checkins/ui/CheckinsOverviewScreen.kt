package com.example.primera.feature.checkins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.primera.core.theme.*
import com.example.primera.feature.dashboard.ui.DashboardLogUiItem
import com.example.primera.ui.components.LogCard

@Composable
fun CheckinsOverviewScreen(
    onNavigateToDailyCheckin: () -> Unit,
    onLogClick: (DashboardLogUiItem) -> Unit,
    viewModel: CheckinsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.overviewState.collectAsStateWithLifecycle()

    CheckinsOverviewContent(
        state = state,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onNavigateToDailyCheckin = onNavigateToDailyCheckin,
        onLogClick = onLogClick,
        modifier = modifier
    )
}

@Composable
fun CheckinsOverviewContent(
    state: CheckinsOverviewUiState,
    onSearchQueryChange: (String) -> Unit,
    onNavigateToDailyCheckin: () -> Unit,
    onLogClick: (DashboardLogUiItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.statusBarsPadding())
            Spacer(Modifier.height(24.dp))
            
            Text(
                text = "Check-Ins",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(Modifier.height(24.dp))
            
            SearchBar(
                query = state.searchQuery,
                onQueryChange = onSearchQueryChange
            )
            
            Spacer(Modifier.height(24.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val filteredLogs = if (state.searchQuery.isBlank()) {
                    state.logs
                } else {
                    state.logs.filter { 
                        it.category.contains(state.searchQuery, ignoreCase = true) || 
                        it.description.contains(state.searchQuery, ignoreCase = true)
                    }
                }
                
                items(filteredLogs) { log ->
                    LogCard(
                        category = log.category,
                        time = log.time,
                        description = log.description,
                        accentColor = log.accentColor,
                        onClick = { onLogClick(log) }
                    )
                }
            }
        }
        
        FloatingActionButton(
            onClick = onNavigateToDailyCheckin,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .size(56.dp),
            shape = CircleShape,
            containerColor = PrimeraViolet,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Check-in")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search in history", color = TextHint) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
        trailingIcon = { Icon(Icons.Default.Tune, contentDescription = null, tint = TextPrimary) },
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

@Preview(showBackground = true)
@Composable
private fun CheckinsOverviewPreview() {
    PrimeraTheme {
        CheckinsOverviewContent(
            state = CheckinsOverviewUiState(
                logs = listOf(
                    DashboardLogUiItem(
                        id = "1",
                        category = "Nausea",
                        description = "Felt sick in the morning",
                        time = "11:02 AM",
                        accentColor = LogPain
                    ),
                    DashboardLogUiItem(
                        id = "2",
                        category = "Back Pain",
                        description = "Lower back pain after walking",
                        time = "8:32 AM",
                        accentColor = LogPain
                    )
                )
            ),
            onSearchQueryChange = {},
            onNavigateToDailyCheckin = {},
            onLogClick = {}
        )
    }
}

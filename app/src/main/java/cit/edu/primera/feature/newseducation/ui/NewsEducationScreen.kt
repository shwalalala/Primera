package cit.edu.primera.feature.newseducation.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cit.edu.primera.core.di.ViewModelProvider
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.InputBorder
import cit.edu.primera.core.theme.MenuButtonBorder
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.SurfaceWhite
import cit.edu.primera.core.theme.TextHint
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.feature.newseducation.domain.EducationalResource
import cit.edu.primera.ui.components.FeatureTopBar
import cit.edu.primera.ui.components.FullScreenLoadingOverlay

@Composable
fun NewsEducationScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewsEducationViewModel = viewModel(factory = ViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
        Column(modifier = Modifier.fillMaxSize()) {
            FeatureTopBar(
                title = "News & Education",
                onBack = onBack
            )

            // Search Bar section on top of page
            EducationalSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                onClearQuery = viewModel::onClearSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )

            if (uiState.isLoading) {
                FullScreenLoadingOverlay()
            } else if (uiState.filteredResources.isEmpty()) {
                EmptyStateView(
                    searchQuery = uiState.searchQuery,
                    onClearSearch = viewModel::onClearSearch,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.filteredResources,
                        key = { it.id }
                    ) { resource ->
                        EducationalResourceCard(resource = resource)
                    }
                    item {
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EducationalSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp,
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(InputBorder, InputBorder)))
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Search articles, guides, handbooks...",
                    fontSize = 14.sp,
                    color = TextHint
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = PrimeraViolet,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(onClick = onClearQuery) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimeraViolet,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                cursorColor = PrimeraViolet
            ),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun EducationalResourceCard(
    resource: EducationalResource,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    val openLinkAction = {
        try {
            uriHandler.openUri(resource.url)
        } catch (_: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.url))
                context.startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { openLinkAction() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(MenuButtonBorder, MenuButtonBorder)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Badges row: Category & Source
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimeraViolet.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = resource.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimeraViolet
                    )
                }

                // Source Name
                Text(
                    text = resource.sourceName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }

            Spacer(Modifier.height(10.dp))

            // Resource Title
            Text(
                text = resource.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))

            // Resource Description
            Text(
                text = resource.description,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(14.dp))

            // Bottom Redirect Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundCream)
                    .clickable { openLinkAction() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PrimeraViolet.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = PrimeraViolet,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = resource.url.removePrefix("https://").removePrefix("http://"),
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Read",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimeraViolet
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Redirect link",
                        tint = PrimeraViolet,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    searchQuery: String,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(PrimeraViolet.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = PrimeraViolet,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "No Educational Resources Found",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = if (searchQuery.isNotBlank()) {
                "No results matched \"$searchQuery\". Try checking for typos or using different keywords."
            } else {
                "No educational resources available at this time."
            },
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (searchQuery.isNotBlank()) {
            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onClearSearch() }
                    .border(1.dp, PrimeraViolet, RoundedCornerShape(16.dp)),
                color = SurfaceWhite
            ) {
                Text(
                    text = "Clear Search",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimeraViolet,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

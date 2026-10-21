package cit.edu.primera.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.SectionHeaderBorder
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary

@Composable
fun DashboardSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onViewAll: (() -> Unit)? = null,
    onAdd: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.weight(1f, fill = false),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onViewAll != null) {
                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable { onViewAll() }
                )
                if (onAdd != null) {
                    Spacer(Modifier.width(10.dp))
                }
            }
            if (onAdd != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(1.dp, SectionHeaderBorder, CircleShape)
                        .clickable { onAdd() }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add",
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer(scaleX = 1.2f, scaleY = 1.2f)

                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionHeadersPreview() {
    PrimeraTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashboardSectionHeader(
                title = "Recent Health Logs",
                onViewAll = {},
                onAdd = {}
            )
            DashboardSectionHeader(title = "Wellness Goals")
        }
    }
}



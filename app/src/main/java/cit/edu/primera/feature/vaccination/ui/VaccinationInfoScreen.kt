package cit.edu.primera.feature.vaccination.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cit.edu.primera.R
import cit.edu.primera.core.di.ViewModelProvider
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.MenuButtonBorder
import cit.edu.primera.core.theme.PrimeraLilac
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.SurfaceWhite
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.feature.vaccination.data.VaccinationRepositoryImpl
import cit.edu.primera.feature.vaccination.domain.VaccineDetail
import cit.edu.primera.feature.vaccination.domain.VaccineIconType
import cit.edu.primera.feature.vaccination.domain.VaccineScheduleRow
import cit.edu.primera.ui.components.FeatureTopBar

@Composable
fun VaccinationInfoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VaccinationViewModel = viewModel(factory = ViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    VaccinationInfoContent(
        state = state,
        onBack = onBack,
        onOpenSourceDialog = viewModel::onOpenSourceDialog,
        onDismissSourceDialog = viewModel::onDismissSourceDialog,
        modifier = modifier,
    )
}

@Composable
fun VaccinationInfoContent(
    state: VaccinationUiState,
    onBack: () -> Unit,
    onOpenSourceDialog: () -> Unit,
    onDismissSourceDialog: () -> Unit,
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
        Column(modifier = Modifier.fillMaxSize()) {
            FeatureTopBar(
                title = "Vaccination Info",
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Header Banner Card
                HeaderBannerCard()

                Spacer(Modifier.height(16.dp))

                // Detailed Vaccine Breakdowns
                VaccineDetailsSection(vaccines = state.vaccines)

                Spacer(Modifier.height(20.dp))

                // Maternal Immunization Schedule Table
                MaternalScheduleTableSection(rows = state.scheduleRows)

                Spacer(Modifier.height(20.dp))

                // RSV Recommended Timeframe Focus Card
                RsvFocusCard()

                Spacer(Modifier.height(20.dp))

                // Footnotes & Medical Guidelines
                ClinicalNotesSection()

                Spacer(Modifier.height(20.dp))

                // Bottom-most Link: Info given by our Obstetrician
                ObstetricianSourceLinkCard(
                    onOpenSourceDialog = onOpenSourceDialog
                )

                Spacer(Modifier.height(28.dp))
            }
        }
    }

    if (state.showSourceDialog) {
        VaccinationImageDialog(
            onDismiss = onDismissSourceDialog
        )
    }
}

@Composable
private fun HeaderBannerCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, MenuButtonBorder),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PrimeraViolet.copy(alpha = 0.08f),
                            SurfaceWhite
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimeraViolet.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Vaccines,
                        contentDescription = "Vaccination Schedule",
                        tint = PrimeraViolet,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Maternal Immunization Schedule",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Protect yourself & your child",
                        fontSize = 12.sp,
                        color = PrimeraViolet,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Every pregnant woman should receive vaccination against whooping cough (Tdap), influenza, COVID-19, and RSV using currently available vaccines.",
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun MaternalScheduleTableSection(rows: List<VaccineScheduleRow>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, MenuButtonBorder),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Recommended Vaccine Timeline",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Scroll horizontally to view full trimester breakdown\n(Weeks 1-40)",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 13.sp
            )

            Spacer(Modifier.height(14.dp))

            val scrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                Column {
                    // Header Row 1: Trimesters
                    Row(
                        modifier = Modifier
                            .background(PrimeraViolet)
                            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    ) {
                        TableCell(
                            text = "Recommended\nvaccines",
                            width = 130.dp,
                            isHeader = true,
                            headerColor = Color.White
                        )
                        TableCell(
                            text = "First trimester\n(0-13 weeks)",
                            width = 160.dp,
                            isHeader = true,
                            headerColor = Color.White
                        )
                        TableCell(
                            text = "Second trimester\n(14-27 weeks)",
                            width = 160.dp,
                            isHeader = true,
                            headerColor = Color.White
                        )
                        TableCell(
                            text = "Third trimester\n(28-40 weeks)",
                            width = 160.dp,
                            isHeader = true,
                            headerColor = Color.White
                        )
                    }

                    // Header Row 2: Weeks
                    Row(
                        modifier = Modifier.background(PrimeraViolet.copy(alpha = 0.85f))
                    ) {
                        TableCell(text = "Weeks", width = 130.dp, isHeader = true, headerColor = Color.White, fontSize = 11.sp)
                        TableCell(text = "1-3  |  4-6  |  7-9  |  10-13", width = 160.dp, isHeader = true, headerColor = Color.White, fontSize = 10.sp)
                        TableCell(text = "14-16 | 17-19 | 20-23 | 24-27", width = 160.dp, isHeader = true, headerColor = Color.White, fontSize = 10.sp)
                        TableCell(text = "28-30 | 31-33 | 34-36 | 37-40", width = 160.dp, isHeader = true, headerColor = Color.White, fontSize = 10.sp)
                    }

                    // Vaccine Rows
                    rows.forEach { row ->
                        VaccineTableRow(
                            name = row.name,
                            timeframeLabel = row.timeframeLabel,
                            barColor = Color(row.barColorHex),
                            spanFirst = row.spanFirst,
                            spanSecond = row.spanSecond,
                            spanThird = row.spanThird,
                            customNote = row.customNote
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VaccineTableRow(
    name: String,
    timeframeLabel: String,
    barColor: Color,
    spanFirst: Boolean,
    spanSecond: Boolean,
    spanThird: Boolean,
    customNote: String? = null
) {
    Row(
        modifier = Modifier
            .background(SurfaceWhite)
            .border(0.5.dp, MenuButtonBorder),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(
            text = name,
            width = 130.dp,
            isBold = true,
            textColor = TextPrimary
        )

        val totalSpanWidth = 480.dp
        Box(
            modifier = Modifier
                .width(totalSpanWidth)
                .height(42.dp)
                .background(BackgroundCream.copy(alpha = 0.5f))
                .padding(horizontal = 4.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (spanFirst && spanSecond && spanThird) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(6.dp))
                            .background(barColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = timeframeLabel,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val startOffset = if (!spanSecond && spanThird) 320.dp else 120.dp
                    val barWidth = if (!spanSecond && spanThird) 120.dp else 220.dp

                    Row(modifier = Modifier.fillMaxSize()) {
                        Spacer(Modifier.width(startOffset))
                        Box(
                            modifier = Modifier
                                .width(barWidth)
                                .height(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(barColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = customNote ?: timeframeLabel,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    isBold: Boolean = false,
    headerColor: Color = Color.Unspecified,
    textColor: Color = TextPrimary,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isHeader) headerColor else textColor,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun RsvFocusCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = PrimeraViolet.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, MenuButtonBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimeraViolet),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "RSV Timing Focus",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                Text(
                    text = "Why RSV vaccine should be given within the recommended timeframe?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            BulletPointItem(
                text = "It allows time for the mother to build and pass protection against RSV to the baby."
            )

            Spacer(Modifier.height(8.dp))

            BulletPointItem(
                text = "Narrowing the timeframe helps to lower the risk of the baby being born too early (known as preterm birth)."
            )
        }
    }
}

@Composable
private fun BulletPointItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp, end = 10.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(PrimeraViolet)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = TextPrimary,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun VaccineDetailsSection(vaccines: List<VaccineDetail>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Vaccine Information Breakdown",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        vaccines.forEachIndexed { index, vaccine ->
            val icon = when (vaccine.iconType) {
                VaccineIconType.HEALTH_SAFETY -> Icons.Default.HealthAndSafety
                VaccineIconType.SHIELD -> Icons.Default.Shield
                VaccineIconType.MEDICAL_SERVICES -> Icons.Default.MedicalServices
                VaccineIconType.VACCINES -> Icons.Default.Vaccines
            }

            VaccineDetailCard(
                title = vaccine.title,
                timing = vaccine.timing,
                description = vaccine.description,
                badgeColor = Color(vaccine.colorHex),
                icon = icon
            )

            if (index < (vaccines.size - 1)) {
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun VaccineDetailCard(
    title: String,
    timing: String,
    description: String,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, MenuButtonBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = badgeColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        lineHeight = 17.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = timing,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ClinicalNotesSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = BackgroundCream,
        border = BorderStroke(1.dp, MenuButtonBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = "Important Clinical Notes & Guidelines",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "• RSV Timing Variations: POGS advises a single dose at 32-36 weeks gestation, while PIDSOG recommends vaccination from 28 weeks onwards. Recommended timing for ABRYSVO® is between weeks 24 and 36 of gestation.\n\n" +
                        "• 1st Trimester Precaution: Based on POGS Practice Bulletin No. 1, immunization should be avoided in most patients during the 1st trimester to avoid coincidental association with spontaneous abortion (except as specifically recommended by your obstetrician).\n\n" +
                        "• Always consult your healthcare provider to tailor immunization to your medical history.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ObstetricianSourceLinkCard(
    onOpenSourceDialog: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenSourceDialog() },
        shape = RoundedCornerShape(18.dp),
        color = PrimeraViolet.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, PrimeraViolet.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(PrimeraViolet),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Obstetrician Verification",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Info given by our Obstetrician",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Tap to view source document & medical schedule image",
                    fontSize = 11.sp,
                    color = PrimeraViolet,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 13.sp
                )
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open Source Document",
                tint = PrimeraViolet,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
//
//@Composable
//private fun VaccinationImageDialog(
//    onDismiss: () -> Unit
//) {
//    Dialog(onDismissRequest = onDismiss) {
//        Surface(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 12.dp),
//            shape = RoundedCornerShape(20.dp),
//            color = SurfaceWhite,
//            border = BorderStroke(1.5.dp, PrimeraViolet),
//            tonalElevation = 8.dp
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "Obstetrician Source Document",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = TextPrimary
//                    )
//
//                    Box(
//                        modifier = Modifier
//                            .size(32.dp)
//                            .clip(CircleShape)
//                            .background(Color.LightGray.copy(alpha = 0.3f))
//                            .clickable { onDismiss() },
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Close,
//                            contentDescription = "Close",
//                            tint = TextPrimary,
//                            modifier = Modifier.size(18.dp)
//                        )
//                    }
//                }
//
//                Spacer(Modifier.height(12.dp))
//
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(480.dp)
//                        .verticalScroll(rememberScrollState())
//                        .horizontalScroll(rememberScrollState())
//                        .clip(RoundedCornerShape(12.dp))
//                        .border(1.dp, MenuButtonBorder, RoundedCornerShape(12.dp))
//                ) {
//                    Image(
//                        painter = painterResource(id = R.drawable.vaccination_info),
//                        contentDescription = "Vaccination Info Document",
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                }
//
//                Spacer(Modifier.height(16.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    TextButton(onClick = onDismiss) {
//                        Text("Close", color = PrimeraViolet, fontWeight = FontWeight.Bold)
//                    }
//                }
//            }
//        }
//    }
//}
@Composable
private fun VaccinationImageDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = SurfaceWhite,
            border = BorderStroke(1.5.dp, PrimeraViolet),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Obstetrician Source Document",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray.copy(alpha = 0.3f))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MenuButtonBorder, RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.vaccination_info),
                        contentDescription = "Vaccination Info Document",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        contentScale = ContentScale.FillWidth
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = PrimeraViolet, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VaccinationInfoScreenPreview() {
    val repository = VaccinationRepositoryImpl()
    val previewState = VaccinationUiState(
        vaccines = repository.getVaccineDetails(),
        scheduleRows = repository.getScheduleRows(),
        sourceInfo = repository.getSourceInfo()
    )
    PrimeraTheme {
        VaccinationInfoContent(
            state = previewState,
            onBack = {},
            onOpenSourceDialog = {},
            onDismissSourceDialog = {}
        )
    }
}

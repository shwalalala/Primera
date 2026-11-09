package cit.edu.primera.feature.dashboard.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.primera.core.theme.BackgroundCream
import cit.edu.primera.core.theme.PrimeraTheme
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.SurfaceWhite
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import cit.edu.primera.feature.dashboard.domain.DashboardBusinessLogic

@Composable
fun BabySizeText(
    babySize: String,
    modifier: Modifier = Modifier,
) {
    val text = if (babySize.startsWith("Your baby", ignoreCase = true)) {
        babySize
    } else {
        "Your baby is the size of a $babySize!"
    }

    Text(
        text = text,
        fontSize = 14.sp,
        color = TextSecondary,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 420, heightDp = 1800)
@Composable
fun AllBabySizesPreview() {
    PrimeraTheme {
        Column(
            modifier = Modifier
                .background(BackgroundCream)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Week-by-Week Baby Size Guide",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            val keyWeeks = listOf(
                1, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                16, 17, 18, 19, 20, 21, 22, 25, 29, 33, 37, 40,
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                maxItemsInEachRow = 3,
            ) {
                keyWeeks.forEach { week ->
                    val size = DashboardBusinessLogic.getBabySize(week)
                    val iconRes = DashboardBusinessLogic.getBabyIllustration(week)

                    Surface(
                        modifier = Modifier.width(115.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceWhite,
                        tonalElevation = 2.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = if (week == 1) "Wk 1-3" else if (week == 40) "Wk 40+" else "Week $week",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimeraViolet,
                            )
                            Spacer(Modifier.height(8.dp))
                            Image(
                                painter = painterResource(id = iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = size,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BabyDevelopmentPreview() {
    PrimeraTheme {
        BabySizeText(babySize = "Large eggplant")
    }
}

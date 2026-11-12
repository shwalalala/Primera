
package cit.edu.primera.feature.checkins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cit.edu.primera.core.theme.PrimeraViolet
import cit.edu.primera.core.theme.TextPrimary
import cit.edu.primera.core.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ==================================================
// SHARED CALENDAR DATA
// ==================================================

private data class CheckinCalendarDay(
    val date: String,
    val number: Int,
    val weekday: String
)

// ==================================================
// DATE FORMAT HELPERS
// ==================================================

private fun localDateFormat(): SimpleDateFormat {
    return SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    ).apply {
        isLenient = false
    }
}

private fun utcDateFormat(): SimpleDateFormat {
    return SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    ).apply {
        timeZone = TimeZone.getTimeZone("UTC")
        isLenient = false
    }
}

// ==================================================
// SHARED SELECTABLE CALENDAR
//
// compact = true:
//   Daily Check-In's tighter spacing.
//
// compact = false:
//   Original prenatal calendar spacing.
//
// All date and indicator behavior remains intact.
// ==================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableCheckinCalendar(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    allowFutureDates: Boolean = false,
    scheduledDates: Set<String> = emptySet(),
    completedDates: Set<String> = emptySet(),
    compact: Boolean = false
) {
    var showDatePicker by remember {
        mutableStateOf(false)
    }

    val today = localDateFormat().format(Date())

    // Prenatal appointments may extend two years
    // into the future. Daily Check-In stops today.
    val lastAllowedDate = remember(
        today,
        allowFutureDates
    ) {
        if (allowFutureDates) {
            val calendar = Calendar.getInstance().apply {
                add(Calendar.YEAR, 2)
            }
            localDateFormat().format(calendar.time)
        } else {
            today
        }
    }

    // ----------------------------------------------
    // GENERATE CALENDAR DAYS
    // ----------------------------------------------

    val calendarDays = remember(
        today,
        lastAllowedDate
    ) {
        val days = mutableListOf<CheckinCalendarDay>()

        val calendar = Calendar.getInstance().apply {
            set(
                1900,
                Calendar.JANUARY,
                1,
                12,
                0,
                0
            )
            set(Calendar.MILLISECOND, 0)
        }

        val weekdays = listOf(
            "S", "M", "T", "W", "T", "F", "S"
        )

        val formatter = localDateFormat()

        while (
            formatter.format(calendar.time) <= lastAllowedDate
        ) {
            days.add(
                CheckinCalendarDay(
                    date = formatter.format(calendar.time),
                    number = calendar.get(
                        Calendar.DAY_OF_MONTH
                    ),
                    weekday = weekdays[
                        calendar.get(
                            Calendar.DAY_OF_WEEK
                        ) - 1
                    ]
                )
            )

            calendar.add(
                Calendar.DAY_OF_MONTH,
                1
            )
        }

        days
    }

    // ----------------------------------------------
    // CURRENT SELECTION
    // ----------------------------------------------

    val selectedIndex = remember(
        selectedDate,
        calendarDays
    ) {
        calendarDays.indexOfFirst {
            it.date == selectedDate
        }.let { index ->
            if (index >= 0) {
                index
            } else {
                calendarDays.indexOfFirst {
                    it.date == today
                }.coerceAtLeast(0)
            }
        }
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (
                selectedIndex - 3
                ).coerceAtLeast(0)
    )

    LaunchedEffect(
        selectedIndex,
        allowFutureDates
    ) {
        listState.scrollToItem(
            (selectedIndex - 3).coerceAtLeast(0)
        )
    }

    // ----------------------------------------------
    // DATE HEADING
    // ----------------------------------------------

    val selectedDateObject = remember(
        selectedDate
    ) {
        runCatching {
            localDateFormat().parse(selectedDate)
        }.getOrNull() ?: Date()
    }

    val heading = remember(
        selectedDateObject
    ) {
        SimpleDateFormat(
            "MMMM d",
            Locale.getDefault()
        ).format(selectedDateObject)
    }

    val showIndicators =
        scheduledDates.isNotEmpty() ||
                completedDates.isNotEmpty()

    // ----------------------------------------------
    // COMPACT SIZING
    // Only Daily Check-In sets compact = true.
    // ----------------------------------------------

    val headingFontSize = if (compact) {
        22.sp
    } else {
        25.sp
    }

    val headingGap = if (compact) {
        6.dp
    } else {
        14.dp
    }

    val dateSize = if (compact) {
        40.dp
    } else {
        42.dp
    }

    val dateSpacing = if (compact) {
        6.dp
    } else {
        8.dp
    }

    val dayVerticalSpacing = if (compact) {
        5.dp
    } else {
        8.dp
    }

    val bottomSpacing = if (compact) {
        2.dp
    } else {
        10.dp
    }

    // ----------------------------------------------
    // MAIN CALENDAR UI
    // ----------------------------------------------

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // CLICKABLE DATE HEADING

        TextButton(
            onClick = {
                showDatePicker = true
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = heading,
                    fontSize = headingFontSize,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Open monthly calendar",
                    tint = PrimeraViolet
                )
            }
        }

        Text(
            text = "Tap the date to open calendar",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(
            modifier = Modifier.height(headingGap)
        )

        // HORIZONTAL DATE STRIP

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                dateSpacing
            ),
            contentPadding = PaddingValues(
                horizontal = 24.dp
            )
        ) {
            items(
                items = calendarDays,
                key = { it.date }
            ) { day ->
                val isSelected =
                    day.date == selectedDate

                val isScheduled =
                    scheduledDates.contains(day.date)

                val isCompleted =
                    completedDates.contains(day.date)

                Column(
                    modifier = Modifier.clickable {
                        onDateSelected(day.date)
                    },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        dayVerticalSpacing
                    )
                ) {
                    Text(
                        text = day.weekday,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    // DATE CIRCLE

                    Box(
                        modifier = Modifier
                            .size(dateSize)
                            .background(
                                color = if (isSelected) {
                                    PrimeraViolet.copy(
                                        alpha = 0.18f
                                    )
                                } else {
                                    Color.White
                                },
                                shape = CircleShape
                            )
                            .border(
                                width = if (isSelected) {
                                    2.dp
                                } else {
                                    1.dp
                                },
                                color = if (isSelected) {
                                    PrimeraViolet
                                } else {
                                    PrimeraViolet.copy(
                                        alpha = 0.16f
                                    )
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.number.toString(),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            },
                            color = TextPrimary
                        )
                    }

                    // PRENATAL EVENT DOTS
                    // Only visible with provided event dates.

                    if (showIndicators) {
                        Row(
                            modifier = Modifier.height(7.dp),
                            horizontalArrangement = Arrangement.spacedBy(
                                3.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isScheduled) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            PrimeraViolet,
                                            CircleShape
                                        )
                                )
                            }

                            if (isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            Color(0xFF79B49E),
                                            CircleShape
                                        )
                                )
                            }

                            if (
                                !isScheduled &&
                                !isCompleted
                            ) {
                                Spacer(
                                    modifier = Modifier.size(
                                        6.dp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(bottomSpacing)
        )

        // HELPER TEXT AND TODAY ACTION

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (allowFutureDates) {
                    "Swipe to explore dates"
                } else {
                    "Swipe to select a previous date"
                },
                fontSize = 11.sp,
                color = TextSecondary
            )

            TextButton(
                onClick = {
                    onDateSelected(today)
                }
            ) {
                Text(
                    text = "Today",
                    fontSize = 12.sp,
                    color = PrimeraViolet
                )
            }
        }

        // PRENATAL LEGEND
        // Preserved for appointments and visits.

        if (showIndicators) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 4.dp
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            PrimeraViolet,
                            CircleShape
                        )
                )

                Spacer(
                    modifier = Modifier.size(6.dp)
                )

                Text(
                    text = "Scheduled",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(
                    modifier = Modifier.size(18.dp)
                )

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            Color(0xFF79B49E),
                            CircleShape
                        )
                )

                Spacer(
                    modifier = Modifier.size(6.dp)
                )

                Text(
                    text = "Completed",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }

    // ==============================================
    // MONTHLY DATE PICKER
    // All original date restrictions retained.
    // ==============================================

    if (showDatePicker) {
        val todayYear = Calendar.getInstance().get(
            Calendar.YEAR
        )

        val lastAllowedYear = remember(
            lastAllowedDate
        ) {
            runCatching {
                localDateFormat()
                    .parse(lastAllowedDate)
                    ?.let { date ->
                        Calendar.getInstance().apply {
                            time = date
                        }.get(Calendar.YEAR)
                    }
            }.getOrNull() ?: todayYear
        }

        val utcFormat = remember {
            utcDateFormat()
        }

        val initialMillis = remember(
            selectedDate
        ) {
            runCatching {
                utcFormat.parse(
                    selectedDate
                )?.time
            }.getOrNull()
        }

        val lastAllowedMillis = remember(
            lastAllowedDate
        ) {
            runCatching {
                utcFormat.parse(
                    lastAllowedDate
                )?.time
            }.getOrNull()
                ?: System.currentTimeMillis()
        }

        val firstAllowedMillis = remember {
            utcFormat.parse(
                "1900-01-01"
            )?.time ?: 0L
        }

        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            yearRange = 1900..lastAllowedYear,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(
                    utcTimeMillis: Long
                ): Boolean {
                    return utcTimeMillis in
                            firstAllowedMillis..lastAllowedMillis
                }

                override fun isSelectableYear(
                    year: Int
                ): Boolean {
                    return year in 1900..lastAllowedYear
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        pickerState.selectedDateMillis
                            ?.let { millis ->
                                if (
                                    millis in
                                    firstAllowedMillis..lastAllowedMillis
                                ) {
                                    val chosenDate =
                                        utcFormat.format(
                                            Date(millis)
                                        )

                                    onDateSelected(chosenDate)
                                }
                            }

                        showDatePicker = false
                    },
                    enabled = pickerState.selectedDateMillis
                        ?.let { millis ->
                            millis in
                                    firstAllowedMillis..lastAllowedMillis
                        } ?: false
                ) {
                    Text("Select Date")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = pickerState,
                showModeToggle = true
            )
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.TealPrimary
import java.util.Calendar
import java.util.Locale

@Composable
fun MonthCalendarView(
    year: Int,
    month: Int, // 1 - 12
    selectedDate: String, // YYYY-MM-DD
    datesWithFiles: List<String>,
    onDateSelected: (String) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthPickerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // English + Bengali month labels
    val monthNames = listOf(
        "January / জানুয়ারি", "February / ফেব্রুয়ারি", "March / মার্চ",
        "April / এপ্রিল", "May / মে", "June / জুন",
        "July / জুলাই", "August / আগস্ট", "September / সেপ্টেম্বর",
        "October / অক্টোবর", "November / নভেম্বর", "December / ডিসেম্বর"
    )
    val monthTitle = monthNames.getOrElse(month - 1) { "Month $month" }

    // Calculate days in month and starting day of week
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month - 1)
    cal.set(Calendar.DAY_OF_MONTH, 1)

    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday...

    val todayCal = Calendar.getInstance()
    val isCurrentMonthAndYear = todayCal.get(Calendar.YEAR) == year && (todayCal.get(Calendar.MONTH) + 1) == month
    val todayDay = if (isCurrentMonthAndYear) todayCal.get(Calendar.DAY_OF_MONTH) else -1

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("month_calendar_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Month Header with Prev / Title / Next
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onPrevMonth,
                    modifier = Modifier.testTag("prev_month_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onMonthPickerClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("month_title_selector")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "$monthTitle $year",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.testTag("next_month_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day of Week Header (Sun to Sat)
            val weekDays = listOf("রবি\nSun", "সোম\nMon", "মঙ্গল\nTue", "বুধ\nWed", "বৃহঃ\nThu", "শুক্র\nFri", "শনি\nSat")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekDays.forEach { dayLabel ->
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid Days
            val totalCells = 42 // 6 weeks * 7 days
            val offset = firstDayOfWeek - 1 // 0-indexed offset

            for (week in 0 until 6) {
                val weekStart = week * 7
                if (weekStart >= offset + maxDays && week > 3) {
                    break // Skip empty last row if not needed
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (dayCol in 0 until 7) {
                        val cellIndex = weekStart + dayCol
                        val dayNumber = cellIndex - offset + 1

                        if (dayNumber in 1..maxDays) {
                            val dayString = String.format(Locale.US, "%04d-%02d-%02d", year, month, dayNumber)
                            val isSelected = selectedDate == dayString
                            val isToday = dayNumber == todayDay
                            val hasFiles = datesWithFiles.contains(dayString)

                            val bgColor by animateColorAsState(
                                targetValue = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else -> Color.Transparent
                                },
                                label = "dayBgColor"
                            )

                            val textColor = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                isToday -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgColor)
                                    .then(
                                        if (isToday && !isSelected) {
                                            Modifier.border(
                                                1.dp,
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(12.dp)
                                            )
                                        } else Modifier
                                    )
                                    .clickable { onDateSelected(dayString) }
                                    .testTag("calendar_day_$dayNumber"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$dayNumber",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                            color = textColor,
                                            fontSize = 13.sp
                                        )
                                    )

                                    if (hasFiles) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.onPrimary else AmberSecondary
                                                )
                                        )
                                    }
                                }
                            }
                        } else {
                            // Blank cell
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FileType
import com.example.data.model.VaultFile
import com.example.ui.components.FileCard
import com.example.ui.components.MonthCalendarView
import com.example.ui.dialogs.MonthYearPickerDialog
import com.example.ui.viewmodel.VaultNavTab
import com.example.ui.viewmodel.VaultViewModel

@Composable
fun HomeScreen(
    viewModel: VaultViewModel,
    modifier: Modifier = Modifier
) {
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val datesWithFiles by viewModel.datesWithFiles.collectAsStateWithLifecycle()
    val dateFiles by viewModel.filesForSelectedDate.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()

    var showMonthPicker by remember { mutableStateOf(false) }
    var selectedTypeFilter by remember { mutableStateOf<String?>(null) }

    // Filter files for selected date
    val filteredFiles = remember(dateFiles, selectedTypeFilter) {
        if (selectedTypeFilter == null) dateFiles
        else dateFiles.filter { it.fileType.equals(selectedTypeFilter, ignoreCase = true) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen_content"),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // App Header
            item {
                HomeHeader(
                    totalFiles = totalCount,
                    onSearchClick = { viewModel.setTab(VaultNavTab.ALL_FILES) }
                )
            }

            // Month Calendar
            item {
                MonthCalendarView(
                    year = selectedYear,
                    month = selectedMonth,
                    selectedDate = selectedDate,
                    datesWithFiles = datesWithFiles,
                    onDateSelected = { viewModel.selectDate(it) },
                    onPrevMonth = { viewModel.prevMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    onMonthPickerClick = { showMonthPicker = true }
                )
            }

            // Date Files Section Header
            item {
                DateSectionHeader(
                    selectedDate = selectedDate,
                    fileCount = dateFiles.size,
                    onExportMonth = {
                        viewModel.exportMonthAsZip(dateFiles)
                    }
                )
            }

            // File Type Filter Chips
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedTypeFilter == null,
                            onClick = { selectedTypeFilter = null },
                            label = { Text("সব (${dateFiles.size})", fontSize = 12.sp) },
                            modifier = Modifier.testTag("filter_all_type")
                        )
                    }
                    items(FileType.entries.filter { it != FileType.OTHER }) { type ->
                        val count = dateFiles.count { it.fileType.equals(type.id, ignoreCase = true) }
                        FilterChip(
                            selected = selectedTypeFilter == type.id,
                            onClick = {
                                selectedTypeFilter = if (selectedTypeFilter == type.id) null else type.id
                            },
                            leadingIcon = {
                                Icon(type.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text("${type.banglaName} ($count)", fontSize = 12.sp) },
                            modifier = Modifier.testTag("filter_type_${type.id}")
                        )
                    }
                }
            }

            // Files list or empty state
            if (filteredFiles.isEmpty()) {
                item {
                    EmptyDateState(
                        selectedDate = selectedDate,
                        onAddClick = { viewModel.openAddFileSheet() }
                    )
                }
            } else {
                items(filteredFiles, key = { it.id }) { file ->
                    FileCard(
                        file = file,
                        onClick = { viewModel.openFileDetail(file) },
                        onToggleFavorite = { viewModel.toggleFavorite(file) }
                    )
                }
            }
        }

        // Floating Action Button to Add New File / Note
        FloatingActionButton(
            onClick = { viewModel.openAddFileSheet() },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .testTag("add_file_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add File")
                Spacer(modifier = Modifier.width(6.dp))
                Text("ফাইল যোগ করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            initialYear = selectedYear,
            initialMonth = selectedMonth,
            onDismiss = { showMonthPicker = false },
            onConfirm = { y, m ->
                showMonthPicker = false
                viewModel.setYearAndMonth(y, m)
            }
        )
    }
}

@Composable
private fun HomeHeader(
    totalFiles: Int,
    onSearchClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "YearVault",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🔒 অফলাইন",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp
                        )
                    )
                }
            }
            Text(
                text = "মোট $totalFiles টি ফাইল সুরক্ষিত আছে",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        IconButton(
            onClick = onSearchClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .testTag("header_search_button")
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DateSectionHeader(
    selectedDate: String,
    fileCount: Int,
    onExportMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "তারিখ: $selectedDate",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "$fileCount টি ফাইল ও রেকর্ড রয়েছে",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun EmptyDateState(
    selectedDate: String,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("empty_date_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$selectedDate তারিখে কোনো ফাইল নেই",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "বিল, রসিদ, প্রেসক্রিপশন, চুক্তি বা নোট যোগ করে এই তারিখের সাথে সংরক্ষণ করুন।",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onAddClick,
                modifier = Modifier.testTag("empty_add_file_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("এই তারিখে ফাইল যোগ করুন")
            }
        }
    }
}

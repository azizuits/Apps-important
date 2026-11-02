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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FileCategory
import com.example.data.model.FileType
import com.example.data.model.VaultFile
import com.example.ui.components.FileCard
import com.example.ui.viewmodel.VaultViewModel

@Composable
fun AllFilesScreen(
    viewModel: VaultViewModel,
    modifier: Modifier = Modifier
) {
    val allFiles by viewModel.allFiles.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val fileTypeFilter by viewModel.selectedFileTypeFilter.collectAsStateWithLifecycle()
    val isMultiSelectActive by viewModel.isMultiSelectActive.collectAsStateWithLifecycle()
    val selectedFileIds by viewModel.selectedFileIds.collectAsStateWithLifecycle()

    // Filter files based on search, category, and type
    val filteredFiles = remember(allFiles, searchQuery, categoryFilter, fileTypeFilter) {
        allFiles.filter { file ->
            val matchesSearch = searchQuery.isBlank() ||
                    file.title.contains(searchQuery, ignoreCase = true) ||
                    file.tags.contains(searchQuery, ignoreCase = true) ||
                    file.note.contains(searchQuery, ignoreCase = true)

            val matchesCategory = categoryFilter == null || file.category.equals(categoryFilter, ignoreCase = true)
            val matchesType = fileTypeFilter == null || file.fileType.equals(fileTypeFilter, ignoreCase = true)

            matchesSearch && matchesCategory && matchesType
        }
    }

    // Group filtered files by Month (e.g. "2026-11", "2026-10"...)
    val groupedByMonth = remember(filteredFiles) {
        filteredFiles.groupBy { it.month }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("all_files_screen")
    ) {
        // Multi-Select Action Bar OR Regular Top Header
        if (isMultiSelectActive) {
            MultiSelectToolbar(
                selectedCount = selectedFileIds.size,
                onSelectAll = { viewModel.selectAllFiles(filteredFiles) },
                onExportZip = { viewModel.exportSelectedAsZip(filteredFiles) },
                onDelete = { viewModel.deleteSelectedFiles(filteredFiles) },
                onCancel = { viewModel.clearSelection() }
            )
        } else {
            // Search Bar & Multi-Select Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("নাম, ট্যাগ বা নোট দিয়ে খুঁজুন...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_text_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.toggleMultiSelectMode() },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("toggle_multiselect_button")
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Multi Select",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = categoryFilter == null,
                    onClick = { viewModel.setCategoryFilter(null) },
                    label = { Text("সব ক্যাটাগরি (${allFiles.size})", fontSize = 12.sp) },
                    modifier = Modifier.testTag("cat_filter_all")
                )
            }
            items(FileCategory.entries) { cat ->
                val count = allFiles.count { it.category.equals(cat.id, ignoreCase = true) }
                FilterChip(
                    selected = categoryFilter == cat.id,
                    onClick = { viewModel.setCategoryFilter(cat.id) },
                    leadingIcon = {
                        Icon(cat.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    label = { Text("${cat.banglaName} ($count)", fontSize = 12.sp) },
                    modifier = Modifier.testTag("cat_filter_${cat.id}")
                )
            }
        }

        // File Type Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(FileType.entries.filter { it != FileType.OTHER }) { type ->
                val count = allFiles.count { it.fileType.equals(type.id, ignoreCase = true) }
                FilterChip(
                    selected = fileTypeFilter == type.id,
                    onClick = { viewModel.setFileTypeFilter(type.id) },
                    label = { Text("${type.banglaName} ($count)", fontSize = 11.sp) },
                    modifier = Modifier.testTag("type_filter_${type.id}")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Files List grouped by Month
        if (filteredFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "কোনো ফাইল পাওয়া যায়নি",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                groupedByMonth.forEach { (monthKey, filesInMonth) ->
                    item(key = "month_header_$monthKey") {
                        MonthGroupHeader(
                            monthKey = monthKey,
                            count = filesInMonth.size,
                            onExportMonthZip = {
                                viewModel.exportMonthAsZip(filesInMonth)
                            }
                        )
                    }

                    items(filesInMonth, key = { it.id }) { file ->
                        val isSelected = selectedFileIds.contains(file.id)
                        FileCard(
                            file = file,
                            onClick = { viewModel.openFileDetail(file) },
                            onToggleFavorite = { viewModel.toggleFavorite(file) },
                            isMultiSelectActive = isMultiSelectActive,
                            isSelected = isSelected,
                            onSelectToggle = { viewModel.toggleFileSelection(file.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthGroupHeader(
    monthKey: String,
    count: Int,
    onExportMonthZip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "মাস: $monthKey",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = "$count টি ফাইল রয়েছে",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        OutlinedButton(
            onClick = onExportMonthZip,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.testTag("export_month_zip_$monthKey")
        ) {
            Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("মাসের ZIP", fontSize = 11.sp)
        }
    }
}

@Composable
private fun MultiSelectToolbar(
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onExportZip: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$selectedCount টি ফাইল নির্বাচিত",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSelectAll, modifier = Modifier.testTag("select_all_button")) {
                    Icon(Icons.Default.SelectAll, contentDescription = "Select All")
                }

                IconButton(
                    onClick = onExportZip,
                    enabled = selectedCount > 0,
                    modifier = Modifier.testTag("export_selected_zip_button")
                ) {
                    Icon(Icons.Default.Archive, contentDescription = "Export ZIP")
                }

                IconButton(
                    onClick = onDelete,
                    enabled = selectedCount > 0,
                    modifier = Modifier.testTag("delete_selected_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

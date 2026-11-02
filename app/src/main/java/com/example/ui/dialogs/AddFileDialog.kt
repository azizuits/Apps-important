package com.example.ui.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileCategory
import com.example.data.model.FileType
import com.example.ui.theme.AmberSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddFileDialog(
    initialDate: String,
    onDismiss: () -> Unit,
    onSaveNote: (title: String, content: String, date: String, category: String, tags: String, isFav: Boolean) -> Unit,
    onSaveFile: (uri: Uri, title: String, date: String, category: String, tags: String, forcedType: FileType?, note: String, isFav: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 0: Image / Photo, 1: Document / PDF / Excel, 2: Note / Notepad
    var selectedTab by remember { mutableIntStateOf(0) }

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedUriName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf(initialDate) }
    var selectedCategory by remember { mutableStateOf(FileCategory.FINANCIAL) }
    var noteContent by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }

    val selectedTags = remember { mutableStateListOf<String>() }
    var customTagInput by remember { mutableStateOf("") }

    // Android Photo Picker (zero-permission, Play policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            selectedUriName = "Photo_${System.currentTimeMillis()}.jpg"
            if (title.isBlank()) {
                title = "ছবি - $dateString"
            }
        }
    }

    // Document Picker
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            selectedUriName = uri.lastPathSegment ?: "document"
            if (title.isBlank()) {
                title = "ডকুমেন্ট - $dateString"
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("add_file_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "নতুন ফাইল বা নোট যোগ করুন",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_add_file_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Type Selector Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text("ছবি / ফটো", fontSize = 12.sp) },
                    modifier = Modifier.testTag("tab_photo")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text("PDF / ফাইল", fontSize = 12.sp) },
                    modifier = Modifier.testTag("tab_document")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text("টেক্সট নোট", fontSize = 12.sp) },
                    modifier = Modifier.testTag("tab_note")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // File selection action button (for Photo / Document)
            if (selectedTab == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            1.5.dp,
                            if (selectedUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(14.dp)
                        )
                        .background(
                            if (selectedUri != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(16.dp)
                        .testTag("pick_photo_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (selectedUri != null) Icons.Default.Check else Icons.Default.Image,
                            contentDescription = null,
                            tint = if (selectedUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedUri != null) "ছবি সিলেক্ট করা হয়েছে ✓" else "গ্যালারি থেকে ছবি নির্বাচন করুন",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            } else if (selectedTab == 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            1.5.dp,
                            if (selectedUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(14.dp)
                        )
                        .background(
                            if (selectedUri != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                        .clickable {
                            docPickerLauncher.launch(
                                arrayOf(
                                    "application/pdf",
                                    "application/msword",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    "application/vnd.ms-excel",
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    "text/plain",
                                    "*/*"
                                )
                            )
                        }
                        .padding(16.dp)
                        .testTag("pick_doc_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (selectedUri != null) Icons.Default.Check else Icons.Default.Description,
                            contentDescription = null,
                            tint = if (selectedUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedUri != null) "ডকুমেন্ট সিলেক্ট করা হয়েছে: ${selectedUriName.take(24)}..." else "PDF, Word, Excel বা ফাইল পছন্দ করুন",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("শিরোনাম / ফাইলের নাম") },
                placeholder = { Text("যেমন: বিদ্যুৎ বিল নভেম্বর, মেডিকেল রিপোর্ট, প্রজেক্ট চুক্তি") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_file_title")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Date & Star Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = dateString,
                    onValueChange = { dateString = it },
                    label = { Text("তারিখ (YYYY-MM-DD)") },
                    leadingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_file_date")
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Favorite Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isFavorite) AmberSecondary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { isFavorite = !isFavorite }
                        .padding(horizontal = 12.dp, vertical = 14.dp)
                        .testTag("toggle_favorite_add"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (isFavorite) AmberSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFavorite) "জরুরি ★" else "সাধারণ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isFavorite) AmberSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Selection Carousel / Chips
            Text(
                text = "ক্যাটাগরি নির্বাচন করুন:",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FileCategory.entries.forEach { cat ->
                    val isCatSelected = cat == selectedCategory
                    FilterChip(
                        selected = isCatSelected,
                        onClick = {
                            selectedCategory = cat
                            // Pre-fill suggested tags
                            selectedTags.clear()
                            selectedTags.addAll(cat.defaultTags.take(3))
                        },
                        leadingIcon = {
                            Icon(cat.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text("${cat.banglaName} / ${cat.englishName}", fontSize = 11.sp) },
                        modifier = Modifier.testTag("category_chip_${cat.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Suggested Tags + Custom Tags
            Text(
                text = "ট্যাগ (সহজে সার্চ করার জন্য):",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Default tags from selected category
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                selectedCategory.defaultTags.forEach { defaultTag ->
                    val isTagSelected = selectedTags.contains(defaultTag)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isTagSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable {
                                if (isTagSelected) selectedTags.remove(defaultTag)
                                else selectedTags.add(defaultTag)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (isTagSelected) "#$defaultTag ✓" else "+ $defaultTag",
                            fontSize = 11.sp,
                            color = if (isTagSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Custom tag field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customTagInput,
                    onValueChange = { customTagInput = it },
                    placeholder = { Text("নতুন ট্যাগ লিখুন...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val clean = customTagInput.trim().replace("#", "")
                        if (clean.isNotBlank() && !selectedTags.contains(clean)) {
                            selectedTags.add(clean)
                            customTagInput = ""
                        }
                    },
                    modifier = Modifier.testTag("add_custom_tag_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Tag")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Note Content input (essential for notes, optional remarks for files)
            OutlinedTextField(
                value = noteContent,
                onValueChange = { noteContent = it },
                label = { Text(if (selectedTab == 2) "নোটের বিস্তারিত লিখুন *" else "সংক্ষিপ্ত নোট / বিবরণ (ঐচ্ছিক)") },
                placeholder = {
                    Text(
                        if (selectedTab == 2) "আপনার চিন্তাভাবনা, ডায়েরি বা গুরুত্বপূর্ণ তথ্য এখানে লিখুন..."
                        else "বিল পরিশোধ আইডি, ডাক্তারের পরামর্শ বা রেফারেন্স..."
                    )
                },
                minLines = if (selectedTab == 2) 5 else 2,
                maxLines = 8,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_note_content")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    val finalTitle = title.ifBlank {
                        if (selectedTab == 2) "নোট - $dateString" else "ফাইল - $dateString"
                    }
                    val tagsString = selectedTags.joinToString(", ")

                    if (selectedTab == 2) {
                        // Text Note
                        onSaveNote(
                            finalTitle,
                            noteContent.ifBlank { "নোটের তথ্য খালি" },
                            dateString,
                            selectedCategory.id,
                            tagsString,
                            isFavorite
                        )
                    } else {
                        // Photo or Document
                        val uri = selectedUri
                        if (uri != null) {
                            val forced = if (selectedTab == 0) FileType.IMAGE else null
                            onSaveFile(
                                uri,
                                finalTitle,
                                dateString,
                                selectedCategory.id,
                                tagsString,
                                forced,
                                noteContent,
                                isFavorite
                            )
                        } else {
                            // If user didn't pick an external file, save as structured entry/note
                            onSaveNote(
                                finalTitle,
                                noteContent.ifBlank { "সংরক্ষিত ফাইল এন্ট্রি ($finalTitle)" },
                                dateString,
                                selectedCategory.id,
                                tagsString,
                                isFavorite
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_file_button")
            ) {
                Text(
                    text = "ভল্টে সংরক্ষণ করুন",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class FileType(
    val id: String,
    val displayName: String,
    val banglaName: String,
    val icon: ImageVector,
    val color: Color
) {
    IMAGE(
        id = "image",
        displayName = "Photo / Image",
        banglaName = "ছবি",
        icon = Icons.Default.Image,
        color = Color(0xFF38BDF8)
    ),
    PDF(
        id = "pdf",
        displayName = "PDF Document",
        banglaName = "পিডিএফ",
        icon = Icons.Default.PictureAsPdf,
        color = Color(0xFFF43F5E)
    ),
    DOCX(
        id = "docx",
        displayName = "Word / Doc",
        banglaName = "ডকুমেন্ট",
        icon = Icons.Default.Description,
        color = Color(0xFF3B82F6)
    ),
    EXCEL(
        id = "excel",
        displayName = "Excel / Sheet",
        banglaName = "এক্সেল",
        icon = Icons.Default.TableChart,
        color = Color(0xFF10B981)
    ),
    NOTE(
        id = "note",
        displayName = "Text Note",
        banglaName = "নোট",
        icon = Icons.Default.EditNote,
        color = Color(0xFFF59E0B)
    ),
    OTHER(
        id = "other",
        displayName = "Other File",
        banglaName = "অন্যান্য",
        icon = Icons.Default.Folder,
        color = Color(0xFF8B5CF6)
    );

    companion object {
        fun fromId(id: String): FileType {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: OTHER
        }

        fun fromExtension(extension: String): FileType {
            return when (extension.lowercase()) {
                "jpg", "jpeg", "png", "webp", "gif", "bmp" -> IMAGE
                "pdf" -> PDF
                "doc", "docx", "rtf" -> DOCX
                "xls", "xlsx", "csv" -> EXCEL
                "txt", "md", "note" -> NOTE
                else -> OTHER
            }
        }
    }
}

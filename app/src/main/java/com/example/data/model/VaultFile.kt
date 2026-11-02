package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "vault_files",
    indices = [
        Index(value = ["date"]),
        Index(value = ["month"]),
        Index(value = ["year"]),
        Index(value = ["category"]),
        Index(value = ["isFavorite"])
    ]
)
data class VaultFile(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val fileType: String, // "image", "pdf", "docx", "excel", "note", "other"
    val filePath: String, // Relative internal storage path (e.g. "files/2026/11/abc.jpg") or note content
    val thumbnailPath: String? = null,
    val date: String, // "YYYY-MM-DD" e.g. "2026-11-02"
    val month: String, // "YYYY-MM" e.g. "2026-11"
    val year: Int, // e.g. 2026
    val note: String = "", // Detailed note / description
    val category: String = "general", // "financial", "health", etc.
    val tags: String = "", // Comma-separated tags
    val isFavorite: Boolean = false,
    val fileSize: Long = 0L, // In bytes
    val originalFileName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val categoryEnum: FileCategory
        get() = FileCategory.fromId(category)

    val fileTypeEnum: FileType
        get() = FileType.fromId(fileType)

    val tagList: List<String>
        get() = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val formattedSize: String
        get() {
            if (fileSize <= 0) return "0 B"
            val kb = fileSize / 1024.0
            if (kb < 1024) return String.format("%.1f KB", kb)
            val mb = kb / 1024.0
            if (mb < 1024) return String.format("%.1f MB", mb)
            val gb = mb / 1024.0
            return String.format("%.2f GB", gb)
        }
}

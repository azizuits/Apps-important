package com.example

import com.example.data.model.FileCategory
import com.example.data.model.FileType
import com.example.data.model.VaultFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testFileTypeDetection() {
        assertEquals(FileType.IMAGE, FileType.fromExtension("jpg"))
        assertEquals(FileType.PDF, FileType.fromExtension("pdf"))
        assertEquals(FileType.DOCX, FileType.fromExtension("docx"))
        assertEquals(FileType.EXCEL, FileType.fromExtension("xlsx"))
        assertEquals(FileType.NOTE, FileType.fromExtension("txt"))
        assertEquals(FileType.OTHER, FileType.fromExtension("xyz"))
    }

    @Test
    fun testFileCategoryDefaultTags() {
        val fin = FileCategory.fromId("financial")
        assertEquals("Financial", fin.englishName)
        assertEquals("আর্থিক", fin.banglaName)
        assertTrue(fin.defaultTags.contains("বিল"))

        val health = FileCategory.fromId("health")
        assertEquals("Health", health.englishName)
        assertTrue(health.defaultTags.contains("প্রেসক্রিপশন"))
    }

    @Test
    fun testVaultFileModelAndFormatting() {
        val file = VaultFile(
            id = "test_1",
            title = "বিদ্যুৎ বিল",
            fileType = "pdf",
            filePath = "files/2026/11/bill.pdf",
            date = "2026-11-02",
            month = "2026-11",
            year = 2026,
            category = "home",
            tags = "বিদ্যুৎ, গ্যাস",
            fileSize = 1024 * 500L
        )

        assertEquals("500.0 KB", file.formattedSize)
        assertEquals(listOf("বিদ্যুৎ", "গ্যাস"), file.tagList)
        assertEquals(FileCategory.HOME, file.categoryEnum)
        assertEquals(FileType.PDF, file.fileTypeEnum)
    }
}

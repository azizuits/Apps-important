package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.db.VaultDao
import com.example.data.model.FileCategory
import com.example.data.model.FileType
import com.example.data.model.VaultFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class VaultRepository(
    private val context: Context,
    private val dao: VaultDao
) {
    val allFiles: Flow<List<VaultFile>> = dao.getAllFiles()
    val favoriteFiles: Flow<List<VaultFile>> = dao.getFavoriteFiles()
    val totalCount: Flow<Int> = dao.getTotalFileCount()
    val totalStorage: Flow<Long> = dao.getTotalStorageBytes()

    fun getFilesByDate(date: String): Flow<List<VaultFile>> = dao.getFilesByDate(date)

    fun getFilesByMonth(month: String): Flow<List<VaultFile>> = dao.getFilesByMonth(month)

    fun getFilesByYear(year: Int): Flow<List<VaultFile>> = dao.getFilesByYear(year)

    fun searchFiles(query: String): Flow<List<VaultFile>> = dao.searchFiles(query)

    fun getDatesWithFiles(month: String): Flow<List<String>> = dao.getDatesWithFilesForMonth(month)

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        dao.updateFavorite(id, isFavorite)
    }

    suspend fun deleteFile(file: VaultFile) = withContext(Dispatchers.IO) {
        // Delete local physical file if exists
        try {
            val physicalFile = File(context.filesDir, file.filePath)
            if (physicalFile.exists()) {
                physicalFile.delete()
            }
        } catch (_: Exception) {}
        dao.deleteFile(file)
    }

    suspend fun deleteMultipleFiles(files: List<VaultFile>) = withContext(Dispatchers.IO) {
        files.forEach { file ->
            try {
                val physicalFile = File(context.filesDir, file.filePath)
                if (physicalFile.exists()) {
                    physicalFile.delete()
                }
            } catch (_: Exception) {}
        }
        dao.deleteFiles(files)
    }

    suspend fun updateFile(file: VaultFile) = withContext(Dispatchers.IO) {
        dao.updateFile(file.copy(updatedAt = System.currentTimeMillis()))
    }

    /**
     * Save a new text note into the vault
     */
    suspend fun saveNote(
        title: String,
        noteContent: String,
        date: String, // YYYY-MM-DD
        category: String,
        tags: String,
        isFavorite: Boolean = false
    ): VaultFile = withContext(Dispatchers.IO) {
        val parts = date.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val month = if (parts.size >= 2) "${parts[0]}-${parts[1]}" else "2026-11"
        val id = UUID.randomUUID().toString()

        val dir = File(context.filesDir, "files/$year/${parts.getOrElse(1) { "01" }}")
        if (!dir.exists()) dir.mkdirs()

        val fileName = "${date}_${id.take(8)}_note.txt"
        val noteFile = File(dir, fileName)
        noteFile.writeText(noteContent)

        val vaultFile = VaultFile(
            id = id,
            title = title.ifBlank { "নোট - $date" },
            fileType = FileType.NOTE.id,
            filePath = "files/$year/${parts.getOrElse(1) { "01" }}/$fileName",
            date = date,
            month = month,
            year = year,
            note = noteContent,
            category = category,
            tags = tags,
            isFavorite = isFavorite,
            fileSize = noteFile.length(),
            originalFileName = fileName
        )
        dao.insertFile(vaultFile)
        vaultFile
    }

    /**
     * Import an external file / image Uri into the vault's structured local storage
     */
    suspend fun saveImportedFile(
        uri: Uri,
        title: String,
        date: String, // YYYY-MM-DD
        category: String,
        tags: String,
        forcedType: FileType? = null,
        noteContent: String = "",
        isFavorite: Boolean = false
    ): VaultFile = withContext(Dispatchers.IO) {
        val parts = date.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val month = if (parts.size >= 2) "${parts[0]}-${parts[1]}" else "2026-11"
        val id = UUID.randomUUID().toString()

        val originalName = getFileNameFromUri(uri)
        val extension = originalName.substringAfterLast('.', "")
        val detectedType = forcedType ?: FileType.fromExtension(extension)

        val targetDir = File(context.filesDir, "files/$year/${parts.getOrElse(1) { "01" }}")
        if (!targetDir.exists()) targetDir.mkdirs()

        val safeExt = if (extension.isNotBlank()) ".$extension" else ""
        val targetFileName = "${date}_${id.take(8)}_file$safeExt"
        val targetFile = File(targetDir, targetFileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }

        val vaultFile = VaultFile(
            id = id,
            title = title.ifBlank { originalName.substringBeforeLast('.') },
            fileType = detectedType.id,
            filePath = "files/$year/${parts.getOrElse(1) { "01" }}/$targetFileName",
            date = date,
            month = month,
            year = year,
            note = noteContent,
            category = category,
            tags = tags,
            isFavorite = isFavorite,
            fileSize = targetFile.length(),
            originalFileName = originalName
        )
        dao.insertFile(vaultFile)
        vaultFile
    }

    /**
     * Creates a ZIP archive for a list of VaultFiles with a structured index manifest
     */
    suspend fun createZipArchive(
        files: List<VaultFile>,
        zipName: String = "YearVault_Export"
    ): File = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val zipFile = File(exportDir, "${zipName}_$timeStamp.zip")

        ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
            // Write README / Manifest
            val manifestBuilder = StringBuilder()
            manifestBuilder.appendLine("==============================================")
            manifestBuilder.appendLine("YearVault - Archive Export")
            manifestBuilder.appendLine("Export Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())}")
            manifestBuilder.appendLine("Total Files: ${files.size}")
            manifestBuilder.appendLine("==============================================\n")

            files.forEachIndexed { index, vf ->
                val categoryName = vf.categoryEnum.englishName + " (" + vf.categoryEnum.banglaName + ")"
                manifestBuilder.appendLine("[${index + 1}] ${vf.title}")
                manifestBuilder.appendLine("    Date: ${vf.date} (Month: ${vf.month})")
                manifestBuilder.appendLine("    Type: ${vf.fileType}")
                manifestBuilder.appendLine("    Category: $categoryName")
                if (vf.tags.isNotBlank()) manifestBuilder.appendLine("    Tags: ${vf.tags}")
                if (vf.note.isNotBlank()) manifestBuilder.appendLine("    Note: ${vf.note}")
                manifestBuilder.appendLine("----------------------------------------------")

                // Add file to ZIP
                val fileOnDisk = File(context.filesDir, vf.filePath)
                val cleanTitle = vf.title.replace(Regex("[^a-zA-Z0-9._\\-\\u0980-\\u09FF]"), "_")
                val ext = when (vf.fileTypeEnum) {
                    FileType.NOTE -> ".txt"
                    FileType.PDF -> ".pdf"
                    FileType.DOCX -> ".docx"
                    FileType.EXCEL -> ".xlsx"
                    FileType.IMAGE -> ".jpg"
                    FileType.OTHER -> ".dat"
                }

                val zipEntryPath = "${vf.year}/${vf.month}/${vf.date}_${cleanTitle.take(30)}$ext"
                zipOut.putNextEntry(ZipEntry(zipEntryPath))

                if (fileOnDisk.exists() && fileOnDisk.length() > 0) {
                    FileInputStream(fileOnDisk).use { input ->
                        input.copyTo(zipOut)
                    }
                } else if (vf.fileTypeEnum == FileType.NOTE || vf.note.isNotBlank()) {
                    // Write note content directly if file missing
                    val content = "TITLE: ${vf.title}\nDATE: ${vf.date}\nCATEGORY: $categoryName\nTAGS: ${vf.tags}\n\n${vf.note}"
                    zipOut.write(content.toByteArray(Charsets.UTF_8))
                } else {
                    val dummy = "YearVault placeholder for: ${vf.title}\nDate: ${vf.date}"
                    zipOut.write(dummy.toByteArray(Charsets.UTF_8))
                }
                zipOut.closeEntry()
            }

            // Put Manifest into ZIP
            zipOut.putNextEntry(ZipEntry("ARCHIVE_MANIFEST.txt"))
            zipOut.write(manifestBuilder.toString().toByteArray(Charsets.UTF_8))
            zipOut.closeEntry()
        }

        zipFile
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var name = "file_${System.currentTimeMillis()}"
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    name = cursor.getString(nameIndex) ?: name
                }
            }
        } catch (_: Exception) {}
        return name
    }

    /**
     * Seeds initial demo items across 2026 months so user has an active, rich vault immediately
     */
    suspend fun seedSampleDataIfEmpty() = withContext(Dispatchers.IO) {
        val existing = dao.getFileById("seed_sample_1")
        if (existing != null) return@withContext

        val samples = listOf(
            VaultFile(
                id = "seed_sample_1",
                title = "বিদ্যুৎ ও গ্যাস বিল (নভেম্বর)",
                fileType = FileType.PDF.id,
                filePath = "sample/nov_utility_bill.pdf",
                date = "2026-11-02",
                month = "2026-11",
                year = 2026,
                note = "ডেসকো ও তিতাস গ্যাস বিল পরিশোধিত। ট্রানজেকশন আইডি: TXN892301।",
                category = FileCategory.HOME.id,
                tags = "বিদ্যুৎ বিল, গ্যাস বিল, নভেম্বর",
                isFavorite = true,
                fileSize = 248 * 1024L
            ),
            VaultFile(
                id = "seed_sample_2",
                title = "বার্ষিক হেলথ চেকআপ ও টেস্ট রিপোর্ট",
                fileType = FileType.PDF.id,
                filePath = "sample/health_checkup_report.pdf",
                date = "2026-11-02",
                month = "2026-11",
                year = 2026,
                note = "ব্লাড টেস্ট, লিপিড প্রোফাইল এবং প্রেসক্রিপশন। ডক্টর পরবর্তী ভিজিট দিয়েছেন ডিসেম্বরে।",
                category = FileCategory.HEALTH.id,
                tags = "টেস্ট রিপোর্ট, প্রেসক্রিপশন, স্বাস্থ্য",
                isFavorite = true,
                fileSize = 1040 * 1024L
            ),
            VaultFile(
                id = "seed_sample_3",
                title = "২০২৬ সালের কর্মপরিকল্পনা ও লক্ষ্য",
                fileType = FileType.NOTE.id,
                filePath = "sample/personal_goals_2026.txt",
                date = "2026-11-01",
                month = "2026-11",
                year = 2026,
                note = "১. নতুন সার্টিফিকেশন সম্পন্ন করা\n২. জরুরি ফান্ডে ২০% সঞ্চয় বৃদ্ধি\n৩. নিয়মিত শারীরিক ব্যায়াম ও রিডিং গোল।",
                category = FileCategory.PERSONAL.id,
                tags = "ডায়েরি, বছরের লক্ষ্য, পরিকল্পনা",
                isFavorite = false,
                fileSize = 420L
            ),
            VaultFile(
                id = "seed_sample_4",
                title = "অফিস প্রজেক্ট চুক্তি ও অফার লেটার",
                fileType = FileType.DOCX.id,
                filePath = "sample/work_agreement.docx",
                date = "2026-10-25",
                month = "2026-10",
                year = 2026,
                note = "কনসাল্টিং প্রজেক্টের চুক্তিপত্র। মেয়াদ ১ বছর।",
                category = FileCategory.WORK.id,
                tags = "চুক্তি, অফার লেটার, প্রজেক্ট ফাইল",
                isFavorite = true,
                fileSize = 512 * 1024L
            ),
            VaultFile(
                id = "seed_sample_5",
                title = "জাতীয় পরিচয়পত্র ও পাসপোর্ট কপি",
                fileType = FileType.IMAGE.id,
                filePath = "sample/nid_passport.jpg",
                date = "2026-10-14",
                month = "2026-10",
                year = 2026,
                note = "স্মার্ট এনআইডি এবং রিনিউড পাসপোর্টের স্ক্যান কপি।",
                category = FileCategory.FAMILY.id,
                tags = "এনআইডি, পাসপোর্ট, পরিচয়পত্র",
                isFavorite = true,
                fileSize = 1840 * 1024L
            ),
            VaultFile(
                id = "seed_sample_6",
                title = "গাড়ির ইন্স্যুরেন্স ও ট্যাক্স টোকেন",
                fileType = FileType.PDF.id,
                filePath = "sample/car_insurance.pdf",
                date = "2026-09-18",
                month = "2026-09",
                year = 2026,
                note = "গ্রিন ডেল্টা ইন্স্যুরেন্স পলিসি এবং বিআরটিএ ট্যাক্স টোকেন রিনিউয়াল কাগজ।",
                category = FileCategory.VEHICLE.id,
                tags = "গাড়ির কাগজ, ইন্স্যুরেন্স, ট্যাক্স টোকেন",
                isFavorite = false,
                fileSize = 380 * 1024L
            ),
            VaultFile(
                id = "seed_sample_7",
                title = "মাসিক খরচের এক্সেল শিট (বাজেট)",
                fileType = FileType.EXCEL.id,
                filePath = "sample/monthly_budget.xlsx",
                date = "2026-09-01",
                month = "2026-09",
                year = 2026,
                note = "বাসা ভাড়া, বাজার, বিদ্যুৎ ও সেভিংস হিসাব।",
                category = FileCategory.FINANCIAL.id,
                tags = "বিল, রসিদ, ব্যাংক স্টেটমেন্ট",
                isFavorite = false,
                fileSize = 92 * 1024L
            )
        )
        dao.insertFiles(samples)
    }
}

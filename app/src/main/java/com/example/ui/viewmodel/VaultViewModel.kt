package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.VaultDatabase
import com.example.data.model.FileCategory
import com.example.data.model.FileType
import com.example.data.model.VaultFile
import com.example.data.repository.VaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class VaultNavTab {
    CALENDAR,
    ALL_FILES,
    FAVORITES,
    STATS_ARCHIVE
}

enum class SortOption {
    NEWEST,
    OLDEST,
    TITLE,
    SIZE
}

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VaultRepository = VaultRepository(
        application,
        VaultDatabase.getInstance(application).vaultDao()
    )

    private val prefs = application.getSharedPreferences("year_vault_prefs", Context.MODE_PRIVATE)

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow(VaultNavTab.CALENDAR)
    val currentTab: StateFlow<VaultNavTab> = _currentTab.asStateFlow()

    // Calendar state
    private val _selectedYear = MutableStateFlow(2026)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(11) // 1-12 (November)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow("2026-11-02")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val currentMonthKey: StateFlow<String> = combine(_selectedYear, _selectedMonth) { y, m ->
        String.format(Locale.US, "%04d-%02d", y, m)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "2026-11")

    // Dates with files in current month
    val datesWithFiles: StateFlow<List<String>> = currentMonthKey.flatMapLatest { mKey ->
        repository.getDatesWithFiles(mKey)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Files for currently selected date
    val filesForSelectedDate: StateFlow<List<VaultFile>> = _selectedDate.flatMapLatest { date ->
        repository.getFilesByDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All files in database
    val allFiles: StateFlow<List<VaultFile>> = repository.allFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favorites
    val favoriteFiles: StateFlow<List<VaultFile>> = repository.favoriteFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stats
    val totalCount: StateFlow<Int> = repository.totalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalStorage: StateFlow<Long> = repository.totalStorage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Filter & Search states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedFileTypeFilter = MutableStateFlow<String?>(null)
    val selectedFileTypeFilter: StateFlow<String?> = _selectedFileTypeFilter.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.NEWEST)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // Multi-Select
    private val _isMultiSelectActive = MutableStateFlow(false)
    val isMultiSelectActive: StateFlow<Boolean> = _isMultiSelectActive.asStateFlow()

    private val _selectedFileIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedFileIds: StateFlow<Set<String>> = _selectedFileIds.asStateFlow()

    // Dialogs / Sheets
    private val _showAddFileSheet = MutableStateFlow(false)
    val showAddFileSheet: StateFlow<Boolean> = _showAddFileSheet.asStateFlow()

    private val _activeViewingFile = MutableStateFlow<VaultFile?>(null)
    val activeViewingFile: StateFlow<VaultFile?> = _activeViewingFile.asStateFlow()

    private val _editingFile = MutableStateFlow<VaultFile?>(null)
    val editingFile: StateFlow<VaultFile?> = _editingFile.asStateFlow()

    private val _showOnboarding = MutableStateFlow(false)
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    // Security Lock State
    private val _isLockEnabled = MutableStateFlow(prefs.getBoolean("lock_enabled", false))
    val isLockEnabled: StateFlow<Boolean> = _isLockEnabled.asStateFlow()

    private val _isVaultUnlocked = MutableStateFlow(!prefs.getBoolean("lock_enabled", false))
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    // Toast / Feedback message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        // Show onboarding if first time
        val firstLaunch = prefs.getBoolean("first_launch", true)
        if (firstLaunch) {
            _showOnboarding.value = true
            prefs.edit().putBoolean("first_launch", false).apply()
        }

        // Initialize today's date if possible
        try {
            val cal = Calendar.getInstance()
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            val d = cal.get(Calendar.DAY_OF_MONTH)
            _selectedYear.value = y
            _selectedMonth.value = m
            _selectedDate.value = String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
        } catch (_: Exception) {}

        // Seed realistic demo data
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
        }
    }

    fun setTab(tab: VaultNavTab) {
        _currentTab.value = tab
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        val parts = date.split("-")
        if (parts.size >= 2) {
            parts[0].toIntOrNull()?.let { _selectedYear.value = it }
            parts[1].toIntOrNull()?.let { _selectedMonth.value = it }
        }
    }

    fun nextMonth() {
        if (_selectedMonth.value == 12) {
            _selectedYear.value += 1
            _selectedMonth.value = 1
        } else {
            _selectedMonth.value += 1
        }
        updateSelectedDateToFirstOrCurrent()
    }

    fun prevMonth() {
        if (_selectedMonth.value == 1) {
            _selectedYear.value -= 1
            _selectedMonth.value = 12
        } else {
            _selectedMonth.value -= 1
        }
        updateSelectedDateToFirstOrCurrent()
    }

    fun setYearAndMonth(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month.coerceIn(1, 12)
        updateSelectedDateToFirstOrCurrent()
    }

    private fun updateSelectedDateToFirstOrCurrent() {
        val y = _selectedYear.value
        val m = _selectedMonth.value
        val nowCal = Calendar.getInstance()
        if (nowCal.get(Calendar.YEAR) == y && (nowCal.get(Calendar.MONTH) + 1) == m) {
            _selectedDate.value = String.format(Locale.US, "%04d-%02d-%02d", y, m, nowCal.get(Calendar.DAY_OF_MONTH))
        } else {
            _selectedDate.value = String.format(Locale.US, "%04d-%02d-01", y, m)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = if (_selectedCategoryFilter.value == category) null else category
    }

    fun setFileTypeFilter(fileType: String?) {
        _selectedFileTypeFilter.value = if (_selectedFileTypeFilter.value == fileType) null else fileType
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun openAddFileSheet() {
        _showAddFileSheet.value = true
    }

    fun closeAddFileSheet() {
        _showAddFileSheet.value = false
    }

    fun openFileDetail(file: VaultFile) {
        _activeViewingFile.value = file
    }

    fun closeFileDetail() {
        _activeViewingFile.value = null
    }

    fun openEditFile(file: VaultFile) {
        _editingFile.value = file
    }

    fun closeEditFile() {
        _editingFile.value = null
    }

    fun closeOnboarding() {
        _showOnboarding.value = false
    }

    fun toggleFavorite(file: VaultFile) {
        viewModelScope.launch {
            repository.toggleFavorite(file.id, !file.isFavorite)
            // If active viewing file is this one, update view
            if (_activeViewingFile.value?.id == file.id) {
                _activeViewingFile.value = file.copy(isFavorite = !file.isFavorite)
            }
        }
    }

    fun deleteFile(file: VaultFile) {
        viewModelScope.launch {
            repository.deleteFile(file)
            if (_activeViewingFile.value?.id == file.id) {
                _activeViewingFile.value = null
            }
            showToast("ফাইল সফলভাবে ডিলিট করা হয়েছে")
        }
    }

    fun saveNote(
        title: String,
        content: String,
        date: String,
        category: String,
        tags: String,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            repository.saveNote(
                title = title,
                noteContent = content,
                date = date,
                category = category,
                tags = tags,
                isFavorite = isFavorite
            )
            _showAddFileSheet.value = false
            showToast("নোট ভল্টে সংরক্ষিত হয়েছে!")
        }
    }

    fun saveImportedFile(
        uri: Uri,
        title: String,
        date: String,
        category: String,
        tags: String,
        forcedType: FileType? = null,
        noteContent: String = "",
        isFavorite: Boolean = false
    ) {
        viewModelScope.launch {
            repository.saveImportedFile(
                uri = uri,
                title = title,
                date = date,
                category = category,
                tags = tags,
                forcedType = forcedType,
                noteContent = noteContent,
                isFavorite = isFavorite
            )
            _showAddFileSheet.value = false
            showToast("ফাইল সফলভাবে ভল্টে যোগ করা হয়েছে!")
        }
    }

    fun updateFileDetails(file: VaultFile) {
        viewModelScope.launch {
            repository.updateFile(file)
            if (_activeViewingFile.value?.id == file.id) {
                _activeViewingFile.value = file
            }
            _editingFile.value = null
            showToast("তথ্য আপডেট করা হয়েছে")
        }
    }

    // Multi-Select Operations
    fun toggleMultiSelectMode() {
        _isMultiSelectActive.value = !_isMultiSelectActive.value
        if (!_isMultiSelectActive.value) {
            _selectedFileIds.value = emptySet()
        }
    }

    fun toggleFileSelection(fileId: String) {
        val current = _selectedFileIds.value.toMutableSet()
        if (current.contains(fileId)) {
            current.remove(fileId)
        } else {
            current.add(fileId)
        }
        _selectedFileIds.value = current
    }

    fun selectAllFiles(files: List<VaultFile>) {
        _selectedFileIds.value = files.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedFileIds.value = emptySet()
        _isMultiSelectActive.value = false
    }

    fun deleteSelectedFiles(allAvailableFiles: List<VaultFile>) {
        val toDelete = allAvailableFiles.filter { _selectedFileIds.value.contains(it.id) }
        viewModelScope.launch {
            repository.deleteMultipleFiles(toDelete)
            clearSelection()
            showToast("${toDelete.size}টি ফাইল ডিলিট করা হয়েছে")
        }
    }

    // ZIP Export Operations
    fun exportMonthAsZip(monthFiles: List<VaultFile>) {
        if (monthFiles.isEmpty()) {
            showToast("এই মাসে কোনো ফাইল নেই")
            return
        }
        viewModelScope.launch {
            val monthStr = currentMonthKey.value
            val zipFile = repository.createZipArchive(monthFiles, "YearVault_Month_$monthStr")
            shareOrOpenZip(zipFile, "YearVault ${monthStr} Archive")
        }
    }

    fun exportYearAsZip(yearFiles: List<VaultFile>) {
        if (yearFiles.isEmpty()) {
            showToast("এই বছরে কোনো ফাইল নেই")
            return
        }
        viewModelScope.launch {
            val year = _selectedYear.value
            val zipFile = repository.createZipArchive(yearFiles, "YearVault_FullYear_$year")
            shareOrOpenZip(zipFile, "YearVault ${year} Complete Archive")
        }
    }

    fun exportSelectedAsZip(allAvailableFiles: List<VaultFile>) {
        val selected = allAvailableFiles.filter { _selectedFileIds.value.contains(it.id) }
        if (selected.isEmpty()) {
            showToast("কোনো ফাইল সিলেক্ট করা হয়নি")
            return
        }
        viewModelScope.launch {
            val zipFile = repository.createZipArchive(selected, "YearVault_Selected_Files")
            shareOrOpenZip(zipFile, "YearVault Selected Archive (${selected.size} files)")
            clearSelection()
        }
    }

    fun shareSingleFile(file: VaultFile) {
        val context = getApplication<Application>()
        val fileOnDisk = File(context.filesDir, file.filePath)
        if (fileOnDisk.exists()) {
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    fileOnDisk
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, file.title)
                    putExtra(Intent.EXTRA_TEXT, "YearVault File: ${file.title}\nCategory: ${file.category}\nDate: ${file.date}\n\n${file.note}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(intent, "Share ${file.title}").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: Exception) {
                showToast("ফাইল শেয়ার করতে সমস্যা হয়েছে: ${e.message}")
            }
        } else {
            // Share as text note
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, file.title)
                putExtra(Intent.EXTRA_TEXT, "${file.title}\nDate: ${file.date}\nCategory: ${file.category}\nTags: ${file.tags}\n\n${file.note}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share Note").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    private fun shareOrOpenZip(zipFile: File, title: String) {
        val context = getApplication<Application>()
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "$title\nCreated with YearVault\nSize: ${zipFile.length() / 1024} KB")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share or Save ZIP Archive").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            showToast("ZIP আর্কাইভ তৈরি হয়েছে (${zipFile.length() / 1024} KB)")
        } catch (e: Exception) {
            showToast("আর্কাইভ প্রস্তুত হয়েছে: ${zipFile.name}")
        }
    }

    // Security Lock Setup
    fun setSecurityLock(enabled: Boolean, pin: String = "") {
        prefs.edit().apply {
            putBoolean("lock_enabled", enabled)
            if (pin.isNotBlank()) putString("vault_pin", pin)
        }.apply()
        _isLockEnabled.value = enabled
        _isVaultUnlocked.value = !enabled
        showToast(if (enabled) "ভল্ট লক সক্রিয় করা হয়েছে" else "ভল্ট লক নিষ্ক্রিয় করা হয়েছে")
    }

    fun verifyPin(pin: String): Boolean {
        val savedPin = prefs.getString("vault_pin", "1234") ?: "1234"
        if (pin == savedPin || (savedPin.isEmpty() && pin.length >= 4)) {
            _isVaultUnlocked.value = true
            return true
        }
        return false
    }

    fun lockVault() {
        if (_isLockEnabled.value) {
            _isVaultUnlocked.value = false
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}

package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.dialogs.AddFileDialog
import com.example.ui.dialogs.FileDetailDialog
import com.example.ui.dialogs.OnboardingDialog
import com.example.ui.dialogs.SecurityLockDialog
import com.example.ui.screens.AllFilesScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.StatsArchiveScreen
import com.example.ui.theme.YearVaultTheme
import com.example.ui.viewmodel.VaultNavTab
import com.example.ui.viewmodel.VaultViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YearVaultTheme {
                YearVaultApp()
            }
        }
    }
}

@Composable
fun YearVaultApp(
    viewModel: VaultViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsStateWithLifecycle()
    val isLockEnabled by viewModel.isLockEnabled.collectAsStateWithLifecycle()

    val showAddFileSheet by viewModel.showAddFileSheet.collectAsStateWithLifecycle()
    val activeViewingFile by viewModel.activeViewingFile.collectAsStateWithLifecycle()
    val showOnboarding by viewModel.showOnboarding.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Handle system back navigation
    BackHandler(enabled = currentTab != VaultNavTab.CALENDAR) {
        viewModel.setTab(VaultNavTab.CALENDAR)
    }

    // If Vault Lock is active and locked, show PIN entry screen
    if (isLockEnabled && !isVaultUnlocked) {
        SecurityLockDialog(
            onUnlock = { pin ->
                viewModel.verifyPin(pin)
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("vault_bottom_navigation")
            ) {
                NavigationBarItem(
                    selected = currentTab == VaultNavTab.CALENDAR,
                    onClick = { viewModel.setTab(VaultNavTab.CALENDAR) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == VaultNavTab.CALENDAR) Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("ক্যালেন্ডার", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_calendar")
                )

                NavigationBarItem(
                    selected = currentTab == VaultNavTab.ALL_FILES,
                    onClick = { viewModel.setTab(VaultNavTab.ALL_FILES) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == VaultNavTab.ALL_FILES) Icons.Default.Folder else Icons.Outlined.Folder,
                            contentDescription = "All Files"
                        )
                    },
                    label = { Text("সব ফাইল", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_all_files")
                )

                NavigationBarItem(
                    selected = currentTab == VaultNavTab.FAVORITES,
                    onClick = { viewModel.setTab(VaultNavTab.FAVORITES) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == VaultNavTab.FAVORITES) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorites"
                        )
                    },
                    label = { Text("ফেভারিট", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_favorites")
                )

                NavigationBarItem(
                    selected = currentTab == VaultNavTab.STATS_ARCHIVE,
                    onClick = { viewModel.setTab(VaultNavTab.STATS_ARCHIVE) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == VaultNavTab.STATS_ARCHIVE) Icons.Default.PieChart else Icons.Outlined.PieChart,
                            contentDescription = "Stats & Archive"
                        )
                    },
                    label = { Text("আর্কাইভ", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_stats_archive")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabContentAnimation"
            ) { targetTab ->
                when (targetTab) {
                    VaultNavTab.CALENDAR -> HomeScreen(viewModel = viewModel)
                    VaultNavTab.ALL_FILES -> AllFilesScreen(viewModel = viewModel)
                    VaultNavTab.FAVORITES -> FavoritesScreen(viewModel = viewModel)
                    VaultNavTab.STATS_ARCHIVE -> StatsArchiveScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Add File / Note Sheet
    if (showAddFileSheet) {
        AddFileDialog(
            initialDate = selectedDate,
            onDismiss = { viewModel.closeAddFileSheet() },
            onSaveNote = { title, content, date, category, tags, isFav ->
                viewModel.saveNote(title, content, date, category, tags, isFav)
            },
            onSaveFile = { uri, title, date, category, tags, forcedType, note, isFav ->
                viewModel.saveImportedFile(uri, title, date, category, tags, forcedType, note, isFav)
            }
        )
    }

    // View / Edit Single File Detail Sheet
    activeViewingFile?.let { file ->
        FileDetailDialog(
            file = file,
            onDismiss = { viewModel.closeFileDetail() },
            onToggleFavorite = { viewModel.toggleFavorite(file) },
            onDelete = { viewModel.deleteFile(file) },
            onShare = { viewModel.shareSingleFile(file) },
            onUpdate = { updated -> viewModel.updateFileDetails(updated) }
        )
    }

    // Onboarding dialog on first launch
    if (showOnboarding) {
        OnboardingDialog(
            onDismiss = { viewModel.closeOnboarding() }
        )
    }
}

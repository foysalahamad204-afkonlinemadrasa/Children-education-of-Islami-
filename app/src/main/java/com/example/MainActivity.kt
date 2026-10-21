package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppTab
import com.example.ui.KidsIslamicViewModel
import com.example.ui.components.DismissibleBannerAlert
import com.example.ui.components.TopAdventureStatusBar
import com.example.ui.screens.AchievementsAndSettingsScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AdventureMapScreen
import com.example.ui.screens.ArabicLettersTreasureScreen
import com.example.ui.screens.AuthAndOnboardingGateScreen
import com.example.ui.screens.DownloadsAndSyncCenterScreen
import com.example.ui.screens.LevelAdventureQuestScreen
import com.example.ui.screens.MainHomeHubScreen
import com.example.ui.screens.MiniGamesPlaygroundScreen
import com.example.ui.screens.ParentDashboardScreen
import com.example.ui.screens.QuranAudioRecitationScreen
import com.example.ui.screens.signOutFromGoogleAndFirebase
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppRootAuthGate()
            }
        }
    }
}

@Composable
fun AppRootAuthGate() {
    val auth = remember {
        try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }
    }
    var currentUser by remember { mutableStateOf(auth?.currentUser) }
    var allowOfflineLocalSession by remember { mutableStateOf(false) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth?.addAuthStateListener(listener)
        onDispose {
            auth?.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null && !allowOfflineLocalSession) {
        AuthAndOnboardingGateScreen(
            onAuthSuccess = {
                currentUser = FirebaseAuth.getInstance().currentUser
            },
            onContinueOfflineLocal = {
                allowOfflineLocalSession = true
            }
        )
    } else {
        IslamicKidsEducationApp(
            currentUserId = currentUser?.uid ?: "local_offline_explorer",
            onExitOfflineOrSignOut = {
                allowOfflineLocalSession = false
            }
        )
    }
}

@Composable
fun IslamicKidsEducationApp(
    currentUserId: String,
    onExitOfflineOrSignOut: () -> Unit = {},
    viewModel: KidsIslamicViewModel = viewModel(
        key = currentUserId,
        factory = KidsIslamicViewModel.Factory(LocalContext.current)
    )
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle back press on sub-tabs to return to HOME hub
    if (uiState.activeAdventureLevel == null && uiState.currentTab != AppTab.HOME) {
        BackHandler {
            viewModel.selectTab(AppTab.HOME)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAdventureStatusBar(
                explorerName = uiState.profile.explorerName,
                avatarIndex = uiState.profile.avatarIndex,
                totalStars = uiState.totalStarsEarned,
                isOnline = uiState.isEffectivelyOnline,
                onToggleOfflineSim = { viewModel.toggleSimulateOfflineMode() }
            )
        },
        bottomBar = {
            if (uiState.activeAdventureLevel == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val primaryNavTabs = listOf(
                        Triple(AppTab.HOME, Icons.Default.Home, Icons.Outlined.Home),
                        Triple(AppTab.MAP, Icons.Default.Map, Icons.Outlined.Map),
                        Triple(AppTab.LETTERS, Icons.Default.AutoAwesome, Icons.Outlined.AutoAwesome),
                        Triple(AppTab.GAMES, Icons.Default.SportsEsports, Icons.Outlined.SportsEsports),
                        Triple(AppTab.QURAN_AUDIO, Icons.Default.LibraryMusic, Icons.Outlined.LibraryMusic)
                    )
                    primaryNavTabs.forEach { (tab, filledIcon, outlinedIcon) ->
                        val selected = uiState.currentTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) filledIcon else outlinedIcon,
                                    contentDescription = tab.labelBn
                                )
                            },
                            label = {
                                Text(
                                    text = tab.labelBn,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EmeraldDeep,
                                selectedTextColor = EmeraldPrimary,
                                indicatorColor = EmeraldLight,
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B)
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
            ) {
                uiState.celebrationBannerMessage?.let { msg ->
                    DismissibleBannerAlert(
                        message = msg,
                        isCelebration = true,
                        onDismiss = { viewModel.dismissCelebrationBanner() }
                    )
                }

                uiState.offlineNoticeMessage?.let { msg ->
                    DismissibleBannerAlert(
                        message = msg,
                        isCelebration = false,
                        onDismiss = { viewModel.dismissOfflineNotice() }
                    )
                }

                val activeLevel = uiState.activeAdventureLevel
                if (activeLevel != null) {
                    LevelAdventureQuestScreen(
                        level = activeLevel,
                        onSpeakArabic = { ar, bn, url -> viewModel.speakArabic(ar, bn, url) },
                        onPauseAudio = { viewModel.pauseAudio() },
                        onReplayAudio = { viewModel.resumeOrReplayAudio() },
                        onPlaySuccessSound = { viewModel.playSuccessSound() },
                        onPlayRetrySound = { viewModel.playRetrySound() },
                        onCompleteLevel = { levelId, stars, score, mistakeCat ->
                            viewModel.completeAdventureLevel(levelId, stars, score, mistakeCat)
                        },
                        onBackToMap = { viewModel.exitAdventureLevel() },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    when (uiState.currentTab) {
                        AppTab.HOME -> MainHomeHubScreen(
                            uiState = uiState,
                            onStartLevel = { level -> viewModel.startAdventureLevel(level) },
                            onNavigateTab = { tab -> viewModel.selectTab(tab) },
                            onCompleteOnboarding = { viewModel.completeOnboarding() },
                            modifier = Modifier.weight(1f)
                        )
                        AppTab.MAP -> AdventureMapScreen(
                            uiState = uiState,
                            onStartLevel = { level -> viewModel.startAdventureLevel(level) },
                            modifier = Modifier.weight(1f)
                        )
                        AppTab.LETTERS -> ArabicLettersTreasureScreen(
                            uiState = uiState,
                            onSelectLetter = { letter -> viewModel.selectLetterForPractice(letter) },
                            onMarkLetterTraced = { letter -> viewModel.markLetterTracingCompleted(letter) },
                            onCloseLetterDialog = { viewModel.closeLetterDetail() },
                            onSpeakArabic = { ar, bn -> viewModel.speakArabic(ar, bn) },
                            modifier = Modifier.weight(1f)
                        )
                        AppTab.GAMES -> MiniGamesPlaygroundScreen(
                            onSpeakArabic = { ar, bn -> viewModel.speakArabic(ar, bn) },
                            onPlaySuccessSound = { viewModel.playSuccessSound() },
                            onPlayRetrySound = { viewModel.playRetrySound() },
                            onAwardBonusStars = { stars, title ->
                                viewModel.addMiniGameBonusStars(stars, title)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        AppTab.QURAN_AUDIO -> QuranAudioRecitationScreen(
                            uiState = uiState,
                            onPlayAyahAudio = { ar, title, url ->
                                viewModel.speakArabic(ar, title, url)
                            },
                            onPauseAudio = { viewModel.pauseAudio() },
                            onReplayAudio = { viewModel.resumeOrReplayAudio() },
                            onVolumeChange = { vol -> viewModel.setAudioVolume(vol) },
                            onDownloadSurahAudio = { url, title ->
                                viewModel.downloadAndCacheSurahAudio(url, title)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        AppTab.DOWNLOADS -> DownloadsAndSyncCenterScreen(
                            uiState = uiState,
                            onRetryCloudSync = { viewModel.syncPendingProgressToCloud(isAutomatic = false) },
                            onToggleSimulateOffline = { viewModel.toggleSimulateOfflineMode() },
                            onDownloadSurahAudio = { url, title ->
                                viewModel.downloadAndCacheSurahAudio(url, title)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        AppTab.PARENT -> ParentDashboardScreen(
                            uiState = uiState,
                            onUpdateProfileAndGoal = { name, avatar, goal ->
                                viewModel.updateExplorerProfileAndParentGoal(name, avatar, goal)
                            },
                            onStartReviewLevel = { level -> viewModel.startAdventureLevel(level) },
                            modifier = Modifier.weight(1f)
                        )
                        AppTab.ADMIN -> AdminPanelScreen(
                            uiState = uiState,
                            onSaveLesson = { id, stage, order, title, ar, pron, mean, url, ver, pub, qQuestion, qOptions, qCorrect ->
                                viewModel.saveAdminLessonToFirestore(
                                    id, stage, order, title, ar, pron, mean, url, ver, pub, qQuestion, qOptions, qCorrect
                                )
                            },
                            onDeleteLesson = { id, title ->
                                viewModel.deleteAdminLessonFromFirestore(id, title)
                            },
                            onPreviewAudio = { ar, title, url ->
                                viewModel.speakArabic(ar, title, url)
                            },
                            onSyncOwnerAdminRole = {
                                viewModel.syncOwnerAdminRoleRecord()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        AppTab.REWARDS -> AchievementsAndSettingsScreen(
                            uiState = uiState,
                            isSettingsView = false,
                            onVolumeChange = { vol -> viewModel.setAudioVolume(vol) },
                            onToggleSimulateOffline = { viewModel.toggleSimulateOfflineMode() },
                            onSignOutClick = {
                                signOutFromGoogleAndFirebase(context, onExitOfflineOrSignOut, scope)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        AppTab.SETTINGS -> AchievementsAndSettingsScreen(
                            uiState = uiState,
                            isSettingsView = true,
                            onVolumeChange = { vol -> viewModel.setAudioVolume(vol) },
                            onToggleSimulateOffline = { viewModel.toggleSimulateOfflineMode() },
                            onSignOutClick = {
                                signOutFromGoogleAndFirebase(context, onExitOfflineOrSignOut, scope)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

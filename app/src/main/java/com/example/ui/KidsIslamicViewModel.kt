package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AdventureLevel
import com.example.data.ArabicLetterItem
import com.example.data.CloudIslamicRepository
import com.example.data.FirestoreAdminAuditLog
import com.example.data.FirestoreCurriculumLesson
import com.example.data.IslamicCurriculumData
import com.example.data.KidProfileEntity
import com.example.data.KidsIslamicDatabase
import com.example.data.KidsIslamicRepository
import com.example.data.LetterMasteryEntity
import com.example.data.LevelProgressEntity
import com.example.util.AudioPlaybackState
import com.example.util.KidsAudioHelper
import com.example.util.NetworkConnectivityObserver
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppTab(val route: String, val labelBn: String) {
    HOME("HOME", "প্রধান পাতা"),
    MAP("MAP", "শেখার যাত্রা"),
    LETTERS("LETTERS", "অক্ষরের বন্ধু"),
    GAMES("GAMES", "গেম ও কুইজ"),
    QURAN_AUDIO("QURAN_AUDIO", "কোরআন শোনা"),
    DOWNLOADS("DOWNLOADS", "ডাউনলোড ও সিঙ্ক"),
    REWARDS("REWARDS", "আমার অর্জন"),
    PARENT("PARENT", "অভিভাবক প্যানেল"),
    ADMIN("ADMIN", "অ্যাডমিন প্যানেল"),
    SETTINGS("SETTINGS", "সেটিংস")
}

enum class SyncStateStatus(val labelBn: String) {
    IDLE_SYNCED("সিঙ্ক সম্পন্ন ✅"),
    PENDING_OFFLINE("অপেক্ষমাণ অফলাইন অগ্রগতি ⏳"),
    SYNCING("Firebase-এ সিঙ্ক হচ্ছে... 🔄"),
    SYNC_FAILED("সিঙ্ক ব্যর্থ হয়েছে ⚠️")
}

data class SyncDiagnostics(
    val status: SyncStateStatus = SyncStateStatus.IDLE_SYNCED,
    val pendingItemsCount: Int = 0,
    val lastSyncedTimeEpoch: Long = 0L,
    val failureReasonBn: String? = null
)

data class KidsAppUiState(
    val profile: KidProfileEntity = KidProfileEntity(),
    val levelProgressMap: Map<Int, LevelProgressEntity> = emptyMap(),
    val letterMasteryMap: Map<Int, LetterMasteryEntity> = emptyMap(),
    val isHardwareOnline: Boolean = true,
    val currentTab: AppTab = AppTab.HOME,
    val activeAdventureLevel: AdventureLevel? = null,
    val selectedLetterDetail: ArabicLetterItem? = null,
    val offlineNoticeMessage: String? = null,
    val celebrationBannerMessage: String? = null,
    val syncDiagnostics: SyncDiagnostics = SyncDiagnostics(),
    val audioPlaybackState: AudioPlaybackState = AudioPlaybackState(),
    val isVerifiedAdmin: Boolean = false,
    val dynamicCloudLessons: List<FirestoreCurriculumLesson> = emptyList(),
    val adminAuditLogs: List<FirestoreAdminAuditLog> = emptyList(),
    val cachedAudioUrls: Set<String> = emptySet(),
    val currentUserEmail: String? = null,
    val currentUserUid: String? = null
) {
    val isEffectivelyOnline: Boolean
        get() = isHardwareOnline && !profile.simulateOfflineMode

    val totalStarsEarned: Int
        get() = levelProgressMap.values.sumOf { it.starsEarned } + profile.bonusGameStars

    val completedLevelsCount: Int
        get() = levelProgressMap.values.count { it.isCompleted }

    val masteredLettersCount: Int
        get() = letterMasteryMap.values.count { it.isMastered }

    /**
     * Progressive unlocking: Level 1 is unlocked from start; each subsequent level
     * unlocks once the previous level is completed.
     */
    fun isLevelUnlocked(levelId: Int): Boolean {
        if (levelId <= 1 || levelId >= 100) return true
        val prevCompleted = levelProgressMap[levelId - 1]?.isCompleted == true
        return prevCompleted || levelProgressMap[levelId]?.isCompleted == true
    }

    val nextRecommendedLevel: AdventureLevel
        get() {
            val firstIncomplete = IslamicCurriculumData.adventureLevels.firstOrNull { level ->
                levelProgressMap[level.id]?.isCompleted != true && isLevelUnlocked(level.id)
            }
            if (firstIncomplete != null) return firstIncomplete
            val lastSaved = IslamicCurriculumData.adventureLevels.find { it.id == profile.lastActiveLevelId }
            return lastSaved ?: IslamicCurriculumData.adventureLevels.first()
        }

    val adaptiveReviewLevels: List<AdventureLevel>
        get() {
            val levelsWithMistakes = levelProgressMap.values
                .filter { it.mistakeCategory != "NONE" && it.mistakeCategory.isNotBlank() }
                .mapNotNull { prog -> IslamicCurriculumData.adventureLevels.find { it.id == prog.levelId } }
            return levelsWithMistakes
        }
}

class KidsIslamicViewModel(
    private val localRepository: KidsIslamicRepository,
    private val cloudRepository: CloudIslamicRepository,
    context: Context
) : ViewModel() {

    private val audioHelper = KidsAudioHelper(context.applicationContext)
    private val networkFlow = NetworkConnectivityObserver.observeNetworkStatus(context.applicationContext)
    private val auth = try { FirebaseAuth.getInstance() } catch (_: Exception) { null }

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    private val _activeAdventureLevel = MutableStateFlow<AdventureLevel?>(null)
    private val _selectedLetterDetail = MutableStateFlow<ArabicLetterItem?>(null)
    private val _offlineNoticeMessage = MutableStateFlow<String?>(null)
    private val _celebrationBannerMessage = MutableStateFlow<String?>(null)
    private val _syncStatus = MutableStateFlow(SyncStateStatus.IDLE_SYNCED)
    private val _syncFailureReason = MutableStateFlow<String?>(null)
    private val _isVerifiedAdmin = MutableStateFlow(false)
    private val _dynamicLessons = MutableStateFlow<List<FirestoreCurriculumLesson>>(emptyList())
    private val _adminLogs = MutableStateFlow<List<FirestoreAdminAuditLog>>(emptyList())
    private val _cachedAudioUrls = MutableStateFlow<Set<String>>(emptySet())

    private data class CoreLocalBundle(
        val profile: KidProfileEntity,
        val levels: List<LevelProgressEntity>,
        val letters: List<LetterMasteryEntity>,
        val isHardwareOnline: Boolean,
        val audioState: AudioPlaybackState
    )

    private val coreLocalFlow = combine(
        localRepository.kidProfile,
        localRepository.allLevelProgress,
        localRepository.allLetterMastery,
        networkFlow,
        audioHelper.playbackState
    ) { profile, levels, letters, online, audio ->
        CoreLocalBundle(
            profile = profile ?: KidProfileEntity(),
            levels = levels,
            letters = letters,
            isHardwareOnline = online,
            audioState = audio
        )
    }

    private data class NavAndBannerBundle(
        val tab: AppTab,
        val activeLevel: AdventureLevel?,
        val selectedLetter: ArabicLetterItem?,
        val offlineNotice: String?,
        val celebration: String?
    )

    private val navBundleFlow = combine(
        _currentTab,
        _activeAdventureLevel,
        _selectedLetterDetail,
        _offlineNoticeMessage,
        _celebrationBannerMessage
    ) { tab, activeLevel, selectedLetter, offlineNotice, celebration ->
        NavAndBannerBundle(tab, activeLevel, selectedLetter, offlineNotice, celebration)
    }

    private data class CloudSyncAndAdminBundle(
        val syncStatus: SyncStateStatus,
        val syncFailureReason: String?,
        val isVerifiedAdmin: Boolean,
        val dynamicLessons: List<FirestoreCurriculumLesson>,
        val adminLogs: List<FirestoreAdminAuditLog>
    )

    private val cloudAdminFlow = combine(
        _syncStatus,
        _syncFailureReason,
        _isVerifiedAdmin,
        _dynamicLessons,
        _adminLogs
    ) { status, reason, isAdmin, lessons, logs ->
        CloudSyncAndAdminBundle(status, reason, isAdmin, lessons, logs)
    }

    val uiState: StateFlow<KidsAppUiState> = combine(
        coreLocalFlow,
        navBundleFlow,
        cloudAdminFlow,
        _cachedAudioUrls
    ) { core, nav, cloud, cachedUrls ->
        val pendingCount = core.levels.count { it.isPendingCloudSync }
        val effectiveStatus = when {
            cloud.syncStatus == SyncStateStatus.SYNCING -> SyncStateStatus.SYNCING
            cloud.syncFailureReason != null -> SyncStateStatus.SYNC_FAILED
            pendingCount > 0 -> SyncStateStatus.PENDING_OFFLINE
            else -> SyncStateStatus.IDLE_SYNCED
        }

        KidsAppUiState(
            profile = core.profile,
            levelProgressMap = core.levels.associateBy { it.levelId },
            letterMasteryMap = core.letters.associateBy { it.letterId },
            isHardwareOnline = core.isHardwareOnline,
            currentTab = nav.tab,
            activeAdventureLevel = nav.activeLevel,
            selectedLetterDetail = nav.selectedLetter,
            offlineNoticeMessage = nav.offlineNotice,
            celebrationBannerMessage = nav.celebration,
            syncDiagnostics = SyncDiagnostics(
                status = effectiveStatus,
                pendingItemsCount = pendingCount,
                lastSyncedTimeEpoch = core.profile.lastSyncedAt,
                failureReasonBn = cloud.syncFailureReason
            ),
            audioPlaybackState = core.audioState,
            isVerifiedAdmin = cloud.isVerifiedAdmin,
            dynamicCloudLessons = cloud.dynamicLessons,
            adminAuditLogs = cloud.adminLogs,
            cachedAudioUrls = cachedUrls,
            currentUserEmail = auth?.currentUser?.email,
            currentUserUid = auth?.currentUser?.uid
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = KidsAppUiState()
    )

    init {
        viewModelScope.launch {
            val existing = localRepository.getKidProfileOnce()
            if (existing == null) {
                localRepository.updateProfile(KidProfileEntity())
            } else {
                audioHelper.setVolumePercent(existing.audioVolumePercent)
            }
            refreshCachedAudioSet()
            refreshCloudSessionAndSync()
        }

        // Auto-sync pending offline progress whenever network comes back online
        viewModelScope.launch {
            networkFlow.collect { isOnline ->
                val profile = localRepository.getKidProfileOnce() ?: KidProfileEntity()
                if (isOnline && !profile.simulateOfflineMode && auth?.currentUser != null) {
                    syncPendingProgressToCloud(isAutomatic = true)
                }
            }
        }
    }

    fun refreshCloudSessionAndSync() {
        val currentUser = auth?.currentUser ?: return
        viewModelScope.launch {
            val adminVerified = cloudRepository.checkIsVerifiedAdmin()
            _isVerifiedAdmin.value = adminVerified

            // Observe published dynamic lessons
            launch {
                if (adminVerified) {
                    cloudRepository.observeAllAdminLessons()
                        .catch { /* logged by handleFirestoreError */ }
                        .collect { _dynamicLessons.value = it }
                } else {
                    cloudRepository.observePublishedLessons()
                        .catch { /* logged by handleFirestoreError */ }
                        .collect { _dynamicLessons.value = it }
                }
            }

            if (adminVerified) {
                launch {
                    cloudRepository.observeAdminAuditLogs()
                        .catch { /* logged by handleFirestoreError */ }
                        .collect { _adminLogs.value = it }
                }
            }

            // Pull remote progress and merge into local Room DB without regressing local progress
            launch {
                cloudRepository.observeUserLessonProgress(currentUser.uid)
                    .catch { /* logged by handleFirestoreError */ }
                    .collect { remoteList ->
                        val localMap = localRepository.getAllLevelProgressList().associateBy { it.levelId }
                        remoteList.forEach { remote ->
                            val lvlId = remote.levelId.toInt()
                            val local = localMap[lvlId]
                            val remoteEpoch = remote.clientUpdatedAtEpoch
                            val localEpoch = local?.updatedAt ?: 0L
                            val mergedStars = maxOf(local?.starsEarned ?: 0, remote.starsEarned.toInt())
                            val mergedScore = maxOf(local?.bestScore ?: 0, remote.bestScore.toInt())
                            val mergedCompleted = (local?.isCompleted == true) || remote.isCompleted

                            if (local == null || remoteStarsHigherOrNewer(local, mergedStars, mergedScore, remoteEpoch)) {
                                localRepository.upsertLevelProgressDirect(
                                    LevelProgressEntity(
                                        levelId = lvlId,
                                        starsEarned = mergedStars,
                                        isCompleted = mergedCompleted,
                                        bestScore = mergedScore,
                                        mistakeCategory = if (remoteEpoch > localEpoch) remote.mistakeCategory else (local?.mistakeCategory ?: "NONE"),
                                        isOfflinePackDownloaded = true,
                                        isPendingCloudSync = false,
                                        updatedAt = maxOf(localEpoch, remoteEpoch)
                                    )
                                )
                            }
                        }
                    }
            }

            syncPendingProgressToCloud(isAutomatic = true)
        }
    }

    private fun remoteStarsHigherOrNewer(
        local: LevelProgressEntity,
        mergedStars: Int,
        mergedScore: Int,
        remoteEpoch: Long
    ): Boolean {
        return mergedStars > local.starsEarned || mergedScore > local.bestScore || remoteEpoch > local.updatedAt
    }

    fun syncPendingProgressToCloud(isAutomatic: Boolean = false) {
        val state = uiState.value
        if (!state.isEffectivelyOnline) {
            if (!isAutomatic) {
                _syncFailureReason.value = "ডিভাইস এখন অফলাইন মোডে আছে। ইন্টারনেট সংযোগ চালু করে আবার চেষ্টা করো।"
                _offlineNoticeMessage.value = "সিঙ্ক করা যায়নি কারণ তুমি এখন অফলাইন মোডে আছো। তবে তোমার সকল অগ্রগতি ডিভাইসে নিরাপদে সংরক্ষিত আছে!"
            }
            return
        }

        if (auth?.currentUser == null) {
            if (!isAutomatic) {
                _syncFailureReason.value = "ক্লাউড সিঙ্কের জন্য Google লগইন সেশন প্রয়োজন।"
            }
            return
        }

        viewModelScope.launch {
            _syncStatus.value = SyncStateStatus.SYNCING
            _syncFailureReason.value = null

            val allLocal = localRepository.getAllLevelProgressList()
            val profile = localRepository.getKidProfileOnce() ?: KidProfileEntity()
            var anyFailed = false
            var firstErrorMsg: String? = null

            for (item in allLocal) {
                val res = cloudRepository.syncSingleLessonProgress(
                    levelId = item.levelId,
                    localStars = item.starsEarned,
                    localBestScore = item.bestScore,
                    localCompleted = item.isCompleted,
                    mistakeCategory = item.mistakeCategory,
                    localUpdatedAtEpoch = item.updatedAt
                )
                if (res.isSuccess) {
                    val syncedModel = res.getOrThrow()
                    localRepository.upsertLevelProgressDirect(
                        item.copy(
                            starsEarned = maxOf(item.starsEarned, syncedModel.starsEarned.toInt()),
                            bestScore = maxOf(item.bestScore, syncedModel.bestScore.toInt()),
                            isPendingCloudSync = false,
                            updatedAt = maxOf(item.updatedAt, syncedModel.clientUpdatedAtEpoch)
                        )
                    )
                } else {
                    anyFailed = true
                    firstErrorMsg = res.exceptionOrNull()?.localizedMessage ?: "নেটওয়ার্ক বা সার্ভার ত্রুটি"
                }
            }

            val totalStars = localRepository.getAllLevelProgressList().sumOf { it.starsEarned } + profile.bonusGameStars
            val completedCount = localRepository.getAllLevelProgressList().count { it.isCompleted }

            val profileRes = cloudRepository.syncUserProfile(
                explorerName = profile.explorerName,
                avatarIndex = profile.avatarIndex,
                totalStars = totalStars,
                completedLevelsCount = completedCount,
                lastActiveLevelId = profile.lastActiveLevelId,
                dailyGoalMinutes = profile.dailyGoalMinutes,
                dailyPracticedMinutes = profile.dailyPracticedMinutes
            )

            if (profileRes.isFailure) {
                anyFailed = true
                firstErrorMsg = profileRes.exceptionOrNull()?.localizedMessage ?: "প্রোফাইল সিঙ্ক ব্যর্থ হয়েছে"
            }

            if (anyFailed) {
                _syncStatus.value = SyncStateStatus.SYNC_FAILED
                _syncFailureReason.value = "সিঙ্ক ব্যর্থতার কারণ: $firstErrorMsg"
            } else {
                val now = System.currentTimeMillis()
                localRepository.updateProfile(profile.copy(lastSyncedAt = now))
                _syncStatus.value = SyncStateStatus.IDLE_SYNCED
                _syncFailureReason.value = null
                if (!isAutomatic) {
                    _celebrationBannerMessage.value = "আলহামদুলিল্লাহ! সকল অফলাইন অগ্রগতি ডুপ্লিকেট ছাড়াই সফলভাবে Firebase-এ সিঙ্ক হয়েছে! ☁️✅"
                }
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val profile = localRepository.getKidProfileOnce() ?: KidProfileEntity()
            localRepository.updateProfile(profile.copy(hasCompletedOnboarding = true))
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
        _activeAdventureLevel.value = null
        _selectedLetterDetail.value = null
        viewModelScope.launch {
            val currentProfile = localRepository.getKidProfileOnce() ?: KidProfileEntity()
            localRepository.updateProfile(currentProfile.copy(lastActiveTab = tab.route))
        }
    }

    fun startAdventureLevel(level: AdventureLevel) {
        if (!uiState.value.isLevelUnlocked(level.id)) {
            audioHelper.playGentleRetryTone()
            _offlineNoticeMessage.value =
                "সোনামণি! এই ধাপটি খুলতে আগে পূর্ববর্তী অভিযানটি সম্পন্ন করো! 🌟"
            return
        }
        _activeAdventureLevel.value = level
        audioHelper.playPleasantChime()
        viewModelScope.launch {
            val currentProfile = localRepository.getKidProfileOnce() ?: KidProfileEntity()
            localRepository.updateProfile(currentProfile.copy(lastActiveLevelId = level.id))
        }
    }

    fun exitAdventureLevel() {
        _activeAdventureLevel.value = null
    }

    fun completeAdventureLevel(
        levelId: Int,
        starsEarned: Int,
        score: Int,
        mistakeCategory: String = "NONE"
    ) {
        audioHelper.playSuccessFanfare()
        viewModelScope.launch {
            val isOnline = uiState.value.isEffectivelyOnline && auth?.currentUser != null
            val savedEntity = localRepository.saveLevelCompletion(
                levelId = levelId,
                starsEarned = starsEarned,
                score = score,
                mistakeCategory = mistakeCategory,
                isPendingCloudSync = true
            )

            val nextLevelId = (levelId + 1).coerceAtMost(IslamicCurriculumData.adventureLevels.size)
            val currentProfile = localRepository.getKidProfileOnce() ?: KidProfileEntity()
            localRepository.updateProfile(
                currentProfile.copy(
                    lastActiveLevelId = nextLevelId,
                    dailyPracticedMinutes = (currentProfile.dailyPracticedMinutes + 5).coerceAtMost(240)
                )
            )

            val levelObj = IslamicCurriculumData.adventureLevels.find { it.id == levelId }
            _celebrationBannerMessage.value =
                "মাশাআল্লাহ! '${levelObj?.titleBn ?: "অভিযান"}' সম্পন্ন হয়েছে এবং ${savedEntity.starsEarned} স্টার অর্জিত হয়েছে! 🌟"

            if (isOnline) {
                syncPendingProgressToCloud(isAutomatic = true)
            }
        }
    }

    fun selectLetterForPractice(letter: ArabicLetterItem) {
        _selectedLetterDetail.value = letter
        speakArabic(letter.phoneticAr, letter.nameBn)
        viewModelScope.launch {
            val currentCount = uiState.value.letterMasteryMap[letter.id]?.practiceCount ?: 0
            localRepository.markLetterPracticed(letter.id, currentCount, traced = false)
        }
    }

    fun markLetterTracingCompleted(letter: ArabicLetterItem) {
        audioHelper.playSuccessFanfare()
        viewModelScope.launch {
            val currentCount = uiState.value.letterMasteryMap[letter.id]?.practiceCount ?: 0
            localRepository.markLetterPracticed(letter.id, currentCount, traced = true)
            _celebrationBannerMessage.value =
                "চমৎকার! তুমি আঙুল দিয়ে '${letter.nameBn}' (${letter.arabic}) অক্ষরটি সুন্দরভাবে এঁকেছ! ✍️🌟"
        }
    }

    fun closeLetterDetail() {
        _selectedLetterDetail.value = null
    }

    fun speakArabic(arabicText: String, banglaHint: String = "", verifiedAudioUrl: String = "") {
        audioHelper.playVerifiedRecitationOrPhonetic(
            arabicText = arabicText,
            titleBn = banglaHint,
            verifiedUrl = verifiedAudioUrl,
            isOnline = uiState.value.isEffectivelyOnline
        )
    }

    fun pauseAudio() {
        audioHelper.pauseAudio()
    }

    fun resumeOrReplayAudio() {
        audioHelper.resumeOrReplayAudio()
    }

    fun setAudioVolume(percent: Int) {
        audioHelper.setVolumePercent(percent)
        viewModelScope.launch {
            val profile = localRepository.getKidProfileOnce() ?: KidProfileEntity()
            localRepository.updateProfile(profile.copy(audioVolumePercent = percent.coerceIn(0, 100)))
        }
    }

    fun downloadAndCacheSurahAudio(url: String, surahTitleBn: String) {
        if (!uiState.value.isEffectivelyOnline) {
            audioHelper.playGentleRetryTone()
            _offlineNoticeMessage.value =
                "অফলাইন মোড: '$surahTitleBn'-এর যাচাইকৃত অডিও ফাইল ডাউনলোড করার জন্য ইন্টারনেট সংযোগ প্রয়োজন।"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = audioHelper.downloadAndCacheVerifiedAudio(url)
            withContext(Dispatchers.Main) {
                refreshCachedAudioSet()
                if (result.isSuccess) {
                    audioHelper.playSuccessFanfare()
                    _celebrationBannerMessage.value =
                        "আলহামদুলিল্লাহ! '$surahTitleBn'-এর যাচাইকৃত তিলাওয়াত অফলাইনে শোনার জন্য ডিভাইসে সংরক্ষিত হয়েছে! 📥"
                } else {
                    audioHelper.playGentleRetryTone()
                    _offlineNoticeMessage.value =
                        "অডিও ডাউনলোড ব্যর্থ হয়েছে: ${result.exceptionOrNull()?.localizedMessage ?: "নেটওয়ার্ক ত্রুটি"}। অনুগ্রহ করে পরে আবার চেষ্টা করো।"
                }
            }
        }
    }

    private fun refreshCachedAudioSet() {
        val urls = mutableSetOf<String>()
        IslamicCurriculumData.verifiedSurahLibrary.forEach { surah ->
            if (audioHelper.isAudioCached(surah.verifiedStreamingAudioUrl)) {
                urls.add(surah.verifiedStreamingAudioUrl)
            }
            surah.ayahs.forEach { ayah ->
                if (audioHelper.isAudioCached(ayah.verifiedAyahAudioUrl)) {
                    urls.add(ayah.verifiedAyahAudioUrl)
                }
            }
        }
        _cachedAudioUrls.value = urls
    }

    fun playSuccessSound() {
        audioHelper.playSuccessFanfare()
    }

    fun playRetrySound() {
        audioHelper.playGentleRetryTone()
    }

    fun addMiniGameBonusStars(stars: Int, gameTitleBn: String) {
        audioHelper.playSuccessFanfare()
        viewModelScope.launch {
            val profile = localRepository.getKidProfileOnce() ?: KidProfileEntity()
            localRepository.updateProfile(
                profile.copy(
                    bonusGameStars = profile.bonusGameStars + stars,
                    dailyPracticedMinutes = (profile.dailyPracticedMinutes + 3).coerceAtMost(240)
                )
            )
            _celebrationBannerMessage.value =
                "অভিনন্দন! '$gameTitleBn' খেলে তুমি +$stars টি সোনালী স্টার জিতেছ! 🏆"
            if (uiState.value.isEffectivelyOnline && auth?.currentUser != null) {
                syncPendingProgressToCloud(isAutomatic = true)
            }
        }
    }

    fun toggleSimulateOfflineMode() {
        viewModelScope.launch {
            val profile = localRepository.getKidProfileOnce() ?: KidProfileEntity()
            val newOfflineState = !profile.simulateOfflineMode
            localRepository.updateProfile(profile.copy(simulateOfflineMode = newOfflineState))
            if (newOfflineState) {
                _offlineNoticeMessage.value =
                    "অফলাইন মোড চালু করা হয়েছে! ডাউনলোড করা সকল পাঠ, ২৯টি আরবি অক্ষর, কুইজ, গেম ও অডিও ইন্টারনেট ছাড়াই চলবে এবং নতুন অগ্রগতি ডিভাইসে সংরক্ষিত থাকবে।"
            } else {
                _offlineNoticeMessage.value = null
                _celebrationBannerMessage.value =
                    "অনলাইন মোড সক্রিয় হয়েছে! অপেক্ষমাণ অফলাইন অগ্রগতি স্বয়ংক্রিয়ভাবে Firebase-এ সিঙ্ক করা হচ্ছে..."
                syncPendingProgressToCloud(isAutomatic = true)
            }
        }
    }

    fun updateExplorerProfileAndParentGoal(newName: String, newAvatarIndex: Int, dailyGoalMins: Int) {
        viewModelScope.launch {
            val profile = localRepository.getKidProfileOnce() ?: KidProfileEntity()
            localRepository.updateProfile(
                profile.copy(
                    explorerName = newName.ifBlank { "ছোট্ট অভিযাত্রী" },
                    avatarIndex = newAvatarIndex,
                    dailyGoalMinutes = dailyGoalMins.coerceIn(5, 120)
                )
            )
            _celebrationBannerMessage.value = "প্রোফাইল ও দৈনিক অনুশীলনের লক্ষ্য ($dailyGoalMins মিনিট) সংরক্ষিত হয়েছে!"
            if (uiState.value.isEffectivelyOnline && auth?.currentUser != null) {
                syncPendingProgressToCloud(isAutomatic = true)
            }
        }
    }

    fun saveAdminLessonToFirestore(
        existingId: String?,
        stageNumber: Int,
        levelOrder: Int,
        titleBn: String,
        arabicContent: String,
        pronunciationBn: String,
        meaningBn: String,
        audioSourceUrl: String,
        teacherVerified: Boolean,
        isPublished: Boolean,
        quizQuestionBn: String = "",
        quizOptionsJoined: String = "",
        quizCorrectIndex: Int = 0
    ) {
        if (!uiState.value.isEffectivelyOnline) {
            _offlineNoticeMessage.value = "অ্যাডমিন প্যানেল থেকে ক্লাউড ডেটাবেসে পাঠ সংরক্ষণের জন্য ইন্টারনেট সংযোগ প্রয়োজন।"
            return
        }
        if (auth?.currentUser == null) {
            _offlineNoticeMessage.value = "অ্যাডমিন প্যানেলে পাঠ সংরক্ষণের জন্য প্রথমে Google অ্যাকাউন্ট দিয়ে সাইন-ইন করুন।"
            return
        }
        viewModelScope.launch {
            val result = cloudRepository.saveAdminCurriculumLesson(
                existingId = existingId,
                stageNumber = stageNumber,
                levelOrder = levelOrder,
                titleBn = titleBn,
                arabicContent = arabicContent,
                pronunciationBn = pronunciationBn,
                meaningBn = meaningBn,
                audioSourceUrl = audioSourceUrl,
                teacherVerified = teacherVerified,
                isPublished = isPublished,
                quizQuestionBn = quizQuestionBn,
                quizOptionsJoined = quizOptionsJoined,
                quizCorrectIndex = quizCorrectIndex
            )
            if (result.isSuccess) {
                audioHelper.playSuccessFanfare()
                _celebrationBannerMessage.value =
                    "আলহামদুলিল্লাহ! পাঠ ও কুইজ '$titleBn' প্রকৃত Cloud Firestore ডেটাবেসে সংরক্ষিত হয়েছে এবং অডিট লগ যুক্ত হয়েছে!"
            } else {
                audioHelper.playGentleRetryTone()
                val rawErr = result.exceptionOrNull()?.localizedMessage ?: ""
                val friendlyErr = if (rawErr.contains("PERMISSION_DENIED", ignoreCase = true) || rawErr.contains("permission", ignoreCase = true)) {
                    "অনুমতির ত্রুটি (PERMISSION_DENIED): শুধুমাত্র প্রজেক্ট ওনার (${CloudIslamicRepository.PROJECT_OWNER_EMAIL}) অথবা '/admins/{uid}' কালেকশনে অনুমোদিত শিক্ষক নতুন পাঠ সংরক্ষণ করতে পারবেন।"
                } else {
                    rawErr.ifBlank { "অ্যাডমিন অনুমতি যাচাই বা নেটওয়ার্ক ত্রুটি" }
                }
                _offlineNoticeMessage.value = "পাঠ সংরক্ষণ ব্যর্থ: $friendlyErr"
            }
        }
    }

    fun deleteAdminLessonFromFirestore(lessonId: String, titleBn: String) {
        if (!uiState.value.isEffectivelyOnline) {
            _offlineNoticeMessage.value = "ক্লাউড ডেটাবেস থেকে পাঠ মুছে ফেলার জন্য ইন্টারনেট সংযোগ প্রয়োজন।"
            return
        }
        viewModelScope.launch {
            val result = cloudRepository.deleteAdminCurriculumLesson(lessonId, titleBn)
            if (result.isSuccess) {
                audioHelper.playSuccessFanfare()
                _celebrationBannerMessage.value = "পাঠ '$titleBn' সফলভাবে মুছে ফেলা হয়েছে এবং অডিট লগে রেকর্ড করা হয়েছে।"
            } else {
                audioHelper.playGentleRetryTone()
                _offlineNoticeMessage.value =
                    "পাঠ মুছে ফেলা ব্যর্থ: ${result.exceptionOrNull()?.localizedMessage ?: "অনুমতি নেই"}"
            }
        }
    }

    fun syncOwnerAdminRoleRecord() {
        if (!uiState.value.isEffectivelyOnline) {
            _offlineNoticeMessage.value = "অ্যাডমিন রোল সিঙ্ক করার জন্য ইন্টারনেট সংযোগ প্রয়োজন।"
            return
        }
        viewModelScope.launch {
            val res = cloudRepository.ensureOwnerAdminRecord()
            if (res.isSuccess) {
                _isVerifiedAdmin.value = true
                refreshCloudSessionAndSync()
                audioHelper.playSuccessFanfare()
                _celebrationBannerMessage.value =
                    "আলহামদুলিল্লাহ! প্রজেক্ট ওনার অ্যাডমিন রোল (/admins/${auth?.currentUser?.uid}) সক্রিয় ও সিঙ্ক হয়েছে!"
            } else {
                audioHelper.playGentleRetryTone()
                _offlineNoticeMessage.value =
                    "অ্যাডমিন রেকর্ড সিঙ্ক করা যায়নি: শুধুমাত্র প্রজেক্ট ওনার ইমেইল (${CloudIslamicRepository.PROJECT_OWNER_EMAIL}) দিয়ে লগইন করলে এটি স্বয়ংক্রিয়ভাবে অনুমোদিত হয়।"
            }
        }
    }

    fun triggerOnlineOnlyAction(actionNameBn: String) {
        if (!uiState.value.isEffectivelyOnline) {
            audioHelper.playGentleRetryTone()
            _offlineNoticeMessage.value =
                "তুমি এখন 'অফলাইন মোডে' আছো! '$actionNameBn' কাজটির জন্য ইন্টারনেট সংযোগ প্রয়োজন।"
        } else {
            syncPendingProgressToCloud(isAutomatic = false)
        }
    }

    fun dismissOfflineNotice() {
        _offlineNoticeMessage.value = null
    }

    fun dismissCelebrationBanner() {
        _celebrationBannerMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        audioHelper.shutdown()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val appCtx = context.applicationContext
            val db = KidsIslamicDatabase.getDatabase(appCtx)
            val localRepo = KidsIslamicRepository(db.dao())
            val cloudRepo = CloudIslamicRepository(appCtx)
            return KidsIslamicViewModel(localRepo, cloudRepo, appCtx) as T
        }
    }
}

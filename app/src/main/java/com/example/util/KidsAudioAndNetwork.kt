package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class AudioPlaybackState(
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val currentTitle: String = "",
    val currentArabicText: String = "",
    val currentSourceLabel: String = "অফলাইন ফনেটিক ইঞ্জিন",
    val isVerifiedQuranAudio: Boolean = false,
    val volumePercent: Int = 90,
    val errorMessage: String? = null
)

class KidsAudioHelper(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var toneGenerator: ToneGenerator? = null
    private var mediaPlayer: MediaPlayer? = null

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (_: Exception) {}

        try {
            val arabicLocale = Locale.forLanguageTag("ar")
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val arResult = tts?.setLanguage(arabicLocale)
                    isTtsReady = arResult != TextToSpeech.LANG_MISSING_DATA &&
                        arResult != TextToSpeech.LANG_NOT_SUPPORTED
                    tts?.setSpeechRate(0.82f)
                    tts?.setPitch(1.06f)
                }
            }
        } catch (_: Exception) {
            isTtsReady = false
        }
    }

    fun setVolumePercent(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        _playbackState.value = _playbackState.value.copy(volumePercent = clamped)
        val volFloat = clamped / 100f
        try {
            mediaPlayer?.setVolume(volFloat, volFloat)
        } catch (_: Exception) {}
    }

    /**
     * Plays verified Quran recitation audio from local cache or verified streaming URL,
     * falling back to offline phonetic guide with a clear notice if network is unavailable.
     */
    fun playVerifiedRecitationOrPhonetic(
        arabicText: String,
        titleBn: String,
        verifiedUrl: String = "",
        isOnline: Boolean = true
    ) {
        stopAudio()

        if (verifiedUrl.isNotBlank()) {
            val cacheFile = getCachedAudioFile(verifiedUrl)
            if (cacheFile.exists() && cacheFile.length() > 512L) {
                playFromLocalFile(cacheFile, arabicText, titleBn, "যাচাইকৃত ক্যাশড তিলাওয়াত (অফলাইন)")
                return
            } else if (isOnline) {
                playFromVerifiedUrl(verifiedUrl, arabicText, titleBn)
                return
            } else {
                // Offline and not yet cached: notify clearly and play offline phonetic voice
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = true,
                    isPaused = false,
                    currentTitle = titleBn,
                    currentArabicText = arabicText,
                    currentSourceLabel = "অফলাইন উচ্চারণ সহায়িকা (যাচাইকৃত তিলাওয়াত ডাউনলোডের জন্য ইন্টারনেট প্রয়োজন)",
                    isVerifiedQuranAudio = false,
                    errorMessage = "অফলাইন মোড: এই আয়াতের মূল স্টুডিও তিলাওয়াত ফাইলটি এখনো ক্যাশ করা হয়নি। অফলাইন ফনেটিক উচ্চারণ শোনানো হচ্ছে।"
                )
                speakWithOfflineTts(arabicText)
                return
            }
        }

        // Standard letter/harakat/word offline phonetic pronunciation
        _playbackState.value = _playbackState.value.copy(
            isPlaying = true,
            isPaused = false,
            currentTitle = titleBn.ifBlank { "আরবি উচ্চারণ অনুশীলন" },
            currentArabicText = arabicText,
            currentSourceLabel = "অফলাইন আরবি ফনেটিক উচ্চারণ (AI/সিন্থেটিক কণ্ঠ—পূর্ণ কোরআন তিলাওয়াতের বিকল্প নয়)",
            isVerifiedQuranAudio = false,
            errorMessage = null
        )
        speakWithOfflineTts(arabicText)
    }

    private fun playFromLocalFile(file: File, arabicText: String, titleBn: String, sourceLabel: String) {
        try {
            val mp = MediaPlayer()
            mp.setDataSource(file.absolutePath)
            val vol = _playbackState.value.volumePercent / 100f
            mp.setVolume(vol, vol)
            mp.setOnPreparedListener {
                it.start()
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = true,
                    isPaused = false,
                    currentTitle = titleBn,
                    currentArabicText = arabicText,
                    currentSourceLabel = sourceLabel,
                    isVerifiedQuranAudio = true,
                    errorMessage = null
                )
            }
            mp.setOnCompletionListener {
                _playbackState.value = _playbackState.value.copy(isPlaying = false, isPaused = false)
            }
            mp.prepareAsync()
            mediaPlayer = mp
        } catch (e: Exception) {
            speakWithOfflineTts(arabicText)
        }
    }

    private fun playFromVerifiedUrl(url: String, arabicText: String, titleBn: String) {
        try {
            val mp = MediaPlayer()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mp.setDataSource(url)
            val vol = _playbackState.value.volumePercent / 100f
            mp.setVolume(vol, vol)
            mp.setOnPreparedListener {
                it.start()
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = true,
                    isPaused = false,
                    currentTitle = titleBn,
                    currentArabicText = arabicText,
                    currentSourceLabel = "যাচাইকৃত তিলাওয়াত (শাইখ মিশারি রশিদ আল-আফাসি)",
                    isVerifiedQuranAudio = true,
                    errorMessage = null
                )
            }
            mp.setOnCompletionListener {
                _playbackState.value = _playbackState.value.copy(isPlaying = false, isPaused = false)
            }
            mp.setOnErrorListener { _, _, _ ->
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    errorMessage = "অডিও স্ট্রিম লোড করতে সমস্যা হয়েছে। অফলাইন ফনেটিক উচ্চারণ চালানো হচ্ছে।"
                )
                speakWithOfflineTts(arabicText)
                true
            }
            mp.prepareAsync()
            mediaPlayer = mp
        } catch (_: Exception) {
            speakWithOfflineTts(arabicText)
        }
    }

    private fun speakWithOfflineTts(arabicText: String) {
        try {
            if (isTtsReady && tts != null) {
                tts?.setLanguage(Locale.forLanguageTag("ar"))
                tts?.speak(arabicText, TextToSpeech.QUEUE_FLUSH, null, "ar_utt_${System.currentTimeMillis()}")
            } else {
                playPleasantChime()
            }
        } catch (_: Exception) {
            playPleasantChime()
        }
    }

    fun pauseAudio() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                _playbackState.value = _playbackState.value.copy(isPlaying = false, isPaused = true)
            } else {
                tts?.stop()
                _playbackState.value = _playbackState.value.copy(isPlaying = false, isPaused = true)
            }
        } catch (_: Exception) {}
    }

    fun resumeOrReplayAudio() {
        val current = _playbackState.value
        try {
            if (current.isPaused && mediaPlayer != null) {
                mediaPlayer?.start()
                _playbackState.value = current.copy(isPlaying = true, isPaused = false)
                return
            }
        } catch (_: Exception) {}

        if (current.currentArabicText.isNotBlank()) {
            speakWithOfflineTts(current.currentArabicText)
            _playbackState.value = current.copy(isPlaying = true, isPaused = false)
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {}
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _playbackState.value = _playbackState.value.copy(isPlaying = false, isPaused = false)
    }

    fun getCachedAudioFile(url: String): File {
        val safeName = url.substringAfterLast("/").replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .ifBlank { "ayah_cached.mp3" }
        val dir = File(context.filesDir, "verified_quran_audio")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, safeName)
    }

    fun isAudioCached(url: String): Boolean {
        if (url.isBlank()) return true
        val file = getCachedAudioFile(url)
        return file.exists() && file.length() > 512L
    }

    fun downloadAndCacheVerifiedAudio(url: String): Result<File> {
        return try {
            val targetFile = getCachedAudioFile(url)
            if (targetFile.exists() && targetFile.length() > 512L) {
                return Result.success(targetFile)
            }
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
            }
            conn.inputStream.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (targetFile.length() > 512L) {
                Result.success(targetFile)
            } else {
                targetFile.delete()
                Result.failure(IllegalStateException("ডাউনলোড করা অডিও ফাইলটি অসম্পূর্ণ।"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun speakArabicOrBangla(arabicText: String, banglaFallback: String = "") {
        playVerifiedRecitationOrPhonetic(
            arabicText = arabicText,
            titleBn = banglaFallback,
            verifiedUrl = "",
            isOnline = true
        )
    }

    fun playSuccessFanfare() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 180)
        } catch (_: Exception) {}
    }

    fun playGentleRetryTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 140)
        } catch (_: Exception) {}
    }

    fun playPleasantChime() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
        } catch (_: Exception) {}
    }

    fun shutdown() {
        stopAudio()
        try {
            tts?.shutdown()
            toneGenerator?.release()
        } catch (_: Exception) {}
    }
}

object NetworkConnectivityObserver {
    fun observeNetworkStatus(context: Context): Flow<Boolean> = callbackFlow {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        if (connectivityManager == null) {
            trySend(true)
            awaitClose {}
            return@callbackFlow
        }

        fun checkCurrentConnected(): Boolean {
            return try {
                val activeNetwork = connectivityManager.activeNetwork ?: return false
                val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } catch (_: Exception) {
                true
            }
        }

        trySend(checkCurrentConnected())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(checkCurrentConnected())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val connected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                trySend(connected)
            }
        }

        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, callback)
        } catch (_: Exception) {}

        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (_: Exception) {}
        }
    }.distinctUntilChanged()
}

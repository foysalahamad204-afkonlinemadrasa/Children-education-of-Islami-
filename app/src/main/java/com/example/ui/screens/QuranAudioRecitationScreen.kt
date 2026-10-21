package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IslamicCurriculumData
import com.example.data.VerifiedQuranSurah
import com.example.ui.KidsAppUiState
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldSoft

@Composable
fun QuranAudioRecitationScreen(
    uiState: KidsAppUiState,
    onPlayAyahAudio: (String, String, String) -> Unit,
    onPauseAudio: () -> Unit,
    onReplayAudio: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onDownloadSurahAudio: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allSurahs = IslamicCurriculumData.verifiedSurahLibrary
    var showAmparaOnly by remember { mutableStateOf(true) }
    val filteredSurahs = remember(showAmparaOnly) {
        if (showAmparaOnly) allSurahs.filter { it.isAmpara } else allSurahs.filter { !it.isAmpara }
    }
    var selectedSurah by remember(showAmparaOnly) {
        mutableStateOf(filteredSurahs.firstOrNull() ?: allSurahs.first())
    }

    val audioState = uiState.audioPlaybackState

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("quran_audio_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Audio Player Control & Authenticity Verification Header
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldDeep),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = GoldBright
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "যাচাইকৃত কোরআন তিলাওয়াত ও অডিও প্লেয়ার",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "উৎস: ${audioState.currentSourceLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldSoft
                    )

                    if (audioState.currentTitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "চলমান পাঠ: ${audioState.currentTitle}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFA7F3D0),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (audioState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "ℹ️ ${audioState.errorMessage}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF78350F),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Play / Pause / Replay / Volume Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                val firstAyah = selectedSurah.ayahs.first()
                                onPlayAyahAudio(
                                    firstAyah.arabicText,
                                    "${selectedSurah.nameBn} - আয়াত ১",
                                    firstAyah.verifiedAyahAudioUrl
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldBright,
                                contentColor = EmeraldDeep
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("audio_play_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "চালু করো")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("চালু", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onPauseAudio,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("audio_pause_button")
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "বিরতি", tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("বিরতি", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = onReplayAudio,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("audio_replay_button")
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = "পুনরায়", tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("পুনরায়", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "ভলিউম",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ভলিউম: ${audioState.volumePercent}%",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Slider(
                            value = audioState.volumePercent.toFloat(),
                            onValueChange = { onVolumeChange(it.toInt()) },
                            valueRange = 0f..100f,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("audio_volume_slider")
                        )
                    }
                }
            }
        }

        // Switch between Stage 5 (Ampara: Surah An-Naba -> An-Nas) and Stage 6 (Full Quran Journey)
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = showAmparaOnly,
                    onClick = { showAmparaOnly = true },
                    label = {
                        Text(
                            text = "🕌 ধাপ ৫: আমপারা অভিযান (নাবা - নাস)",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = !showAmparaOnly,
                    onClick = { showAmparaOnly = false },
                    label = {
                        Text(
                            text = "👑 ধাপ ৬: পূর্ণ কোরআন যাত্রা",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Surah Selector Pills
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "সূরা নির্বাচন করো (সঠিক ক্রম ও যাচাইকৃত আরবি পাঠ):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDeep
                )
                filteredSurahs.forEach { surah ->
                    val isSelected = selectedSurah.surahNumber == surah.surahNumber
                    val isCached = uiState.cachedAudioUrls.contains(surah.verifiedStreamingAudioUrl)

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) GoldSoft else Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) GoldAccent else Color(0xFFCBD5E1),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { selectedSurah = surah }
                            .testTag("surah_selector_${surah.surahNumber}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "সূরা ক্রম #${surah.surahNumber} • ${surah.nameBn}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDeep
                                )
                                Text(
                                    text = "অর্থ: ${surah.meaningBn} • মোট আয়াত: ${surah.ayahCount} (${surah.revelationPlaceBn})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF475569)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = surah.nameAr,
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        onDownloadSurahAudio(
                                            surah.verifiedStreamingAudioUrl,
                                            surah.nameBn
                                        )
                                    },
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Icon(
                                        imageVector = if (isCached) Icons.Default.DownloadDone else Icons.Default.CloudDownload,
                                        contentDescription = "ক্যাশ করো",
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isCached) "ক্যাশড" else "ডাউনলোড")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Ayah-by-Ayah Verified Display & Recitation
        items(
            items = selectedSurah.ayahs,
            key = { "ayah_${selectedSurah.surahNumber}_${it.ayahNumber}" }
        ) { ayah ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, EmeraldLight, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldLight
                        ) {
                            Text(
                                text = "আয়াত ক্রম: ${ayah.ayahNumber}",
                                style = MaterialTheme.typography.labelLarge,
                                color = EmeraldDeep,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Button(
                            onClick = {
                                onPlayAyahAudio(
                                    ayah.arabicText,
                                    "${selectedSurah.nameBn} - আয়াত ${ayah.ayahNumber}",
                                    ayah.verifiedAyahAudioUrl
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("আয়াত শোনো")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = ayah.arabicText,
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        lineHeight = 48.sp,
                        color = EmeraldDeep,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "উচ্চারণ সহায়িকা: ${ayah.banglaPronunciation}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "অনুবাদ: ${ayah.translationBn}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF334155)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF0FDF4),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🎯 তাজবীদ শিক্ষা: ${ayah.tajweedTipBn}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldDeep,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}

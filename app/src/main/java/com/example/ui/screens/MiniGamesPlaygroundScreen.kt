package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IslamicCurriculumData
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CoralPlayful
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldSoft

private enum class MiniGameMode(val titleBn: String, val emoji: String) {
    SOUND_HUNTER("আওয়াজ শুনে হরফ ধরো", "🎧"),
    MEMORY_MATCH("জোড়া মেলানো মেমোরি গেম", "🧩"),
    WORD_BUILDER("শব্দ তৈরির রেলগাড়ি", "🚂")
}

@Composable
fun MiniGamesPlaygroundScreen(
    onSpeakArabic: (String, String) -> Unit,
    onPlaySuccessSound: () -> Unit,
    onPlayRetrySound: () -> Unit,
    onAwardBonusStars: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGame by remember { mutableStateOf(MiniGameMode.SOUND_HUNTER) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("mini_games_screen")
    ) {
        Text(
            text = "🎮 আনন্দময় ইসলামী গেম জোন (অফলাইন)",
            style = MaterialTheme.typography.headlineMedium,
            color = EmeraldDeep,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "খেলতে খেলতে আরবি হরফ, উচ্চারণ ও শব্দ শেখার ৩টি মজার গেম!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF475569)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Game Mode Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MiniGameMode.entries.forEach { mode ->
                val isSelected = selectedGame == mode
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedGame = mode },
                    label = {
                        Text(
                            text = "${mode.emoji} ${mode.titleBn}",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("game_chip_${mode.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedGame) {
            MiniGameMode.SOUND_HUNTER -> SoundHunterMiniGame(
                onSpeakArabic = onSpeakArabic,
                onPlaySuccessSound = onPlaySuccessSound,
                onPlayRetrySound = onPlayRetrySound,
                onAwardBonusStars = onAwardBonusStars
            )
            MiniGameMode.MEMORY_MATCH -> MemoryMatchMiniGame(
                onSpeakArabic = onSpeakArabic,
                onPlaySuccessSound = onPlaySuccessSound,
                onAwardBonusStars = onAwardBonusStars
            )
            MiniGameMode.WORD_BUILDER -> WordTrainBuilderMiniGame(
                onSpeakArabic = onSpeakArabic,
                onPlaySuccessSound = onPlaySuccessSound,
                onPlayRetrySound = onPlayRetrySound,
                onAwardBonusStars = onAwardBonusStars
            )
        }
    }
}

@Composable
private fun SoundHunterMiniGame(
    onSpeakArabic: (String, String) -> Unit,
    onPlaySuccessSound: () -> Unit,
    onPlayRetrySound: () -> Unit,
    onAwardBonusStars: (Int, String) -> Unit
) {
    val letters = IslamicCurriculumData.allArabicLetters
    var roundSeed by remember { mutableIntStateOf(0) }
    val options = remember(roundSeed) {
        letters.shuffled().take(4)
    }
    val targetLetter = remember(roundSeed) {
        options.random()
    }
    var feedbackText by remember(roundSeed) { mutableStateOf<String?>(null) }
    var isSolved by remember(roundSeed) { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, GoldBright, RoundedCornerShape(24.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "🎧 আওয়াজ ও নাম শুনে সঠিক বেলুনটি ফাটাও!",
                style = MaterialTheme.typography.titleLarge,
                color = EmeraldDeep,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onSpeakArabic(targetLetter.phoneticAr, targetLetter.nameBn) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldBright,
                    contentColor = EmeraldDeep
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(52.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "খুঁজে বের করো: '${targetLetter.nameBn}' (${targetLetter.exampleMeaningBn})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                options.forEach { item ->
                    val isTarget = item.id == targetLetter.id
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSolved && isTarget) EmeraldPrimary
                                else EmeraldLight
                            )
                            .border(2.dp, EmeraldDark, CircleShape)
                            .clickable(enabled = !isSolved) {
                                onSpeakArabic(item.phoneticAr, item.nameBn)
                                if (isTarget) {
                                    isSolved = true
                                    onPlaySuccessSound()
                                    feedbackText = "মাশাআল্লাহ! তুমি সঠিক হরফ '${item.nameBn}' (${item.arabic}) ধরেছ!"
                                    onAwardBonusStars(2, "আওয়াজ শুনে হরফ ধরা")
                                } else {
                                    onPlayRetrySound()
                                    feedbackText = "ওহ! এটি হলো '${item.nameBn}' (${item.arabic})। আবার চেষ্টা করো!"
                                }
                            }
                            .testTag("balloon_letter_${item.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.arabic,
                            fontFamily = AmiriFontFamily,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSolved && isTarget) Color.White else EmeraldDeep
                        )
                    }
                }
            }

            if (feedbackText != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSolved) Color(0xFFD1FAE5) else GoldSoft,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = feedbackText!!,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDeep,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { roundSeed++ },
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("নতুন রাউন্ড খেলো")
            }
        }
    }
}

private data class MemoryTile(
    val id: Int,
    val pairId: Int,
    val displayText: String,
    val audioAr: String
)

@Composable
private fun MemoryMatchMiniGame(
    onSpeakArabic: (String, String) -> Unit,
    onPlaySuccessSound: () -> Unit,
    onAwardBonusStars: (Int, String) -> Unit
) {
    var gameRound by remember { mutableIntStateOf(0) }
    val tiles = remember(gameRound) {
        val sampleLetters = IslamicCurriculumData.allArabicLetters.shuffled().take(4)
        val rawList = mutableListOf<MemoryTile>()
        sampleLetters.forEachIndexed { idx, letter ->
            rawList.add(MemoryTile(idx * 2, letter.id, letter.arabic, letter.phoneticAr))
            rawList.add(MemoryTile(idx * 2 + 1, letter.id, letter.nameBn, letter.phoneticAr))
        }
        rawList.shuffled()
    }

    val matchedPairIds = remember(gameRound) { mutableStateListOf<Int>() }
    var firstSelectedIndex by remember(gameRound) { mutableStateOf<Int?>(null) }
    var statusText by remember(gameRound) {
        mutableStateOf("আরবি হরফের সাথে তার বাংলা নামের জোড়া মেলাও!")
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, EmeraldPrimary, RoundedCornerShape(24.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "🧩 হরফ ও নামের মেমোরি ম্যাচ",
                style = MaterialTheme.typography.titleLarge,
                color = EmeraldDeep,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = EmeraldDark,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2 rows of 4 cards
            for (row in 0 until 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (col in 0 until 4) {
                        val idx = row * 4 + col
                        val tile = tiles[idx]
                        val isMatched = matchedPairIds.contains(tile.pairId)
                        val isSelected = firstSelectedIndex == idx

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = when {
                                isMatched -> Color(0xFFD1FAE5)
                                isSelected -> GoldSoft
                                else -> Color(0xFFF8FAFC)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(74.dp)
                                .border(
                                    width = 2.dp,
                                    color = when {
                                        isMatched -> EmeraldPrimary
                                        isSelected -> GoldAccent
                                        else -> Color(0xFFCBD5E1)
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = !isMatched) {
                                    onSpeakArabic(tile.audioAr, tile.displayText)
                                    if (firstSelectedIndex == null) {
                                        firstSelectedIndex = idx
                                    } else if (firstSelectedIndex != idx) {
                                        val prevTile = tiles[firstSelectedIndex!!]
                                        if (prevTile.pairId == tile.pairId) {
                                            matchedPairIds.add(tile.pairId)
                                            onPlaySuccessSound()
                                            firstSelectedIndex = null
                                            if (matchedPairIds.size == 4) {
                                                statusText = "মাশাআল্লাহ! সবগুলো জোড়া মিলে গেছে!"
                                                onAwardBonusStars(3, "মেমোরি ম্যাচ গেম")
                                            } else {
                                                statusText = "সাবাশ! জোড়া মিলেছে!"
                                            }
                                        } else {
                                            firstSelectedIndex = idx
                                            statusText = "আবার চেষ্টা করো, জোড়া মেলেনি!"
                                        }
                                    }
                                },
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = tile.displayText,
                                    fontFamily = AmiriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = EmeraldDeep,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = { gameRound++ },
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("নতুন বোর্ড সাজাও")
            }
        }
    }
}

private data class WordTrainPuzzle(
    val fullWordAr: String,
    val banglaPronunciation: String,
    val meaningBn: String,
    val orderedParts: List<String>
)

private val WordPuzzles = listOf(
    WordTrainPuzzle("كَتَبَ", "কাতাবা", "সে লিখেছে", listOf("كَ", "تَ", "بَ")),
    WordTrainPuzzle("قَرَأَ", "ক্বারাআ", "সে পড়েছে", listOf("قَ", "رَ", "أَ")),
    WordTrainPuzzle("عَلِمَ", "আলিমা", "সে জেনেছে", listOf("عَ", "لِ", "مَ")),
    WordTrainPuzzle("حَمِدَ", "হামিদা", "সে প্রশংসা করেছে", listOf("حَ", "مِ", "دَ"))
)

@Composable
private fun WordTrainBuilderMiniGame(
    onSpeakArabic: (String, String) -> Unit,
    onPlaySuccessSound: () -> Unit,
    onPlayRetrySound: () -> Unit,
    onAwardBonusStars: (Int, String) -> Unit
) {
    var puzzleIndex by remember { mutableIntStateOf(0) }
    val puzzle = WordPuzzles[puzzleIndex % WordPuzzles.size]
    val pickedParts = remember(puzzleIndex) { mutableStateListOf<String>() }
    val shuffledChoices = remember(puzzleIndex) { puzzle.orderedParts.shuffled() }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, GoldAccent, RoundedCornerShape(24.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "🚂 শব্দের রেলগাড়ি (ডান থেকে বামে হরফ সাজাও)",
                style = MaterialTheme.typography.titleLarge,
                color = EmeraldDeep,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "লক্ষ্য শব্দ: ${puzzle.fullWordAr} (${puzzle.banglaPronunciation} — ${puzzle.meaningBn})",
                style = MaterialTheme.typography.titleMedium,
                color = EmeraldDark,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Train Coaches Display (Right-to-Left order for Arabic)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GoldSoft, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🚂", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                for (i in puzzle.orderedParts.indices) {
                    val part = pickedParts.getOrNull(i) ?: "؟"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(58.dp)
                            .border(2.dp, EmeraldPrimary, RoundedCornerShape(12.dp))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = part,
                                fontFamily = AmiriFontFamily,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDeep
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "নিচের হরফগুলোতে ক্রমানুসারে ট্যাপ করো:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF475569)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                shuffledChoices.forEach { candidate ->
                    val nextExpected = puzzle.orderedParts.getOrNull(pickedParts.size)
                    Button(
                        onClick = {
                            if (candidate == nextExpected) {
                                pickedParts.add(candidate)
                                onSpeakArabic(candidate, "")
                                if (pickedParts.size == puzzle.orderedParts.size) {
                                    onPlaySuccessSound()
                                    onSpeakArabic(puzzle.fullWordAr, puzzle.banglaPronunciation)
                                    onAwardBonusStars(3, "শব্দের রেলগাড়ি")
                                }
                            } else {
                                onPlayRetrySound()
                            }
                        },
                        enabled = !pickedParts.contains(candidate),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(68.dp)
                    ) {
                        Text(
                            text = candidate,
                            fontFamily = AmiriFontFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { puzzleIndex++ },
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("পরবর্তী শব্দ রেলগাড়ি")
            }
        }
    }
}

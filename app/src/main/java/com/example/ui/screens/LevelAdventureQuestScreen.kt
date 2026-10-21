package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdventureLevel
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CoralPlayful
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldSoft

private enum class QuestPhase {
    DISCOVERY_CARDS,
    CHALLENGE_QUIZ,
    VICTORY_CELEBRATION
}

@Composable
fun LevelAdventureQuestScreen(
    level: AdventureLevel,
    onSpeakArabic: (String, String, String) -> Unit,
    onPauseAudio: () -> Unit,
    onReplayAudio: () -> Unit,
    onPlaySuccessSound: () -> Unit,
    onPlayRetrySound: () -> Unit,
    onCompleteLevel: (Int, Int, Int, String) -> Unit,
    onBackToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToMap() }

    var phase by remember(level.id) { mutableStateOf(QuestPhase.DISCOVERY_CARDS) }
    var currentCardIndex by remember(level.id) { mutableIntStateOf(0) }
    var currentQuizIndex by remember(level.id) { mutableIntStateOf(0) }
    var selectedOptionIndex by remember(level.id) { mutableStateOf<Int?>(null) }
    var isAnswerVerified by remember(level.id) { mutableStateOf(false) }
    var correctCount by remember(level.id) { mutableIntStateOf(0) }
    var mistakesCount by remember(level.id) { mutableIntStateOf(0) }
    var lastMistakeCategory by remember(level.id) { mutableStateOf("NONE") }

    val totalSteps = level.items.size + level.challenges.size
    val currentStep = when (phase) {
        QuestPhase.DISCOVERY_CARDS -> currentCardIndex + 1
        QuestPhase.CHALLENGE_QUIZ -> level.items.size + currentQuizIndex + 1
        QuestPhase.VICTORY_CELEBRATION -> totalSteps
    }

    LaunchedEffect(phase, currentCardIndex) {
        if (phase == QuestPhase.DISCOVERY_CARDS) {
            val card = level.items.getOrNull(currentCardIndex)
            if (card != null) {
                onSpeakArabic(card.audioTextAr, card.banglaPronunciation, card.verifiedAudioUrl)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("level_quest_screen")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackToMap,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(EmeraldLight)
                    .testTag("quest_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "ম্যাপে ফিরে যাও",
                    tint = EmeraldDeep
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = level.titleBn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDeep
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (currentStep.toFloat() / totalSteps.coerceAtLeast(1)).coerceIn(0f, 1f) },
                    color = EmeraldPrimary,
                    trackColor = Color(0xFFE2E8F0),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = GoldSoft
            ) {
                Text(
                    text = "$currentStep/$totalSteps",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF78350F),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(targetState = phase, label = "quest_phase_anim") { currentPhase ->
            when (currentPhase) {
                QuestPhase.DISCOVERY_CARDS -> {
                    val card = level.items[currentCardIndex]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = EmeraldLight,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "🌟 দেখো, শোনো ও অনুশীলন করো (${currentCardIndex + 1}/${level.items.size}) • কোনো টাইমার নেই",
                                style = MaterialTheme.typography.labelLarge,
                                color = EmeraldDeep,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            shape = RoundedCornerShape(28.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(3.dp, GoldBright, RoundedCornerShape(28.dp))
                                .clickable {
                                    onSpeakArabic(card.audioTextAr, card.banglaPronunciation, card.verifiedAudioUrl)
                                }
                                .testTag("discovery_arabic_card")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(GoldSoft, Color(0xFFECFDF5))
                                            )
                                        )
                                        .border(2.dp, EmeraldPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = card.arabicText,
                                        fontFamily = AmiriFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (card.arabicText.length > 14) 28.sp else 50.sp,
                                        color = EmeraldDeep,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 54.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = card.banglaPronunciation,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = EmeraldDark,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = card.meaningOrTipBn,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF334155),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF0FDF4)
                                ) {
                                    Text(
                                        text = "💡 তাজবীদ ও উচ্চারণ নিয়ম: ${card.highlightRuleBn}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = EmeraldDeep,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Play, Pause, and Replay controls
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            onSpeakArabic(card.audioTextAr, card.banglaPronunciation, card.verifiedAudioUrl)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GoldBright,
                                            contentColor = EmeraldDeep
                                        ),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("listen_pronunciation_button")
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "চালু করো")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("শোনো", fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = onPauseAudio,
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Icon(Icons.Default.Pause, contentDescription = "বিরতি")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("বিরতি")
                                    }

                                    OutlinedButton(
                                        onClick = onReplayAudio,
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Icon(Icons.Default.Replay, contentDescription = "পুনরায় শোনো")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("পুনরায়")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (currentCardIndex > 0) currentCardIndex--
                                },
                                enabled = currentCardIndex > 0,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("আগের পাঠ")
                            }

                            Button(
                                onClick = {
                                    if (currentCardIndex < level.items.size - 1) {
                                        currentCardIndex++
                                    } else {
                                        phase = QuestPhase.CHALLENGE_QUIZ
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .height(50.dp)
                                    .testTag("next_step_button")
                            ) {
                                Text(
                                    text = if (currentCardIndex < level.items.size - 1) "পরের পাঠ" else "মজার চ্যালেঞ্জে যাও!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                            }
                        }
                    }
                }

                QuestPhase.CHALLENGE_QUIZ -> {
                    val quiz = level.challenges[currentQuizIndex]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = GoldSoft,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "🎮 আনন্দময় কুইজ চ্যালেঞ্জ (${currentQuizIndex + 1}/${level.challenges.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF78350F),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = quiz.questionBn,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = EmeraldDeep,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(84.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = quiz.displayPromptAr,
                                            fontFamily = AmiriFontFamily,
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldDeep
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    OutlinedButton(
                                        onClick = { onSpeakArabic(quiz.targetArabicAudio, "", "") },
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "আওয়াজ শোনো")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("আওয়াজ শোনো")
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    quiz.options.forEachIndexed { idx, optionText ->
                                        val isSelected = selectedOptionIndex == idx
                                        val isCorrectOption = idx == quiz.correctOptionIndex

                                        val optionBg = when {
                                            !isAnswerVerified && isSelected -> GoldSoft
                                            isAnswerVerified && isCorrectOption -> Color(0xFFD1FAE5)
                                            isAnswerVerified && isSelected && !isCorrectOption -> Color(0xFFFFE4E6)
                                            else -> Color(0xFFF8FAFC)
                                        }
                                        val optionBorder = when {
                                            !isAnswerVerified && isSelected -> GoldAccent
                                            isAnswerVerified && isCorrectOption -> EmeraldPrimary
                                            isAnswerVerified && isSelected && !isCorrectOption -> CoralPlayful
                                            else -> Color(0xFFCBD5E1)
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = optionBg,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(2.dp, optionBorder, RoundedCornerShape(16.dp))
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable(enabled = !isAnswerVerified) {
                                                    selectedOptionIndex = idx
                                                    isAnswerVerified = true
                                                    if (idx == quiz.correctOptionIndex) {
                                                        correctCount++
                                                        onPlaySuccessSound()
                                                    } else {
                                                        mistakesCount++
                                                        lastMistakeCategory = quiz.mistakeCategoryTag
                                                        onPlayRetrySound()
                                                    }
                                                }
                                                .testTag("quiz_option_$idx")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = optionText,
                                                    fontFamily = AmiriFontFamily,
                                                    fontSize = 22.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldDeep
                                                )
                                                if (isAnswerVerified && isCorrectOption) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "সঠিক",
                                                        tint = EmeraldPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (isAnswerVerified) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    val isRight = selectedOptionIndex == quiz.correctOptionIndex
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isRight) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (isRight) {
                                                "🌟 মাশাআল্লাহ! ${quiz.explanationBn}"
                                            } else {
                                                "💛 কোনো ব্যাপার না সোনামণি, চলো সঠিক নিয়মটি জেনে নিই: ${quiz.explanationBn}"
                                            },
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldDeep,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = {
                                            if (currentQuizIndex < level.challenges.size - 1) {
                                                currentQuizIndex++
                                                selectedOptionIndex = null
                                                isAnswerVerified = false
                                            } else {
                                                val stars = when {
                                                    mistakesCount == 0 -> 3
                                                    mistakesCount == 1 -> 2
                                                    else -> 1
                                                }
                                                val score = (correctCount * 100)
                                                onCompleteLevel(level.id, stars, score, lastMistakeCategory)
                                                phase = QuestPhase.VICTORY_CELEBRATION
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("quiz_continue_button")
                                    ) {
                                        Text(
                                            text = if (currentQuizIndex < level.challenges.size - 1) "পরের প্রশ্ন" else "পুরস্কার গ্রহণ করো! 🏆",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                QuestPhase.VICTORY_CELEBRATION -> {
                    val earnedStars = when {
                        mistakesCount == 0 -> 3
                        mistakesCount == 1 -> 2
                        else -> 1
                    }
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(3.dp, GoldBright, RoundedCornerShape(28.dp))
                            .testTag("victory_celebration_card")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "বিজয় ট্রফি",
                                tint = GoldAccent,
                                modifier = Modifier.size(72.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "মাশাআল্লাহ! অভিযান সম্পন্ন!",
                                style = MaterialTheme.typography.displayMedium,
                                color = EmeraldDeep,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "তুমি '${level.badgeTitleBn}' অর্জন করেছ এবং পরবর্তী ধাপ আনলক হয়েছে!",
                                style = MaterialTheme.typography.titleLarge,
                                color = EmeraldPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (i in 1..3) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (i <= earnedStars) GoldBright else Color(0xFFCBD5E1),
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "তোমার অর্জিত স্টার ও অগ্রগতি ডিভাইসে সংরক্ষিত হয়েছে এবং ডুপ্লিকেট ছাড়াই ক্লাউডে সিঙ্ক হচ্ছে!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF475569),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        currentCardIndex = 0
                                        currentQuizIndex = 0
                                        selectedOptionIndex = null
                                        isAnswerVerified = false
                                        correctCount = 0
                                        mistakesCount = 0
                                        lastMistakeCategory = "NONE"
                                        phase = QuestPhase.DISCOVERY_CARDS
                                    },
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("আবার অনুশীলন")
                                }

                                Button(
                                    onClick = onBackToMap,
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("victory_back_to_map_button")
                                ) {
                                    Text(
                                        text = "শেখার যাত্রায় যাও",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

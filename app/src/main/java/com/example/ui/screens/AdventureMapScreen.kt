package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdventureLevel
import com.example.data.IslamicCurriculumData
import com.example.data.StageCategory
import com.example.ui.KidsAppUiState
import com.example.ui.components.AdventureHeroBanner
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldSoft

@Composable
fun AdventureMapScreen(
    uiState: KidsAppUiState,
    onStartLevel: (AdventureLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val allLevels = IslamicCurriculumData.adventureLevels
    val groupedByStage = allLevels.groupBy { it.category }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("adventure_map_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AdventureHeroBanner(
                nextLevel = uiState.nextRecommendedLevel,
                completedCount = uiState.completedLevelsCount,
                totalLevels = allLevels.size,
                onContinueClick = { onStartLevel(uiState.nextRecommendedLevel) }
            )
        }

        // Adaptive Review Prompt if child made mistakes in any level
        if (uiState.adaptiveReviewLevels.isNotEmpty()) {
            item {
                val reviewLvl = uiState.adaptiveReviewLevels.first()
                val mistakeCat = uiState.levelProgressMap[reviewLvl.id]?.mistakeCategory ?: ""
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldSoft),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, GoldAccent, RoundedCornerShape(20.dp))
                        .clickable { onStartLevel(reviewLvl) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "💡 বিশেষ অনুশীলনের সুযোগ ($mistakeCat)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDeep
                            )
                            Text(
                                text = "'${reviewLvl.titleBn}' পাঠটি আরেকবার অনুশীলন করে ৩ স্টার অর্জন করো!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF78350F)
                            )
                        }
                    }
                }
            }
        }

        StageCategory.entries.forEach { stage ->
            val stageLevels = groupedByStage[stage].orEmpty()
            item(key = "stage_header_${stage.name}") {
                StageSectionHeader(stage = stage)
            }

            items(
                items = stageLevels,
                key = { "level_${it.id}" }
            ) { level ->
                val progress = uiState.levelProgressMap[level.id]
                val isCompleted = progress?.isCompleted == true
                val isUnlocked = uiState.isLevelUnlocked(level.id)
                val stars = progress?.starsEarned ?: 0
                val isNext = uiState.nextRecommendedLevel.id == level.id

                AdventureLevelNodeCard(
                    level = level,
                    isCompleted = isCompleted,
                    isUnlocked = isUnlocked,
                    starsEarned = stars,
                    isRecommendedNext = isNext,
                    onClick = { onStartLevel(level) }
                )
            }
        }

        // Dynamic Cloud Lessons published by Verified Admins
        if (uiState.dynamicCloudLessons.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = EmeraldLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "☁️ যাচাইকৃত নতুন ক্লাউড পাঠ (${uiState.dynamicCloudLessons.size}টি)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDeep
                        )
                        Text(
                            text = "যোগ্য কোরআন শিক্ষকের যাচাইকৃত অতিরিক্ত অনুশীলন পাঠ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldDark
                        )
                    }
                }
            }
            items(
                items = uiState.dynamicCloudLessons,
                key = { "dyn_${it.id}" }
            ) { dyn ->
                val playableLevel = dyn.toPlayableAdventureLevel()
                val dynProgress = uiState.levelProgressMap[playableLevel.id]
                val dynStars = dynProgress?.starsEarned ?: 0
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, EmeraldPrimary, RoundedCornerShape(18.dp))
                        .clickable { onStartLevel(playableLevel) }
                        .testTag("dynamic_lesson_card_${dyn.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ধাপ ${dyn.stageNumber} • ${dyn.titleBn}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDeep
                                )
                            }
                            Text(
                                text = "⭐ $dynStars/3",
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = dyn.arabicContent,
                            fontFamily = AmiriFontFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                        Text(
                            text = "${dyn.pronunciationBn} — ${dyn.meaningBn}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldPrimary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "অভিযান ও কুইজ শুরু করো",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StageSectionHeader(stage: StageCategory) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stage.badgeEmoji, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stage.titleBn,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stage.subtitleBn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun AdventureLevelNodeCard(
    level: AdventureLevel,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    starsEarned: Int,
    isRecommendedNext: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        !isUnlocked -> Color(0xFFCBD5E1)
        isRecommendedNext -> GoldAccent
        isCompleted -> EmeraldPrimary
        else -> EmeraldDark
    }
    val cardBg = when {
        !isUnlocked -> Color(0xFFF1F5F9)
        isRecommendedNext -> Color(0xFFFFFBEB)
        isCompleted -> Color(0xFFF0FDF4)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isRecommendedNext) 5.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isRecommendedNext) 2.5.dp else 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("level_card_${level.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    !isUnlocked -> Color(0xFF94A3B8)
                                    isCompleted -> EmeraldPrimary
                                    isRecommendedNext -> GoldAccent
                                    else -> EmeraldDark
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isUnlocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "লক করা",
                                tint = Color.White
                            )
                        } else {
                            Text(
                                text = "${level.levelNumber}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isRecommendedNext && isUnlocked) {
                                Surface(
                                    color = GoldAccent,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = "বর্তমান ধাপ",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Surface(
                                color = Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(50)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DownloadDone,
                                        contentDescription = "অফলাইনে সংরক্ষিত",
                                        tint = EmeraldDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "শিক্ষক যাচাইকৃত • অফলাইন",
                                        fontSize = 11.sp,
                                        color = EmeraldDeep,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = level.titleBn,
                            style = MaterialTheme.typography.titleLarge,
                            color = if (isUnlocked) EmeraldDeep else Color(0xFF64748B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 3 Stars Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..3) {
                        Icon(
                            imageVector = if (i <= starsEarned) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (i <= starsEarned) GoldAccent else Color(0xFF94A3B8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = level.storyIntroBn,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF334155)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    level.items.take(3).forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            modifier = Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                text = item.arabicText.take(8),
                                fontFamily = AmiriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = EmeraldDark,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = when {
                        !isUnlocked -> Color(0xFFCBD5E1)
                        isCompleted -> EmeraldPrimary
                        else -> GoldBright
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                !isUnlocked -> Icons.Default.Lock
                                isCompleted -> Icons.Default.CheckCircle
                                else -> Icons.Default.PlayArrow
                            },
                            contentDescription = null,
                            tint = if (isCompleted) Color.White else EmeraldDeep,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                !isUnlocked -> "আগের ধাপ শেষ করো"
                                isCompleted -> "আবার অনুশীলন"
                                else -> "শুরু করো"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isCompleted) Color.White else EmeraldDeep
                        )
                    }
                }
            }
        }
    }
}

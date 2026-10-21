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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdventureLevel
import com.example.data.IslamicCurriculumData
import com.example.ui.AppTab
import com.example.ui.KidsAppUiState
import com.example.ui.components.AdventureHeroBanner
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldSoft

private data class HomeMenuCardItem(
    val titleBn: String,
    val subtitleBn: String,
    val emoji: String,
    val icon: ImageVector,
    val containerColor: Color,
    val borderColor: Color,
    val targetTab: AppTab,
    val testTagId: String
)

@Composable
fun MainHomeHubScreen(
    uiState: KidsAppUiState,
    onStartLevel: (AdventureLevel) -> Unit,
    onNavigateTab: (AppTab) -> Unit,
    onCompleteOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allLevelsCount = IslamicCurriculumData.adventureLevels.size
    val menuItems = listOf(
        HomeMenuCardItem(
            titleBn = "শেখা শুরু করো",
            subtitleBn = "বর্তমান ধাপ: ${uiState.nextRecommendedLevel.titleBn}",
            emoji = "🚀",
            icon = Icons.Default.PlayArrow,
            containerColor = Color(0xFFECFDF5),
            borderColor = EmeraldPrimary,
            targetTab = AppTab.MAP,
            testTagId = "home_card_start_learning"
        ),
        HomeMenuCardItem(
            titleBn = "আমার শেখার যাত্রা",
            subtitleBn = "ধাপ ১ থেকে ধাপ ৬ (অক্ষর, হরকত, শব্দ, আমপারা ও কোরআন)",
            emoji = "🗺️",
            icon = Icons.Default.Explore,
            containerColor = GoldSoft,
            borderColor = GoldAccent,
            targetTab = AppTab.MAP,
            testTagId = "home_card_learning_journey"
        ),
        HomeMenuCardItem(
            titleBn = "অক্ষরের বন্ধু (আঁকা ও মাখরাজ)",
            subtitleBn = "২৯টি আরবি অক্ষর শোনা ও আঙুল দিয়ে আঁকার অনুশীলন",
            emoji = "✍️",
            icon = Icons.Default.AutoAwesome,
            containerColor = Color(0xFFE0F2FE),
            borderColor = Color(0xFF0284C7),
            targetTab = AppTab.LETTERS,
            testTagId = "home_card_letters_friend"
        ),
        HomeMenuCardItem(
            titleBn = "গেম ও কুইজ",
            subtitleBn = "আওয়াজ ধরা, জোড়া মেলানো ও শব্দের রেলগাড়ি গেম",
            emoji = "🎮",
            icon = Icons.Default.SportsEsports,
            containerColor = Color(0xFFEDE9FE),
            borderColor = Color(0xFF7C3AED),
            targetTab = AppTab.GAMES,
            testTagId = "home_card_games_quiz"
        ),
        HomeMenuCardItem(
            titleBn = "কোরআন শোনা",
            subtitleBn = "আমপারা (নাবা-নাস) ও পূর্ণ কোরআনের যাচাইকৃত তিলাওয়াত",
            emoji = "🎧",
            icon = Icons.Default.LibraryMusic,
            containerColor = Color(0xFFFEF3C7),
            borderColor = Color(0xFFD97706),
            targetTab = AppTab.QURAN_AUDIO,
            testTagId = "home_card_listen_quran"
        ),
        HomeMenuCardItem(
            titleBn = "ডাউনলোড করা পাঠ ও সিঙ্ক",
            subtitleBn = "অফলাইন পাঠের তালিকা ও Firebase ক্লাউড সিঙ্ক অবস্থা",
            emoji = "📥",
            icon = Icons.Default.DownloadDone,
            containerColor = Color(0xFFF0FDF4),
            borderColor = EmeraldDark,
            targetTab = AppTab.DOWNLOADS,
            testTagId = "home_card_downloaded_lessons"
        ),
        HomeMenuCardItem(
            titleBn = "আমার অর্জন",
            subtitleBn = "${uiState.totalStarsEarned} স্টার ও ${uiState.completedLevelsCount}টি ব্যাজ অর্জিত",
            emoji = "🏆",
            icon = Icons.Default.EmojiEvents,
            containerColor = Color(0xFFFFFBEB),
            borderColor = GoldBright,
            targetTab = AppTab.REWARDS,
            testTagId = "home_card_my_achievements"
        ),
        HomeMenuCardItem(
            titleBn = "অভিভাবক প্যানেল",
            subtitleBn = "শিশুর অগ্রগতি রিপোর্ট, দৈনিক লক্ষ্য ও ভুলের বিশ্লেষণ",
            emoji = "👨‍👩‍👧",
            icon = Icons.Default.FamilyRestroom,
            containerColor = Color(0xFFF1F5F9),
            borderColor = Color(0xFF475569),
            targetTab = AppTab.PARENT,
            testTagId = "home_card_parent_panel"
        ),
        HomeMenuCardItem(
            titleBn = "সেটিংস ও অডিও নিয়ন্ত্রণ",
            subtitleBn = "ভলিউম, অফলাইন মোড পরীক্ষা ও অ্যাকাউন্ট তথ্য",
            emoji = "⚙️",
            icon = Icons.Default.Settings,
            containerColor = Color(0xFFF8FAFC),
            borderColor = Color(0xFF64748B),
            targetTab = AppTab.SETTINGS,
            testTagId = "home_card_settings"
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_home_hub_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // First-time gentle welcome card for child
        if (!uiState.profile.hasCompletedOnboarding) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldSoft),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, GoldAccent, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "👋 আসসালামু আলাইকুম, ${uiState.profile.explorerName}!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = EmeraldDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "এই আনন্দময় রাজ্যে আমরা কোনো কঠিন পড়া ছাড়াই খেলতে খেলতে আলিফ (ا) থেকে শুরু করে পুরো কোরআন পড়া শিখব! প্রথমে 'শেখা শুরু করো' বোতামে চাপ দাও।",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF78350F)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onCompleteOnboarding()
                                onStartLevel(uiState.nextRecommendedLevel)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("চলো প্রথম অভিযান শুরু করি!")
                        }
                    }
                }
            }
        }

        // Hero Continue Adventure Card
        item {
            AdventureHeroBanner(
                nextLevel = uiState.nextRecommendedLevel,
                completedCount = uiState.completedLevelsCount,
                totalLevels = allLevelsCount,
                onContinueClick = { onStartLevel(uiState.nextRecommendedLevel) }
            )
        }

        // Daily Practice Goal & Online/Offline Sync Bar
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎯 আজকের অনুশীলন: ${uiState.profile.dailyPracticedMinutes}/${uiState.profile.dailyGoalMinutes} মিনিট",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDeep
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (uiState.isEffectivelyOnline) EmeraldLight else Color(0xFFFFE4E6)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (uiState.isEffectivelyOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (uiState.isEffectivelyOnline) EmeraldDeep else Color(0xFF881337),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.syncDiagnostics.status.labelBn,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isEffectivelyOnline) EmeraldDeep else Color(0xFF881337)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val progressRatio = (uiState.profile.dailyPracticedMinutes.toFloat() /
                        uiState.profile.dailyGoalMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        color = EmeraldPrimary,
                        trackColor = Color(0xFFE2E8F0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                    )
                }
            }
        }

        // Grid of 2-column Big Child-Friendly Navigation Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                menuItems.chunked(2).forEach { rowPair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowPair.forEach { cardItem ->
                            Card(
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = cardItem.containerColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(2.dp, cardItem.borderColor, RoundedCornerShape(22.dp))
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable {
                                        if (cardItem.testTagId == "home_card_start_learning") {
                                            onStartLevel(uiState.nextRecommendedLevel)
                                        } else {
                                            onNavigateTab(cardItem.targetTab)
                                        }
                                    }
                                    .testTag(cardItem.testTagId)
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
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = cardItem.emoji, fontSize = 24.sp)
                                        }
                                        Icon(
                                            imageVector = cardItem.icon,
                                            contentDescription = cardItem.titleBn,
                                            tint = cardItem.borderColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = cardItem.titleBn,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = EmeraldDeep,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = cardItem.subtitleBn,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF334155),
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                        if (rowPair.size == 1) {
                            // Admin Panel Quick Card in the remaining slot
                            Card(
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(2.dp, Color(0xFFDC2626), RoundedCornerShape(22.dp))
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable { onNavigateTab(AppTab.ADMIN) }
                                    .testTag("home_card_admin_panel")
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
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "🛡️", fontSize = 24.sp)
                                        }
                                        Icon(
                                            imageVector = Icons.Default.AdminPanelSettings,
                                            contentDescription = "অ্যাডমিন প্যানেল",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "অ্যাডমিন প্যানেল",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = EmeraldDeep,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (uiState.isVerifiedAdmin) "যাচাইকৃত অ্যাডমিন রোল সক্রিয়" else "রোল-ভিত্তিক কনটেন্ট ও অডিট লগ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF334155),
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

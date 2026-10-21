package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.KidsAppUiState
import com.example.ui.components.ExplorerAvatars
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CoralPlayful
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldSoft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DownloadsAndSyncCenterScreen(
    uiState: KidsAppUiState,
    onRetryCloudSync: () -> Unit,
    onToggleSimulateOffline: () -> Unit,
    onDownloadSurahAudio: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val syncDiag = uiState.syncDiagnostics
    val formattedSyncTime = remember(syncDiag.lastSyncedTimeEpoch) {
        if (syncDiag.lastSyncedTimeEpoch <= 0L) "এখনো সিঙ্ক হয়নি"
        else SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(syncDiag.lastSyncedTimeEpoch))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("downloads_sync_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sync Engine Status, Anti-Duplicate Protection & Retry Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (syncDiag.failureReasonBn != null) Color(0xFFFFE4E6) else Color(0xFFF0FDF4)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    2.dp,
                    if (syncDiag.failureReasonBn != null) CoralPlayful else EmeraldPrimary,
                    RoundedCornerShape(24.dp)
                )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (uiState.isEffectivelyOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (uiState.isEffectivelyOnline) EmeraldPrimary else CoralPlayful,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "অফলাইন ও ক্লাউড সিঙ্ক ইঞ্জিন",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDeep
                            )
                            Text(
                                text = "সিঙ্ক অবস্থা: ${syncDiag.status.labelBn}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                        }
                    }

                    Switch(
                        checked = uiState.profile.simulateOfflineMode,
                        onCheckedChange = { onToggleSimulateOffline() },
                        modifier = Modifier.testTag("simulate_offline_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "• অপেক্ষমাণ অফলাইন পাঠের সংখ্যা: ${syncDiag.pendingItemsCount}টি",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• সর্বশেষ সফল সিঙ্ক: $formattedSyncTime",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "• ডুপ্লিকেট ও ওভাররাইট সুরক্ষা: প্রতিটি পাঠের অগ্রগতি 'lesson_{id}' কী এবং মনোটনিক টাইমস্ট্যাম্প দিয়ে মার্জ হয়, তাই কখনো ডুপ্লিকেট তৈরি হয় না বা পুরোনো অফলাইন ডেটা দিয়ে নতুন স্টার মুছে যায় না।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = "• লগআউট ও লোকাল ডেটা নীতিমালা: লগআউট করলেও ডিভাইসে ডাউনলোড করা পাঠ ও অডিও অক্ষুণ্ণ থাকে; পুনরায় সাইন-ইন করলে ক্লাউড থেকে অগ্রগতি পুনরুদ্ধার (Restore) হয়।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF334155)
                        )
                    }
                }

                if (syncDiag.failureReasonBn != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFFBEB),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = syncDiag.failureReasonBn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF78350F),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRetryCloudSync,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("retry_cloud_sync_button")
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("এখনই ক্লাউড সিঙ্ক / পুনরায় চেষ্টা করো", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Downloaded Lessons & Offline Audio Inventory
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "📥 ডিভাইসে ডাউনলোড করা অফলাইন পাঠ ও অডিও তালিকা",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDeep
                )
                Spacer(modifier = Modifier.height(8.dp))

                IslamicCurriculumData.adventureLevels.forEach { level ->
                    val prog = uiState.levelProgressMap[level.id]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.DownloadDone,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = level.titleBn,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDeep
                                )
                                Text(
                                    text = if (prog?.isPendingCloudSync == true) "অফলাইনে সংরক্ষিত (সিঙ্ক অপেক্ষমাণ)" else "অফলাইন ও ক্লাউডে সংরক্ষিত",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                        Text(
                            text = "⭐ ${prog?.starsEarned ?: 0}/3",
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParentDashboardScreen(
    uiState: KidsAppUiState,
    onUpdateProfileAndGoal: (String, Int, Int) -> Unit,
    onStartReviewLevel: (AdventureLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    var editedName by remember(uiState.profile.explorerName) { mutableStateOf(uiState.profile.explorerName) }
    var selectedAvatar by remember(uiState.profile.avatarIndex) { mutableIntStateOf(uiState.profile.avatarIndex) }
    var dailyGoalMins by remember(uiState.profile.dailyGoalMinutes) { mutableIntStateOf(uiState.profile.dailyGoalMinutes) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("parent_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldDeep),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = GoldBright, modifier = Modifier.size(30.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "অভিভাবক প্যানেল (Parent Dashboard)",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "শিশুর শেখার অগ্রগতি, দৈনিক অনুশীলনের লক্ষ্য ও গোপনীয়তা সুরক্ষা",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                }
            }
        }

        // Progress Overview Cards
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "📊 শিশুর সামগ্রিক শেখার সারসংক্ষেপ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDeep
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("• অভিযাত্রীর নাম: ${uiState.profile.explorerName}", style = MaterialTheme.typography.bodyLarge)
                Text("• মোট অর্জিত স্টার: ${uiState.totalStarsEarned} টি ⭐", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("• সম্পন্ন লেভেল: ${uiState.completedLevelsCount}/${IslamicCurriculumData.adventureLevels.size} টি", style = MaterialTheme.typography.bodyLarge)
                Text("• আয়ত্ত করা আরবি অক্ষর: ${uiState.masteredLettersCount}/২৯ টি", style = MaterialTheme.typography.bodyLarge)
                Text("• সর্বশেষ শেখার অবস্থান: ${uiState.nextRecommendedLevel.titleBn}", style = MaterialTheme.typography.bodyLarge)
                Text("• আজকের অনুশীলনের সময়: ${uiState.profile.dailyPracticedMinutes} মিনিট (লক্ষ্য: ${uiState.profile.dailyGoalMinutes} মিনিট)", style = MaterialTheme.typography.bodyLarge)
            }
        }

        // Completed Levels & Mistake Review List
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "📋 পাঠভিত্তিক অগ্রগতি ও ভুলের ধরন বিশ্লেষণ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDeep
                )
                Spacer(modifier = Modifier.height(8.dp))

                IslamicCurriculumData.adventureLevels.forEach { lvl ->
                    val p = uiState.levelProgressMap[lvl.id]
                    val done = p?.isCompleted == true
                    val mistake = p?.mistakeCategory ?: "NONE"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = lvl.titleBn,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (done) EmeraldDeep else Color(0xFF64748B)
                            )
                            Text(
                                text = if (done) {
                                    if (mistake != "NONE") "সম্পন্ন (পুনরায় অনুশীলনের ক্ষেত্র: $mistake)" else "নির্ভুলভাবে সম্পন্ন ✅"
                                } else "এখনো সম্পন্ন হয়নি",
                                fontSize = 12.sp,
                                color = if (mistake != "NONE") Color(0xFFD97706) else Color(0xFF475569)
                            )
                        }
                        if (mistake != "NONE") {
                            OutlinedButton(
                                onClick = { onStartReviewLevel(lvl) },
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("অনুশীলন করান")
                            }
                        } else {
                            Text(
                                text = "${p?.starsEarned ?: 0} ⭐",
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }
                    }
                }
            }
        }

        // Daily Practice Target Configuration & Child Privacy Notice
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "🎯 দৈনিক অনুশীলনের লক্ষ্য ও প্রোফাইল নির্ধারণ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDeep
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("শিশুর ডাকনাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("দৈনিক অনুশীলনের লক্ষ্য: $dailyGoalMins মিনিট", fontWeight = FontWeight.Bold)
                Slider(
                    value = dailyGoalMins.toFloat(),
                    onValueChange = { dailyGoalMins = it.toInt() },
                    valueRange = 5f..60f,
                    steps = 10
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0FDF4),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 শিশু সুরক্ষা নীতিমালা: অভিভাবকের অনুমতি ছাড়া শিশুর কোনো ব্যক্তিগত তথ্য প্রকাশ করা হয় না এবং অ্যাপে কোনো বিজ্ঞাপন বা বাহ্যিক বিপজ্জনক লিংক নেই।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmeraldDeep,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onUpdateProfileAndGoal(editedName, selectedAvatar, dailyGoalMins) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_parent_goal_button")
                ) {
                    Icon(Icons.Default.Storage, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("লক্ষ্য ও প্রোফাইল সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminPanelScreen(
    uiState: KidsAppUiState,
    onSaveLesson: (String?, Int, Int, String, String, String, String, String, Boolean, Boolean, String, String, Int) -> Unit,
    onDeleteLesson: (String, String) -> Unit = { _, _ -> },
    onPreviewAudio: (String, String, String) -> Unit = { _, _, _ -> },
    onSyncOwnerAdminRole: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var stageNumber by remember { mutableIntStateOf(1) }
    var levelOrder by remember { mutableIntStateOf(13) }
    var titleBn by remember { mutableStateOf("") }
    var arabicContent by remember { mutableStateOf("") }
    var pronunciationBn by remember { mutableStateOf("") }
    var meaningBn by remember { mutableStateOf("") }
    var audioSourceUrl by remember { mutableStateOf("https://everyayah.com/data/Alafasy_128kbps/001001.mp3") }
    var quizQuestionBn by remember { mutableStateOf("") }
    var quizOptionsJoined by remember { mutableStateOf("") }
    var quizCorrectIndex by remember { mutableIntStateOf(0) }
    var teacherVerified by remember { mutableStateOf(false) }
    var isPublished by remember { mutableStateOf(false) }
    var editingLessonId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("admin_panel_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin Security & Role Status Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.isVerifiedAdmin) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    2.dp,
                    if (uiState.isVerifiedAdmin) EmeraldPrimary else GoldAccent,
                    RoundedCornerShape(24.dp)
                )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (uiState.isVerifiedAdmin) Icons.Default.VerifiedUser else Icons.Default.Security,
                        contentDescription = null,
                        tint = if (uiState.isVerifiedAdmin) EmeraldPrimary else Color(0xFFD97706),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "পূর্ণাঙ্গ অ্যাডমিন প্যানেল (Firebase RBAC)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDeep
                        )
                        Text(
                            text = if (uiState.isVerifiedAdmin) {
                                "অবস্থা: যাচাইকৃত অ্যাডমিন রোল সক্রিয় (${uiState.currentUserEmail ?: uiState.currentUserUid})"
                            } else {
                                "অবস্থা: সাধারণ ব্যবহারকারী (${uiState.currentUserEmail ?: "অফলাইন/গেস্ট"})"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isVerifiedAdmin) EmeraldDark else Color(0xFFB45309)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "নিরাপত্তা নীতিমালা: Firestore Security Rules সরাসরি `/admins/{uid}` এবং প্রজেক্ট ওনার ইমেইল (`foysalahamad102@gmail.com`) যাচাই করে। সাধারণ ব্যবহারকারী সরাসরি ডেটাবেস অনুরোধ পাঠালেও তা `PERMISSION_DENIED` দ্বারা সুরক্ষিত থাকে।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF334155)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onSyncOwnerAdminRole,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sync_owner_admin_button")
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "প্রজেক্ট ওনার অ্যাডমিন রোল সিঙ্ক / যাচাই করুন",
                        color = EmeraldDeep,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Create / Edit / Verify / Publish Curriculum Lesson + Audio + Quiz Form
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (editingLessonId == null) "➕ নতুন পাঠ, অডিও ও কুইজ যুক্ত করুন" else "✏️ পাঠ ও কুইজ সম্পাদনা ($editingLessonId)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDeep
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = stageNumber.toString(),
                        onValueChange = { stageNumber = it.toIntOrNull()?.coerceIn(1, 6) ?: 1 },
                        label = { Text("ধাপ (১-৬)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = levelOrder.toString(),
                        onValueChange = { levelOrder = it.toIntOrNull()?.coerceAtLeast(1) ?: 1 },
                        label = { Text("পাঠের ক্রম") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = titleBn,
                    onValueChange = { titleBn = it },
                    label = { Text("পাঠের বাংলা শিরোনাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = arabicContent,
                    onValueChange = { arabicContent = it },
                    label = { Text("যাচাইকৃত আরবি পাঠ (হরকতসহ)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pronunciationBn,
                    onValueChange = { pronunciationBn = it },
                    label = { Text("বাংলা উচ্চারণ নির্দেশিকা") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = meaningBn,
                    onValueChange = { meaningBn = it },
                    label = { Text("অর্থ ও তাজবীদ নিয়ম") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = audioSourceUrl,
                        onValueChange = { audioSourceUrl = it },
                        label = { Text("অনুমোদিত অডিও ফাইল URL") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = {
                            onPreviewAudio(
                                arabicContent.ifBlank { "بِسْمِ اللَّهِ" },
                                titleBn.ifBlank { "অ্যাডমিন অডিও প্রিভিউ" },
                                audioSourceUrl
                            )
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("admin_preview_audio_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "অডিও শোনো")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("শোনো")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "🎮 কুইজ প্রশ্ন ও উত্তর কনফিগারেশন (ঐচ্ছিক):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDeep
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = quizQuestionBn,
                    onValueChange = { quizQuestionBn = it },
                    label = { Text("কুইজ প্রশ্ন (বাংলায়)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = quizOptionsJoined,
                        onValueChange = { quizOptionsJoined = it },
                        label = { Text("অপশনসমূহ ('|' দিয়ে আলাদা, যেমন: বা|তা|সা)") },
                        singleLine = true,
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = quizCorrectIndex.toString(),
                        onValueChange = { quizCorrectIndex = it.toIntOrNull()?.coerceIn(0, 3) ?: 0 },
                        label = { Text("সঠিক ইনডেক্স (০-৩)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = teacherVerified,
                        onCheckedChange = {
                            teacherVerified = it
                            if (!it) isPublished = false
                        }
                    )
                    Text(
                        text = "যোগ্য কোরআন শিক্ষক দ্বারা আরবি বানান, হরকত ও অডিও যাচাইকৃত (Teacher Verified)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isPublished,
                        onCheckedChange = { isPublished = it }
                    )
                    Text(
                        text = "শিক্ষার্থীদের জন্য প্রকাশ করুন (Publish Lesson)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isPublished && !teacherVerified) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFE4E6),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ সতর্কবার্তা: কোনো পাঠ যোগ্য কোরআন শিক্ষক দ্বারা যাচাই করা না থাকলে (Teacher Verified = false) তা প্রকাশ করা যাবে না!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF881337),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onSaveLesson(
                            editingLessonId,
                            stageNumber,
                            levelOrder,
                            titleBn.ifBlank { "নতুন তাজবীদ পাঠ" },
                            arabicContent.ifBlank { "بِسْمِ اللَّهِ" },
                            pronunciationBn.ifBlank { "বিসমিল্লাহি" },
                            meaningBn.ifBlank { "আল্লাহর নামে" },
                            audioSourceUrl,
                            teacherVerified,
                            isPublished,
                            quizQuestionBn,
                            quizOptionsJoined,
                            quizCorrectIndex
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_save_lesson_button")
                ) {
                    Text(
                        text = if (editingLessonId == null) "Firestore-এ পাঠ ও কুইজ সংরক্ষণ করুন" else "পাঠ ও কুইজ আপডেট করুন",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Existing Cloud Lessons & Audit Logs
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "📚 ক্লাউড পাঠ ও অ্যাডমিন অডিট লগ (${uiState.adminAuditLogs.size}টি লগ)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDeep
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.dynamicCloudLessons.isEmpty()) {
                    Text(
                        text = "বর্তমানে ক্লাউডে কোনো অতিরিক্ত কাস্টম পাঠ নেই। ১২টি মূল যাচাইকৃত পাঠ অফলাইনে বান্ডেল করা আছে।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B)
                    )
                } else {
                    uiState.dynamicCloudLessons.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.titleBn, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "ধাপ ${item.stageNumber} • যাচাইকৃত: ${if (item.teacherVerified) "হ্যাঁ" else "না"} • প্রকাশিত: ${if (item.isPublished) "হ্যাঁ" else "ড্রাফট"}",
                                    fontSize = 12.sp
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        editingLessonId = item.id
                                        stageNumber = item.stageNumber.toInt()
                                        levelOrder = item.levelOrder.toInt()
                                        titleBn = item.titleBn
                                        arabicContent = item.arabicContent
                                        pronunciationBn = item.pronunciationBn
                                        meaningBn = item.meaningBn
                                        audioSourceUrl = item.audioSourceUrl
                                        quizQuestionBn = item.quizQuestionBn
                                        quizOptionsJoined = item.quizOptionsJoined
                                        quizCorrectIndex = item.quizCorrectIndex.toInt()
                                        teacherVerified = item.teacherVerified
                                        isPublished = item.isPublished
                                    }
                                ) {
                                    Text("সম্পাদনা")
                                }
                                OutlinedButton(
                                    onClick = { onDeleteLesson(item.id, item.titleBn) },
                                    modifier = Modifier.testTag("admin_delete_lesson_${item.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "মুছে ফেলুন", tint = CoralPlayful, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementsAndSettingsScreen(
    uiState: KidsAppUiState,
    isSettingsView: Boolean,
    onVolumeChange: (Int) -> Unit,
    onToggleSimulateOffline: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag(if (isSettingsView) "settings_screen" else "achievements_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isSettingsView) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(30.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🏆 আমার অর্জন, স্টার ও ব্যাজ সমূহ",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDeep
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    IslamicCurriculumData.adventureLevels.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pair.forEach { level ->
                                val isEarned = uiState.levelProgressMap[level.id]?.isCompleted == true
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isEarned) GoldSoft else Color(0xFFF1F5F9),
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp,
                                            if (isEarned) GoldAccent else Color(0xFFCBD5E1),
                                            RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = if (isEarned) "🏅" else "🔒", fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = level.badgeTitleBn,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (isEarned) EmeraldDeep else Color(0xFF64748B)
                                            )
                                            Text(
                                                text = if (isEarned) "অর্জিত!" else "অভিযান ${level.levelNumber}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF475569)
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

        // Settings & Account Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "⚙️ অ্যাপ সেটিংস, অডিও ও অ্যাকাউন্ট",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDeep
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text("অডিও ভলিউম: ${uiState.audioPlaybackState.volumePercent}%", fontWeight = FontWeight.Bold)
                Slider(
                    value = uiState.audioPlaybackState.volumePercent.toFloat(),
                    onValueChange = { onVolumeChange(it.toInt()) },
                    valueRange = 0f..100f
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("অফলাইন মোড সিমুলেশন", fontWeight = FontWeight.Bold)
                        Text(
                            "ইন্টারনেট বন্ধ থাকলে অ্যাপ ও সিঙ্ক কীভাবে কাজ করে তা পরীক্ষা করো",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                    }
                    Switch(
                        checked = uiState.profile.simulateOfflineMode,
                        onCheckedChange = { onToggleSimulateOffline() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "লগইন অ্যাকাউন্ট: ${uiState.currentUserEmail ?: "সংযুক্ত Google অ্যাকাউন্ট"}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "নোট: লগআউট করলে তোমার স্থানীয় ডাউনলোড করা পাঠ ও অডিও ডিভাইসে সংরক্ষিত থাকবে এবং ক্লাউড অগ্রগতি তোমার অ্যাকাউন্টে নিরাপদ থাকবে।",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onSignOutClick,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sign_out_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = CoralPlayful)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("লগআউট করো (Sign Out)", color = CoralPlayful, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

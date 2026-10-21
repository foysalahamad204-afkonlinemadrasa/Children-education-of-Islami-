package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdventureLevel
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldSoft

val ExplorerAvatars = listOf("🧒🏽", "👧🏽", "🦉", "🦁", "🌙", "🐪")

@Composable
fun TopAdventureStatusBar(
    explorerName: String,
    avatarIndex: Int,
    totalStars: Int,
    isOnline: Boolean,
    onToggleOfflineSim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarEmoji = ExplorerAvatars.getOrElse(avatarIndex) { "🧒🏽" }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Kid Explorer Avatar + Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(EmeraldPrimary, EmeraldDark)
                                )
                            )
                            .border(2.dp, GoldBright, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = avatarEmoji,
                            fontSize = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "শিশুদের ইসলাম শিক্ষা",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "অভিযাত্রী: $explorerName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Star Counter Pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = GoldSoft,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .border(1.5.dp, GoldAccent, RoundedCornerShape(50))
                        .testTag("star_counter_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "অর্জিত স্টার",
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$totalStars",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78350F)
                        )
                    }
                }

                // Online / Offline Status Pill (Tappable to test offline mode easily)
                val pillBg = if (isOnline) Color(0xFFD1FAE5) else Color(0xFFFFE4E6)
                val pillBorder = if (isOnline) EmeraldPrimary else Color(0xFFF43F5E)
                val pillText = if (isOnline) "অনলাইন" else "অফলাইন মোড"
                val pillTextColor = if (isOnline) EmeraldDeep else Color(0xFF881337)

                Surface(
                    shape = RoundedCornerShape(50),
                    color = pillBg,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onToggleOfflineSim() }
                        .border(1.5.dp, pillBorder, RoundedCornerShape(50))
                        .testTag("network_mode_toggle_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = pillText,
                            tint = pillBorder,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = pillText,
                            style = MaterialTheme.typography.labelLarge,
                            color = pillTextColor
                        )
                    }
                }
            }

            // Prominent Offline Mode Banner when offline
            AnimatedVisibility(
                visible = !isOnline,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("offline_mode_active_banner"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "অফলাইন মোড আইকন",
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "অফলাইন মোড সক্রিয়: ডাউনলোড করা পাঠ, হরফ, কুইজ, গেম ও অডিও ইন্টারনেট ছাড়াই চলছে এবং তোমার অগ্রগতি ডিভাইসে সংরক্ষিত হচ্ছে।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF78350F),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun AdventureHeroBanner(
    nextLevel: AdventureLevel,
    completedCount: Int,
    totalLevels: Int,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_adventure_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            EmeraldDeep,
                            EmeraldDark,
                            EmeraldPrimary
                        )
                    )
                )
        ) {
            // Custom Canvas Crescent Moon, Stars & Desert Oasis Hills
            Canvas(
                modifier = Modifier
                    .matchParentSize()
            ) {
                val w = size.width
                val h = size.height

                // Glowing background stars
                val starPositions = listOf(
                    Offset(w * 0.82f, h * 0.22f),
                    Offset(w * 0.68f, h * 0.15f),
                    Offset(w * 0.92f, h * 0.45f),
                    Offset(w * 0.15f, h * 0.18f),
                    Offset(w * 0.45f, h * 0.14f)
                )
                starPositions.forEach { pos ->
                    drawCircle(
                        color = Color(0x44FDE047),
                        radius = 12f,
                        center = pos
                    )
                    drawCircle(
                        color = Color(0xFFFef08A),
                        radius = 5f,
                        center = pos
                    )
                }

                // Golden Crescent Moon on Top Right
                val moonCenter = Offset(w * 0.85f, h * 0.32f)
                drawCircle(
                    color = Color(0x33FBBF24),
                    radius = 58f,
                    center = moonCenter
                )
                drawCircle(
                    color = Color(0xFFFBBF24),
                    radius = 40f,
                    center = moonCenter
                )
                drawCircle(
                    color = EmeraldDark,
                    radius = 33f,
                    center = Offset(moonCenter.x - 14f, moonCenter.y - 8f)
                )

                // Soft decorative oasis wave at bottom
                val wavePath = Path().apply {
                    moveTo(0f, h * 0.78f)
                    quadraticTo(w * 0.3f, h * 0.65f, w * 0.65f, h * 0.82f)
                    quadraticTo(w * 0.85f, h * 0.92f, w, h * 0.75f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = wavePath,
                    color = Color(0x2234D399)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = GoldBright,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "✨ আনন্দময় কোরআন অভিযান • $completedCount/$totalLevels সম্পন্ন",
                            style = MaterialTheme.typography.labelLarge,
                            color = EmeraldDeep,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = nextLevel.titleBn,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = nextLevel.storyIntroBn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onContinueClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldBright,
                            contentColor = EmeraldDeep
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("continue_adventure_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "অভিযান শুরু করো"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "অভিযান চালিয়ে যাও",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "📦 অফলাইন রেডি",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFA7F3D0)
                    )
                }
            }
        }
    }
}

@Composable
fun DismissibleBannerAlert(
    message: String,
    isCelebration: Boolean,
    onDismiss: () -> Unit
) {
    val bgColor = if (isCelebration) Color(0xFFD1FAE5) else Color(0xFFFFE4E6)
    val borderColor = if (isCelebration) EmeraldPrimary else Color(0xFFF43F5E)
    val textColor = if (isCelebration) EmeraldDeep else Color(0xFF881337)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .testTag(if (isCelebration) "celebration_banner" else "offline_alert_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isCelebration) "🎉" else "📡",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "বন্ধ করো",
                    tint = textColor
                )
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ArabicLetterItem
import com.example.data.IslamicCurriculumData
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
fun ArabicLettersTreasureScreen(
    uiState: KidsAppUiState,
    onSelectLetter: (ArabicLetterItem) -> Unit,
    onMarkLetterTraced: (ArabicLetterItem) -> Unit,
    onCloseLetterDialog: () -> Unit,
    onSpeakArabic: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val letters = IslamicCurriculumData.allArabicLetters

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("arabic_letters_grid")
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🌟 ধাপ ১: অক্ষরের বন্ধু (২৯টি অক্ষর ও আঙুল দিয়ে আঁকা)",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "যেকোনো অক্ষরে ট্যাপ করে বড় ও পরিষ্কার আরবি অক্ষর দেখো, সঠিক মাখরাজ শোনো, ৪টি রূপ চেনো এবং আঙুল দিয়ে অক্ষর আঁকার অনুশীলন করো!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD1FAE5)
                        )
                    }
                }
            }

            items(
                items = letters,
                key = { "letter_${it.id}" }
            ) { letter ->
                val mastery = uiState.letterMasteryMap[letter.id]
                val isMastered = mastery?.isMastered == true
                val isTraced = mastery?.isTracingPracticed == true

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMastered) Color(0xFFF0FDF4) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isMastered) 2.dp else 1.dp,
                            color = if (isMastered) EmeraldPrimary else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSelectLetter(letter) }
                        .testTag("letter_tile_${letter.id}")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${letter.id}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Bold
                            )
                            Row {
                                if (isTraced) {
                                    Text(text = "✍️", fontSize = 12.sp)
                                }
                                if (isMastered) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "শেখা সম্পন্ন",
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = letter.arabic,
                            fontFamily = AmiriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp,
                            color = EmeraldDeep
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = letter.nameBn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )

                        Text(
                            text = letter.exampleMeaningBn,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Interactive Letter Detail + Finger Tracing Modal Dialog
        val selectedLetter = uiState.selectedLetterDetail
        if (selectedLetter != null) {
            var showTracingPad by remember(selectedLetter.id) { mutableStateOf(false) }
            val tracedPoints = remember(selectedLetter.id) { mutableStateListOf<Offset>() }

            Dialog(onDismissRequest = onCloseLetterDialog) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, GoldBright, RoundedCornerShape(28.dp))
                        .testTag("letter_detail_dialog")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = GoldSoft,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "অক্ষরের বন্ধু #${selectedLetter.id}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFF78350F),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            IconButton(onClick = onCloseLetterDialog) {
                                Icon(Icons.Default.Close, contentDescription = "বন্ধ করো")
                            }
                        }

                        if (!showTracingPad) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldLight)
                                    .border(2.dp, EmeraldPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedLetter.arabic,
                                    fontFamily = AmiriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 60.sp,
                                    color = EmeraldDeep
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "${selectedLetter.nameBn} (${selectedLetter.nameEn})",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDeep
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF0FDF4),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Text(
                                        text = "🎯 সঠিক উচ্চারণ কৌশল (মাখরাজ):",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = EmeraldDark
                                    )
                                    Text(
                                        text = selectedLetter.makhrajHintBn,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = EmeraldDeep,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "শব্দের ভেতর ৪টি আকৃতি (শুরু, মাঝ ও শেষ):",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                PositionalFormChip("একাকী", selectedLetter.isolatedForm)
                                PositionalFormChip("শুরুতে", selectedLetter.initialForm)
                                PositionalFormChip("মাঝে", selectedLetter.medialForm)
                                PositionalFormChip("শেষে", selectedLetter.finalForm)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = GoldSoft,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSpeakArabic(selectedLetter.exampleWordAr, selectedLetter.exampleWordBn)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "শব্দের উদাহরণ (ট্যাপ করে শোনো):",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color(0xFF78350F)
                                        )
                                        Text(
                                            text = "${selectedLetter.exampleWordBn} = ${selectedLetter.exampleMeaningBn}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldDeep
                                        )
                                    }
                                    Text(
                                        text = selectedLetter.exampleWordAr,
                                        fontFamily = AmiriFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 30.sp,
                                        color = EmeraldDeep
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        onSpeakArabic(selectedLetter.phoneticAr, selectedLetter.nameBn)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("উচ্চারণ শোনো", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showTracingPad = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldBright,
                                        contentColor = EmeraldDeep
                                    ),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("open_tracing_pad_button")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("আঙুল দিয়ে আঁকো", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // Finger Tracing Practice Canvas
                            Text(
                                text = "✍️ আঙুল ঘুরিয়ে '${selectedLetter.nameBn}' (${selectedLetter.arabic}) আঁকো",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDeep,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFFFFBEB))
                                    .border(2.5.dp, GoldAccent, RoundedCornerShape(20.dp))
                                    .pointerInput(selectedLetter.id) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                tracedPoints.add(offset)
                                            },
                                            onDrag = { change, _ ->
                                                change.consume()
                                                tracedPoints.add(change.position)
                                            }
                                        )
                                    }
                                    .testTag("letter_tracing_canvas"),
                                contentAlignment = Alignment.Center
                            ) {
                                // Faint guide letter in background
                                Text(
                                    text = selectedLetter.arabic,
                                    fontFamily = AmiriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 130.sp,
                                    color = Color(0x33059669)
                                )

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    for (i in 1 until tracedPoints.size) {
                                        val prev = tracedPoints[i - 1]
                                        val curr = tracedPoints[i]
                                        if ((curr - prev).getDistance() < 85f) {
                                            drawLine(
                                                color = EmeraldPrimary,
                                                start = prev,
                                                end = curr,
                                                strokeWidth = 22f,
                                                cap = StrokeCap.Round
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = { tracedPoints.clear() },
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("মুছে আবার আঁকো")
                                }

                                Button(
                                    onClick = {
                                        onMarkLetterTraced(selectedLetter)
                                        showTracingPad = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("complete_tracing_button")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("আঁকা সম্পন্ন!")
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
private fun PositionalFormChip(labelBn: String, formAr: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = formAr,
            fontFamily = AmiriFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = EmeraldDeep
        )
        Text(
            text = labelBn,
            fontSize = 11.sp,
            color = Color(0xFF475569)
        )
    }
}

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.GameSettings
import com.example.story.EndingType
import com.example.ui.theme.BoneText
import com.example.ui.theme.ElevatedPanel
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MutedAsh
import com.example.ui.theme.NightSlate
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.RustAlert
import com.example.ui.theme.WarmTungsten

@Composable
fun TitleScreen(
    settings: GameSettings,
    onStartGame: () -> Unit,
    onUpdateSensitivity: (Float) -> Unit,
    onUpdatePsxJitter: (Boolean) -> Unit,
    onUpdateCrtScanlines: (Boolean) -> Unit,
    onUpdateVolume: (Float) -> Unit
) {
    var showEndingsDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_title_backdrop_1795317404463),
            contentDescription = "مشهد ليلي لمنزل رقم ١٢",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.68f)
        )

        // Asymmetric dark gradient for Arabic RTL readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            ObsidianVoid.copy(alpha = 0.28f),
                            ObsidianVoid.copy(alpha = 0.88f)
                        )
                    )
                )
        )

        CrtOverlay(enabled = settings.crtScanlinesEnabled, peepholeActive = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 28.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top VHS REC header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(NightSlate.copy(alpha = 0.78f), RoundedCornerShape(6.dp))
                        .border(1.dp, HairlineBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(RustAlert, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "REC  ●  CH-03  21:42:08",
                        style = MaterialTheme.typography.labelMedium,
                        color = BoneText
                    )
                }

                Text(
                    text = "النهايات المكتشفة: ${settings.unlockedEndings.size} / 3",
                    style = MaterialTheme.typography.labelMedium,
                    color = WarmTungsten
                )
            }

            // Main Title & Menu Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 460.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "شريط حي الروابي — واقعة حقيقية الطابع",
                        style = MaterialTheme.typography.labelMedium,
                        color = WarmTungsten
                    )
                    Text(
                        text = "المنزل رقم ١٢",
                        style = MaterialTheme.typography.displayLarge,
                        color = BoneText
                    )
                    Text(
                        text = "أول ليلة لك في منزلك الجديد. مهام اعتيادية، وجارٌ ودود، ومشوار قصير إلى البقالة... قبل أن تبدأ الطرقات المتأخرة خلف الباب.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MutedAsh
                    )
                    Spacer(Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onStartGame,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarmTungsten,
                                contentColor = ObsidianVoid
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("start_game_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "ابدأ اللعب",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { showEndingsDialog = true },
                            border = BorderStroke(1.dp, HairlineBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("endings_button")
                        ) {
                            Icon(Icons.Outlined.Description, contentDescription = null, tint = BoneText)
                            Spacer(Modifier.width(6.dp))
                            Text("سجل النهايات", color = BoneText)
                        }

                        OutlinedButton(
                            onClick = { showSettingsDialog = true },
                            border = BorderStroke(1.dp, HairlineBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("settings_button")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = BoneText)
                            Spacer(Modifier.width(6.dp))
                            Text("الإعدادات", color = BoneText)
                        }
                    }
                }
            }
        }

        if (showEndingsDialog) {
            EndingsArchiveModal(
                unlocked = settings.unlockedEndings,
                onDismiss = { showEndingsDialog = false }
            )
        }

        if (showSettingsDialog) {
            SettingsModal(
                settings = settings,
                onDismiss = { showSettingsDialog = false },
                onUpdateSensitivity = onUpdateSensitivity,
                onUpdatePsxJitter = onUpdatePsxJitter,
                onUpdateCrtScanlines = onUpdateCrtScanlines,
                onUpdateVolume = onUpdateVolume
            )
        }
    }
}

@Composable
private fun EndingsArchiveModal(
    unlocked: Set<String>,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = NightSlate,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, HairlineBorder),
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxHeight(0.9f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("سجل النهايات (${unlocked.size}/3)", style = MaterialTheme.typography.titleLarge, color = WarmTungsten)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = BoneText)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EndingType.entries.forEach { ending ->
                        val isOpen = ending.id in unlocked
                        Surface(
                            color = ElevatedPanel,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isOpen) WarmTungsten.copy(alpha = 0.5f) else HairlineBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isOpen) Icons.Default.CheckCircle else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isOpen) WarmTungsten else MutedAsh
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = if (isOpen) ending.title else "نهاية مغلقة — ${ending.badge}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isOpen) BoneText else MutedAsh
                                    )
                                    Text(
                                        text = if (isOpen) ending.summary else "غيّر قراراتك في الحوارات وإقفال المداخل لاكتشاف هذا المسار.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MutedAsh
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

@Composable
fun SettingsModal(
    settings: GameSettings,
    onDismiss: () -> Unit,
    onUpdateSensitivity: (Float) -> Unit,
    onUpdatePsxJitter: (Boolean) -> Unit,
    onUpdateCrtScanlines: (Boolean) -> Unit,
    onUpdateVolume: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = NightSlate,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, HairlineBorder),
            modifier = Modifier.widthIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الإعدادات", style = MaterialTheme.typography.titleLarge, color = WarmTungsten)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = BoneText)
                    }
                }

                Column {
                    Text("حساسية الكاميرا: ${"%.1f".format(settings.lookSensitivity)}x", color = BoneText)
                    Slider(
                        value = settings.lookSensitivity,
                        onValueChange = onUpdateSensitivity,
                        valueRange = 0.4f..2.2f
                    )
                }

                Column {
                    Text("مستوى الصوت: ${(settings.masterVolume * 100).toInt()}%", color = BoneText)
                    Slider(
                        value = settings.masterVolume,
                        onValueChange = onUpdateVolume,
                        valueRange = 0f..1f
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ارتجاف المضلعات الكلاسيكي (PSX Snap)", color = BoneText)
                    Switch(checked = settings.psxJitterEnabled, onCheckedChange = onUpdatePsxJitter)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("خطوط شاشة التلفاز القديمة (CRT)", color = BoneText)
                    Switch(checked = settings.crtScanlinesEnabled, onCheckedChange = onUpdateCrtScanlines)
                }
            }
        }
    }
}

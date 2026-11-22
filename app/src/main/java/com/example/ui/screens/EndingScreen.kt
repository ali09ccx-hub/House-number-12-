package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.story.EndingType
import com.example.ui.theme.BoneText
import com.example.ui.theme.CrtPhosphor
import com.example.ui.theme.ElevatedPanel
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MutedAsh
import com.example.ui.theme.NightSlate
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.RustAlert
import com.example.ui.theme.WarmTungsten

@Composable
fun EndingScreen(
    ending: EndingType,
    unlockedCount: Int,
    crtEnabled: Boolean,
    onReplay: () -> Unit,
    onBackToTitle: () -> Unit
) {
    val accent = when (ending) {
        EndingType.VIGILANT_SURVIVAL -> CrtPhosphor
        EndingType.NARROW_ESCAPE -> WarmTungsten
        EndingType.FATAL_TRUST -> RustAlert
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        CrtOverlay(enabled = crtEnabled, peepholeActive = false)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Evidence Photo Frame
            Surface(
                color = NightSlate,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, HairlineBorder),
                modifier = Modifier
                    .weight(0.42f)
                    .fillMaxHeight(0.86f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.img_case_evidence_1795317404766),
                        contentDescription = "صورة ملف التحقيق",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "ملف الحادثة — المنزل رقم ١٢",
                        style = MaterialTheme.typography.labelMedium,
                        color = WarmTungsten
                    )
                    Text(
                        text = "النهايات المكتشفة: $unlockedCount / 3",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedAsh
                    )
                }
            }

            // Right: Report & Actions
            Column(
                modifier = Modifier
                    .weight(0.58f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = accent.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, accent)
                ) {
                    Text(
                        text = ending.badge,
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = ending.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = BoneText
                )

                Text(
                    text = ending.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = BoneText
                )

                Surface(
                    color = ElevatedPanel,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, HairlineBorder)
                ) {
                    Text(
                        text = ending.policeNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedAsh,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onReplay,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WarmTungsten,
                            contentColor = ObsidianVoid
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("replay_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("العب مجدداً", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onBackToTitle,
                        border = BorderStroke(1.dp, HairlineBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("ending_home_button")
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = BoneText)
                        Spacer(Modifier.width(6.dp))
                        Text("القائمة الرئيسية", color = BoneText)
                    }
                }
            }
        }
    }
}

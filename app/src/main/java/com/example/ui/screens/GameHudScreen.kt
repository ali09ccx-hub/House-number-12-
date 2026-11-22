package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine3d.GameGLSurfaceView
import com.example.story.DialogueChoice
import com.example.story.DialogueNode
import com.example.story.StoryStage
import com.example.ui.theme.BoneText
import com.example.ui.theme.CrtPhosphor
import com.example.ui.theme.ElevatedPanel
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MutedAsh
import com.example.ui.theme.NightSlate
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.RustAlert
import com.example.ui.theme.WarmTungsten
import com.example.viewmodel.GameUiState
import com.example.viewmodel.GameViewModel
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun GameHudScreen(
    uiState: GameUiState,
    viewModel: GameViewModel
) {
    val context = LocalContext.current
    val glView = remember {
        GameGLSurfaceView(
            context = context,
            onTargetChanged = viewModel::onTargetChanged,
            onFootstep = viewModel::onFootstep,
            onIndoorsChanged = viewModel::onIndoorsChanged,
            onPlayerPositionChanged = viewModel::onPlayerPositionChanged
        )
    }

    LaunchedEffect(glView) {
        viewModel.attachSurfaceCallbacks(
            onReset = { glView.resetPlayerToStart() },
            onCloset = { hide -> glView.teleportToBedroomHiding(hide) }
        )
    }

    LaunchedEffect(uiState.renderState) {
        glView.updateRenderState(uiState.renderState)
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(glView, lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> glView.onResume()
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> glView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            glView.onPause()
        }
    }

    BackHandler {
        when {
            uiState.isPhoneOpen -> viewModel.togglePhone()
            uiState.activeDialogue == null -> viewModel.togglePause()
        }
    }

    // Physical LTR layout for FPS touch pads (Left = Move, Right = Look)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. OpenGL ES 3.0 3D World Viewport
            AndroidView(
                factory = { glView },
                modifier = Modifier.fillMaxSize()
            )

            // 2. CRT / Peephole Optical Overlay
            CrtOverlay(
                enabled = uiState.settings.crtScanlinesEnabled,
                peepholeActive = uiState.renderState.peepholeActive
            )

            // 3. Dual-Thumb Touch Input Zones (Left Move Joystick + Right Look Pad)
            if (!uiState.renderState.inputFrozen && !uiState.renderState.peepholeActive) {
                DualThumbControlLayer(
                    onMove = { mx, my -> glView.setMoveInput(mx, my) },
                    onLook = { dx, dy -> glView.addLookDelta(dx, dy) }
                )
            } else {
                LaunchedEffect(Unit) { glView.setMoveInput(0f, 0f) }
            }

            // 4. Center Crosshair & Contextual Target Pill
            if (uiState.activeDialogue == null && !uiState.isPhoneOpen && !uiState.isPaused) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val hasTarget = uiState.interactPrompt != null
                    Box(
                        modifier = Modifier
                            .size(if (hasTarget) 10.dp else 6.dp)
                            .background(
                                if (hasTarget) WarmTungsten else BoneText.copy(alpha = 0.75f),
                                CircleShape
                            )
                            .border(1.dp, Color.Black.copy(alpha = 0.65f), CircleShape)
                    )
                    if (uiState.interactPrompt != null) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Surface(
                                color = NightSlate.copy(alpha = 0.86f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, WarmTungsten.copy(alpha = 0.7f)),
                                modifier = Modifier.offset(y = 32.dp)
                            ) {
                                Text(
                                    text = uiState.interactPrompt,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BoneText,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 5. Safe-Area Top & Bottom HUD (RTL text inside)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Top-Left: Pause & VHS Timestamp
                Row(
                    modifier = Modifier.align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledIconButton(
                        onClick = viewModel::togglePause,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = NightSlate.copy(alpha = 0.82f),
                            contentColor = BoneText
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("pause_button")
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = "إيقاف مؤقت")
                    }

                    Surface(
                        color = NightSlate.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, HairlineBorder)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(RustAlert, CircleShape)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "REC ${uiState.stage.vhsTime}",
                                style = MaterialTheme.typography.labelMedium,
                                color = BoneText
                            )
                        }
                    }
                }

                // Top-Right: Sequential Objective Tracker (RTL)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        color = NightSlate.copy(alpha = 0.84f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, HairlineBorder),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .widthIn(max = 310.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(
                                text = uiState.stage.chapterTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = WarmTungsten
                            )
                            Spacer(Modifier.height(2.dp))
                            val extraProgress = when {
                                uiState.stage == StoryStage.CLEAN_FLOOR -> " (${uiState.cleanedCount}/3)"
                                uiState.policeCalled -> " — وصول الدورية خلال ${uiState.policeEtaSeconds} ث"
                                else -> ""
                            }
                            Text(
                                text = uiState.stage.objectiveTitle + extraProgress,
                                style = MaterialTheme.typography.bodyMedium,
                                color = BoneText
                            )
                        }
                    }
                }

                // Bottom-Center Subtitle / Narrative Toast
                AnimatedVisibility(
                    visible = uiState.toastSubtitle != null && uiState.activeDialogue == null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 14.dp)
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Surface(
                            color = ObsidianVoid.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, HairlineBorder),
                            modifier = Modifier.widthIn(max = 460.dp)
                        ) {
                            Text(
                                text = uiState.toastSubtitle.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = BoneText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                            )
                        }
                    }
                }

                // Bottom-Right Action Buttons (Interact, Flashlight, Phone)
                if (uiState.activeDialogue == null && !uiState.isPhoneOpen && !uiState.isPaused) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Phone Button with unread badge
                        BadgedBox(
                            badge = {
                                if (uiState.unreadMessages > 0) {
                                    Badge(containerColor = RustAlert) {
                                        Text("${uiState.unreadMessages}", color = BoneText)
                                    }
                                }
                            }
                        ) {
                            FilledIconButton(
                                onClick = viewModel::togglePhone,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = NightSlate.copy(alpha = 0.86f),
                                    contentColor = if (uiState.stage == StoryStage.CHECK_PHONE_AT_HOME ||
                                        uiState.stage == StoryStage.CALL_POLICE_AND_SURVIVE
                                    ) WarmTungsten else BoneText
                                ),
                                modifier = Modifier
                                    .size(54.dp)
                                    .border(1.dp, HairlineBorder, CircleShape)
                                    .testTag("phone_button")
                            ) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = "الهاتف")
                            }
                        }

                        // Flashlight Button
                        FilledIconButton(
                            onClick = viewModel::toggleFlashlight,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (uiState.renderState.flashlightOn) WarmTungsten else NightSlate.copy(alpha = 0.86f),
                                contentColor = if (uiState.renderState.flashlightOn) ObsidianVoid else BoneText
                            ),
                            modifier = Modifier
                                .size(54.dp)
                                .border(1.dp, HairlineBorder, CircleShape)
                                .testTag("flashlight_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.renderState.flashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                contentDescription = "المصباح"
                            )
                        }

                        // Interact Button
                        val canInteract = uiState.currentTarget != null
                        Button(
                            onClick = viewModel::interactWithCurrentTarget,
                            enabled = canInteract,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canInteract) WarmTungsten else NightSlate.copy(alpha = 0.65f),
                                contentColor = ObsidianVoid,
                                disabledContainerColor = NightSlate.copy(alpha = 0.45f),
                                disabledContentColor = MutedAsh
                            ),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("interact_button")
                        ) {
                            Icon(Icons.Default.TouchApp, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Text("تفاعل", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 6. Dialogue Overlay (Arabic RTL)
            if (uiState.activeDialogue != null) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    DialogueOverlay(
                        node = uiState.activeDialogue,
                        onChoose = viewModel::selectDialogueChoice
                    )
                }
            }

            // 7. Smartphone Overlay (Arabic RTL)
            if (uiState.isPhoneOpen) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    SmartphoneOverlay(
                        uiState = uiState,
                        onClose = viewModel::togglePhone,
                        onCallPolice = viewModel::callEmergencyPolice
                    )
                }
            }

            // 8. Pause / Settings Overlay (Arabic RTL)
            if (uiState.isPaused) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    PauseOverlay(
                        uiState = uiState,
                        onResume = viewModel::togglePause,
                        onReturnTitle = viewModel::returnToTitle,
                        onUpdateSensitivity = viewModel::updateSensitivity,
                        onUpdatePsxJitter = viewModel::updatePsxJitter,
                        onUpdateCrtScanlines = viewModel::updateCrtScanlines,
                        onUpdateVolume = viewModel::updateVolume
                    )
                }
            }
        }
    }
}

@Composable
private fun DualThumbControlLayer(
    onMove: (Float, Float) -> Unit,
    onLook: (Float, Float) -> Unit
) {
    val density = LocalDensity.current
    val maxRadiusPx = with(density) { 52.dp.toPx() }
    var stickOrigin by remember { mutableStateOf<Offset?>(null) }
    var stickKnob by remember { mutableStateOf(Offset.Zero) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Half: Virtual Analog Movement Joystick
        BoxWithConstraints(
            modifier = Modifier
                .weight(0.46f)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        stickOrigin = down.position
                        stickKnob = Offset.Zero
                        onMove(0f, 0f)
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null || !change.pressed) {
                                stickOrigin = null
                                stickKnob = Offset.Zero
                                onMove(0f, 0f)
                                break
                            }
                            change.consume()
                            val raw = change.position - down.position
                            val dist = sqrt(raw.x * raw.x + raw.y * raw.y)
                            val clamped = if (dist > maxRadiusPx) {
                                Offset(raw.x / dist * maxRadiusPx, raw.y / dist * maxRadiusPx)
                            } else raw
                            stickKnob = clamped
                            // Screen Y increases downward, forward movement is -Y
                            onMove(clamped.x / maxRadiusPx, -clamped.y / maxRadiusPx)
                        }
                    }
                }
        ) {
            val baseCenter = stickOrigin ?: Offset(
                with(density) { 92.dp.toPx() },
                constraints.maxHeight - with(density) { 90.dp.toPx() }
            )
            val baseSizePx = with(density) { 104.dp.toPx() }
            val knobSizePx = with(density) { 44.dp.toPx() }

            // Outer ring
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (baseCenter.x - baseSizePx / 2f).roundToInt(),
                            (baseCenter.y - baseSizePx / 2f).roundToInt()
                        )
                    }
                    .size(104.dp)
                    .background(NightSlate.copy(alpha = 0.34f), CircleShape)
                    .border(1.5.dp, BoneText.copy(alpha = 0.28f), CircleShape)
            )
            // Inner movable thumb knob
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (baseCenter.x + stickKnob.x - knobSizePx / 2f).roundToInt(),
                            (baseCenter.y + stickKnob.y - knobSizePx / 2f).roundToInt()
                        )
                    }
                    .size(44.dp)
                    .background(
                        if (stickOrigin != null) WarmTungsten.copy(alpha = 0.85f) else BoneText.copy(alpha = 0.45f),
                        CircleShape
                    )
            )
        }

        // Right Half: Touch Look Area
        Box(
            modifier = Modifier
                .weight(0.54f)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null || !change.pressed) break
                            val delta = change.positionChange()
                            if (delta != Offset.Zero) {
                                change.consume()
                                onLook(delta.x, delta.y)
                            }
                        }
                    }
                }
        )
    }
}

@Composable
private fun DialogueOverlay(
    node: DialogueNode,
    onChoose: (DialogueChoice) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.36f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = NightSlate.copy(alpha = 0.95f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, WarmTungsten.copy(alpha = 0.55f)),
            modifier = Modifier.widthIn(max = 680.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = node.speaker,
                        style = MaterialTheme.typography.titleMedium,
                        color = WarmTungsten
                    )
                    Text(
                        text = "• ${node.speakerRole}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedAsh
                    )
                }

                Text(
                    text = node.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = BoneText
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    node.choices.forEachIndexed { idx, choice ->
                        OutlinedButton(
                            onClick = { onChoose(choice) },
                            border = BorderStroke(1.dp, HairlineBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialogue_choice_$idx")
                        ) {
                            Text(
                                text = choice.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = BoneText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartphoneOverlay(
    uiState: GameUiState,
    onClose: () -> Unit,
    onCallPolice: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = NightSlate,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(2.dp, HairlineBorder),
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxHeight(0.92f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الرسائل والطوارئ — ${uiState.stage.vhsTime.take(5)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = CrtPhosphor
                    )
                    IconButton(onClick = onClose, modifier = Modifier.testTag("close_phone_button")) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق الهاتف", tint = BoneText)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.messages.reversed().forEach { msg ->
                        Surface(
                            color = if (msg.isAlert) RustAlert.copy(alpha = 0.16f) else ElevatedPanel,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (msg.isAlert) RustAlert.copy(alpha = 0.65f) else HairlineBorder
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (msg.isAlert) {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = RustAlert,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = msg.sender,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (msg.isAlert) RustAlert else WarmTungsten
                                        )
                                    }
                                    Text(msg.time, style = MaterialTheme.typography.labelMedium, color = MutedAsh)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(msg.body, style = MaterialTheme.typography.bodyMedium, color = BoneText)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = onCallPolice,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RustAlert,
                        contentColor = BoneText
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("call_police_button")
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (uiState.policeCalled) "تم الاتصال بالنجدة (٩٩٩)" else "اتصال طارئ بالنجدة (٩٩٩)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PauseOverlay(
    uiState: GameUiState,
    onResume: () -> Unit,
    onReturnTitle: () -> Unit,
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
            modifier = Modifier.widthIn(max = 480.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("إيقاف مؤقت", style = MaterialTheme.typography.titleLarge, color = WarmTungsten)

                Text("حساسية الكاميرا: ${"%.1f".format(uiState.settings.lookSensitivity)}x", color = BoneText)
                Slider(
                    value = uiState.settings.lookSensitivity,
                    onValueChange = onUpdateSensitivity,
                    valueRange = 0.4f..2.2f
                )

                Text("مستوى الصوت: ${(uiState.settings.masterVolume * 100).toInt()}%", color = BoneText)
                Slider(
                    value = uiState.settings.masterVolume,
                    onValueChange = onUpdateVolume,
                    valueRange = 0f..1f
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ارتجاف المضلعات (PSX)", color = BoneText)
                    Switch(checked = uiState.settings.psxJitterEnabled, onCheckedChange = onUpdatePsxJitter)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("خطوط الشاشة (CRT)", color = BoneText)
                    Switch(checked = uiState.settings.crtScanlinesEnabled, onCheckedChange = onUpdateCrtScanlines)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onResume,
                        colors = ButtonDefaults.buttonColors(containerColor = WarmTungsten, contentColor = ObsidianVoid),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("متابعة", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onReturnTitle,
                        border = BorderStroke(1.dp, HairlineBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = BoneText)
                        Spacer(Modifier.width(4.dp))
                        Text("القائمة", color = BoneText)
                    }
                }
            }
        }
    }
}

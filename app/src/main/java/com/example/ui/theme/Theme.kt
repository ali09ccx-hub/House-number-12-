package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val House12ColorScheme = darkColorScheme(
    primary = WarmTungsten,
    onPrimary = ObsidianVoid,
    primaryContainer = DimAmber,
    onPrimaryContainer = BoneText,
    secondary = CrtPhosphor,
    onSecondary = ObsidianVoid,
    tertiary = RustAlert,
    onTertiary = BoneText,
    background = ObsidianVoid,
    onBackground = BoneText,
    surface = NightSlate,
    onSurface = BoneText,
    surfaceVariant = ElevatedPanel,
    onSurfaceVariant = MutedAsh,
    outline = HairlineBorder,
    error = RustAlert,
    onError = BoneText
)

@Composable
fun House12Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = House12ColorScheme,
        typography = House12Typography,
        content = content
    )
}

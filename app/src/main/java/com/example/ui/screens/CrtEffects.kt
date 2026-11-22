package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun CrtOverlay(enabled: Boolean, peepholeActive: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (enabled) {
            val step = 5f
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = Color.Black.copy(alpha = 0.16f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.5f
                )
                y += step
            }
            // Subtle VHS edge vignette
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.42f)),
                    center = center,
                    radius = size.maxDimension * 0.68f
                )
            )
        }

        if (peepholeActive) {
            // Optical peephole vignette
            drawRect(
                brush = Brush.radialGradient(
                    0.0f to Color.Transparent,
                    0.34f to Color.Transparent,
                    0.46f to Color.Black.copy(alpha = 0.94f),
                    1.0f to Color.Black,
                    center = center,
                    radius = size.minDimension * 0.72f
                )
            )
        }
    }
}

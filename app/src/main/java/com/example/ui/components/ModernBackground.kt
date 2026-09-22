package com.example.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Modern Ambient Aurora Background designed for 120Hz high-refresh performance.
 * Uses hardware-accelerated drawBehind with zero layout passes and zero recomposition overhead.
 */
@Composable
fun ModernBackground(
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable BoxScope.() -> Unit
) {
    val baseBg = if (isDark) Color(0xFF090D16) else Color(0xFFF8FAFC)
    val glow1 = if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.22f) else Color(0xFF93C5FD).copy(alpha = 0.25f)
    val glow2 = if (isDark) Color(0xFF4F46E5).copy(alpha = 0.16f) else Color(0xFFA5B4FC).copy(alpha = 0.22f)
    val glow3 = if (isDark) Color(0xFF0D9488).copy(alpha = 0.12f) else Color(0xFF6EE7B7).copy(alpha = 0.18f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // 1. Solid base background
                drawRect(color = baseBg)

                val w = size.width
                val h = size.height

                // 2. Top-right cyan/blue ambient orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glow1, Color.Transparent),
                        center = Offset(w * 0.85f, h * 0.12f),
                        radius = w * 0.65f
                    ),
                    center = Offset(w * 0.85f, h * 0.12f),
                    radius = w * 0.65f
                )

                // 3. Middle-left indigo/purple ambient orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glow2, Color.Transparent),
                        center = Offset(w * 0.1f, h * 0.45f),
                        radius = w * 0.7f
                    ),
                    center = Offset(w * 0.1f, h * 0.45f),
                    radius = w * 0.7f
                )

                // 4. Bottom-right subtle teal/emerald ambient orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glow3, Color.Transparent),
                        center = Offset(w * 0.8f, h * 0.85f),
                        radius = w * 0.6f
                    ),
                    center = Offset(w * 0.8f, h * 0.85f),
                    radius = w * 0.6f
                )
            },
        content = content
    )
}

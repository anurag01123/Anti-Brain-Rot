package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .then(if (hazeState != null) Modifier.hazeChild(state = hazeState) else Modifier)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    )
                ),
                shape = shape
            ),
        content = content
    )
}

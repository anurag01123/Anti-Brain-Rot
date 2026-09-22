package com.example.ui.utils

import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset

object FluidSprings {
    /**
     * High-refresh 120Hz responsive spring specs.
     */
    val QuickBounce = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val SmoothSlide = spring<Float>(
        dampingRatio = 0.85f,
        stiffness = 380f
    )

    val SmoothSlideOffset = spring<IntOffset>(
        dampingRatio = 0.85f,
        stiffness = 380f
    )

    val ColorSpring = spring<Color>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val Snappy = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}

/**
 * Modern GPU-accelerated tactile press bounce modifier.
 * Uses graphicsLayer to avoid Compose layout passes, ensuring locked 120Hz smoothness.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.96f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    val view: View = LocalView.current

    LaunchedEffect(isPressed) {
        if (isPressed) {
            Haptics.playLightTick(view)
            scale.animateTo(scaleDown, FluidSprings.QuickBounce)
        } else {
            scale.animateTo(1f, FluidSprings.QuickBounce)
        }
    }

    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

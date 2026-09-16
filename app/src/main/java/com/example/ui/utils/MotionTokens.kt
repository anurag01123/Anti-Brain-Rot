package com.example.ui.utils

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween

object MotionTokens {
    val EasingStandard = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EasingDecelerate = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
    
    fun <T> micro() = tween<T>(durationMillis = 120, easing = EasingStandard)
    fun <T> standard() = tween<T>(durationMillis = 220, easing = EasingStandard)
    fun <T> emphasis() = tween<T>(durationMillis = 400, easing = EasingDecelerate)
}

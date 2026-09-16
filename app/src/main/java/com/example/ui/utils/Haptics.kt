package com.example.ui.utils

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

object Haptics {
    fun playLightTick(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun playMediumToggle(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun playSuccess(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    fun playWarning(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }
}

package com.masum.fathom.core.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.compositionLocalOf

enum class HapticStyle {
    LIGHT,
    MEDIUM,
    HEAVY,
    SUCCESS,
    ERROR
}

class FathomHaptics(private val context: Context) {
    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun perform(style: HapticStyle) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        try {
            when (style) {
                HapticStyle.LIGHT -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(12L, 80))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(12L)
                    }
                }
                HapticStyle.MEDIUM -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(24L, 160))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(24L)
                    }
                }
                HapticStyle.HEAVY -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(45L)
                    }
                }
                HapticStyle.SUCCESS -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 20, 60, 30), intArrayOf(0, 120, 0, 200), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(longArrayOf(0, 20, 60, 30), -1)
                    }
                }
                HapticStyle.ERROR -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 50, 40), intArrayOf(0, 220, 0, 220), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(longArrayOf(0, 40, 50, 40), -1)
                    }
                }
            }
        } catch (_: Throwable) {
        }
    }

    fun light() = perform(HapticStyle.LIGHT)
    fun medium() = perform(HapticStyle.MEDIUM)
    fun heavy() = perform(HapticStyle.HEAVY)
    fun success() = perform(HapticStyle.SUCCESS)
    fun error() = perform(HapticStyle.ERROR)
}

val LocalFathomHaptics = compositionLocalOf<FathomHaptics> {
    error("No FathomHaptics provided")
}

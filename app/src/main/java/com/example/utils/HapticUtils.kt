package com.example.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticUtils {

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Subtle tick effect for seeking/scrubbing steps or minor gesture level changes.
     */
    fun performTick(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(8, 40))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(8)
            }
        } catch (e: Exception) {
            // Ignore if vibration fails on specific hardware
        }
    }

    /**
     * Click effect for button taps and double-tap seeking.
     */
    fun performClick(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(15, 100))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(15)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Distinct feedback for locking and unlocking player controls.
     */
    fun performLockToggle(context: Context, isNowLocked: Boolean) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = if (isNowLocked) {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                } else {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                }
                vibrator.vibrate(effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = if (isNowLocked) 200 else 120
                vibrator.vibrate(VibrationEffect.createOneShot(25, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(25)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}

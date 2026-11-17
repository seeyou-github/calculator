package net.youapps.calcyou.ui.components.buttons

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Vibrates for keypad interactions using the [Vibrator] service directly.
 * Unlike [android.view.View.performHapticFeedback], this also works when
 * the system-wide touch feedback setting is disabled.
 */
fun Context.vibrateOnClick(longPress: Boolean = false) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    } ?: return

    if (!vibrator.hasVibrator()) return

    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> vibrator.vibrate(
            VibrationEffect.createPredefined(
                if (longPress) VibrationEffect.EFFECT_HEAVY_CLICK else VibrationEffect.EFFECT_CLICK
            )
        )

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> vibrator.vibrate(
            VibrationEffect.createOneShot(
                if (longPress) 50 else 30,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )

        else -> {
            @Suppress("DEPRECATION")
            vibrator.vibrate(if (longPress) 50 else 30)
        }
    }
}

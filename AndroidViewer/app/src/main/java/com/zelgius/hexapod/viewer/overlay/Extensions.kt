package com.zelgius.hexapod.viewer.overlay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.withRotation
import androidx.core.graphics.withSave
import androidx.core.graphics.withScale
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

fun Bitmap.dp(
    dp: Int,
    diagonalInch: Float = (sqrt(
        width.toDouble().pow(2) + height.toDouble().pow(2)
    ) / 2.54).toFloat()
): Float =
    (dp * sqrt((width * width.toDouble()) + (height * height)) / diagonalInch).toFloat() / 1000

fun Canvas.dp(
    dp: Float,
    diagonalInch: Float = (sqrt(
        width.toDouble().pow(2) + height.toDouble().pow(2)
    ) / 1000 / 2.54).toFloat()
): Float =
    (dp * sqrt((width * width.toDouble()) + (height * height)) / diagonalInch).toFloat() / 1000

fun Int.alpha(alpha: Float): Int =
    Color.argb(
        (alpha * 255).roundToInt(),
        Color.red(this),
        Color.green(this),
        Color.blue(this)
    )


fun Bitmap.drawOverlay(context: Context, info: OverlayInfo): Bitmap {
    val paint = Paint()
    val copy = copy(Bitmap.Config.ARGB_8888, true)
    Canvas(copy).apply {
        withScale(1f, -1f, width / 2f, height / 2f) {
            //EyeSeparator(this, paint).draw()
            BatteryLevel(context, this, paint).draw(info.batteryLevel)
            WalkMode(context, this, paint).draw(info.walkMode)
            Claw(context, this, paint).draw(info.clawIndex)
        }
    }

    return copy
}
package com.zelgius.hexapod.viewer.overlay

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

fun Bitmap.dp(dp: Int, diagonalInch: Float = (sqrt(width.toDouble().pow(2) + height.toDouble().pow(2)) / 2.54).toFloat()): Float = (dp * sqrt((width * width.toDouble()) + (height * height)) / diagonalInch).toFloat() / 1000
fun Canvas.dp(dp: Float, diagonalInch: Float =  (sqrt(width.toDouble().pow(2) + height.toDouble().pow(2))/ 1000 / 2.54).toFloat()): Float =
        (dp * sqrt((width * width.toDouble()) + (height * height)) / diagonalInch).toFloat() / 1000

fun Int.alpha(alpha: Float): Int =
        Color.argb(
                (alpha * 255).roundToInt(),
                Color.red(this),
                Color.green(this),
                Color.blue(this)
        )
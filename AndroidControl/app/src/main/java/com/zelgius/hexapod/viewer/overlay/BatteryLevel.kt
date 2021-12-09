package com.zelgius.hexapod.viewer.overlay

import android.content.Context
import android.graphics.*
import androidx.core.graphics.withTranslation
import com.zelgius.hexapod.viewer.R

class BatteryLevel(val context: Context, canvas: Canvas, private val paint: Paint) : Overlay(canvas) {
    fun draw(level: Int) {
        drawLevel(level, )
    }

    private fun drawLevel(level: Int) {
        with(canvas) {
            val thickness = 4f.x

            paint.apply {
                reset()
                color = context.getColor(R.color.hud_main_color)
                style = Paint.Style.STROKE
                strokeWidth = thickness
            }
            drawPath(Path().apply {
                moveTo(end - 35.x, offset.y)
                lineTo(end - 35f.x, 100f.y + offset.y)
                lineTo(end - 5f.x, 150f.y + offset.y)
                lineTo(end - 5f.x, bottom.y - WalkMode.offsetY.y-  WalkMode.size.y  - (WalkMode.margin.y) /2)
                lineTo(end - WalkMode.offsetX.x -  WalkMode.size.x - (WalkMode.margin.y) /2  , bottom.y - 5f.y - offset.y)

            }, paint)


            paint.apply {
                reset()
                color = context.getColor(R.color.hud_main_color)
                style = Paint.Style.STROKE
                strokeWidth = 2.x
            }
            drawPath(Path().apply {
                moveTo(end - 12f.x, 145f.y + offset.y)
                lineTo(end - 12f.x, bottom.y - WalkMode.offsetY.y -  WalkMode.size.y  - (WalkMode.margin.y) /2)
                lineTo(end - WalkMode.offsetX.x -  WalkMode.size.x  - (WalkMode.margin.y) /2  , bottom.y - 12f.y - offset.y)
            }, paint)


            paint.apply {
                reset()
                color = when (level) {
                    in 0..30 -> context.getColor(R.color.hud_battery_low)
                    in 31..60 -> context.getColor(R.color.hud_battery_medium)
                    else -> context.getColor(R.color.hud_battery_high)
                }.alpha(.7f)

                style = Paint.Style.FILL
            }

            val bmp = Bitmap.createBitmap(42.x.toInt(), 145.y.toInt(), Bitmap.Config.ARGB_8888)
            val c = Canvas(bmp)
            c.withTranslation(0f.x, (-((100 - level)/ 100f) * bmp.height).y) {

                drawPath(with(Path()) {
                    moveTo(0.x, 0f.y)
                    lineTo(0.x, 100f.y)
                    lineTo(27f.x, 145f.y)
                    lineTo(27f.x, 0f.y)
                    //lineTo(end - 42f.x, 0f.y)
                    close()
                    this
                }, paint)
            }

            drawBitmap(bmp, end - 32f.x, (((100 - level)/ 100f) * bmp.height).y + offset.y, paint)

        }
    }


    private fun getSubPath(path: Path, start: Float, end: Float): Path {
        val subPath = Path()
        val pathMeasure = PathMeasure(path, false)
        pathMeasure.getSegment(start * pathMeasure.length, end * pathMeasure.length, subPath, true)
        return subPath
    }
}
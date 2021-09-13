package com.zelgius.hexapod.viewer.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.annotation.IntRange
import com.zelgius.hexapod.viewer.R

class Claw(val context: Context, canvas: Canvas, private val paint: Paint) : Overlay(canvas) {
    fun draw(@IntRange(from = 0, to = 3) activated: Int) {
        for (i in 0..3) {
            if (i == 3) drawClaw(i, activated = activated == i)
            else drawPart(i, activated = activated == i)
        }

        for (i in 0..3) {
            if (i == 3) drawClaw(i, activated = activated == i)
            else drawPart(i, activated = activated == i)
        }
    }

    private fun drawPart(index: Int, activated: Boolean) {

        with(canvas) {
            val thickness = 4f.x

            paint.apply {
                reset()
                color = if (!activated)
                    context.getColor(R.color.hud_main_color).alpha(.5f)
                else
                    context.getColor(R.color.hud_battery_high).alpha(.5f)
                style = Paint.Style.FILL
                strokeWidth = thickness
            }

            Path().apply {
                val startX = start + 30.x
                val startY = bottom.y - 40.y - index * 30.y - offset.y

                moveTo(startX + 5.x, startY)
                // _
                lineTo(startX + 20.x, startY)

                // _/
                lineTo(startX + 25.x, startY - 10.y)

                //  \
                // _/
                lineTo(startX + 20.x, startY - 20.y)

                // -\
                // _/
                lineTo(startX + 5.x, startY - 20.y)

                // /-\
                //  _/
                lineTo(startX, startY - 10.y)

                // /-\
                // \_/
                lineTo(startX + 5.x, startY)

                drawPath(this, paint)
            }
        }
    }

    private fun drawClaw(index: Int, activated: Boolean) {

        with(canvas) {
            val thickness = 4f.x

            paint.apply {
                reset()
                color = if (!activated)
                    context.getColor(R.color.hud_main_color).alpha(.5f)
                else
                    context.getColor(R.color.hud_battery_high).alpha(.5f)
                style = Paint.Style.STROKE
                strokeWidth = thickness
            }

            Path().apply {
                val startX = start + 30.x
                val startY = bottom.y - 40.y - 3 * 30.y - offset.y

                moveTo(startX + 5.x, startY)

                // \
                lineTo(startX, startY - 10.y)

                // /
                // \
                lineTo(startX + 5.x, startY - 20.y)

                moveTo(startX + 20.x, startY )
                //  /
                lineTo(startX + 25.x, startY - 10.y)

                //  \
                //  /
                lineTo(startX + 20.x, startY - 20.y)


                drawPath(this, paint)
            }
        }
    }
}
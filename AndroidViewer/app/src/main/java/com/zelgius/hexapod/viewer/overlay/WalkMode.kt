package com.zelgius.hexapod.viewer.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import com.zelgius.hexapod.viewer.R

class WalkMode(private val context: Context, canvas: Canvas, private val paint: Paint) :
    Overlay(canvas) {

    fun draw(mode: WalkMode) {
        drawMode(mode)
    }

    private fun drawMode(mode: WalkMode) {

        paint.apply {
            reset()
            color = context.getColor(R.color.hud_main_color)
            style = Paint.Style.FILL
        }

        canvas.withRotation(
            45f, end - offsetX.x - size.x - margin.x / 2,
            bottom.y - offsetY.y - size.y - margin.y / 2
        ) {

            // ^
            paint.color = if (mode == WalkMode.RIPPLE)
                context.getColor(R.color.hud_main_color)
            else
                context.getColor(R.color.hud_main_color).alpha(0.3f)

            drawRect(
                end - 2 * size.x - margin.x - offsetX.x,
                bottom.y - 2 * size.y - margin.y - offsetY.y,
                end - size.x - margin.x - offsetX.x,
                bottom.y - size.y - margin.y - offsetY.y, paint
            )

            // >
            paint.color = if (mode == WalkMode.TETRAPOD)
                context.getColor(R.color.hud_main_color)
            else
                context.getColor(R.color.hud_main_color).alpha(0.3f)

            drawRect(
                end - size.x - offsetX.x,
                bottom.y - 2 * size.y - offsetY.y - margin.y,
                end - offsetX.x,
                bottom.y - size.y - margin.y - offsetY.y, paint
            )

            // <
            paint.color = if (mode == WalkMode.WAVE)
                context.getColor(R.color.hud_main_color)
            else
                context.getColor(R.color.hud_main_color).alpha(0.3f)

            drawRect(
                end - 2 * size.x - margin.x - offsetX.x,
                bottom.y - size.y - offsetY.y,
                end - size.x - margin.x - offsetX.x,
                bottom.y - offsetY.y, paint
            )

            // v
            paint.color = if (mode == WalkMode.TRIPOD)
                context.getColor(R.color.hud_main_color)
            else
                context.getColor(R.color.hud_main_color).alpha(0.3f)

            drawRect(
                end - size.x - offsetX.x,
                bottom.y - size.y - offsetY.y,
                end - offsetX.x,
                bottom.y - offsetY.y, paint
            )

        }

        with(canvas) {
            val centerX = end - offsetX.x - size.x - margin.x / 2
            val centerY = bottom.y - offsetY.y - size.y - margin.y / 2
            val margin = (size - icon) / 2 + (size - icon) / 4
            // -
            paint.color = if (mode == WalkMode.WAVE)
                Color.BLACK
            else
                Color.BLACK.alpha(0.5f)

            drawDrawable(
                R.drawable.ic_wave_gait,
                Rect(
                    (centerX - size.x + margin.x).toInt(),
                    (centerY).toInt(),
                    (centerX - size.x + margin.x + icon.x).toInt(),
                    (centerY + icon.y).toInt()
                ),
                paint
            )

            // - -
            paint.color = if (mode == WalkMode.TETRAPOD)
                Color.BLACK
            else
                Color.BLACK.alpha(0.3f)

            drawDrawable(
                R.drawable.ic_tetrapod_gait,
                Rect(
                    (centerX + size.x - margin.x).toInt(),
                    (centerY).toInt(),
                    (centerX + size.x - margin.x + icon.x).toInt(),
                    (centerY + icon.y).toInt()
                ),
                paint
            )

            // -'-
            paint.color = if (mode == WalkMode.RIPPLE)
                Color.BLACK
            else
                Color.BLACK.alpha(0.3f)

            drawDrawable(
                R.drawable.ic_ripple_gait,
                Rect(
                    (centerX).toInt(),
                    (centerY - size.y + margin.y).toInt(),
                    (centerX + icon.x).toInt(),
                    (centerY - size.y + margin.y + icon.y).toInt()
                ),
                paint
            )

            // -:-
            paint.color = if (mode == WalkMode.TRIPOD)
                Color.BLACK
            else
                Color.BLACK.alpha(0.3f)

            drawDrawable(
                R.drawable.ic_tripod_gait,
                Rect(
                    (centerX).toInt(),
                    (centerY + size.y - margin.y).toInt(),
                    (centerX + icon.x).toInt(),
                    (centerY + size.y - margin.y + icon.y).toInt()
                ),
                paint
            )
        }


        with(canvas) {
            paint.apply {
                reset()
                color = context.getColor(R.color.hud_main_color)
                style = Paint.Style.STROKE
                strokeWidth = 2.x
            }

            drawLine(
                end - offsetX.x - size.x - (margin.x) / 2,
                bottom.y - 12f.y - offset.y,
                start,
                bottom.y - 12.y - offset.y,
                paint
            )

            val thickness = 4f.x

            paint.apply {
                reset()
                color = context.getColor(R.color.hud_main_color)
                style = Paint.Style.STROKE
                strokeWidth = thickness
            }
            drawLine(
                end - offsetX.x - size.x - (margin.x) / 2,
                bottom.y - 5.y - offset.y,
                start,
                bottom.y - 5.y - offset.y,
                paint
            )
        }
    }

    companion object {
        const val size = 50f
        const val icon = 40f
        const val margin = 10f
        const val offsetX = 45f
        const val offsetY = 45f + offset
    }

    enum class WalkMode {
        WAVE, TRIPOD, TETRAPOD, RIPPLE
    }

    private fun Canvas.drawDrawable(@DrawableRes resource: Int, rect: Rect, paint: Paint) {
        withTranslation(
            rect.left.toFloat() - rect.width() / 2f,
            rect.top.toFloat() - rect.height() / 2f
        ) {
            ContextCompat.getDrawable(context, resource)?.let {
                it.setTint(paint.color)
                it.setBounds(0, 0, rect.width(), rect.height())
                it.draw(canvas)
            }
        }
    }
}
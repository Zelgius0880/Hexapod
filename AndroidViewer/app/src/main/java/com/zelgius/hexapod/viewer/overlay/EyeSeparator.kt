package com.zelgius.hexapod.viewer.overlay

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class EyeSeparator(canvas: Canvas, private val paint: Paint) : Overlay(canvas){
    fun draw() {
        val thickness = 2f.x

        paint.apply {
            reset()
            color = Color.WHITE
        }

        with(canvas) {
            drawRect(width/2 - thickness /2, height / 2f, width/2 + thickness /2, height.toFloat(), paint)
        }
    }
}
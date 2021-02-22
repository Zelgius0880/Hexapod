package com.zelgius.hexapod.viewer.overlay

import android.graphics.Bitmap
import android.graphics.Canvas

class EyeSeparator(val canvas: Canvas) {
    fun draw(level: Int){
        drawLevel(false)
        drawLevel(true)
    }

   private fun drawLevel(offset: Boolean = false) {

   }
}
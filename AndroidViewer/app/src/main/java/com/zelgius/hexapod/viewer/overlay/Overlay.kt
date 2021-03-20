package com.zelgius.hexapod.viewer.overlay

import android.graphics.Canvas

open class Overlay(val canvas: Canvas) {

    private val referenceWidth = 1200
    private val referenceHeight = 600

    val Float.x
        get() = canvas.width / referenceWidth.toFloat() * this
    val Int.x
        get() = canvas.width / referenceWidth.toFloat() * this
    val Float.y
        get() = canvas.height / referenceHeight.toFloat() * this
    val Int.y
        get() = canvas.height / referenceHeight.toFloat() * this
}
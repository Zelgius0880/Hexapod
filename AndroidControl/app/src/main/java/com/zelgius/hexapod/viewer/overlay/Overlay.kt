package com.zelgius.hexapod.viewer.overlay

import android.graphics.Canvas

abstract class Overlay(val canvas: Canvas) {

    companion object{
        const val offset = 50f
    }

    private val referenceWidth = 600
    private val referenceHeight = 600

    protected val start = 50f + offset
    protected val bottom = canvas.height - offset

    protected val end = canvas.width - offset.x

    val Float.x
        get() = canvas.width / referenceWidth.toFloat() * this
    val Int.x
        get() = canvas.width / referenceWidth.toFloat() * this
    val Float.y
        get() = canvas.height / referenceHeight.toFloat() * this
    val Int.y
        get() = canvas.height / referenceHeight.toFloat() * this
}


data class OverlayInfo(
    val batteryLevel: Int,
    val walkMode: WalkMode.WalkMode,
    val clawIndex: Int
)
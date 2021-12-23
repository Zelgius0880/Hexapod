package com.zelgius.api.model

import com.zelgius.controller.ControlOuterClass

data class Control(
    val isPressed: Boolean,
    val type: Type,
    val xL: Int = 0,
    val yL: Int = 0,
    val zL: Int = 0,
    val xR: Int = 0,
    val yR: Int = 0,
    val zR: Int = 0
) {

    enum class Type(val code: Int) {
        CROSS_LEFT(0),
        CROSS_RIGHT(1),
        CROSS_UP(2),
        CROSS_DOWN(3),
        BUTTON_A(4),
        BUTTON_B(5),
        BUTTON_X(11),
        BUTTON_Y(12),
        BUTTON_MINUS(7),
        BUTTON_PLUS(6),
        BUTTON_HOME(8),
        BUTTON_L(13),
        BUTTON_R(14),
        BUTTON_ZL(15),
        BUTTON_ZR(16),
        BUTTON_STICK_L(17),
        BUTTON_STICK_R(18),
        STICK(19)
    }
}

fun ControlOuterClass.Control.toDataClass() =
    Control(this.isPressed, Control.Type.values().first { it.code == this.type.number }, xl, yl, zl, xr, yr, zr)

fun Control.toProtobuf() = ControlOuterClass.Control.newBuilder()
    .setIsPressed(isPressed)
    .setTypeValue(type.code )
    .setXL(xL)
    .setYL(yL)
    .setZL(zL)
    .setXR(xR)
    .setYR(yR)
    .setZR(zR)
    .build()
package com.zelgius.remoteController.arm.controls

import java.nio.ByteBuffer

class Control(val type: CONTROLS, data: ByteBuffer) {
    val batteryLevel: Int = data.short.toInt()
    val isPressed: Boolean = data.get() == 0x01.toByte()

    constructor(data: ByteBuffer) : this(
        type = data.get()
            .let { b ->
                CONTROLS.values()
                    .find {
                        b.toInt() == it.code
                    }!!
            }, data
    )
}

enum class CONTROLS(val code: Int) {
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
    STICK(19),
}

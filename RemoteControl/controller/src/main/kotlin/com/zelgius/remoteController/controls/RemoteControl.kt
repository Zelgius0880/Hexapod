package com.zelgius.remoteController.controls

import java.nio.ByteBuffer

class RemoteControl(val type: CONTROLS, val data: ByteBuffer) {
    var timestamp: Long = System.currentTimeMillis()
    val batteryLevel: Int = data.short.toInt()
    val isPressed: Boolean = if (type != CONTROLS.STICK) data.short == 0x01.toShort() else false

    constructor(data: ByteBuffer) : this(
        type = data.get()
            .let { b ->
                CONTROLS.values()
                    .find {
                        b.toInt() == it.code
                    }!!
            }, data
    )

    fun array(): ByteArray = data.array().also {
        println(it.joinToString { b -> String.format("%2x", b) })
    }

    fun frame(): String = (if (type == CONTROLS.STICK)
        StringBuilder("[${type.code}")
    else
        StringBuilder("[${type.code},${if (isPressed) 1 else 0}")).apply {
        while (data.hasRemaining()) {
            append(",${data.short}")
        }
        append("]\n")
    }.toString()

    override fun toString(): String {
        return "RemoteControl(type=$type, batteryLevel=$batteryLevel, isPressed=$isPressed)"
    }


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

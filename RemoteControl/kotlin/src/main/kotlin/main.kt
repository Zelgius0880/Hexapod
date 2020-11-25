package com.zelgius.remoteController

import com.pi4j.io.serial.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.nio.ByteBuffer


fun main(args: Array<String>) {
    testingInput()
}

fun testingInput() {
    val ds = DatagramSocket(5005)
    val buf = ByteArray(128)

    val config = SerialConfig()
    config.device(SerialPort.getDefaultPort())
        .baud(Baud._9600)
        .dataBits(DataBits._8)
        .parity(Parity.NONE)
        .stopBits(StopBits._1)
        .flowControl(FlowControl.NONE)
    val serial = SerialFactory.createInstance()
    serial.open(config)

    while (true) {
        val dp = DatagramPacket(buf, 128)
        ds.receive(dp)

        val result = dp.data.sliceArray(0 until dp.length)
        println(result.joinToString())
        transmitUart(serial, result)
    }
    ds.close()
}

fun transmitUart(serial: Serial, bytes: ByteArray) {
    val array = if (bytes.size > 4) {
        ByteBuffer.allocate(7).apply {
            put(19.toByte())
            put(bytes.sliceArray(3..14))
        }.array()
    } else {
        when (
            CONTROLS.values()
                .find { it.code == bytes.first().toInt() }?: error("Unknown input: ${bytes.first()}")) {
            CONTROLS.CROSS_LEFT,
            CONTROLS.CROSS_RIGHT,
            CONTROLS.CROSS_UP,
            CONTROLS.CROSS_DOWN,
            CONTROLS.BUTTON_A,
            CONTROLS.BUTTON_B,
            CONTROLS.BUTTON_X,
            CONTROLS.BUTTON_Y,
            CONTROLS.BUTTON_MINUS,
            CONTROLS.BUTTON_PLUS,
            CONTROLS.BUTTON_HOME,
            CONTROLS.BUTTON_L,
            CONTROLS.BUTTON_R,
            CONTROLS.BUTTON_ZL,
            CONTROLS.BUTTON_ZR,
            CONTROLS.BUTTON_STICK_L,
            CONTROLS.BUTTON_STICK_R -> {
                ByteBuffer.allocate(2).apply {
                    put(bytes.first())
                    put(bytes[3])
                }.array()
            }
        }
    }

    serial.write(array)
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
}

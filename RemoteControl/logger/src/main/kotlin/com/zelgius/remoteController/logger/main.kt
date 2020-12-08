package com.zelgius.remoteController.logger

import com.pi4j.io.serial.*
import java.nio.ByteBuffer
import kotlin.concurrent.thread


fun main(args: Array<String>) {
    startLogger()
    //testingInput()
}

fun startLogger() {
    thread {
        val config = SerialConfig()
        config
                .device("/dev/ttyAMA1")
                .baud(Baud._9600)
                .dataBits(DataBits._8)
                .parity(Parity.NONE)
                .stopBits(StopBits._1)
                .flowControl(FlowControl.NONE)
        val serial = SerialFactory.createInstance()
        serial.open(config)
        println("=== UART Ready /dev/ttyAMA1 ===")

        while (true) {
            val input = serial.inputStream
            with(input.read()) {
                print(toChar())
            }
        }
    }
}

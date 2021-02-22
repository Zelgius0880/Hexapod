package com.zelgius.remoteController.logger

import com.pi4j.io.serial.*
import kotlinx.coroutines.coroutineScope
import java.nio.ByteBuffer
import kotlin.concurrent.thread


fun main(args: Array<String>) {
    startLogger()
    //testingInput()
}
const val serialPort = "/dev/ttyS0"
fun startLogger() {
    thread {
        val config = SerialConfig()
        config
                .device(serialPort)
                .baud(Baud._9600)
                .dataBits(DataBits._8)
                .parity(Parity.NONE)
                .stopBits(StopBits._1)
                .flowControl(FlowControl.NONE)
        val serial = SerialFactory.createInstance()
        serial.open(config)
        println("=== UART Ready $serialPort ===")

        while (true) {
            val input = serial.inputStream
            with(input.read()) {
                print(toChar())
            }
        }
    }


}

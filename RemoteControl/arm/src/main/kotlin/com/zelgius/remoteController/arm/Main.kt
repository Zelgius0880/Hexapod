package com.zelgius.remoteController.arm

import com.zelgius.remoteController.arm.arm.Arm
import com.zelgius.remoteController.arm.controls.CONTROLS
import com.zelgius.remoteController.arm.controls.Control
import com.zelgius.remoteController.arm.controls.RemoteCommand
import kotlinx.coroutines.runBlocking
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Socket
import java.nio.ByteBuffer


fun main() {

    val socket: Socket by lazy {
        Socket("127.0.0.1", 5000)
    }

    runBlocking {
        val arm = Arm.newInstance()
        arm.test()
        return@runBlocking

        RemoteCommand(socket).apply {
            sendLed(led1 = true, led2 = true, led3 = true, led4 = true)
        }

        fun switchLed() {
            when (arm.currentIndex) {
                0 -> RemoteCommand(socket).apply {
                    sendLed(led1 = false, led2 = true, led3 = true, led4 = true)
                    rumble(50)
                }

                1 -> RemoteCommand(socket).apply {
                    sendLed(led1 = true, led2 = false, led3 = true, led4 = true)
                    rumble(50)
                }

                2 -> RemoteCommand(socket).apply {
                    sendLed(led1 = true, led2 = true, led3 = false, led4 = true)
                    rumble(50)
                }

                3 -> RemoteCommand(socket).apply {
                    sendLed(led1 = true, led2 = true, led3 = true, led4 = false)
                    rumble(50)
                }
            }
        }

        InputListener {
            if (it.type != CONTROLS.STICK)
                arm.stopMoving()

            when (it.type) {
                CONTROLS.CROSS_LEFT -> {
                    if (arm.enabled && it.isPressed) {
                        arm.previous()
                        switchLed()
                    }
                }
                CONTROLS.CROSS_RIGHT -> {
                    if (arm.enabled && it.isPressed) {
                        arm.next()
                        switchLed()
                    }
                }
                CONTROLS.CROSS_UP -> {
                }
                CONTROLS.CROSS_DOWN -> {
                }
                CONTROLS.BUTTON_A -> {
                }
                CONTROLS.BUTTON_B -> {
                }
                CONTROLS.BUTTON_X -> {
                }
                CONTROLS.BUTTON_Y -> {
                }
                CONTROLS.BUTTON_MINUS -> {
                    if (it.isPressed) {
                        arm.enabled = !arm.enabled
                        if (arm.enabled) {
                            RemoteCommand(socket).apply {
                                sendLed(led1 = false, led2 = true, led3 = true, led4 = true)
                                rumble()
                            }
                        } else {
                            RemoteCommand(socket).apply {
                                sendLed(led1 = true, led2 = true, led3 = true, led4 = true)
                                rumble()
                            }
                        }
                    }
                }
                CONTROLS.BUTTON_PLUS -> {
                }
                CONTROLS.BUTTON_HOME -> {
                }
                CONTROLS.BUTTON_L -> {
                    if (it.isPressed) {
                        arm.startMoving(Arm.Direction.BACKWARD) {
                            RemoteCommand(socket).apply {
                                rumble(300)
                            }
                        }
                    }
                }
                CONTROLS.BUTTON_R -> {
                    if (it.isPressed) {
                        arm.startMoving(Arm.Direction.FORWARD){
                            RemoteCommand(socket).apply {
                                rumble(300)
                            }
                        }
                    }
                }
                CONTROLS.BUTTON_ZL -> {
                }
                CONTROLS.BUTTON_ZR -> {
                }
                CONTROLS.BUTTON_STICK_L -> {
                }
                CONTROLS.BUTTON_STICK_R -> {
                }
                CONTROLS.STICK -> {
                }
            }
        }.start()

    }
}

class InputListener(val inputReceived: (Control) -> Unit) : Thread() {
    private val ds = DatagramSocket(5005)
    private val buf = ByteArray(128)

    var stop = false

    override fun run() {
        while (!stop) {
            val dp = DatagramPacket(buf, 128)
            ds.receive(dp)

            val result = dp.data.sliceArray(0 until dp.length)
            println(result.joinToString())

            inputReceived(Control(ByteBuffer.wrap(result)))
            // transmitUart(serial, result)
        }
    }
}

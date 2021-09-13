package com.zelgius.remoteController

import com.pi4j.Pi4J
import com.pi4j.library.pigpio.PiGpio
import com.pi4j.plugin.pigpio.provider.i2c.PiGpioI2CProvider
import com.zelgius.remoteController.arm.Arm
import com.zelgius.remoteController.controls.CONTROLS
import com.zelgius.remoteController.controls.RemoteCommand
import com.zelgius.remoteController.hexapod.*
import com.zelgius.remoteController.pca9685.ServoDriver
import kotlinx.coroutines.runBlocking
import java.net.Socket

val socket: Socket by lazy {
    Socket("127.0.0.1", 5000)
}

val datagramManager = DatagramManager {}
private val context by lazy { Pi4J.newAutoContext() }
fun main() {
    datagramManager.start()

    val provider = I2CProviderWrapper.createProvider()

    runBlocking {
        val driver1 = ServoDriver.newInstance(context, provider, address = 0x041)
        val driver2 = ServoDriver.newInstance(context, provider, address = 0x040)

        val arm = Arm.newInstance(driver2)
        arm.enabled = true

        updateIndicators(arm)
        val hexapod = HexapodController(driver1, driver2)

        datagramManager.inputReceived = {
            println("command")
            if (it.type != CONTROLS.STICK)
                arm.stopMoving()

            when (it.type) {
                CONTROLS.CROSS_LEFT,
                CONTROLS.CROSS_RIGHT,
                CONTROLS.CROSS_UP,
                CONTROLS.CROSS_DOWN -> {/* PITCH */
                }
                CONTROLS.BUTTON_A -> if (it.isPressed) hexapod.gait = TetrapodGait()
                CONTROLS.BUTTON_B -> if (it.isPressed) hexapod.gait = TripodGait()
                CONTROLS.BUTTON_X -> if (it.isPressed) hexapod.gait = RippleGait()
                CONTROLS.BUTTON_Y -> if (it.isPressed) hexapod.gait = WaveGait()
                CONTROLS.BUTTON_MINUS -> if (it.isPressed) hexapod.speedDown()
                CONTROLS.BUTTON_PLUS -> if (it.isPressed) hexapod.speedUp()
                CONTROLS.BUTTON_STICK_L -> {
                }
                CONTROLS.BUTTON_STICK_R -> {
                }
                CONTROLS.STICK -> {
                    //hexapod.move(it)
                }
                CONTROLS.BUTTON_HOME -> {
                    if (it.isPressed) {
                        arm.initPosition()

                        hexapod.reset()
                        RemoteCommand(socket).apply {
                            rumble(300)
                        }
                    }
                }
                CONTROLS.BUTTON_ZL -> {
                    if (it.isPressed) {
                        arm.startMoving(Arm.Direction.BACKWARD) {
                            RemoteCommand(socket).apply {
                                rumble(300)
                            }
                        }
                    }
                }
                CONTROLS.BUTTON_ZR -> {
                    if (it.isPressed) {
                        arm.startMoving(Arm.Direction.FORWARD) {
                            RemoteCommand(socket).apply {
                                rumble(300)
                            }
                        }
                    }
                }
                CONTROLS.BUTTON_L -> {
                    if (arm.enabled && it.isPressed) {
                        arm.previous()
                    }
                }
                CONTROLS.BUTTON_R -> {
                    if (arm.enabled && it.isPressed) {
                        arm.next()
                    }
                }

            }

            if (it.type != CONTROLS.STICK)
                updateIndicators(arm)
        }

    }
}


fun updateIndicators(arm: Arm) {
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

    // TODO Broadcast the different indicators
}
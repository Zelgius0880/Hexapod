package com.zelgius.remoteController

import com.pi4j.Pi4J
import com.pi4j.exception.Pi4JException
import com.zelgius.drivers.ADS1115Driver
import com.zelgius.drivers.servo.ServoDriver
import com.zelgius.remoteController.arm.Arm
import com.zelgius.remoteController.controls.CONTROLS
import com.zelgius.remoteController.controls.RemoteCommand
import com.zelgius.remoteController.hexapod.*
import com.zelgius.remoteController.ui.DebugScreen
import kotlinx.coroutines.*
import java.io.IOException
import java.net.Socket
import kotlin.concurrent.timerTask

var _socket: Socket? = null
    get() = if (field == null)
        Socket("127.0.0.1", 5000)
    else field

val socket: Socket get() = _socket!!

val datagramManager = DatagramManager {}
private val context by lazy { Pi4J.newAutoContext() }

val RUMBLE_CONTROLS = arrayOf(
    CONTROLS.BUTTON_A,
    CONTROLS.BUTTON_B,
    CONTROLS.BUTTON_X,
    CONTROLS.BUTTON_Y,
    CONTROLS.BUTTON_MINUS,
    CONTROLS.BUTTON_PLUS,
    CONTROLS.BUTTON_L,
    CONTROLS.BUTTON_R,
    CONTROLS.BUTTON_HOME,
)

fun main(args: Array<String>) = runBlocking {
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "INFO")

    datagramManager.start()

    val provider = I2CProviderWrapper.createProvider()

    val driver1 = ServoDriver.newInstance(context, provider, address = 0x041)
    val driver2 = ServoDriver.newInstance(context, provider, address = 0x040)
    val analogDriver = ADS1115Driver(context, provider.create(1, ADS1115Driver.DEFAULT_ADDRESS))
    val arm = Arm.newInstance(driver1)
    arm.enabled = true

    updateIndicators(arm)
    val screen = DebugScreen
    val hexapod = HexapodController(driver1, driver2, screen).apply {
        reset()
        run()
    }

    //hexapod.test()

    var homePressedTime = 0L
    var timerJob: Job? = null
    datagramManager.inputReceived = {
        if (datagramManager.debug) println("command")

        if (it.type != CONTROLS.STICK)
            arm.stopMoving()

        when (it.type) {
            CONTROLS.CROSS_LEFT,
            CONTROLS.CROSS_RIGHT,
            CONTROLS.CROSS_UP,
            CONTROLS.CROSS_DOWN -> {
                if (hexapod.moveAxes(it))
                    RemoteCommand(socket).apply {
                        rumble(300)
                    }
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
                hexapod.move(it)
            }
            CONTROLS.BUTTON_HOME -> {
                if (it.isPressed) {
                    homePressedTime = System.currentTimeMillis()
                    timerJob = async {
                        delay(1000)
                        hexapod.testMode = true
                        RemoteCommand(socket).apply {
                            rumble(800)
                        }
                    }
                } else {
                    if (System.currentTimeMillis() - homePressedTime < 1000L)
                        hexapod.testMode = false

                    if (timerJob?.isActive == true)
                        timerJob?.cancel()

                    arm.initPosition()

                    hexapod.reset()

                    if (!hexapod.testMode) {
                        RemoteCommand(socket).apply {
                            rumble(300)
                        }
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

        if (it.type in RUMBLE_CONTROLS)
            updateIndicators(arm)
    }

    while (true) {
        val calibrationValue = 1.11
        try {
            DebugScreen.voltage =
                analogDriver.readToVoltage(ADS1115Driver.Pin.A0) * 2 + calibrationValue// * 0.0001875 * 2
        } catch (e: Pi4JException) {
            println(e.message)
        }

        screen.render()
        delay(1000)

    }
}


fun updateIndicators(arm: Arm) {
    try {
        when (arm.currentIndex) {
            0 -> RemoteCommand(socket).apply {
                sendLed(led1 = true)
            }

            1 -> RemoteCommand(socket).apply {
                sendLed(led2 = true)
            }

            2 -> RemoteCommand(socket).apply {
                sendLed(led3 = true)
            }

            3 -> RemoteCommand(socket).apply {
                sendLed(led4 = true)
            }
            else -> return
        }.also { it.rumble(100) }
    } catch (e: Exception) {
        e.printStackTrace()
        try {
            socket.close()
        } catch (e: Exception) {
        }
        _socket = null
    }

    // TODO Broadcast the different indicators
}

package com.zelgius.remoteController

import com.pi4j.Pi4J
import com.pi4j.exception.Pi4JException
import com.pi4j.io.pwm.PwmType
import com.zelgius.drivers.ADS1115Driver
import com.zelgius.drivers.Led
import com.zelgius.drivers.servo.ServoDriver
import com.zelgius.remoteController.arm.Arm
import com.zelgius.remoteController.controls.CONTROLS
import com.zelgius.remoteController.controls.CommandManager
import com.zelgius.remoteController.controls.Control
import com.zelgius.remoteController.controls.RemoteCommand
import com.zelgius.remoteController.hexapod.*
import com.zelgius.remoteController.ui.DebugScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.net.Socket

var _socket: Socket? = null
    get() {
        if (field == null) {
            field = Socket("127.0.0.1", 5000)
        }
        return field
    }

val socket: Socket get() = _socket!!

val datagramManager = DatagramManager(true) {}
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
    val led = Led.Builder(context)
        .red(23)
        .green(27)
        .blue(22)
        .build()

    led.off()

    try {
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

        val commandManager = CommandManager(hexapod, arm, led, analogDriver)

        datagramManager.inputReceived = {
            if (datagramManager.debug) println("command")

            commandManager.handleControl(it)
        }

        while (true) {
            screen.render()
            delay(1000)

        }
    } catch (e: Exception) {
        // Initialization fail
        led.pulse(Led.red, milli = 10L)

        while (true) {
            //blobking to keep the pulse
            delay(100)
        }
    }
}



fun Double.toBatteryLevel() = when {
    this > 8.3 -> 100
    this > 8.22 -> 95
    this > 8.16 -> 90
    this > 8.05 -> 85
    this > 7.97 -> 80
    this > 7.91 -> 75
    this > 7.83 -> 70
    this > 7.75 -> 65
    this > 7.71 -> 60
    this > 7.67 -> 55
    this > 7.63 -> 50
    this > 7.59 -> 45
    this > 7.57 -> 40
    this > 7.53 -> 35
    this > 7.49 -> 30
    this > 7.45 -> 25
    this > 7.41 -> 20
    this > 7.37 -> 15
    this > 7.22 -> 10
    this > 6.55 -> 5
    else -> 0
} / 100.0



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

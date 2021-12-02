package com.zelgius.remoteController

import com.pi4j.Pi4J
import com.pi4j.exception.Pi4JException
import com.pi4j.io.pwm.PwmType
import com.zelgius.drivers.ADS1115Driver
import com.zelgius.drivers.Led
import com.zelgius.drivers.servo.ServoDriver
import com.zelgius.remoteController.arm.Arm
import com.zelgius.remoteController.controls.CONTROLS
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
    val led = Led.Builder(context)
        .red(23)
        .green(27)
        .blue(22)
        .build()

    led.off()

    try {
        var isHome = true
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
        updateBatteryLevel(led, analogDriver)

        var homePressedTime = 0L
        var timerJob: Job? = null
        datagramManager.inputReceived = {
            if (datagramManager.debug) println("command")

            if (it.type != CONTROLS.STICK)
                arm.stopMoving()

            val oldIsHome = isHome
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
                CONTROLS.BUTTON_A -> if (it.isPressed) {
                    isHome = false
                    hexapod.gait = TetrapodGait()
                }
                CONTROLS.BUTTON_B -> if (it.isPressed) {
                    isHome = false
                    hexapod.gait = TripodGait()
                }
                CONTROLS.BUTTON_X -> if (it.isPressed) {
                    isHome = false
                    hexapod.gait = RippleGait()
                }
                CONTROLS.BUTTON_Y -> if (it.isPressed) {
                    isHome = false
                    hexapod.gait = WaveGait()
                }
                CONTROLS.BUTTON_MINUS -> if (it.isPressed) hexapod.speedDown()
                CONTROLS.BUTTON_PLUS -> if (it.isPressed) hexapod.speedUp()
                CONTROLS.BUTTON_STICK_L -> {
                    if (it.isPressed)
                        if (led.isOn) led.off(transition = true, milli = 1L)
                        else led.on(transition = true, milli = 1L)
                }
                CONTROLS.BUTTON_STICK_R -> {
                }
                CONTROLS.STICK -> {
                    hexapod.move(it)
                }
                CONTROLS.BUTTON_HOME -> {
                    if (it.isPressed) {
                        isHome = true
                        updateBatteryLevel(led, analogDriver)
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

            if (isHome != oldIsHome)
                if (!isHome) {
                    led.off(true)
                }
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

private fun updateBatteryLevel(led: Led, analogDriver: ADS1115Driver) {
    runBlocking {
        val calibrationValue = 1.11
        try {
            val voltage = analogDriver.readToVoltage(ADS1115Driver.Pin.A0) * 2 + calibrationValue// * 0.0001875 * 2
            DebugScreen.voltage = voltage

            val percent = voltage.toBatteryLevel()
            val red = (2 * 255 * percent).coerceAtMost(255.0).toInt()
            val green = (2 * 255 * (1 - percent)).coerceAtMost(255.0).toInt()
            val blue = 0
            led.setColor(
                0x000000 or (red shl 24)
                        or (green shl 16)
                        or blue,
                transition = true
            )
        } catch (e: Pi4JException) {
            println(e.message)
        }
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

package com.zelgius.remoteController.controls

import com.pi4j.exception.Pi4JException
import com.zelgius.drivers.ADS1115Driver
import com.zelgius.drivers.Led
import com.zelgius.remoteController.*
import com.zelgius.remoteController.arm.Arm
import com.zelgius.remoteController.hexapod.*
import com.zelgius.remoteController.ui.DebugScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class CommandManager(
    private val hexapod: HexapodController,
    private val arm: Arm,
    private val led: Led,
    private val analogDriver: ADS1115Driver
) {

    private var homePressedTime = 0L
    private var timerJob: Job? = null
    private var isHome = true

    init {
        updateBatteryLevel()
    }

    fun handleControl(control: Control) = runBlocking {
        if (control.type != CONTROLS.STICK)
            arm.stopMoving()

        val oldIsHome = isHome
        when (control.type) {
            CONTROLS.CROSS_LEFT,
            CONTROLS.CROSS_RIGHT,
            CONTROLS.CROSS_UP,
            CONTROLS.CROSS_DOWN -> {
                if (hexapod.moveAxes(control))
                    RemoteCommand(socket).apply {
                        rumble(300)
                    }
            }
            CONTROLS.BUTTON_A -> if (control.isPressed) {
                isHome = false
                hexapod.gait = TetrapodGait()
            }
            CONTROLS.BUTTON_B -> if (control.isPressed) {
                isHome = false
                hexapod.gait = TripodGait()
            }
            CONTROLS.BUTTON_X -> if (control.isPressed) {
                isHome = false
                hexapod.gait = RippleGait()
            }
            CONTROLS.BUTTON_Y -> if (control.isPressed) {
                isHome = false
                hexapod.gait = WaveGait()
            }
            CONTROLS.BUTTON_MINUS -> if (control.isPressed) hexapod.speedDown()
            CONTROLS.BUTTON_PLUS -> if (control.isPressed) hexapod.speedUp()
            CONTROLS.BUTTON_STICK_L -> {
                if (control.isPressed)
                    if (led.isOn) led.off(transition = true, milli = 1L)
                    else led.on(transition = true, milli = 1L)
            }
            CONTROLS.BUTTON_STICK_R -> {
            }
            CONTROLS.STICK -> {
                hexapod.move(control)
            }
            CONTROLS.BUTTON_HOME -> {
                if (control.isPressed) {
                    isHome = true
                    updateBatteryLevel()
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
                if (control.isPressed) {
                    arm.startMoving(Arm.Direction.BACKWARD) {
                        RemoteCommand(socket).apply {
                            rumble(300)
                        }
                    }
                }
            }
            CONTROLS.BUTTON_ZR -> {
                if (control.isPressed) {
                    arm.startMoving(Arm.Direction.FORWARD) {
                        RemoteCommand(socket).apply {
                            rumble(300)
                        }
                    }
                }
            }
            CONTROLS.BUTTON_L -> {
                if (arm.enabled && control.isPressed) {
                    arm.previous()
                }
            }
            CONTROLS.BUTTON_R -> {
                if (arm.enabled && control.isPressed) {
                    arm.next()
                }
            }
        }

        if (control.type in RUMBLE_CONTROLS)
            updateIndicators(arm)

        if (isHome != oldIsHome)
            if (!isHome) {
                led.off(true)
            }
    }

    private fun updateBatteryLevel() {
        runBlocking {
            val calibrationValue = 1.11
            try {
                val voltage = analogDriver.readToVoltage(ADS1115Driver.Pin.A0) * 2 + calibrationValue// * 0.0001875 * 2
                DebugScreen.voltage = voltage
                /*
                val red = (2 * 255 * percent).coerceAtMost(255.0).toInt()
                val green = (2 * 255 * (1 - percent)).coerceAtMost(255.0).toInt()
                val blue = 0
                led.setColor(
                    0x000000 or (red shl 24)
                            or (green shl 16)
                            or blue,
                    transition = true
                )*/

                val orange = 0xFFA00
                val green = 0x0000FF // Colorblind friendly
                val red = 0xFF0000

                led.setColor(
                    when {
                        voltage > 7.5 -> green
                        voltage > 7.0 -> orange
                        else -> red
                    }, true
                )
            } catch (e: Pi4JException) {
                println(e.message)
            }
        }
    }

}
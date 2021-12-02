package com.zelgius.drivers

import com.pi4j.context.Context
import com.pi4j.io.pwm.Pwm
import com.pi4j.io.pwm.PwmConfigBuilder
import com.pi4j.io.pwm.PwmType
import kotlinx.coroutines.*


class Led(private val red: Pwm?, private val green: Pwm?, private val blue: Pwm?) {

    private var currentJob: Job? = null

    companion object {
        val blue = Color.Blue
        val red = Color.Red
        val green = Color.Green
    }

    val isOn: Boolean
        get() = red != null && red.dutyCycle > 0
                || green != null && green.dutyCycle > 0
                || blue != null && blue.dutyCycle > 0

    fun on(dutyCycle: Int = 100, transition: Boolean = false, milli: Long = 5L) {
        if (!transition) {
            red?.dutyCycle(100)?.on()
            green?.dutyCycle(100)?.on()
            blue?.dutyCycle(100)?.on()
        } else {
            transition(listOfNotNull(red, green, blue), dutyCycle, milli)
        }
    }

    fun setColor(color: Int, transition: Boolean = false, milli: Long = 5L) {
        val redDuty = (color.red / 255f * 100).toInt()
        val greenDuty = (color.green / 255f * 100).toInt()
        val blueDuty = (color.blue / 255f * 100).toInt()
        if (!transition) {
            red?.on(redDuty)
            green?.on(greenDuty)
            blue?.on(blueDuty)
        } else {
            if (currentJob?.isActive == true) currentJob?.cancel()
            currentJob = CoroutineScope(Dispatchers.IO).launch {
                val r = red?.let {
                    transitionAsync(listOf(it), redDuty, milli)
                }

                val g = green?.let {
                    transitionAsync(listOf(it), greenDuty, milli)
                }

                val b = red?.let {
                    transitionAsync(listOf(it), blueDuty, milli)
                }

                r?.await()
                g?.await()
                b?.await()
            }
        }
    }

    private fun transition(
        colors: List<Pwm>,
        dutyCycle: Int,
        milli: Long
    ) {
        if (currentJob?.isActive == true) currentJob?.cancel()
        currentJob =
            CoroutineScope(Dispatchers.IO).launch {
                transitionAsync(colors, dutyCycle, milli).await()
            }
    }

    private fun CoroutineScope.transitionAsync(
        colors: List<Pwm>,
        dutyCycle: Int,
        milli: Long
    ) = async {
        while (colors.find { it.dutyCycle.toInt() != dutyCycle } != null) {
            colors.forEach {
                if (it.dutyCycle.toInt() != dutyCycle)
                    it.dutyCycle(
                        if (it.dutyCycle < dutyCycle) it.dutyCycle + 1
                        else it.dutyCycle - 1
                    ).on()
            }
            delay(milli)
        }
    }

    fun pulse(vararg leds: Color, milli: Long = 1L) {
        val colors = leds.mapNotNull {
            when (it) {
                Color.Red -> red
                Color.Green -> green
                Color.Blue -> blue
            }
        }
        currentJob =
            CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    transitionAsync(colors, 100, milli).await()
                    transitionAsync(colors, 0, milli).await()
                }
            }
    }


    fun off(transition: Boolean = false, milli: Long = 5L) {
        if (!transition) {
            red?.dutyCycle(0)?.on()
            green?.dutyCycle(0)?.on()
            blue?.dutyCycle(0)?.on()
        } else {
            transition(listOfNotNull(red, green, blue), 0, milli)
        }
    }

    fun on(vararg leds: Color, dutyCycle: Int = 100, transition: Boolean = false, milli: Long = 5L) {
        if (!transition) {
            leds.forEach { led ->
                when (led) {
                    Color.Red -> red?.dutyCycle(dutyCycle)?.on()
                    Color.Green -> green?.dutyCycle(dutyCycle)?.on()
                    Color.Blue -> blue?.dutyCycle(dutyCycle)?.on()
                }
            }
        } else {
            transition(leds.mapNotNull {
                when (it) {
                    Color.Red -> red
                    Color.Green -> green
                    Color.Blue -> blue
                }
            }, 0, milli)
        }
    }

    enum class Color {
        Red, Green, Blue
    }

    private val Int.red get() = 0xFF and (this shr 16)
    private val Int.green get() = 0xFF and (this shr 8)
    private val Int.blue get() = 0xFF and this

    private class LedConfig(val address: Int, val initialDutyCycle: Int = 0, val type: PwmType = PwmType.SOFTWARE)
    class Builder(private val pi4j: Context) {
        private var red: LedConfig? = null
        private var green: LedConfig? = null
        private var blue: LedConfig? = null

        fun red(address: Int, initialDutyCycle: Int = 0, type: PwmType = PwmType.SOFTWARE): Builder {
            red = LedConfig(address, initialDutyCycle, type)
            return this
        }

        fun blue(address: Int, initialDutyCycle: Int = 0, type: PwmType = PwmType.SOFTWARE): Builder {
            blue = LedConfig(address, initialDutyCycle, type)
            return this
        }

        fun green(address: Int, initialDutyCycle: Int = 0, type: PwmType = PwmType.SOFTWARE): Builder {
            green = LedConfig(address, initialDutyCycle, type)
            return this
        }

        fun build(): Led {
            val pwmBuilder = Pwm.newConfigBuilder(pi4j)
            pwmBuilder
                .provider("pigpio-pwm")
                .shutdown(0)

            return Led(
                buildPwm(pwmBuilder, red),
                buildPwm(pwmBuilder, green),
                buildPwm(pwmBuilder, blue),
            )
        }

        private fun buildPwm(pwmBuilder: PwmConfigBuilder, config: LedConfig?) =
            config?.let {
                pi4j.create(
                    pwmBuilder.initial(config.initialDutyCycle)
                        .pwmType(it.type)
                        .id("BCM${it.address}")
                        .address(it.address)
                        /*.apply {
                            if (config.type == PwmType.SOFTWARE)
                                frequency(100)
                        }*/
                        .build()
                )
            }

    }
}
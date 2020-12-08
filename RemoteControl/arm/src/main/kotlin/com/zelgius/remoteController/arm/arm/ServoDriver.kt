package com.zelgius.remoteController.arm.arm

import com.pi4j.io.i2c.I2CBus
import com.pi4j.io.i2c.I2CFactory
import kotlin.math.roundToInt

class ServoDriver private constructor(
    i2CBus: I2CBus,
    address: Int,
    private val clockSpeed: Long,
) {
    val pca = PCA9685Driver(i2CBus, address, clockSpeed)

    suspend fun frequency(frequency: Long) {
        pca.frequency(frequency)
    }

    companion object {
        suspend fun newInstance(
            i2CBus: I2CBus? = null,
            address: Int = 0x40,
            clockSpeed: Long = 25000000,
            frequency: Long = 50
        ): ServoDriver = (i2CBus ?: I2CFactory.getInstance(I2CBus.BUS_1)).let {
            ServoDriver(it, address, clockSpeed).apply {
                frequency(frequency)
            }
        }
    }

    val servos = ServoList()

    inner class ServoList(servos: List<Servo> = listOf()) : List<Servo> by servos {
        private val channels = mutableMapOf<Int, Servo>()

        override operator fun get(index: Int): Servo =
            channels.getOrPut(index) {
                Servo(
                    pca.channels[index], actuationRange = 180, minPulse = 450,
                    maxPulse = 2450,
                )
            }
    }
}

abstract class BaseServo(
    val pwmChannel: PWMChannel,
    val minPulse: Long = 750,
    val maxPulse: Long = 2250,
    val maxDuty: Double = maxPulse.toDouble() * pwmChannel.frequency / 1000000 * 0xFFFF,
    val minDuty: Double = minPulse.toDouble() * pwmChannel.frequency / 1000000 * 0xFFFF,
    val dutyRange: Double = maxDuty - minDuty
) {
    var fraction: Double?
        get() =
            if (pwmChannel.dutyCycle == 0.toShort()) null
            else ((pwmChannel.dutyCycle - minDuty) / dutyRange)
        set(value) {
            if (value == null) pwmChannel.dutyCycle = 0
            else if (value < 0.0 || value > 1.0) error("Out of range")
            else {
                pwmChannel.dutyCycle = (minDuty + value * dutyRange).toInt().toShort()
            }
        }
}

class Servo(
    pwmChannel: PWMChannel,
    val actuationRange: Int = 180,
    minPulse: Long = 550,
    maxPulse: Long = 2250,
) : BaseServo(pwmChannel, minPulse, maxPulse) {
    var currentAngle: Int = angle?: 0
        private set
    var angle: Int?
        get() = fraction?.let {
            (actuationRange * it).roundToInt()
        }
        set(value) {
            currentAngle = value?: 0
            fraction = if (value == null) null
            else {
                if (value < 0 || value > actuationRange) error("Angle out of range: $value")
                value / actuationRange.toDouble()
            }
        }
}



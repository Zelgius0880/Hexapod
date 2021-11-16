package com.zelgius.drivers.servo

import com.pi4j.context.Context
import com.pi4j.io.i2c.I2C
import com.pi4j.io.i2c.I2CProvider
import kotlin.math.roundToInt

open class ServoDriver protected constructor(
    val pca: PCA9685Driver,
) {

    protected constructor(
        context: Context,
        i2c: I2C,
        clockSpeed: Long
    ) : this(PCA9685Driver(context, i2c, clockSpeed))

    suspend fun frequency(frequency: Long) {
        pca.frequency(frequency)
    }

    companion object {
        suspend fun newInstance(
            context: Context,
            i2CProvider: I2CProvider,
            address: Int = 0x40,
            clockSpeed: Long = 25000000,
            frequency: Long = 50
        ): ServoDriver = (i2CProvider.create<I2C>(1, address)).let {
            ServoDriver(context, it, clockSpeed).apply {
                frequency(frequency)
            }
        }
    }

    val servos = ServoList()

    open inner class ServoList(servos: List<Servo> = listOf()) : List<Servo> by servos {
        private val channels = mutableMapOf<Int, Servo>()

        override operator fun get(index: Int): Servo =
            channels.getOrPut(index) {
                Servo(
                    pca.channels[index], actuationRange = 180, minPulse = 450,
                    maxPulse = 2450,
                )
            }

        fun get(index: Int, config: ServoConfig): Servo = Servo(
            pca.channels[index],
            config.actuationRange,
            config.minPulse,
            config.maxPulse
        ).also {
            channels[index] = it
        }
    }
}

abstract class BaseServo(
    private val pwmChannel: PWMChannel,
    private val minPulse: Long = 750,
    private val maxPulse: Long = 2250,
    private val maxDuty: Double = maxPulse.toDouble() * pwmChannel.frequency / 1000000 * 0xFFFF,
    private val minDuty: Double = minPulse.toDouble() * pwmChannel.frequency / 1000000 * 0xFFFF,
    private val dutyRange: Double = maxDuty - minDuty
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
    var currentAngle: Int = angle ?: 0
        private set
    var angle: Int?
        get() = fraction?.let {
            (actuationRange * it).roundToInt()
        }
        set(value) {
            currentAngle = value ?: 0
            fraction = if (value == null) null
            else {
                if (value < 0 || value > actuationRange) error("Angle out of range: $value")
                value / actuationRange.toDouble()
            }
        }

    var radian: Double?
        get() = angle?.toDouble()?.toRadian()
        set(value) {
            angle = value?.toDegree()?.roundToInt()
        }

    private fun Double.toDegree() = this * 180 / Math.PI
    private fun Double.toRadian() = this / 180 * Math.PI
}

enum class ServoConfig(val actuationRange: Int, val minPulse: Long, val maxPulse: Long) {
    MG90S(actuationRange = 180, minPulse = 450, maxPulse = 2450),
    MG90Sv2(actuationRange = 200, minPulse = 450, maxPulse = 2450),
    MG90D(actuationRange = 180, minPulse = 700, maxPulse = 2100)
    //MG90D(actuationRange = 180, minPulse = 690, maxPulse = 2200)
}



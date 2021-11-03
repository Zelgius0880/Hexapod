package com.zelgius.drivers

import com.pi4j.context.Context
import com.pi4j.io.i2c.I2C
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ADS1115Driver(
    private val context: Context,
    private val device: I2C = context.provider(DEFAULT_I2C_PROVIDER),
    private val address: String = "$DEFAULT_ADDRESS",
    private val mode: Mode = Mode.CONTINUOUS,
    private val dataRate: String = DATA_RATE
) {
    private var lastReadPin: Pin? = null
    private var gain = "1"

    companion object {
        const val DEFAULT_ADDRESS = 0x48
        const val POINTER_CONVERSION = 0x00
        const val POINTER_CONFIG = 0x01
        const val CONFIG_OS_SINGLE = 0x8000
        const val CONFIG_MUX_OFFSET = 12
        const val CONFIG_COMP_QUE_DISABLE = 0x0003
        const val DATA_RATE = "128"

        val CONFIG_GAIN = mapOf(
            "2/3" to 0x0000,
            "1" to 0x0200,
            "2" to 0x0400,
            "4" to 0x0600,
            "8" to 0x0800,
            "16" to 0x0A00,
        )

        val CONFIG_DR = mapOf(
            "8" to 0x0000,
            "16" to 0x0020,
            "32" to 0x0040,
            "64" to 0x0060,
            "128" to 0x0080,
            "250" to 0x00A0,
            "475" to 0x00C0,
            "860" to 0x00E0
        )
    }

    private fun toVoltage(value: Short) = value / 32767.0 * when(gain) {
        "2/3" -> {6.144}
        "1" -> {4.096}
        "2" -> {2.048}
        "4" -> {1.024}
        "8" -> {0.512}
        "16" -> {0.256}
        else -> {0.0}
    }

    suspend fun readToVoltage(pin: Pin) = toVoltage(read(pin))

    val lastResult: Short
        get() = readRegister(Register.POINTER_CONVERSION)

    val fastLastValue: Short
        get() = readRegister(Register.POINTER_CONVERSION, true)

    val isConversionCompted: Boolean
        get() = readRegister(Register.POINTER_CONFIG).toUInt() and 0x800.toUInt() > 1.toUInt()

    // TODO: is it really useful?
    private fun conversionValue(raw: Short) =
        with(ByteBuffer.allocate(2)) {
            order(ByteOrder.BIG_ENDIAN)
            putShort(raw)
            position(0)
            short
        }


    suspend fun read(pin: Pin): Short = withContext(Dispatchers.IO) {
        if (mode == Mode.CONTINUOUS && lastReadPin == pin) {
            return@withContext conversionValue(fastLastValue)
        }

        lastReadPin = pin
        val config: Int = (if (mode == Mode.SINGLE) CONFIG_OS_SINGLE else 0).let {
            it or
                    ((pin.number.toInt() and 0x07) shl CONFIG_MUX_OFFSET) or
                    CONFIG_GAIN[gain]!! or
                    mode.value or
                    CONFIG_DR[dataRate]!! or
                    CONFIG_COMP_QUE_DISABLE
        }
        writeRegister(Register.POINTER_CONFIG, config.toShort())

        if (mode == Mode.SINGLE)
            while (!isConversionCompted)
                delay(10)
        else
            delay((2000 / dataRate.toInt()).toLong())

        conversionValue(lastResult)
    }

    private fun writeRegister(register: Register, value: Short) {
        with(ByteBuffer.allocate(3)) {
            put(register.address)
            putShort(value)
            device.write(this)
        }
    }

    private fun readRegister(register: Register, fast: Boolean = false) = with(ByteBuffer.allocate(3)) {
        if (fast) {
            device.read(this, 2)
            position(0)
            short
        } else {
           // device.write(register.address)
            device.readRegister(register.address.toInt(), this)
            position(0)
            short.also {
                println(String.format("%x",it))
            }
        }
    }

    enum class Pin(val number: Number) {
        A0(0), A1(1), A2(2), A3(2)
    }

    enum class Register(val address: Byte) {
        POINTER_CONVERSION(0x00.toByte()),
        POINTER_CONFIG(0x01.toByte()),
        CONFIG_OS_SINGLE(0x8000.toByte()),
        CONFIG_MUX_OFFSET(12.toByte()),
        CONFIG_COMP_QUE_DISABLE(0x0003.toByte()),
    }

    enum class Mode(val value: Int) {
        CONTINUOUS(0x0000),
        SINGLE(0x0100)
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
}


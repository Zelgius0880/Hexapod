package com.zelgius.remoteController.arm.arm

import com.pi4j.io.i2c.I2CBus
import com.pi4j.io.i2c.I2CDevice
import com.pi4j.io.i2c.I2CFactory
import kotlinx.coroutines.delay
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PCA9685Driver(
    i2CBus: I2CBus = I2CFactory.getInstance(I2CBus.BUS_1),
    address: Int = 0x40,
    private val clockSpeed: Long = 25000000
) {
    private val device = i2CBus.getDevice(address)

    companion object {
        const val CHANNELS = 16
    }

    val frequency: Long
        get() = clockSpeed / 4096 / prescale.data().get()

    suspend fun frequency(f: Long) {
        //val prescale = (clockSpeed / (4096.0 * f) - 1).toInt()
        val prescale = (clockSpeed / 4096.0 / f + 0.5).toInt()
        if (prescale < 3) error(("PCA9685 cannot output at the given frequency"))
        val oldMode = mode1.data().get()
        this.prescale.data(prescale.toByte())
        mode1.data(oldMode)

        delay(5)
        mode1.data((oldMode.toInt() or 0xA1).toByte())
    }

    val channels = PCAChannel(this)

    private val mode1 = I2CRegister(device, 0x00, 1)
    private val prescale = I2CRegister(device, 0xFE, 1)
    val pwm = I2CRegister(device, 0x06, 32)

    init {
        reset()
    }

    fun reset() {
        mode1.data(0x00.toByte())
    }
}

class PWMChannel(private val driver: PCA9685Driver, val index: Int) {
    val frequency: Long
        get() = driver.frequency

    var dutyCycle: Short
        get() {
            val pwm = driver.pwm.data().asShortBuffer()
            val s1 = pwm.get(index)
            val s2 = pwm.get(index + 1).toInt()
            if (s1 == 0x1000.toShort())
                return 0xFFFF.toShort()

            return (s2 shl 4).toShort()
        }
        set(value) {
            if (value < 0 || value > 0xFFFF) error("Out of range")
            val buffer = ByteBuffer.allocate(4)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            if (value == 0xFFFF.toShort()) {
                buffer.putShort(0x1000)
                buffer.putShort(0x0000)
            } else {
                buffer.putShort(0x0000)
                //buffer.putShort(307)
                buffer.putShort(((value + 1) shr 4).toShort())
            }

            driver.pwm.data(index * 4, *buffer.array())

        }
}

class PCAChannel(
    private val driver: PCA9685Driver
) {
    private val channels = mutableMapOf<Int, PWMChannel>()

    operator fun get(index: Int): PWMChannel =
        channels.getOrPut(index) {
            PWMChannel(driver = driver, index = index)
        }

}

class I2CRegister(
    private val device: I2CDevice,
    private val register: Int,
    val size: Int,
    private val debug: Boolean = false
) {
    fun data(vararg array: Byte) {
        device.write(register, array)
    }

    fun data(offset: Int, vararg array: Byte) {
        if(debug) {
            print(String.format("Writing: %2X -> ", register + offset))
            println(array.joinToString { String.format("%2X", it) })
        }
        device.write(register + offset, array)
    }

    fun data(): ByteBuffer {
        val array = ByteArray(size)
        device.read(register, array, 0, size)
        if(debug) {
            print(String.format("Reading: %2X -> ", register))
            println(array.joinToString { String.format("%2X", it) })
        }
        return ByteBuffer.wrap(array).apply { order(ByteOrder.LITTLE_ENDIAN) }
    }
}

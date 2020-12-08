package com.zelgius.remoteController.arm

import com.pi4j.io.i2c.I2CBus
import com.pi4j.io.i2c.I2CDevice
import java.nio.ByteBuffer
import java.nio.IntBuffer


class FakeI2CBus : I2CBus {
    class FakeI2CDevice(private val _address: Int) : I2CDevice {
        override fun getAddress(): Int  = _address

        override fun write(b: Byte) {
            println("Writing ${String.format("%2X", b)}")
        }

        override fun write(buffer: ByteArray, offset: Int, size: Int) {
            println("Writing with offset $offset ${buffer.joinToString { String.format("%2X", it)}}")
        }

        override fun write(buffer: ByteArray) {
            println("Writing ${buffer.joinToString { String.format("%2X", it)}}")
        }

        override fun write(address: Int, b: Byte) {
            println("Writing at ${String.format("%2X", address)} -> ${String.format("%2X", b.toInt())}")

        }

        override fun write(address: Int, buffer: ByteArray, offset: Int, size: Int) {
            println("Writing with offset $offset at ${String.format("%2X", address)} ->  ${buffer.joinToString { String.format("%2X", it)}}")
        }

        override fun write(address: Int, buffer: ByteArray) {
            println("Writing at ${String.format("%2X", address)} ->  ${buffer.joinToString { String.format("%2X", it)}}")
        }

        override fun read(): Int {
            println("reading")
            return 1
        }

        override fun read(buffer: ByteArray, offset: Int, size: Int): Int {
            println("reading")
            (ByteArray(size){1}).copyInto(buffer)
            return size
        }

        override fun read(address: Int): Int {
            println("reading a $address")
            return 1
        }

        override fun read(address: Int, buffer: ByteArray, offset: Int, size: Int): Int {
            println("reading a $address")
            (ByteArray(size){1}).copyInto(buffer)
            return size
        }

        override fun read(
            writeBuffer: ByteArray?,
            writeOffset: Int,
            writeSize: Int,
            readBuffer: ByteArray?,
            readOffset: Int,
            readSize: Int
        ): Int {
            return 1
        }

        override fun ioctl(command: Long, value: Int) {

        }

        override fun ioctl(command: Long, data: ByteBuffer?, offsets: IntBuffer?) {

        }

    }
    override fun getDevice(address: Int): I2CDevice {
        return FakeI2CDevice(0x40)
    }

    override fun getBusNumber(): Int {
        return 1000
    }

    override fun close() {
    }

}

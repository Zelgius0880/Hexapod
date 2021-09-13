package com.zelgius.remoteController

import com.pi4j.common.Metadata
import com.pi4j.context.Context
import com.pi4j.io.i2c.I2C
import com.pi4j.io.i2c.I2CConfig
import com.pi4j.io.i2c.I2CProvider
import com.pi4j.io.i2c.I2CRegister
import kotlin.random.Random


class FakeI2CProvider(private val context: Context) : I2CProvider {

    class FakeI2C(
        private val _address: Int,
        private val provider: I2CProvider,
        private val config: I2CConfig
    ) : I2C {


        override fun initialize(context: Context): FakeI2C {
            return this
        }

        override fun shutdown(context: Context): Any? {
            return null
        }

        override fun id(): String = "$_address"

        override fun name(name: String): I2C = this

        override fun name(): String = provider.name

        override fun description(description: String): I2C = this
        override fun description(): String = provider.description

        override fun metadata(): Metadata = provider.metadata

        override fun config(): I2CConfig = config

        override fun provider(): I2CProvider = provider

        override fun write(b: Byte): Int {
            println("Writing ${String.format("%2X", b)}}")
            return 1
        }

        override fun write(data: ByteArray, offset: Int, length: Int): Int {
            println("Writing with offset $offset ->  ${data.joinToString { String.format("%2X", it) }}")
            return length
        }

        override fun read(): Int = Random.nextInt()

        override fun read(buffer: ByteArray, offset: Int, size: Int): Int {
            println("reading")
            (ByteArray(size) { 1 }).copyInto(buffer)
            return size
        }

        override fun readRegister(register: Int): Int {
            println("reading")
            return 0
        }

        override fun readRegister(register: Int, buffer: ByteArray, offset: Int, length: Int): Int {
            println("reading a $register")
            (ByteArray(length) { 1 }).copyInto(buffer)
            return length
        }

        override fun writeRegister(register: Int, b: Byte): Int {
            println("Writing at ${String.format("%2X", register)} -> ${String.format("%2X", b.toInt())}")
            return 0
        }

        override fun writeRegister(register: Int, data: ByteArray, offset: Int, length: Int): Int {
            println(
                "Writing with offset $offset at ${
                    String.format(
                        "%2X",
                        register
                    )
                } ->  ${data.joinToString { String.format("%2X", it) }}"
            )
            return length
        }

        override fun close() {
        }

        override fun isOpen(): Boolean = true

        override fun getRegister(address: Int): I2CRegister = FakeI2CRegister(address)

        class FakeI2CRegister(private val _address: Int) : I2CRegister {
            override fun write(b: Byte): Int {
                println("Writing ${String.format("%2X", b)}}")
                return 1
            }

            override fun write(data: ByteArray, offset: Int, length: Int): Int {
                println("reading a $_address")
                (ByteArray(length) { 1 }).copyInto(data)
                return length
            }

            override fun read(): Int = Random.nextInt()

            override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                println("reading")
                (ByteArray(length) { 1 }).copyInto(buffer)
                return length
            }

            override fun getAddress(): Int = _address

            override fun writeWord(word: Int) {
                write(word)
            }

            override fun readWord(): Int = read()

            override fun writeReadWord(word: Int): Int = word

        }
    }

    override fun id(): String = "1000"

    override fun name(): String = FakeI2C::class.simpleName!!

    override fun description(): String = "FakeI2C bus for testing purpose"

    override fun metadata(): Metadata = Metadata.create()

    override fun initialize(context: Context?): I2CProvider = this

    override fun shutdown(context: Context?): I2CProvider = this

    override fun context(): Context = context

    override fun create(config: I2CConfig): I2C = FakeI2C(1000, this, config)
}

package com.zelgius.remoteController

import com.pi4j.io.i2c.I2CProvider
import com.pi4j.plugin.linuxfs.provider.i2c.LinuxFsI2CProvider

object I2CProviderWrapper {
    //private val provider = PiGpioI2CProvider.newInstance(PiGpio.newSocketInstance())
    private val provider = LinuxFsI2CProvider.newInstance()

    fun createProvider(): I2CProvider = provider
}
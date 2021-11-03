package com.zelgius.remoteController

import com.pi4j.io.i2c.I2CProvider
import com.pi4j.library.pigpio.PiGpio
import com.pi4j.plugin.linuxfs.provider.i2c.LinuxFsI2CProvider
import com.pi4j.plugin.pigpio.provider.i2c.PiGpioI2CProvider
import com.pi4j.plugin.raspberrypi.RaspberryPi
import com.pi4j.plugin.raspberrypi.platform.RaspberryPiPlatform

object I2CProviderWrapper {
    //private val provider = PiGpioI2CProvider.newInstance(PiGpio.newNativeInstance())
    private val provider = LinuxFsI2CProvider.newInstance()

    fun createProvider(): I2CProvider = provider
}
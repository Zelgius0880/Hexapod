package com.zelgius.remoteController.controls

import java.net.DatagramSocket
import java.net.Socket
import java.nio.ByteBuffer

class RemoteCommand (private val socket: Socket){


    fun sendLed(led1: Boolean = false,
                led2: Boolean = false,
                led3: Boolean = false,
                led4: Boolean = false) {
        val buffer = ByteBuffer.allocate(5)
        buffer.put(0)
        buffer.put(led1.toByte())
        buffer.put(led2.toByte())
        buffer.put(led3.toByte())
        buffer.put(led4.toByte())

        socket.getOutputStream().apply {
            write(buffer.array())
            flush()
        }
    }

    fun rumble(millisecond: Long = 200) {
        val buffer = ByteBuffer.allocate(9)
        buffer.put(1)
        buffer.putLong(millisecond)

        socket.getOutputStream().apply {
            write(buffer.array())
            flush()
        }
    }

    private fun Boolean.toByte() = (if (this) 1 else 0).toByte()
}

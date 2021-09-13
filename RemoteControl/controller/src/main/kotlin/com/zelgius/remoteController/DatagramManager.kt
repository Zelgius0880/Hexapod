package com.zelgius.remoteController

import com.zelgius.remoteController.controls.Control
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DatagramManager(private val debug: Boolean = false, var inputReceived: (Control) -> Unit) : Thread() {
    private val ds = DatagramSocket(5005)
    private val buf = ByteArray(128)

    var stop = false

    override fun run() {
        while (!stop) {
            val dp = DatagramPacket(buf, 128)
            ds.receive(dp)

            val result = dp.data.sliceArray(0 until dp.length)
            println(result.joinToString())

            inputReceived(Control(ByteBuffer.wrap(result).apply { order(ByteOrder.LITTLE_ENDIAN) }))
        }
    }

    fun send(bytes: ByteArray) {
        ds.send(DatagramPacket(bytes, bytes.size))
    }
}

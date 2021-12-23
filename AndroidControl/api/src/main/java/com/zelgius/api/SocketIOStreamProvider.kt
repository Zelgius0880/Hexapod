package com.zelgius.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Singleton
import kotlin.concurrent.withLock

@Singleton
class SocketIOStreamProvider(private val ip: String, private val port: Int) : IOStreamProvider {
    private var _socket: Socket? = null
    private val socket: Socket
        get() {
            if (_socket == null) _socket = Socket(ip, port)
            return _socket!!
        }

    private val lock = ReentrantLock()

    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    override suspend fun getInputStream(): InputStream = withContext(Dispatchers.IO) {
        if (inputStream == null) inputStream = socket.getInputStream()
        inputStream!!
    }

    override suspend fun getOutputStream(): OutputStream = withContext(Dispatchers.IO) {
        if (outputStream == null) outputStream = socket.getOutputStream()
        outputStream!!
    }

    override fun close() {
        lock.withLock {
            if (_socket != null) socket.close()
            _socket = null

            inputStream = null
            outputStream = null
        }
    }
}
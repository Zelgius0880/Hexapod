package com.zelgius.remoteController

import com.zelgius.controller.ControlOuterClass
import kotlinx.coroutines.*
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException

class ClientManager {
    private val socket = ServerSocket(5432)
    private var client: Socket? = null

    private var stop = false

    private var listeningJob: Job? = null
    private val jobScope = CoroutineScope(Dispatchers.IO)

    var controlListener: (Control) -> Unit = {}
    fun start() {
        listeningJob = jobScope.launch {
            withContext(Dispatchers.IO) {
                while (true) {
                    try {
                        client = socket.accept()

                        client?.let {
                            it.soTimeout = 1000
                            println("${it.inetAddress.hostAddress}:${it.port}")
                            val inputStream = it.getInputStream()
                            while (client != null) {
                                try {
                                    ControlOuterClass.Control.parseDelimitedFrom(inputStream)?.let { control ->
                                       controlListener(control.toDataClass())
                                    }
                                } catch (_: SocketTimeoutException) {
                                }
                            }
                        }
                    } catch (e: IOException) {
                        println(e.message)
                        client?.close()
                        client = null
                    }
                }
            }
        }
    }

    suspend fun sendStatus(status: Status) = withContext(Dispatchers.IO) {
        try {
            client?.let { socket ->
                synchronized(socket) {
                    socket.getOutputStream()?.let {
                        status.toProtobuf().writeDelimitedTo(it)
                    }
                }
            }
        } catch (e: IOException) {
            println(e.message)
            client?.close()
            client = null
        }
    }

    fun stop() {
        stop = true
        listeningJob?.cancel()
        client?.close()
        socket.close()
    }
}
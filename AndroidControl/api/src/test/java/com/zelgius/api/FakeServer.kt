package com.zelgius.api

import com.zelgius.api.model.Control
import com.zelgius.api.model.toDataClass
import com.zelgius.controller.ControlOuterClass
import com.zelgius.controller.StatusOuterClass.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOError
import java.net.ServerSocket
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FakeServer {
    private val socket = ServerSocket(5432)

    private var stop = false

    private var writingJob: Job? = null
    private var listeningJob: Job? = null
    private val jobScope = CoroutineScope(Dispatchers.IO)
    private val _control = MutableStateFlow<Control?>(null)

    val control: Flow<Control?> = _control
    fun start() {
        jobScope.launch {
            val client = socket.accept()
            println("${client.inetAddress.hostAddress}:${client.port}")

            writingJob = async {
                val outputStream = client.getOutputStream()
                while (!stop) {
                    val status = Status.newBuilder()
                        .setPitch(Random.nextInt(6, 8).toDouble())
                        .setRoll(Random.nextInt(6, 8).toDouble())
                        .setWalkMode(Status.Walk.values().random())
                        .setVoltage(Random.nextInt(6, 8).toDouble())
                        .build()

                    status.writeDelimitedTo(outputStream)
                    delay(1000)
                }
            }

            listeningJob = async {
                try {
                    val inputStream = client.getInputStream()
                    while (true) {
                        ControlOuterClass.Control.parseDelimitedFrom(inputStream)?.let {
                            _control.emit(it.toDataClass())
                        }
                    }
                } catch (e: IOError) {
                    e.printStackTrace()
                }
            }
        }

    }

    fun stop() {
        stop = true
        writingJob?.cancel()
        listeningJob?.cancel()
        socket.close()
    }
}
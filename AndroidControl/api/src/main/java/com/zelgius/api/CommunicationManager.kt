package com.zelgius.api

import com.zelgius.api.model.ConnectionStatus
import com.zelgius.api.model.toDataClass
import com.zelgius.api.model.toProtobuf
import com.zelgius.controller.StatusOuterClass.ArmSegment
import com.zelgius.controller.StatusOuterClass.Status
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOError
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import com.zelgius.api.model.Control as ApiControl
import com.zelgius.api.model.Status as ApiStatus

@Singleton
class CommunicationManager @Inject constructor(private val ioStreamProvider: IOStreamProvider) {

    private val _status = MutableStateFlow(
        Status.newBuilder()
            .setPitch(0.0)
            .setRoll(0.0)
            .setWalkMode(Status.Walk.NONE)
            .setLevel(Status.BatteryLevel.LOW)
            .addAllArmSegments((0..4).map {
                ArmSegment.newBuilder()
                    .setAngle(0.0)
                    .setIsSelected(it == 0)
                    .build()
            })
            .build().toDataClass()
    )
    val status: Flow<ApiStatus> = _status
    private var _connectionStatus = MutableStateFlow(ConnectionStatus.CONNECTING)
    val connectionStatus: Flow<ConnectionStatus> get() = _connectionStatus

    private var job: Job? = null
    private val jobScope = CoroutineScope(Dispatchers.IO)

    suspend fun startListening() {
        job?.cancel()
        _connectionStatus.emit(ConnectionStatus.CONNECTING)
         kotlin.runCatching {
             val inputStream =withContext(Dispatchers.IO) {
                ioStreamProvider.getInputStream()
            }

            job = jobScope.launch {

                withContext(Dispatchers.IO) {
                    try {
                        while (true) {
                            Status.parseDelimitedFrom(inputStream)?.let {
                                _status.emit(it.toDataClass())
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        ioStreamProvider.close()
                        _connectionStatus.emit(ConnectionStatus.ERROR)
                    }
                }
            }

            _connectionStatus.emit(ConnectionStatus.CONNECTED)
        } .onFailure {
            _connectionStatus.value = ConnectionStatus.ERROR
         }
    }

    suspend fun sendControl(control: ApiControl) = withContext(Dispatchers.IO) {
        val outputStream = ioStreamProvider.getOutputStream()
        control.toProtobuf().writeDelimitedTo(outputStream)
    }

    suspend fun sendButtonPressed(type: ApiControl.Type) = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            val outputStream = ioStreamProvider.getOutputStream()

            ApiControl(type = type, isPressed = true).toProtobuf().writeDelimitedTo(outputStream)
            delay(50)
            ApiControl(type = type, isPressed = false).toProtobuf().writeDelimitedTo(outputStream)
        }.onFailure {
            ioStreamProvider.close()
            _connectionStatus.emit(ConnectionStatus.ERROR)
        }

    }

    fun stop() {
        job?.cancel()
        ioStreamProvider.close()
    }
}
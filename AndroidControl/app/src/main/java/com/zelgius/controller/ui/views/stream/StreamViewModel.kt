package com.zelgius.controller.ui.views.stream

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.zelgius.api.Result
import com.zelgius.controller.BuildConfig
import com.zelgius.mjpg_streamer.MjpegInputStream
import com.zelgius.mjpg_streamer.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StreamViewModel : ViewModel() {
    private var inputStream: MjpegInputStream? = null
    val bitmap: LiveData<Bitmap>
        get() = inputStream!!.flow.filterNotNull()
            .asLiveData()

    val state: LiveData<State>
        get() = inputStream!!.status.asLiveData()

    val isConnected: Boolean
        get() = inputStream != null


    fun startStream() = flow<Result<MjpegInputStream>> {
        MjpegInputStream.read(BuildConfig.CAMERA_URL)
            .onSuccess {
                inputStream = it
                emit(Result.Success(it))
                viewModelScope.launch {
                    inputStream!!.status.collect { state ->
                        if (state == State.ERROR) {
                            withContext(Dispatchers.IO) {
                                kotlin.runCatching {
                                    inputStream?.close()
                                }
                            }
                            inputStream = null
                        }
                    }
                }
            }
            .onFailure { emit(Result.Error(it)) }
    }

    override fun onCleared() {
        inputStream?.stop()
    }

}
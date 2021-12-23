package com.zelgius.controller.ui.views.panel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.zelgius.api.CommunicationManager
import com.zelgius.api.model.ConnectionStatus
import com.zelgius.api.model.Control
import com.zelgius.api.model.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PanelViewModel @Inject constructor(private val communicationManager: CommunicationManager) :
    ViewModel() {
    val status: State<Status> get() = _status
    private var _status = mutableStateOf(previewStatus)

    val connectionStatus: Flow<ConnectionStatus>
        get() = communicationManager.connectionStatus

    init {
        retry()
    }

    fun toggleFlashLight() {
        viewModelScope.launch {
            communicationManager.sendButtonPressed(Control.Type.BUTTON_STICK_L)
        }
    }

    fun setSpeed(isFast: Boolean) {
        viewModelScope.launch {
            if (isFast)
                communicationManager.sendButtonPressed(Control.Type.BUTTON_MINUS)
            else
                communicationManager.sendButtonPressed(Control.Type.BUTTON_PLUS)
        }
    }

    fun setMode(mode: Status.Walk) {
        viewModelScope.launch {
            communicationManager.sendButtonPressed(
                when (mode) {
                    Status.Walk.RIPPLE -> Control.Type.BUTTON_X
                    Status.Walk.TETRAPOD -> Control.Type.BUTTON_A
                    Status.Walk.WAVE -> Control.Type.BUTTON_Y
                    Status.Walk.TRIPOD -> Control.Type.BUTTON_B
                    Status.Walk.NONE -> Control.Type.BUTTON_HOME
                }
            )

        }
    }

    fun retry() {
        viewModelScope.launch {
            communicationManager.startListening()
            communicationManager.status.collect {
                _status.value = it
            }
        }
    }
}


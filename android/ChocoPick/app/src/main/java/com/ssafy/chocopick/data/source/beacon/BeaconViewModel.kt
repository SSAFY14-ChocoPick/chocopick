package com.ssafy.chocopick.data.source.beacon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.repository.BeaconRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

sealed interface BeaconUiEvent {
    data object ShowEntryNotification : BeaconUiEvent
}

class BeaconViewModel(
    private val repo: BeaconRepository
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<BeaconUiEvent>(extraBufferCapacity = 8)
    val uiEvent: SharedFlow<BeaconUiEvent> = _uiEvent

    private var collecting = false

    fun startScanning() {
        repo.start()
        if (collecting) return
        collecting = true

        viewModelScope.launch {
            repo.enterEvents().collect {
                _uiEvent.emit(BeaconUiEvent.ShowEntryNotification)
            }
        }
    }

    fun stopScanning() {
        repo.stop()
    }
}
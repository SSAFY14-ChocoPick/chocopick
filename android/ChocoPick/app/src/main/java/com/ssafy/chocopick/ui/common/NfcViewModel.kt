package com.ssafy.chocopick.ui.common


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class NfcViewModel : ViewModel() {

    private val _tagEvent = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val tagEvent: SharedFlow<Unit> = _tagEvent

    fun onTagDetected() {
        _tagEvent.tryEmit(Unit)
    }
}
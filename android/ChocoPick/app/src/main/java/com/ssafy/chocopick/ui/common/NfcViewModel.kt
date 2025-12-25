package com.ssafy.chocopick.ui.common


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

//class NfcViewModel : ViewModel() {
//
//    private val _tagEvent = MutableSharedFlow<Unit>(
//        replay = 0,
//        extraBufferCapacity = 8,
//        onBufferOverflow = BufferOverflow.DROP_OLDEST
//    )
//    val tagEvent: SharedFlow<Unit> = _tagEvent
//
//    fun onTagDetected() {
//        _tagEvent.tryEmit(Unit)
//    }
//}
class NfcViewModel : ViewModel() {

    private val _waiting = MutableStateFlow(false)
    val waiting: StateFlow<Boolean> = _waiting

    private val _tagEvent = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val tagEvent: SharedFlow<Unit> = _tagEvent

    fun startWaiting() { _waiting.value = true }
    fun stopWaiting() { _waiting.value = false }

    fun onTagDetected() {
        if (!_waiting.value) return
        _tagEvent.tryEmit(Unit)
        _waiting.value = false // 1회 처리 후 자동 종료
    }
}
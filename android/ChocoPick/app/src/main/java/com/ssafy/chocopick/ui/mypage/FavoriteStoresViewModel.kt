package com.ssafy.chocopick.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.repository.StoreRepository
import com.ssafy.chocopick.data.source.firebase.realtime.StoreWithId
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoriteStoresViewModel(
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _storesState = MutableStateFlow<UiState<List<StoreWithId>>>(UiState.Idle)
    val storesState: StateFlow<UiState<List<StoreWithId>>> = _storesState

    fun loadStores() {
        _storesState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { storeRepository.getAllStores() }
                .onSuccess { _storesState.value = UiState.Success(it) }
                .onFailure { _storesState.value = UiState.Error(it.message ?: "매장 로드 실패", it) }
        }
    }
}

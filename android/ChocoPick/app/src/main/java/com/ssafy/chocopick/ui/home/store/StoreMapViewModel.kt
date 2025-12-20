package com.ssafy.chocopick.ui.home.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.model.Store
import com.ssafy.chocopick.data.repository.StoreRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StoreMapViewModel(
    private val repo : StoreRepository
) : ViewModel() {
    private val _storesState = MutableStateFlow<UiState<List<Store>>>(UiState.Idle)
    val storesState: StateFlow<UiState<List<Store>>> = _storesState

    fun loadStore() {
        _storesState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { repo.getAllStores() }
                .onSuccess { _storesState.value = UiState.Success(it) }
                .onFailure { _storesState.value = UiState.Error(it.message ?: "매장 조회 실패") }
        }
    }
}

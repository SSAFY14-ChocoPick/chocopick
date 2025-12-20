package com.ssafy.chocopick.ui.home.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.model.Store
import com.ssafy.chocopick.data.repository.StoreRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StoreListViewModel(
    private val repo : StoreRepository
) : ViewModel() {

    private val _storesState = MutableStateFlow<UiState<List<Store>>>(UiState.Idle)
    val storesState : StateFlow<UiState<List<Store>>> = _storesState

    fun loadStores(){
        _storesState.value = UiState.Loading
        viewModelScope.launch{
            runCatching {
                repo.getAllStores()
            }
                .onSuccess { list ->
                    _storesState.value = UiState.Success(list)
                }
                .onFailure { e ->
                    _storesState.value = UiState.Error(e.message ?: "매장 목록 조회 실패")
                }
        }
    }

}
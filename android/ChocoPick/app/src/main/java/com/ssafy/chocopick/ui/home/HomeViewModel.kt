package com.ssafy.chocopick.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.repository.RecommendProductRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val recommendRepo: RecommendProductRepository
) : ViewModel() {

    private val _recommendState =
        MutableStateFlow<UiState<List<RecommendUi>>>(UiState.Idle)
    val recommendState: StateFlow<UiState<List<RecommendUi>>> = _recommendState

    fun loadRecommendTop4() {
        viewModelScope.launch {
            _recommendState.value = UiState.Loading

            runCatching {
                recommendRepo.getTop4RecommendProducts()
            }.onSuccess { list ->
                val uiList = list.map {
                    RecommendUi(
                        productId = it.productId,
                        name = it.name,
                        imageUrl = it.imageUrl,
                        price = it.price
                    )
                }
                _recommendState.value = UiState.Success(uiList)
            }.onFailure { e ->
                _recommendState.value = UiState.Error(
                    e.message ?: "추천 상품 로딩 실패"
                )
            }
        }
    }
}

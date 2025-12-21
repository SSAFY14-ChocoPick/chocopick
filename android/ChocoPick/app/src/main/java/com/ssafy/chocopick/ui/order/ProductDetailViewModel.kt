package com.ssafy.chocopick.ui.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.model.Product
import com.ssafy.chocopick.data.model.ReviewStats
import com.ssafy.chocopick.data.repository.ProductRepository
import com.ssafy.chocopick.data.repository.ReviewRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val repo : ProductRepository,
    private val reviewRepo: ReviewRepository
) : ViewModel() {

    private val _productDetailState = MutableStateFlow<UiState<Product>>(UiState.Idle)
    val productDetailState : StateFlow<UiState<Product>> = _productDetailState

    fun loadProductDetail(productId : String){
       _productDetailState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { repo.fetchProductById(productId) }
                .onSuccess {  product ->
                    if(product == null){
                        _productDetailState.value = UiState.Error("상품을 찾을 수 없습니다.")
                    }else{
                        _productDetailState.value = UiState.Success(product)
                    }
                }
                .onFailure {  e ->
                    _productDetailState.value = UiState.Error(e.message ?: "상품 조회 실패")
                }
        }
    }

    private val _reviewStatsState = MutableStateFlow<UiState<ReviewStats>>(UiState.Idle)
    val reviewStatsState: StateFlow<UiState<ReviewStats>> = _reviewStatsState

    fun loadReviewStats(productId: String) {
        _reviewStatsState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { reviewRepo.getStats(productId) }
                .onSuccess { stats ->
                    _reviewStatsState.value =
                        UiState.Success(stats ?: ReviewStats(productId = productId))
                }
                .onFailure { e ->
                    _reviewStatsState.value = UiState.Error(e.message ?: "리뷰 통계 로드 실패", e)
                }
        }
    }
}
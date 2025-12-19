package com.ssafy.chocopick.ui.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.model.Product
import com.ssafy.chocopick.data.repository.ProductRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val repo : ProductRepository
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
}
package com.ssafy.chocopick.ui.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.model.Product
import com.ssafy.chocopick.data.repository.ProductRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderViewModel(
    private val repo: ProductRepository
) : ViewModel(){
    private val _productState = MutableStateFlow<UiState<List<Product>>>(UiState.Idle)
    val productsState: StateFlow<UiState<List<Product>>> = _productState

    fun loadProducts(){
        _productState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { repo.fetchProducts() }
                .onSuccess { list -> _productState.value = UiState.Success(list) }
                .onFailure { e -> _productState.value = UiState.Error(e.message ?: "상품 불러오기",e) }
        }
    }



}
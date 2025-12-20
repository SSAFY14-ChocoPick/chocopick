package com.ssafy.chocopick.ui.order

import androidx.lifecycle.ViewModel
import com.ssafy.chocopick.data.model.CartItem
import com.ssafy.chocopick.data.model.Product
import com.ssafy.chocopick.data.repository.CartRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CartViewModel(
    private val repo : CartRepository
) : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems : StateFlow<List<CartItem>> = _cartItems

    fun addToCart(product: Product, qty: Int) {
        repo.add(product, qty)
        refresh()
    }

    fun increase(productId: String) {
        repo.increase(productId)
        refresh()
    }

    fun decrease(productId: String) {
        repo.decrease(productId)
        refresh()
    }

    fun remove(productId: String) {
        repo.remove(productId)
        refresh()
    }

    fun clear() {
        repo.clear()
        refresh()
    }

    fun refresh() {
        _cartItems.value = repo.getItems()
    }

    fun totalPrice(): Int =
        _cartItems.value.sumOf { it.price * it.quantity }
}
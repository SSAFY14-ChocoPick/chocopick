package com.ssafy.chocopick.ui.order

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ssafy.chocopick.data.model.CartItem
import com.ssafy.chocopick.data.model.Product
import com.ssafy.chocopick.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CartViewModel(
    private val cartRepository: CartRepository
) : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems : StateFlow<List<CartItem>> = _cartItems

    fun addToCart(product: Product, qty: Int) {
        Log.d("CART_TRACE", "▶ VM addToCart productId=${product.productId}, qty=$qty")

        cartRepository.add(product, qty)

        // add 이후 바로 로드
        refresh()
    }


    fun increase(productId: String) {
        cartRepository.increase(productId)
        refresh()
    }

    fun decrease(productId: String) {
        cartRepository.decrease(productId)
        refresh()
    }

    fun remove(productId: String) {
        cartRepository.remove(productId)
        refresh()
    }

    fun clear() {
        cartRepository.clear()
        refresh()
    }

    fun refresh() {
        _cartItems.value = cartRepository.getItems()
    }

    fun totalPrice(): Int =
        _cartItems.value.sumOf { it.price * it.quantity }
}
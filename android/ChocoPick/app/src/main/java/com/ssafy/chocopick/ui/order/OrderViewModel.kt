package com.ssafy.chocopick.ui.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.ssafy.chocopick.data.model.CartItem
import com.ssafy.chocopick.data.model.FcmRequestDto
import com.ssafy.chocopick.data.model.Order
import com.ssafy.chocopick.data.model.OrderItem
import com.ssafy.chocopick.data.remote.ApiProvider
import com.ssafy.chocopick.data.repository.OrderRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _orderState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val orderState: StateFlow<UiState<Unit>> = _orderState

    fun placeOrder(
        cartItems: List<CartItem>,
        storeId: String
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) {
            _orderState.value = UiState.Error("로그인이 필요합니다.")
            return
        }

        if (cartItems.isEmpty()) {
            _orderState.value = UiState.Error("장바구니가 비어있습니다.")
            return
        }

        viewModelScope.launch {
            _orderState.value = UiState.Loading

            runCatching {
                val orderId = orderRepository
                    .getOrders(uid, 1) // dummy call 제거용 아님, 아래에서 직접 pushKey 쓰는 게 더 좋음
                // 👉 orderId는 DataSource에서 pushKey로 만드는 구조면 여기서 생성
            }

            val orderId = System.currentTimeMillis().toString() // 🔥 실무에선 pushKey로 교체 가능

            val items = cartItems.mapIndexed { index, item ->
                OrderItem(
                    productId = item.productId,
                    name = item.name,
                    price = item.price,
                    quantity = item.quantity
                )
            }

            val totalPrice = items.sumOf { it.price * it.quantity }

            val order = Order(
                orderId = orderId,
                uid = uid,
                store = storeId,
                items = items,
                totalPrice = totalPrice,
                orderDate = System.currentTimeMillis(),
                status = "주문 완료"
            )

            runCatching {
                orderRepository.upsertOrder(order)
            }.onSuccess {
                sendOrderCompleteFcm()
                _orderState.value = UiState.Success(Unit)
            }.onFailure { e ->
                _orderState.value = UiState.Error("주문 실패: ${e.message}", e)
            }
        }
    }

    private suspend fun sendOrderCompleteFcm() {
        val token = FirebaseMessaging.getInstance().token.await()

        ApiProvider.fcmApi.sendDelayed(
            FcmRequestDto(
                token = token,
                title = "주문 완료!",
                body = "주문이 정상적으로 접수되었습니다. 픽업 준비 알림을 기다려주세요 ☕🍫"
            )
        )
    }

    fun clearState() {
        _orderState.value = UiState.Idle
    }
}

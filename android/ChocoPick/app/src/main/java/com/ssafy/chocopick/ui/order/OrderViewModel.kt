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
import com.ssafy.chocopick.data.repository.RewardRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderViewModel(
    private val orderRepository: OrderRepository,
    private val rewardRepository: RewardRepository
) : ViewModel() {

    private val _orderState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val orderState: StateFlow<UiState<Unit>> = _orderState

    fun placeOrder(
        cartItems: List<CartItem>,
        storeId: String,
        orderType: String,
        tableNo: Int?
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
        if (storeId.isBlank()) {
            _orderState.value = UiState.Error("매장 정보가 올바르지 않습니다.")
            return
        }

        viewModelScope.launch {
            _orderState.value = UiState.Loading

            val orderId = System.currentTimeMillis().toString()

            val items = cartItems.map { item ->
                OrderItem(
                    productId = item.productId,
                    name = item.name,
                    price = item.price,
                    quantity = item.quantity
                )
            }

            val totalPrice = items.sumOf { it.price * it.quantity }

            // ✅ 스탬프는 "상품 수량 합계"
            val stampAdd = items.sumOf { it.quantity }.coerceAtLeast(0)

            val order = Order(
                orderId = orderId,
                uid = uid,
                store = storeId,
                items = items,
                totalPrice = totalPrice,
                orderDate = System.currentTimeMillis(),
                status = "주문 완료",
                orderType = orderType,
                tableNo = tableNo
            )

            runCatching {
                // 1) 주문 저장
                orderRepository.upsertOrder(order)

                // 2) ✅ Reward 반영
                // - stamps: +stampAdd
                // - totalOrders: +1 (주문 1건)
                rewardRepository.applyRewardForOrderIfNeeded(
                    uid = uid,
                    orderId = orderId,
                    stampAdd = stampAdd
                )

                // 3) FCM 전송
                if (orderType == "PICKUP") sendPickupFcm()
                else sendStoreFcm(tableNo ?: 1)

            }.onSuccess {
                _orderState.value = UiState.Success(Unit)
            }.onFailure { e ->
                _orderState.value = UiState.Error("주문 실패: ${e.message}", e)
            }
        }
    }

    private suspend fun sendPickupFcm() {
        val token = FirebaseMessaging.getInstance().token.await()
        ApiProvider.fcmApi.sendDelayed(
            FcmRequestDto(
                token = token,
                title = "주문 완료!",
                body = "주문이 정상적으로 접수되었습니다. 픽업 준비 알림을 기다려주세요 ☕🍫"
            )
        )
    }

    private suspend fun sendStoreFcm(tableNo: Int) {
        val token = FirebaseMessaging.getInstance().token.await()
        ApiProvider.fcmApi.sendStoreDelayed(
            FcmRequestDto(
                token = token,
                title = "테이블 주문",
                body = tableNo.toString()
            )
        )
    }

    fun clearState() {
        _orderState.value = UiState.Idle
    }
}

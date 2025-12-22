package com.ssafy.chocopick.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.model.OrderDetailUi
import com.ssafy.chocopick.data.model.OrderWithStore
import com.ssafy.chocopick.data.repository.AuthRepository
import com.ssafy.chocopick.data.repository.OrderRepository
import com.ssafy.chocopick.data.repository.StoreRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderDetailViewModel(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _orderState =
        MutableStateFlow<UiState<OrderDetailUi>>(UiState.Loading)
    val orderState: StateFlow<UiState<OrderDetailUi>> = _orderState

    fun loadOrder(orderId: String) {
        val uid = authRepository.getCurrentUid() ?: run {
            _orderState.value = UiState.Error("로그인 필요")
            return
        }

        viewModelScope.launch {
            runCatching {
                val orderWithStore =
                    orderRepository.getOrderWithStore(uid, orderId)
                        ?: return@runCatching null

                val store =
                    storeRepository.getStore(orderWithStore.storeId)

                OrderDetailUi(orderWithStore, store)
            }.onSuccess { result ->
                _orderState.value =
                    if (result == null) UiState.Error("주문 정보 없음")
                    else UiState.Success(result)
            }.onFailure {
                _orderState.value = UiState.Error("주문 조회 실패", it)
            }
        }
    }

}

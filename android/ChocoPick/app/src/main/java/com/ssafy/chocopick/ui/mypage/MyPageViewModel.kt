package com.ssafy.chocopick.ui.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.model.Order
import com.ssafy.chocopick.data.model.Reward
import com.ssafy.chocopick.data.model.User
import com.ssafy.chocopick.data.repository.AuthRepository
import com.ssafy.chocopick.data.repository.OrderRepository
import com.ssafy.chocopick.data.repository.RewardRepository
import com.ssafy.chocopick.data.repository.UserRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "MyPageViewModel"

class MyPageViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val rewardRepository: RewardRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val userState: StateFlow<UiState<User>> = _userState

    fun loadMyProfile() {
        val uid = authRepository.getCurrentUid()
        if (uid.isNullOrBlank()) {
            _userState.value = UiState.Error("로그인이 필요합니다.")
            return
        }

        Log.d(TAG, "func loadMyProfile / uid: $uid")

        _userState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { userRepository.getUser(uid) }
                .onSuccess { user ->
                    if (user == null) _userState.value = UiState.Error("유저 정보를 찾을 수 없습니다.")
                    else _userState.value = UiState.Success(user)
                }
                .onFailure { e ->
                    _userState.value = UiState.Error(e.message ?: "프로필 로드 실패", e)
                }
        }
    }

    private val _rewardState = MutableStateFlow<UiState<Reward>>(UiState.Idle)
    val rewardState: StateFlow<UiState<Reward>> = _rewardState

    fun loadReward() {
        val uid = authRepository.getCurrentUid()
        if (uid.isNullOrBlank()) {
            _rewardState.value = UiState.Error("로그인이 필요합니다.")
            return
        }

        Log.d(TAG, "func loadReward / uid: $uid")

        _rewardState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { rewardRepository.getReward(uid) }
                .onSuccess { reward ->
                    Log.d(TAG, "func loadReward / success")
                    if (reward == null) _rewardState.value = UiState.Error("리워드 정보 없음")
                    else _rewardState.value = UiState.Success(reward)
                }
                .onFailure { e ->
                    Log.d(TAG, "func loadReward / fail")
                    _rewardState.value = UiState.Error(e.message ?: "리워드 로드 실패", e)
                }
        }
    }

    fun getBenefitText(tier: String): String =
        when (tier) {
            "BRONZE" -> "이번 달 혜택: 무료 사이즈업 1회"
            "SILVER" -> "이번 달 혜택: 픽업 무료 쿠폰 1장"
            "GOLD" -> "이번 달 혜택: 매장 에코(개인컵) 무료 쿠폰 1장"
            else -> ""
        }

    // 최근 주문 목록 1개 로드
    private val _recentOrderState = MutableStateFlow<UiState<Order>>(UiState.Idle)
    val recentOrderState: StateFlow<UiState<Order>> = _recentOrderState

    fun loadRecentOrder() {
        val uid = authRepository.getCurrentUid() ?: run {
            _recentOrderState.value = UiState.Error("로그인 필요")
            return
        }
        Log.d(TAG, "func loadRecentOrder / uid: $uid")

        viewModelScope.launch {
            runCatching {
                orderRepository.getMostRecentOrder(uid)
            }.onSuccess { order ->
                if (order == null) {
                    _recentOrderState.value = UiState.Error("주문 없음")
                } else {
                    _recentOrderState.value = UiState.Success(order)
                }
            }.onFailure {
                _recentOrderState.value = UiState.Error("주문 로드 실패", it)
            }
        }
    }

    // 최근 주문 목록 전체 로드
    private val _ordersState = MutableStateFlow<UiState<List<Order>>>(UiState.Idle)
    val ordersState: StateFlow<UiState<List<Order>>> = _ordersState

    fun loadOrderList(limit: Int = 50) {
        val uid = authRepository.getCurrentUid()
        if (uid.isNullOrBlank()) {
            _ordersState.value = UiState.Error("로그인이 필요합니다.")
            return
        }

        _ordersState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { orderRepository.getOrders(uid, limit) }
                .onSuccess { orders ->
                    _ordersState.value = UiState.Success(orders) // ✅ createdAt 최신순 정렬 이미 DS에서 처리됨
                }
                .onFailure { e ->
                    _ordersState.value = UiState.Error(e.message ?: "주문 목록 로드 실패", e)
                }
        }
    }


}

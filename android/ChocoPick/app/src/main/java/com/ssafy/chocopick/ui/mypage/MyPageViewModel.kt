package com.ssafy.chocopick.ui.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.model.Order
import com.ssafy.chocopick.data.model.OrderWithStore
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    fun calcTier(totalOrders: Int): String =
        when {
            totalOrders >= 30 -> "GOLD"
            totalOrders >= 10 -> "SILVER"
            else -> "BRONZE"
        }

    fun getBenefitText(tier: String): String =
        when (tier) {
            "BRONZE" -> "이번 달 혜택: 롯데 아몬드 초코볼 46g 증정"
            "SILVER" -> "이번 달 혜택: 밀카 요거트 초콜릿 100g 증정"
            "GOLD" -> "이번 달 혜택: 몰티져스 밀크 버켓 초콜릿 465g 증정"
            else -> ""
        }

    fun getBenefitDetailText(tier: String): String =
        "매장 방문 시 직원 확인 후 제공돼요. (재고 상황에 따라 동일 가격대 상품으로 대체될 수 있어요.)"

    // 최근 주문 목록 1개 로드
    private val _recentOrderState =
        MutableStateFlow<UiState<OrderWithStore>>(UiState.Idle)
    val recentOrderState: StateFlow<UiState<OrderWithStore>> = _recentOrderState

    fun loadRecentOrder() {
        val uid = authRepository.getCurrentUid() ?: run {
            _recentOrderState.value = UiState.Error("로그인 필요")
            return
        }

        _recentOrderState.value = UiState.Loading

        viewModelScope.launch {
            runCatching {
                orderRepository.getMostRecentOrderWithStore(uid)
            }.onSuccess { order ->
                _recentOrderState.value =
                    if (order == null) UiState.Error("주문 없음")
                    else UiState.Success(order)
            }.onFailure {
                _recentOrderState.value = UiState.Error("최근 주문 로드 실패", it)
            }
        }
    }



    fun formatOrderDate(millis: Long): String {
        if (millis <= 0L) return ""
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
        return sdf.format(Date(millis))
    }

    // 최근 주문 목록 전체 로드
    private val _ordersState = MutableStateFlow<UiState<List<OrderWithStore>>>(UiState.Idle)
    val ordersState: StateFlow<UiState<List<OrderWithStore>>> = _ordersState

    fun loadOrderList(limit: Int = 50) {
        val uid = authRepository.getCurrentUid() ?: run {
            _ordersState.value = UiState.Error("로그인 필요")
            return
        }
        _ordersState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { orderRepository.getOrdersWithStore(uid, limit) }
                .onSuccess { _ordersState.value = UiState.Success(it) }
                .onFailure { _ordersState.value = UiState.Error("주문 목록 로드 실패", it) }
        }
    }


}

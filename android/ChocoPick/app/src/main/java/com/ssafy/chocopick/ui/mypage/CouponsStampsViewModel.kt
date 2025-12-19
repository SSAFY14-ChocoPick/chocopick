package com.ssafy.chocopick.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.model.Coupon
import com.ssafy.chocopick.data.model.Reward
import com.ssafy.chocopick.data.repository.AuthRepository
import com.ssafy.chocopick.data.repository.CouponRepository
import com.ssafy.chocopick.data.repository.RewardRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CouponsStampsViewModel(
    private val authRepository: AuthRepository,
    private val rewardRepository: RewardRepository,
    private val couponRepository: CouponRepository
) : ViewModel() {

    private val _rewardState = MutableStateFlow<UiState<Reward>>(UiState.Idle)
    val rewardState: StateFlow<UiState<Reward>> = _rewardState

    private val _couponsState = MutableStateFlow<UiState<List<Coupon>>>(UiState.Idle)
    val couponsState: StateFlow<UiState<List<Coupon>>> = _couponsState

    fun load() {
        val uid = authRepository.getCurrentUid()
        if (uid.isNullOrBlank()) {
            _rewardState.value = UiState.Error("로그인이 필요합니다.")
            _couponsState.value = UiState.Error("로그인이 필요합니다.")
            return
        }

        loadReward(uid)
        loadCoupons(uid)
    }

    private fun loadReward(uid: String) {
        _rewardState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { rewardRepository.getReward(uid) }
                .onSuccess { reward ->
                    if (reward == null) _rewardState.value = UiState.Error("리워드 정보가 없습니다.")
                    else _rewardState.value = UiState.Success(reward)
                }
                .onFailure { e ->
                    _rewardState.value = UiState.Error(e.message ?: "리워드 로드 실패", e)
                }
        }
    }

    private fun loadCoupons(uid: String) {
        _couponsState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { couponRepository.getCoupons(uid) }
                .onSuccess { _couponsState.value = UiState.Success(it) }
                .onFailure { e -> _couponsState.value = UiState.Error(e.message ?: "쿠폰 로드 실패", e) }
        }
    }

    /**
     * "쿠폰 사용하기" 버튼 동작(임시):
     * - 보유 쿠폰이 있으면 첫 번째 쿠폰을 사용 처리(삭제 or used=true)
     */
    fun useCouponIfPossible(
        onNotEnough: () -> Unit,
        onUsed: () -> Unit
    ) {
        val uid = authRepository.getCurrentUid()
        if (uid.isNullOrBlank()) {
            onNotEnough()
            return
        }

        val state = _couponsState.value
        val coupons = (state as? UiState.Success)?.data.orEmpty()
        if (coupons.isEmpty()) {
            onNotEnough()
            return
        }

        val first = coupons.first()

        viewModelScope.launch {
            runCatching {
                couponRepository.useCoupon(uid, first.couponId)
            }.onSuccess {
                onUsed()
                loadCoupons(uid) // 사용 후 다시 로드
            }.onFailure {
                onNotEnough()
            }
        }
    }
}

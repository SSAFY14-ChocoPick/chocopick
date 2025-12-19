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

class RewardViewModel(
    private val authRepository: AuthRepository,
    private val rewardRepository: RewardRepository,
    private val couponRepository: CouponRepository // (선택) 이벤트 쿠폰 등 확장용
) : ViewModel() {

    private val _rewardState = MutableStateFlow<UiState<Reward>>(UiState.Idle)
    val rewardState: StateFlow<UiState<Reward>> = _rewardState

    // (선택) “이벤트 쿠폰 목록” 같은 게 있으면 유지
    private val _couponsState = MutableStateFlow<UiState<List<Coupon>>>(UiState.Idle)
    val couponsState: StateFlow<UiState<List<Coupon>>> = _couponsState

    fun load(loadCouponsToo: Boolean = false) {
        val uid = authRepository.getCurrentUid()
        if (uid.isNullOrBlank()) {
            _rewardState.value = UiState.Error("로그인이 필요합니다.")
            _couponsState.value = UiState.Error("로그인이 필요합니다.")
            return
        }

        loadReward(uid)
        if (loadCouponsToo) loadCoupons(uid)
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

    // ✅ 화면에서 그대로 호출하기 편하게
    fun getBenefitText(tier: String): String =
        when (tier) {
            "BRONZE" -> "이번 달 혜택: 무료 사이즈업 1회"
            "SILVER" -> "이번 달 혜택: 픽업 무료 쿠폰 1장"
            "GOLD" -> "이번 달 혜택: 매장 에코(개인컵) 무료 쿠폰 1장"
            else -> "이번 달 혜택: -"
        }

    /**
     * ✅ 아메리카노 쿠폰 사용하기 (Reward 기반)
     * - americanoCoupons > 0 이면 1장 감소
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

        viewModelScope.launch {
            runCatching { rewardRepository.useAmericanoIfPossible(uid) }
                .onSuccess { updated ->
                    _rewardState.value = UiState.Success(updated)
                    onUsed()
                }
                .onFailure {
                    onNotEnough()
                }
        }
    }

    /**
     * ✅ 아메리카노 쿠폰 발행
     * - stamps >= 10 이면 stamps-10, americanoCoupons+1
     */
    fun issueAmericano(
        onNotEnough: () -> Unit,
        onIssued: () -> Unit
    ) {
        val uid = authRepository.getCurrentUid()
        if (uid.isNullOrBlank()) {
            onNotEnough()
            return
        }

        viewModelScope.launch {
            runCatching { rewardRepository.issueAmericanoIfPossible(uid) }
                .onSuccess { updated ->
                    _rewardState.value = UiState.Success(updated)
                    onIssued()
                }
                .onFailure {
                    onNotEnough()
                }
        }
    }
}

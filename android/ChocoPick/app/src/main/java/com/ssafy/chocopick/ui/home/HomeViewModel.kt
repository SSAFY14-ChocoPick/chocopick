package com.ssafy.chocopick.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.model.Reward
import com.ssafy.chocopick.data.repository.RecommendProductRepository
import com.ssafy.chocopick.data.repository.RewardRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val recommendRepo: RecommendProductRepository,
    private val rewardRepo: RewardRepository
) : ViewModel() {

    private val _recommendState =
        MutableStateFlow<UiState<List<RecommendUi>>>(UiState.Idle)
    val recommendState: StateFlow<UiState<List<RecommendUi>>> = _recommendState

    // ✅ 추가: 홈에서 스탬프/쿠폰 상태
    private val _rewardState =
        MutableStateFlow<UiState<Reward>>(UiState.Idle)
    val rewardState: StateFlow<UiState<Reward>> = _rewardState

    fun loadRecommendTop4() {
        viewModelScope.launch {
            _recommendState.value = UiState.Loading

            runCatching { recommendRepo.getTop4RecommendProducts() }
                .onSuccess { list ->
                    val uiList = list.map {
                        RecommendUi(
                            productId = it.productId,
                            name = it.name,
                            imageUrl = it.imageUrl,
                            price = it.price
                        )
                    }
                    _recommendState.value = UiState.Success(uiList)
                }
                .onFailure { e ->
                    _recommendState.value = UiState.Error(e.message ?: "추천 상품 로딩 실패")
                }
        }
    }

    // ✅ 추가: Reward 로드 (stamps가 여기 있음)
    fun loadReward(uid: String) {
        if (uid.isBlank()) return

        viewModelScope.launch {
            _rewardState.value = UiState.Loading

            runCatching {
                rewardRepo.getReward(uid) ?: Reward(uid = uid)
            }.onSuccess { reward ->
                _rewardState.value = UiState.Success(reward)
            }.onFailure { e ->
                _rewardState.value = UiState.Error(e.message ?: "리워드 로딩 실패")
            }
        }
    }
}

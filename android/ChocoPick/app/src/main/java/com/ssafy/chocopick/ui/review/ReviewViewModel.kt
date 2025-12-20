package com.ssafy.chocopick.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.chocopick.data.model.Review
import com.ssafy.chocopick.data.model.ReviewStats
import com.ssafy.chocopick.data.repository.ReviewRepository
import com.ssafy.chocopick.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _statsState = MutableStateFlow<UiState<ReviewStats>>(UiState.Idle)
    val statsState: StateFlow<UiState<ReviewStats>> = _statsState

    private val _reviewsState = MutableStateFlow<UiState<List<Review>>>(UiState.Idle)
    val reviewsState: StateFlow<UiState<List<Review>>> = _reviewsState

    private val _myReviewState = MutableStateFlow<UiState<Review?>>(UiState.Idle)
    val myReviewState: StateFlow<UiState<Review?>> = _myReviewState

    fun load(productId: String, myUid: String) {
        loadStats(productId)
        loadReviews(productId)
        loadMyReview(productId, myUid)
    }

    fun loadStats(productId: String) {
        _statsState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { reviewRepository.getStats(productId) }
                .onSuccess { stats ->
                    _statsState.value = UiState.Success(stats ?: ReviewStats(productId = productId))
                }
                .onFailure { e ->
                    _statsState.value = UiState.Error(e.message ?: "평점 로드 실패", e)
                }
        }
    }

    fun loadReviews(productId: String, limit: Int = 100) {
        _reviewsState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { reviewRepository.getReviews(productId, limit) }
                .onSuccess { list -> _reviewsState.value = UiState.Success(list) }
                .onFailure { e -> _reviewsState.value = UiState.Error(e.message ?: "리뷰 로드 실패", e) }
        }
    }

    fun loadMyReview(productId: String, myUid: String) {
        _myReviewState.value = UiState.Loading
        viewModelScope.launch {
            runCatching { reviewRepository.getMyReview(productId, myUid) }
                .onSuccess { r -> _myReviewState.value = UiState.Success(r) }
                .onFailure { e -> _myReviewState.value = UiState.Error(e.message ?: "내 리뷰 로드 실패", e) }
        }
    }

    fun upsert(review: Review, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            runCatching { reviewRepository.upsertReview(review) }
                .onSuccess {
                    onDone?.invoke()
                    // 화면 최신화
                    loadStats(review.productId)
                    loadReviews(review.productId)
                }
                .onFailure {
                    // 필요하면 별도 이벤트로 토스트 처리
                }
        }
    }

    fun delete(productId: String, reviewId: String, myUid: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            runCatching { reviewRepository.deleteReview(productId, reviewId, myUid) }
                .onSuccess {
                    onDone?.invoke()
                    loadStats(productId)
                    loadReviews(productId)
                    loadMyReview(productId, myUid)
                }
                .onFailure {
                    // 필요하면 별도 이벤트
                }
        }
    }
}
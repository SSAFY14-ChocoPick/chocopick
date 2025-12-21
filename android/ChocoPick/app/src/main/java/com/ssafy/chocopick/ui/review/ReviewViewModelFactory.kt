package com.ssafy.chocopick.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.ReviewRepository
import com.ssafy.chocopick.data.repository.ReviewRepositoryImpl
import com.ssafy.chocopick.data.source.firebase.realtime.ReviewDataSource

class ReviewViewModelFactory(
    private val reviewRepository: ReviewRepository =
        ReviewRepositoryImpl(ReviewDataSource())
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReviewViewModel(reviewRepository) as T
    }
}
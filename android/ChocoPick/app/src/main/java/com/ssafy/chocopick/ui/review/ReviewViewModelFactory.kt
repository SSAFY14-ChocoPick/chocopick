package com.ssafy.chocopick.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.chocopick.data.repository.AiSummaryRepository
import com.ssafy.chocopick.data.repository.AiSummaryRepositoryImpl
import com.ssafy.chocopick.data.repository.ReviewRepository
import com.ssafy.chocopick.data.repository.ReviewRepositoryImpl
import com.ssafy.chocopick.data.source.firebase.realtime.ReviewDataSource
import com.ssafy.chocopick.data.source.gms.GmsReviewSummaryDataSource

class ReviewViewModelFactory(
    private val reviewRepository: ReviewRepository =
        ReviewRepositoryImpl(ReviewDataSource()),
    private val aiSummaryRepository: AiSummaryRepository =
        AiSummaryRepositoryImpl(
            GmsReviewSummaryDataSource()
        )
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReviewViewModel(reviewRepository, aiSummaryRepository) as T
    }
}

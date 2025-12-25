package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.AiReviewSummary

interface AiSummaryRepository {
    suspend fun summarize(reviews: List<String>): AiReviewSummary
}

package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.model.AiReviewSummary
import com.ssafy.chocopick.data.source.gms.GmsReviewSummaryDataSource

class AiSummaryRepositoryImpl(
    private val ds: GmsReviewSummaryDataSource
) : AiSummaryRepository {
    override suspend fun summarize(reviews: List<String>): AiReviewSummary =
        ds.summarize(reviews)
}

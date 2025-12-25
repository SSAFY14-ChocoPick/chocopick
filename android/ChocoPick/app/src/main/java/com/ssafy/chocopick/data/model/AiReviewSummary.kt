package com.ssafy.chocopick.data.model

data class AiReviewSummary(
    val overall: String = "",
    val pros: String = "",
    val cons: String = "",
    val trend: String = "", // "대체로 긍정" | "대체로 부정" | "혼합"
    val keywords: List<String> = emptyList()
)


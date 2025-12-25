package com.ssafy.chocopick.data.remote

import com.google.gson.annotations.SerializedName

data class GeminiGenerateRequest(
    @SerializedName("contents") val contents: List<Content>
) {
    data class Content(
        @SerializedName("parts") val parts: List<Part>
    )
    data class Part(
        @SerializedName("text") val text: String
    )
}

data class GeminiGenerateResponse(
    @SerializedName("candidates") val candidates: List<Candidate>?
) {
    data class Candidate(
        @SerializedName("content") val content: Content?
    )
    data class Content(
        @SerializedName("parts") val parts: List<Part>?
    )
    data class Part(
        @SerializedName("text") val text: String?
    )
}

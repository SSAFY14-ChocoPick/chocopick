package com.ssafy.chocopick.data.source.gms

import com.google.gson.Gson
import com.ssafy.chocopick.data.model.AiReviewSummary
import com.ssafy.chocopick.data.remote.GeminiGenerateRequest
import com.ssafy.chocopick.data.remote.GmsApiProvider

class GmsReviewSummaryDataSource(
    private val gson: Gson = Gson(),
    private val apiKey: String = "S14P02DF08-1240d998-fe86-4037-b5c3-1883d107e8ef" // ✅ 요청대로 예시(실제론 BuildConfig 권장)
) {
    suspend fun summarize(reviews: List<String>): AiReviewSummary {
        val prompt = buildPrompt(reviews)

        val body = GeminiGenerateRequest(
            contents = listOf(
                GeminiGenerateRequest.Content(
                    parts = listOf(GeminiGenerateRequest.Part(prompt))
                )
            )
        )

        val res = GmsApiProvider.gemini.generateContent(
            url = GmsApiProvider.GEMINI_25_FLASH_URL,
            apiKey = apiKey,
            body = body
        )

        val text = res.candidates
            ?.firstOrNull()
            ?.content?.parts
            ?.firstOrNull()
            ?.text
            ?.trim()
            ?: throw IllegalStateException("AI 응답이 비어있습니다.")

        val cleaned = text
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return gson.fromJson(cleaned, AiReviewSummary::class.java)
    }

    private fun buildPrompt(reviews: List<String>): String {
        val clipped = reviews
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(60)
            .joinToString("\n") { "- $it" }

        return """
        너는 한국어 리뷰 요약 전문가다.
        아래 "리뷰 목록"을 바탕으로 짧게 요약해라.
        
        규칙:
        - 반드시 JSON만 출력한다. (설명/마크다운/코드블록 금지)
        - pros/cons: 각각 1문장 (너무 길면 안 됨)
        - keywords: 3~6개 (일반적인 단어 제외)
        
        출력 스키마:
        {
          "pros": "...",
          "cons": "...",
          "keywords": ["...", "..."]
        }
        
        리뷰 목록:
        $clipped
        """.trimIndent()
            }

}

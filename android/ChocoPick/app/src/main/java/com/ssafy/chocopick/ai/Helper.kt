// app/src/main/java/com/ssafy/chocopick/ai/Helper.kt
package com.ssafy.chocopick.ai

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.ssafy.chocopick.util.ModelCopier
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "ChocoPickAI"

// 안정화 설정 (LLM은 "설명/추천"에만 사용)
private const val MAX_TOKENS = 256
private const val TOP_K = 40
private const val TOP_P = 0.9f
private const val TEMP = 0.7f

typealias OnResult = (String) -> Unit
typealias OnError = (String) -> Unit

object Helper {

    private var engine: LlmInference? = null
    private val ready = AtomicBoolean(false)

    fun isReady(): Boolean = ready.get()

    fun initialize(
        context: Context,
        onReady: (String) -> Unit,
        onError: OnError
    ) {
        try {
            if (ready.get()) {
                onReady("✅ 모델 준비 완료")
                return
            }

            val modelFile = ModelCopier.copyIfNeeded(context)

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setPreferredBackend(LlmInference.Backend.CPU)
                .setMaxTokens(MAX_TOKENS)
                .build()

            engine = LlmInference.createFromOptions(context, options)

            ready.set(true)
            onReady("✅ 모델 준비 완료")

        } catch (t: Throwable) {
            Log.e(TAG, "initialize fail", t)
            onError("초기화 실패: ${t.message}")
        }
    }

    /**
     * 1) 상품/가격/매장/주소 같은 "정확 데이터"는 ruleBasedReply로 즉시 응답 (LLM 호출 X)
     * 2) 그 외(추천/설명/응대)만 LLM 동기 1회 호출 (안정)
     */
    fun chat(
        userInput: String,
        onResult: OnResult,
        onError: OnError
    ) {
        if (!ready.get() || engine == null) {
            onError("모델이 아직 준비되지 않았어요.")
            return
        }

        val input = userInput.trim()
        if (input.isEmpty()) return

        // ✅ 정확 데이터는 코드로 바로 반환 (포맷 100% 보장)
        ruleBasedReply(input)?.let { onResult(it); return }

        // ✅ 그 외만 LLM
        try {
            val prompt = buildPrompt(input)

            val localSession = LlmInferenceSession.createFromOptions(
                engine!!,
                LlmInferenceSession.LlmInferenceSessionOptions.builder()
                    .setTopK(TOP_K)
                    .setTopP(TOP_P)
                    .setTemperature(TEMP)
                    .build()
            )

            localSession.addQueryChunk(prompt)
            val text = localSession.generateResponse().trim()

            try { localSession.close() } catch (_: Throwable) {}

            if (text.isBlank()) onError("응답이 비어있어요.")
            else onResult(text)

        } catch (t: Throwable) {
            Log.e(TAG, "chat error", t)
            onError("추론 실패: ${t.message}")
        }
    }

    // ------------------------
    // Rule-based (정확 데이터 응답)
    // ------------------------
    private fun ruleBasedReply(input: String): String? {
        val q = input.replace(" ", "")

        // 강남점 주소 / 홍대점 주소 등
        if (q.contains("주소") && q.contains("점")) {
            val storeKey = extractStoreKey(q) // "강남점"
            val addr = HardcodedContext.findStoreAddress(storeKey)
            return if (addr != null) "$storeKey - $addr" else "해당 매장을 찾지 못했어요."
        }

        // 매장 전부
        if (q.contains("매장") || q.contains("지점") || q.contains("매장정보") || q.contains("어디있")) {
            return HardcodedContext.allStoresText()
        }

        // 가격 전부
        if (q.contains("가격") || q.contains("얼마") || q.contains("원")) {
            return HardcodedContext.allProductPricesText()
        }

        // 상품 목록 전부
        if (q.contains("상품") || q.contains("메뉴") || q.contains("뭐있")) {
            return HardcodedContext.allProductNamesText()
        }

        return null
    }

    private fun extractStoreKey(noSpace: String): String {
        // "강남점주소알려줘" -> "강남점"
        val idx = noSpace.indexOf("점")
        return if (idx != -1) noSpace.substring(0, idx + 1) else noSpace
    }

    // ------------------------
    // LLM prompt (짧게, 설명/추천용)
    // ------------------------
    private fun buildPrompt(userInput: String): String {
        val input = userInput.replace(Regex("\\s+"), " ").take(120)
        return buildString {
            appendLine("System: 너는 ChocoPick 안내 챗봇이야. 한국어로 2~3문장으로만 짧게 답해.")
            appendLine("System: 상품/가격/매장 목록은 앱이 직접 제공하니, 너는 설명/추천/응대만 해.")
            appendLine("User: $input")
            appendLine("Assistant:")
        }
    }

    fun close() {
        try { engine?.close() } catch (_: Throwable) {}
        engine = null
        ready.set(false)
    }
}

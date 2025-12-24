package com.ssafy.chocopick.ai

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.ssafy.chocopick.util.ModelCopier
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "ChocoPickAI"

// 안정화 설정
private const val MAX_TOKENS = 128
private const val TOP_K = 40
private const val TOP_P = 0.9f
private const val TEMP = 0.7f
private const val HISTORY_LIMIT = 6

typealias OnResult = (String) -> Unit
typealias OnError = (String) -> Unit

object Helper {

    private var engine: LlmInference? = null
    private var session: LlmInferenceSession? = null
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
                .setPreferredBackend(LlmInference.Backend.CPU) // GPU ❌
                .setMaxTokens(MAX_TOKENS)
                .build()

            engine = LlmInference.createFromOptions(context, options)
            session = LlmInferenceSession.createFromOptions(
                engine!!,
                LlmInferenceSession.LlmInferenceSessionOptions.builder()
                    .setTopK(TOP_K)
                    .setTopP(TOP_P)
                    .setTemperature(TEMP)
                    .build()
            )

            ready.set(true)
            onReady("✅ 모델 준비 완료")

        } catch (t: Throwable) {
            Log.e(TAG, "initialize fail", t)
            onError("초기화 실패: ${t.message}")
        }
    }

    fun chat(
        history: List<Pair<String, String>>,
        userInput: String,
        onResult: OnResult,
        onError: OnError
    ) {
        if (!ready.get() || session == null) {
            onError("모델이 아직 준비되지 않았어요.")
            return
        }

        try {
            val prompt = buildPrompt(history, userInput)
            session!!.addQueryChunk(prompt)

            session!!.generateResponseAsync { partial, done ->
                if (done) onResult(partial)
            }

        } catch (t: Throwable) {
            Log.e(TAG, "chat error", t)
            onError("추론 실패: ${t.message}")
            resetSession()
        }
    }

    private fun resetSession() {
        try {
            val e = engine ?: return
            session?.close()
            session = LlmInferenceSession.createFromOptions(
                e,
                LlmInferenceSession.LlmInferenceSessionOptions.builder()
                    .setTopK(TOP_K)
                    .setTopP(TOP_P)
                    .setTemperature(TEMP)
                    .build()
            )
        } catch (_: Throwable) {}
    }

    private fun buildPrompt(
        history: List<Pair<String, String>>,
        userInput: String
    ): String {
        fun cut(s: String, n: Int) = if (s.length <= n) s else s.take(n)

        val safeHistory = history.takeLast(HISTORY_LIMIT)
        val safeInput = cut(userInput.replace(Regex("\\s+"), " "), 120)

        val sb = StringBuilder()
        sb.appendLine("System: 너는 ChocoPick 매장 안내 챗봇이야. 한국어로 짧게 답해.")
        for ((who, text) in safeHistory) {
            sb.appendLine("$who: ${cut(text, 200)}")
        }
        sb.appendLine("User: $safeInput")
        sb.append("Assistant: ")
        return sb.toString()
    }

    fun close() {
        try { session?.close() } catch (_: Throwable) {}
        try { engine?.close() } catch (_: Throwable) {}
        session = null
        engine = null
        ready.set(false)
    }
}

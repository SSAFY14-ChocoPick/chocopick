package com.ssafy.chocopick.data.source.beacon


import com.ssafy.chocopick.data.model.BeaconDistance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BeaconRepository(
    private val ds: BeaconDataSource,
    private val enterDistanceM: Double = 1.0,
    private val consecutiveHits: Int = 3,
    private val cooldownMs: Long = 180_000L,
    private val exitDistanceM: Double = 1.5, // ✅ 히스테리시스 (튐 방지)
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _enter = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    fun enterEvents(): SharedFlow<Unit> = _enter.asSharedFlow()

    // ✅ 상태 변수들 (여기가 핵심)
    private var inside = false
    private var hitCount = 0
    private var lastEmitAt = 0L
    private var entryShownOnce = false // ✅ “앱 켜둔 동안 1번만”이면 이거 true 유지

    fun start() {
        ds.start()

        scope.launch {
            ds.distances().collect { d: BeaconDistance ->
                handleDistance(d.distanceM)
            }
        }
    }

    fun stop() {
        ds.stop()
        // 앱 화면 떠 있을 때만 감지라면 stop에서 inside/hitCount만 리셋 추천
        inside = false
        hitCount = 0
        // entryShownOnce는 유지하면 “앱 켜둔 동안 1번만” 보장
        // 만약 "다시 들어오면 다시 알림"이 목표면 entryShownOnce=false 로 바꾸면 됨.
    }

    private fun handleDistance(distanceM: Double) {
        val now = System.currentTimeMillis()

        // ✅ 이미 한 번 띄웠으면 (앱 실행 중 1회만)
        if (entryShownOnce) {
            // 그래도 inside 상태는 업데이트해주면 좋음 (원하면)
            if (inside && distanceM >= exitDistanceM) inside = false
            return
        }

        // ✅ 쿨다운(혹시 future 확장 대비)
        if (now - lastEmitAt < cooldownMs) return

        // ✅ 퇴장 처리
        if (inside) {
            if (distanceM >= exitDistanceM) {
                inside = false
                hitCount = 0
            }
            return
        }

        // ✅ 입장 후보: 연속 히트 누적
        if (distanceM <= enterDistanceM) {
            hitCount++
            if (hitCount >= consecutiveHits) {
                inside = true
                lastEmitAt = now
                entryShownOnce = true // ✅ “앱 켜둔 동안 1번만”
                _enter.tryEmit(Unit)
            }
        } else {
            hitCount = 0
        }
    }
}
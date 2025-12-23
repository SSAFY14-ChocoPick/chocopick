package com.ssafy.chocopick.data.repository

//import com.ssafy.chocopick.data.source.beacon.BeaconDataSource
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.filter
//import kotlinx.coroutines.flow.onEach
//import kotlinx.coroutines.flow.map
//
//class BeaconRepository(
//    private val ds: BeaconDataSource,
//    private val enterDistanceM: Double = 1.0,
//    private val consecutiveHits: Int = 3,          // ✅ 3번 연속 1m 이내
//    private val cooldownMs: Long = 3 * 60 * 1000L   // ✅ 3분 쿨다운(원하면 줄여도 됨)
//) {
//    private var hitCount = 0
//    private var lastNotifiedAt = 0L
//
//    fun start() = ds.start()
//    fun stop() = ds.stop()
//
//    fun enterEvents(): Flow<Unit> =
//        ds.distances()
//            .onEach { d ->
//                hitCount = if (d.distanceM <= enterDistanceM) hitCount + 1 else 0
//            }
//            .filter { d ->
//                val now = d.timestamp
//                val entered = hitCount >= consecutiveHits
//                val canNotify = (now - lastNotifiedAt) > cooldownMs
//                if (entered && canNotify) {
//                    lastNotifiedAt = now
//                    hitCount = 0
//                    true
//                } else false
//            }
//            .map { Unit }
//}

import com.ssafy.chocopick.data.source.beacon.BeaconDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class BeaconRepository(
    private val ds: BeaconDataSource,
    private val enterDistanceM: Double = 1.0,
    private val consecutiveHits: Int = 3,           // ✅ 3번 연속 1m 이내
    private val cooldownMs: Long = 3 * 60 * 1000L    // ✅ 혹시 "재알림" 정책으로 바꿀 때 사용
) {
    private var hitCount = 0

    // ✅ 핵심 상태값
    private var inside = false                // 현재 매장 안에 있다고 판단하는지
    private var entryShownOnce = false        // 앱 실행 동안 1번만 띄울지
    private var lastNotifiedAt = 0L           // 쿨다운용(옵션)

    private val exitDistanceM = 1.5           // ✅ 튐 방지(히스테리시스)

    fun start() = ds.start()
    fun stop() = ds.stop()

    fun enterEvents(): Flow<Unit> =
        ds.distances()
            .onEach { d ->
                val now = d.timestamp

                // ✅ 앱 켜둔 동안 1번만: 이미 보여줬으면 아무 것도 안 함
                if (entryShownOnce) {
                    // 그래도 inside 상태는 업데이트해서 “나갔다/들어왔다”를 기록할 수 있음(선택)
                    if (inside && d.distanceM >= exitDistanceM) {
                        inside = false
                        hitCount = 0
                    }
                    return@onEach
                }

                // ✅ 이미 inside면 ENTER 판정할 필요 없음. 나갔는지만 본다.
                if (inside) {
                    if (d.distanceM >= exitDistanceM) {
                        inside = false
                        hitCount = 0
                    }
                    return@onEach
                }

                // ✅ inside == false일 때만 "입장 후보" 누적
                hitCount = if (d.distanceM <= enterDistanceM) hitCount + 1 else 0

                val entered = hitCount >= consecutiveHits
                val canNotify = (now - lastNotifiedAt) > cooldownMs

                // ✅ 여기서 "진짜 입장" 확정
                if (entered && canNotify) {
                    inside = true
                    entryShownOnce = true          // ✅ 앱 켜둔 동안 1번만!
                    lastNotifiedAt = now
                    hitCount = 0
                }
            }
            .filter {
                // ✅ entryShownOnce가 true로 바뀌는 순간에만 emit 하고 싶다
                // (위 onEach에서 inside=true, entryShownOnce=true로 만든 직후 한 번만 통과)
                // 단, filter는 onEach의 상태 변경 이후 실행되므로 조건을 “inside && entryShownOnce”로 두면
                // 이후에도 계속 true가 될 수 있다.
                //
                // 그래서 timestamp 기반으로 "막 갱신된 순간"을 구분하는 방식이 필요하지만,
                // 가장 간단히는: onEach에서 lastNotifiedAt을 갱신했으니, 현재 이벤트의 timestamp == lastNotifiedAt 이면 emit.
                //
                // (BeaconDistance.timestamp가 System.currentTimeMillis()로 찍힌다는 전제)
                it.timestamp == lastNotifiedAt && entryShownOnce
            }
            .map { Unit }
}
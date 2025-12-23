package com.ssafy.chocopick.data.repository

import com.ssafy.chocopick.data.source.beacon.BeaconDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.map

class BeaconRepository(
    private val ds: BeaconDataSource,
    private val enterDistanceM: Double = 1.0,
    private val consecutiveHits: Int = 3,          // ✅ 3번 연속 1m 이내
    private val cooldownMs: Long = 3 * 60 * 1000L   // ✅ 3분 쿨다운(원하면 줄여도 됨)
) {
    private var hitCount = 0
    private var lastNotifiedAt = 0L

    fun start() = ds.start()
    fun stop() = ds.stop()

    fun enterEvents(): Flow<Unit> =
        ds.distances()
            .onEach { d ->
                hitCount = if (d.distanceM <= enterDistanceM) hitCount + 1 else 0
            }
            .filter { d ->
                val now = d.timestamp
                val entered = hitCount >= consecutiveHits
                val canNotify = (now - lastNotifiedAt) > cooldownMs
                if (entered && canNotify) {
                    lastNotifiedAt = now
                    hitCount = 0
                    true
                } else false
            }
            .map { Unit }
}
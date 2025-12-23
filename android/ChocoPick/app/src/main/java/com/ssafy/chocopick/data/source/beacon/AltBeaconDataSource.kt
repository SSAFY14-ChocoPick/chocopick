package com.ssafy.chocopick.data.source.beacon

import android.content.Context
import android.util.Log
import com.ssafy.chocopick.data.model.BeaconDistance
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.RangeNotifier
import org.altbeacon.beacon.Region

class AltBeaconDataSource(context: Context) : BeaconDataSource {

    private val appCtx = context.applicationContext
    private val bm = BeaconManager.getInstanceForApplication(appCtx)

    private val _flow = MutableSharedFlow<BeaconDistance>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override fun distances() = _flow.asSharedFlow()

    // ✅ 비콘 1개만 쓴다 했으니 “아무 비콘이나” ranging 해서 가장 가까운 1개만 사용
    private val storeRegion = Region(
        "chocopick-store",
        Identifier.parse("fda50693-a4e2-4fb1-afcf-c6eb07647825"),
        Identifier.parse("10004"),
        Identifier.parse("54480")
    )

    private val notifier = RangeNotifier { beacons: Collection<Beacon>, _ ->
        val nearest = beacons.minByOrNull { it.distance } ?: return@RangeNotifier
        Log.d("Beacon", "distance=${nearest.distance} rssi=${nearest.rssi}")
        _flow.tryEmit(
            BeaconDistance(
                distanceM = nearest.distance,
                rssi = nearest.rssi
            )
        )
    }

    private var started = false

    override fun start() {
        if (started) return
        started = true
        bm.addRangeNotifier(notifier)
        bm.startRangingBeacons(storeRegion)
    }

    override fun stop() {
        if (!started) return
        started = false
        bm.stopRangingBeacons(storeRegion)
        bm.removeRangeNotifier(notifier)
    }
}
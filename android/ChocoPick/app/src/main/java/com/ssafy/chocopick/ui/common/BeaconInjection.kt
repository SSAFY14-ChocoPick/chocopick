package com.ssafy.chocopick.ui.common

import android.app.Application
import com.ssafy.chocopick.data.repository.BeaconRepository
import com.ssafy.chocopick.data.source.beacon.AltBeaconDataSource

object BeaconInjection {
    fun provideRepo(app: Application): BeaconRepository {
        val ds = AltBeaconDataSource(app)
        return BeaconRepository(
            ds = ds,
            enterDistanceM = 1.0,
            consecutiveHits = 3,
            cooldownMs = 3 * 60 * 1000L
        )
    }
}
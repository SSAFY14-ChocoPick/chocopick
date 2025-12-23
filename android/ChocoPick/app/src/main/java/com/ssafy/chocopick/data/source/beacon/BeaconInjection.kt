package com.ssafy.chocopick.data.source.beacon

import com.ssafy.chocopick.data.repository.BeaconRepository


object BeaconInjection {
    fun provideRepo(app: android.app.Application): BeaconRepository {
        val ds = AltBeaconDataSource(app)
        return BeaconRepository(
            ds = ds,
            enterDistanceM = 1.0,
            consecutiveHits = 3,
            cooldownMs = 3 * 60 * 1000L
        )
    }
}

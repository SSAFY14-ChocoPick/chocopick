package com.ssafy.chocopick.data.source.beacon

import com.ssafy.chocopick.data.model.BeaconDistance
import kotlinx.coroutines.flow.Flow

interface BeaconDataSource {

    fun distances(): Flow<BeaconDistance>
    fun start()
    fun stop()
}
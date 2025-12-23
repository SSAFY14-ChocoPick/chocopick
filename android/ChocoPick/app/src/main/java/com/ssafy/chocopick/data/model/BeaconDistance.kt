package com.ssafy.chocopick.data.model

data class BeaconDistance(
    val distanceM: Double,
    val rssi: Int,
    val timestamp: Long = System.currentTimeMillis()
)
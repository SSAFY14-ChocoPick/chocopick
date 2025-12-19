package com.ssafy.chocopick.data.model

data class Store(
    val name: String = "",
    val address: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val nfcRequired: Boolean = false,
    val region: String = ""
)

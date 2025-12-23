package com.ssafy.chocopick

import android.app.Application
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser

class ChocoPickApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val bm = BeaconManager.getInstanceForApplication(this)
        bm.beaconParsers.clear()
        bm.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
    }
}
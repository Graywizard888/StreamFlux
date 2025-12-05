package com.streamflux

import android.app.Application

class StreamFluxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("streamflux-native")
    }
}

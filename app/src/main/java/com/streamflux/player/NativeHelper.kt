package com.streamflux.player

object NativeHelper {
    external fun isHardwareAccelerationSupported(): Boolean
    external fun getOptimalBufferSize(width: Int, height: Int): Int
}

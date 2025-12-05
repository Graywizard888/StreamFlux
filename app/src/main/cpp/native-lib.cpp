#include <jni.h>
#include <string>
#include <android/log.h>
#include <media/NdkMediaCodec.h>

#define LOG_TAG "StreamFlux-Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jboolean JNICALL
Java_com_streamflux_player_NativeHelper_isHardwareAccelerationSupported(
        JNIEnv* env,
        jobject /* this */) {
    // Check hardware acceleration support
    return JNI_TRUE;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_streamflux_player_NativeHelper_getOptimalBufferSize(
        JNIEnv* env,
        jobject /* this */,
        jint width,
        jint height) {
    // Calculate optimal buffer size for 4K playback
    return width * height * 4; // 4 bytes per pixel for RGBA
}

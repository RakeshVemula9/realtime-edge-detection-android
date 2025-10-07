#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>

#define LOG_TAG "NativeImageProcessor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

using namespace cv;

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_cameraimageprocessor_NativeImageProcessor_applyGrayscaleNative(
        JNIEnv *env,
        jobject /* this */,
        jobject bitmap) {

    AndroidBitmapInfo info;
    void *pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
        LOGI("Failed to get bitmap info");
        return;
    }

    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        LOGI("Failed to lock pixels");
        return;
    }

    // Create OpenCV Mat from bitmap
    Mat mat(info.height, info.width, CV_8UC4, pixels);

    // Convert to grayscale using OpenCV
    cvtColor(mat, mat, COLOR_RGBA2GRAY);
    cvtColor(mat, mat, COLOR_GRAY2RGBA);

    AndroidBitmap_unlockPixels(env, bitmap);
    LOGI("✓ OpenCV grayscale applied successfully");
}

JNIEXPORT void JNICALL
Java_com_example_cameraimageprocessor_NativeImageProcessor_applyEdgeDetectionNative(
        JNIEnv *env,
        jobject /* this */,
        jobject inputBitmap,
        jobject outputBitmap) {

    AndroidBitmapInfo info;
    void *inputPixels, *outputPixels;

    if (AndroidBitmap_getInfo(env, inputBitmap, &info) < 0) return;
    if (AndroidBitmap_lockPixels(env, inputBitmap, &inputPixels) < 0) return;
    if (AndroidBitmap_lockPixels(env, outputBitmap, &outputPixels) < 0) {
        AndroidBitmap_unlockPixels(env, inputBitmap);
        return;
    }

    // Create OpenCV Mats
    Mat input(info.height, info.width, CV_8UC4, inputPixels);
    Mat output(info.height, info.width, CV_8UC4, outputPixels);
    Mat gray, edges;

    // Convert to grayscale using OpenCV
    cvtColor(input, gray, COLOR_RGBA2GRAY);

    // Apply Canny edge detection using OpenCV
    Canny(gray, edges, 50, 150);

    // Convert back to RGBA
    cvtColor(edges, output, COLOR_GRAY2RGBA);

    AndroidBitmap_unlockPixels(env, inputBitmap);
    AndroidBitmap_unlockPixels(env, outputBitmap);
    LOGI("✓ OpenCV Canny edge detection applied successfully");
}

} // extern "C"
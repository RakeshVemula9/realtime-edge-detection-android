package com.example.cameraimageprocessor;

import android.graphics.Bitmap;
import android.util.Log;

public class NativeImageProcessor {

    private static final String TAG = "NativeImageProcessor";

    static {
        try {
            System.loadLibrary("opencv_java4");
            System.loadLibrary("imageprocessor");
            Log.d(TAG, "Native libraries loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load native libraries: " + e.getMessage());
        }
    }

    public static native void applyGrayscaleNative(Bitmap bitmap);
    public static native void applyEdgeDetectionNative(Bitmap inputBitmap, Bitmap outputBitmap);
    public static native void applyBrightnessNative(Bitmap bitmap, int brightnessValue);
    public static native void applyContrastNative(Bitmap bitmap, float contrastValue);
    public static native void applyBlurNative(Bitmap bitmap);

    public static Bitmap processWithNative(Bitmap input, ImageProcessor.FilterType filter) {
        if (input == null) return null;

        Bitmap output = input.copy(input.getConfig(), true);

        try {
            switch (filter) {
                case GRAYSCALE:
                    applyGrayscaleNative(output);
                    break;

                case EDGE_DETECTION:
                    applyEdgeDetectionNative(input, output);
                    break;

                case BRIGHTNESS:
                    applyBrightnessNative(output, 50);
                    break;

                case CONTRAST:
                    applyContrastNative(output, 1.5f);
                    break;

                case INVERT:
                    // Keep Java implementation for invert
                    return ImageProcessor.applyFilter(input, filter);

                default:
                    return input;
            }

            Log.d(TAG, "Successfully processed image with OpenCV: " + filter);

        } catch (Exception e) {
            Log.e(TAG, "Error processing image: " + e.getMessage());
            return input;
        }

        return output;
    }
}
package com.example.cameraimageprocessor;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ImageProcessor {

    public enum FilterType {
        NONE,
        GRAYSCALE,
        BRIGHTNESS,
        CONTRAST,
        INVERT,
        EDGE_DETECTION
    }

    public static Bitmap applyFilter(Bitmap input, FilterType filter) {
        if (input == null) return null;

        // Use native processing for grayscale and edge detection
        if (filter == FilterType.GRAYSCALE || filter == FilterType.EDGE_DETECTION) {
            return NativeImageProcessor.processWithNative(input, filter);
        }

        Bitmap output = Bitmap.createBitmap(input.getWidth(), input.getHeight(), input.getConfig());

        int width = input.getWidth();
        int height = input.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = input.getPixel(x, y);
                int newPixel = processPixel(pixel, filter);
                output.setPixel(x, y, newPixel);
            }
        }

        return output;
    }

    private static int processPixel(int pixel, FilterType filter) {
        int r = Color.red(pixel);
        int g = Color.green(pixel);
        int b = Color.blue(pixel);
        int a = Color.alpha(pixel);

        switch (filter) {
            case GRAYSCALE:
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                return Color.argb(a, gray, gray, gray);

            case BRIGHTNESS:
                int brighten = 50;
                r = clamp(r + brighten);
                g = clamp(g + brighten);
                b = clamp(b + brighten);
                return Color.argb(a, r, g, b);

            case CONTRAST:
                float contrast = 1.5f;
                r = clamp((int)((r - 128) * contrast + 128));
                g = clamp((int)((g - 128) * contrast + 128));
                b = clamp((int)((b - 128) * contrast + 128));
                return Color.argb(a, r, g, b);

            case INVERT:
                return Color.argb(a, 255 - r, 255 - g, 255 - b);

            case NONE:
            default:
                return pixel;
        }
    }

    private static Bitmap applyEdgeDetection(Bitmap input) {
        int width = input.getWidth();
        int height = input.getHeight();

        // First convert to grayscale
        int[][] grayPixels = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = input.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                grayPixels[x][y] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            }
        }

        // Sobel operators
        int[][] sobelX = {
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
        };

        int[][] sobelY = {
                {-1, -2, -1},
                { 0,  0,  0},
                { 1,  2,  1}
        };

        Bitmap output = Bitmap.createBitmap(width, height, input.getConfig());

        // Apply Sobel operator
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                int gx = 0;
                int gy = 0;

                // Convolve with Sobel kernels
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int pixel = grayPixels[x + i][y + j];
                        gx += pixel * sobelX[i + 1][j + 1];
                        gy += pixel * sobelY[i + 1][j + 1];
                    }
                }

                // Calculate gradient magnitude
                int magnitude = (int) Math.sqrt(gx * gx + gy * gy);
                magnitude = clamp(magnitude);

                // Invert for better visibility (white edges on black background)
                magnitude = 255 - magnitude;

                output.setPixel(x, y, Color.rgb(magnitude, magnitude, magnitude));
            }
        }

        return output;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
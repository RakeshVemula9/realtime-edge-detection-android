package com.example.cameraimageprocessor;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ImageProcessor {

    public enum FilterType {
        NONE,
        GRAYSCALE,
        BRIGHTNESS,
        CONTRAST,
        INVERT
    }

    public static Bitmap applyFilter(Bitmap input, FilterType filter) {
        if (input == null) return null;

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

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
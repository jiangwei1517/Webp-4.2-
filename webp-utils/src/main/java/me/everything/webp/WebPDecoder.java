package me.everything.webp;

import java.nio.ByteBuffer;

import android.graphics.Bitmap;

public class WebPDecoder {
    private static WebPDecoder instance = null;

    private WebPDecoder() {
        System.loadLibrary("webp_evme");
    }

    public static WebPDecoder getInstance() {
        if (instance == null) {
            synchronized (WebPDecoder.class) {
                if (instance == null) {
                    instance = new WebPDecoder();
                }
            }
        }

        return instance;
    }

    public Bitmap decodeWebP(byte[] encoded) {
        return decodeWebP(encoded, 0, 0);
    }

    public Bitmap decodeWebP(byte[] encoded, int w, int h) {
        int[] width = new int[]{w};
        int[] height = new int[]{h};

        byte[] decoded = decodeRGBAnative(encoded, encoded.length, width, height);
        if (decoded.length == 0) return null;

        int[] pixels = new int[decoded.length / 4];
        ByteBuffer.wrap(decoded).asIntBuffer().get(pixels);

        return Bitmap.createBitmap(pixels, width[0], height[0], Bitmap.Config.ARGB_8888);
    }

    public static native byte[] decodeRGBAnative(byte[] encoded, long encodedLength, int[] width, int[] height);
}

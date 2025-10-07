package com.example.cameraimageprocessor;

import android.util.Log;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageUploader {

    private static final String TAG = "ImageUploader";
    private static final String SERVER_URL = "http://192.168.105.166:3000/upload";

    public interface OnUploadListener {
        void onSuccess();
        void onFailure(String error);
    }

    public static void uploadImage(File imageFile, OnUploadListener listener) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                String boundary = "*****" + System.currentTimeMillis() + "*****";
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes("--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\"" +
                        imageFile.getName() + "\"\r\n");
                dos.writeBytes("Content-Type: image/jpeg\r\n\r\n");

                FileInputStream fis = new FileInputStream(imageFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
                fis.close();

                dos.writeBytes("\r\n--" + boundary + "--\r\n");
                dos.flush();
                dos.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    listener.onSuccess();
                    Log.d(TAG, "✓ Upload successful: " + imageFile.getName());
                } else {
                    listener.onFailure("Server returned: " + responseCode);
                    Log.e(TAG, "✗ Upload failed with code: " + responseCode);
                }

            } catch (Exception e) {
                listener.onFailure(e.getMessage());
                Log.e(TAG, "✗ Upload exception: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}
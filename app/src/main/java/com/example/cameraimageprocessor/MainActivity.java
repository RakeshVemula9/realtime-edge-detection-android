package com.example.cameraimageprocessor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 10;

    private PreviewView previewView;
    private Button captureButton;
    private Spinner filterSpinner;
    private TextView statusText;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    private ImageProcessor.FilterType selectedFilter = ImageProcessor.FilterType.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        captureButton = findViewById(R.id.captureButton);
        filterSpinner = findViewById(R.id.filterSpinner);
        statusText = findViewById(R.id.statusText);

        setupFilterSpinner();

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (checkPermissions()) {
            startCamera();
        } else {
            requestPermissions();
        }

        captureButton.setOnClickListener(v -> captureAndProcessImage());
    }

    private void setupFilterSpinner() {
        String[] filters = {"None", "Grayscale", "Brightness", "Contrast", "Invert"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, filters);
        filterSpinner.setAdapter(adapter);

        filterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedFilter = ImageProcessor.FilterType.values()[position];
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                statusText.setText("Camera ready - Select filter and capture");

            } catch (Exception e) {
                statusText.setText("Error: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureAndProcessImage() {
        if (imageCapture == null) return;

        statusText.setText("Capturing...");
        captureButton.setEnabled(false);

        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + "_original.jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        processImage(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        runOnUiThread(() -> {
                            statusText.setText("Capture failed: " + exception.getMessage());
                            captureButton.setEnabled(true);
                        });
                    }
                });
    }

    private void processImage(File originalFile) {
        try {
            Bitmap original = BitmapFactory.decodeFile(originalFile.getAbsolutePath());
            Bitmap processed = ImageProcessor.applyFilter(original, selectedFilter);

            String filename = originalFile.getName().replace("_original", "_processed");
            File processedFile = new File(originalFile.getParent(), filename);

            FileOutputStream out = new FileOutputStream(processedFile);
            processed.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            runOnUiThread(() -> {
                statusText.setText("Saved: " + processedFile.getName());
                captureButton.setEnabled(true);
                Toast.makeText(MainActivity.this,
                        "Images saved to Pictures folder", Toast.LENGTH_LONG).show();
            });

        } catch (Exception e) {
            runOnUiThread(() -> {
                statusText.setText("Processing failed: " + e.getMessage());
                captureButton.setEnabled(true);
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
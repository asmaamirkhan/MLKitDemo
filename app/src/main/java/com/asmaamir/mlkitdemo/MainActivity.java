package com.asmaamir.mlkitdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    private static TextureView tv;
    private static final String TAG = "MAIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.texture_view);
        if (allPermissionsGranted()) {
            tv.post(this::startCamera);
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION);
        }
        tv.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            updateTransform();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (allPermissionsGranted()) {
                tv.post(this::startCamera);
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void startCamera() {
        PreviewConfig pc = new PreviewConfig
                .Builder()
                .setTargetResolution(new Size(640, 480))
                .build();
        Preview preview = new Preview(pc);
        preview.setOnPreviewOutputUpdateListener(output -> {
            ViewGroup vg = (ViewGroup) tv.getParent();
            vg.removeView(tv);
            vg.addView(tv, 0);
            tv.setSurfaceTexture(output.getSurfaceTexture());
            updateTransform();
        });

        ImageCaptureConfig icc = new ImageCaptureConfig
                .Builder()
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .build();
        ImageCapture imgCap = new ImageCapture(icc);
        ImageButton ib = findViewById(R.id.img_cap);
        ib.setOnClickListener(v -> {
            File file = new File(getExternalMediaDirs()[0],  System.currentTimeMillis() + ".jpg");
            imgCap.takePicture(file, (command -> {
                command.run();
            }), new ImageCapture.OnImageSavedListener() {
                @Override
                public void onImageSaved(@NonNull File file) {
                    //Toast.makeText(getBaseContext(), "DONE", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, file.getAbsolutePath());
                }

                @Override
                public void onError(@NonNull ImageCapture.ImageCaptureError imageCaptureError, @NonNull String message, @Nullable Throwable cause) {
                    //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, message);
                }
            });
        });


        CameraX.bindToLifecycle(this, preview, imgCap);
    }

    private void updateTransform() {
        Matrix mat = new Matrix();
        float centerX = tv.getWidth() / 2.0f;
        float centerY = tv.getHeight() / 2.0f;

        float rotationDegrees;
        switch (tv.getDisplay().getRotation()) {
            case Surface.ROTATION_0:
                rotationDegrees = 0;
                break;
            case Surface.ROTATION_90:
                rotationDegrees = 90;
                break;
            case Surface.ROTATION_180:
                rotationDegrees = 180;
                break;
            case Surface.ROTATION_270:
                rotationDegrees = 270;
                break;
            default:
                return;
        }
        mat.postRotate(rotationDegrees, centerX, centerY);
        tv.setTransform(mat);

    }
}
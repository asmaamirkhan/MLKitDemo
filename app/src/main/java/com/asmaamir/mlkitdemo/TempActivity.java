package com.asmaamir.mlkitdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TempActivity extends AppCompatActivity {

    public static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    public static final int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    TextureView tv;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);
        tv = findViewById(R.id.texture_view_temp);
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }


    }

    private void startCamera() {
        CameraX.unbindAll();

        //Rational aspectRatio = new Rational(tv.getWidth(), tv.getHeight());
        Size screen = new Size(tv.getWidth(), tv.getHeight());

        PreviewConfig pc = new PreviewConfig.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).setTargetResolution(screen).build();
        Preview preview = new Preview(pc);
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(@NonNull Preview.PreviewOutput output) {
                ViewGroup vg = (ViewGroup) tv.getParent();
                vg.removeView(tv);
                vg.addView(tv, 0);
                tv.setSurfaceTexture(output.getSurfaceTexture());
                //updateTransform();
            }
        });
        ImageCaptureConfig icc = new ImageCaptureConfig
                .Builder()
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .build();
        final ImageCapture cap = new ImageCapture(icc);
        findViewById(R.id.img_cap_temp).setOnClickListener(v -> {
            File file = new File(Environment.getExternalStorageState() + "/" + System.currentTimeMillis() + ".png");
            cap.takePicture(file, (command -> {
                        command.run();
                    }),
                    new ImageCapture.OnImageSavedListener() {
                        @Override
                        public void onImageSaved(@NonNull File file) {
                            String msg = "Image captured at: " + file.getAbsolutePath();
                            Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(@NonNull ImageCapture.ImageCaptureError imageCaptureError, @NonNull String message, @Nullable Throwable cause) {
                            String msg = "Pic capture failed : " + message;
                            Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                            if (cause != null) {
                                cause.printStackTrace();
                            }
                        }
                    });
        });
        CameraX.bindToLifecycle((LifecycleOwner) this, preview, cap);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            startCamera();
        } else {
            Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
            finish();
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

    private void initKitOptions() {
        FirebaseVisionFaceDetectorOptions realTime = new FirebaseVisionFaceDetectorOptions
                .Builder()
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                .build();
        FirebaseVisionImage img;
        try {
            img = FirebaseVisionImage.fromFilePath(this, Uri.parse("https://drive.google.com/file/d/1s7H6bY3K6ok9LzWAVbdAkN9kOUIcappz/view?usp=sharing"));
            FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(realTime);
            Task<List<FirebaseVisionFace>> faces = detector.detectInImage(img).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                @Override
                public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(String cameraID, Activity activity, Context context) throws CameraAccessException {
        int result;
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);
        CameraManager cm = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cm.getCameraCharacteristics(cameraID).get(CameraCharacteristics.SENSOR_ORIENTATION);
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;
        switch (rotationCompensation) {
            case 0:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                break;
            case 90:
                result = FirebaseVisionImageMetadata.ROTATION_90;
                break;
            case 180:
                result = FirebaseVisionImageMetadata.ROTATION_180;
                break;
            case 270:
                result = FirebaseVisionImageMetadata.ROTATION_270;
                break;
            default:
                result = FirebaseVisionImageMetadata.ROTATION_0;
        }
        return result;
    }
}
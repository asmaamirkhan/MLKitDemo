package com.asmaamir.mlkitdemo.RealTimeObjectDetection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;

import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;

import java.util.Arrays;
import java.util.List;

public class MLKitObjectsAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "MLKitObjectsAnalyzer";
    private FirebaseVisionObjectDetector objectDetector;
    private TextureView tv;
    private ImageView iv;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint linePaint;
    private float widthScaleFactor = 1.0f;
    private float heightScaleFactor = 1.0f;
    private FirebaseVisionImage fbImage;
    private CameraX.LensFacing lens;

    MLKitObjectsAnalyzer(TextureView tv, ImageView iv, CameraX.LensFacing lens) {
        this.tv = tv;
        this.iv = iv;
        this.lens = lens;
    }


    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
        if (image == null || image.getImage() == null) {
            return;
        }
        int rotation = degreesToFirebaseRotation(rotationDegrees);
        fbImage = FirebaseVisionImage.fromMediaImage(image.getImage(), rotation);
        initDrawingUtils();
        initDetector();
        detectObjects();
    }

    private void initDrawingUtils() {
        bitmap = Bitmap.createBitmap(tv.getWidth(), tv.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        linePaint = new Paint();
        linePaint.setColor(Color.CYAN);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3f);
        linePaint.setTextSize(40);
        widthScaleFactor = canvas.getWidth() / (fbImage.getBitmap().getWidth() * 1.0f);
        heightScaleFactor = canvas.getHeight() / (fbImage.getBitmap().getHeight() * 1.0f);
    }

    private void initDetector() {
        FirebaseVisionObjectDetectorOptions detectorOptions = new FirebaseVisionObjectDetectorOptions
                .Builder()
                .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .build();
        objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(detectorOptions);
    }

    private void detectObjects() {
        objectDetector.processImage(fbImage).addOnSuccessListener(firebaseVisionObjects -> {
            if (!firebaseVisionObjects.isEmpty()) {
                processObjects(firebaseVisionObjects);
            } else {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
            }
        }).addOnFailureListener(e -> {

        });
    }

    private void processObjects(List<FirebaseVisionObject> firebaseVisionObjects) {
        List<String> classes = Arrays.asList("CATEGORY_UNKNOWN", "CATEGORY_HOME_GOOD",
                "CATEGORY_FASHION_GOOD", "CATEGORY_FOOD",
                "CATEGORY_PLACE", "CATEGORY_PLANT");
        Log.i(TAG, "Size: " + firebaseVisionObjects.size());
        for (FirebaseVisionObject object : firebaseVisionObjects) {
            Log.i(TAG, object.getClassificationCategory() + "");
            Rect box = new Rect((int) translateX(object.getBoundingBox().left),
                    (int) translateY(object.getBoundingBox().top),
                    (int) translateX(object.getBoundingBox().right),
                    (int) translateY(object.getBoundingBox().bottom));
            canvas.drawRect(box, linePaint);
            canvas.drawText(String.format("%s %.2f",
                    classes.get(object.getClassificationCategory()),
                    object.getClassificationConfidence() == null ? 0 : object.getClassificationConfidence()),
                    translateX(object.getBoundingBox().centerX()),
                    translateY(object.getBoundingBox().centerY()),
                    linePaint);
        }
        iv.setImageBitmap(bitmap);
    }

    private float translateY(float y) {
        return y * heightScaleFactor;
    }

    private float translateX(float x) {
        float scaledX = x * widthScaleFactor;
        if (lens == CameraX.LensFacing.FRONT) {
            return canvas.getWidth() - scaledX;
        } else {
            return scaledX;
        }
    }

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException("Rotation must be 0, 90, 180, or 270.");
        }
    }
}

package com.asmaamir.mlkitdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;
import android.view.TextureView;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

public class MLKitAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "MLKitAnalyzer";
    private FirebaseVisionFaceDetectorOptions detectorOptions;
    private FirebaseVisionFaceDetector faceDetector;
    private Context context;
    private TextureView tv;
    private Image img;
    private GraphicOverlay graphicOverlay;

    MLKitAnalyzer(Context context, TextureView tv, GraphicOverlay graphicOverlay) {
        this.context = context;
        this.tv = tv;
        this.graphicOverlay = graphicOverlay;
    }

    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
        if (image == null || image.getImage() == null) {
            return;
        }
        img = image.getImage();
        int rotation = degreesToFirebaseRotation(rotationDegrees);
        FirebaseVisionImage fbImage = FirebaseVisionImage.fromMediaImage(img, rotation);
        initDetector();
        detectFaces(fbImage);
    }

    private void detectFaces(FirebaseVisionImage fbImage) {
        Bitmap overlay = Bitmap.createBitmap(tv.getWidth(), tv.getHeight(), Bitmap.Config.ARGB_8888);
        Task<List<FirebaseVisionFace>> result = faceDetector
                .detectInImage(fbImage)
                .addOnSuccessListener(firebaseVisionFaces -> {
                    Log.i(TAG, "" + firebaseVisionFaces.size());
                    if (!firebaseVisionFaces.isEmpty()) {
                        Paint paint = new Paint();
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setAntiAlias(true);
                        paint.setColor(Color.RED);
                        paint.setStrokeWidth(10f);
                        for (FirebaseVisionFace face : firebaseVisionFaces) {

                            Rect bounds = face.getBoundingBox();
                            RectOverlay rectOverlay = new RectOverlay(graphicOverlay, bounds);
                            graphicOverlay.add(rectOverlay);
                            /*float rotZ = face.getHeadEulerAngleZ();
                            float rotY = face.getHeadEulerAngleY();
                            FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                            if (leftEar != null) {
                                FirebaseVisionPoint leftEarPos = leftEar.getPosition();
                            }
                            List<FirebaseVisionPoint> leftEyeContour = face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints();
                            List<FirebaseVisionPoint> upperLipContour = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints();*/

                        }
                    }
                }).addOnFailureListener(e -> {
                    Log.i(TAG, e.getMessage());
                });
    }

    private void initDetector() {
        detectorOptions = new FirebaseVisionFaceDetectorOptions
                .Builder()
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .build();
        faceDetector = FirebaseVision
                .getInstance()
                .getVisionFaceDetector(detectorOptions);

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

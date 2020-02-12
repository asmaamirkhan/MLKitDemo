package com.asmaamir.mlkitdemo;

import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.util.List;

public class MLKitAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "MLKitAnalyzer";
    private FirebaseVisionFaceDetectorOptions detectorOptions;
    private FirebaseVisionFaceDetector faceDetector;

    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
        if (image == null || image.getImage() == null) {
            return;
        }
        Image img = image.getImage();
        int rotation = degreesToFirebaseRotation(rotationDegrees);
        FirebaseVisionImage fbImage = FirebaseVisionImage.fromMediaImage(img, rotation);
        initDetector();
        detectFaces(fbImage);
    }

    private void detectFaces(FirebaseVisionImage fbImage) {
        Task<List<FirebaseVisionFace>> result = faceDetector
                .detectInImage(fbImage)
                .addOnSuccessListener(firebaseVisionFaces -> {
                    Log.i(TAG, "" + firebaseVisionFaces.size());
                    for (FirebaseVisionFace face : firebaseVisionFaces) {
                        Rect bounds = new Rect();
                        bounds.set(face.getBoundingBox());
                        float rotZ = face.getHeadEulerAngleZ();
                        float rotY = face.getHeadEulerAngleY();
                        FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                        if (leftEar != null) {
                            FirebaseVisionPoint leftEarPos = leftEar.getPosition();
                        }
                        List<FirebaseVisionPoint> leftEyeContour = face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints();
                        List<FirebaseVisionPoint> upperLipContour = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints();

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

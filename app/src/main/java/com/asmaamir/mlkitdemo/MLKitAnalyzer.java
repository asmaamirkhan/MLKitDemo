package com.asmaamir.mlkitdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.Image;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;

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

import java.util.List;

public class MLKitAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "MLKitAnalyzer";
    private FirebaseVisionFaceDetectorOptions detectorOptions;
    private FirebaseVisionFaceDetector faceDetector;
    private Context context;
    private TextureView tv;
    private Image img;
    private ImageView iv;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint dotPaint, linePaint;

    MLKitAnalyzer(Context context, TextureView tv, ImageView iv) {
        this.context = context;
        this.tv = tv;
        this.iv = iv;
    }

    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
        if (image == null || image.getImage() == null) {
            return;
        }
        img = image.getImage();
        int rotation = degreesToFirebaseRotation(rotationDegrees);
        FirebaseVisionImage fbImage = FirebaseVisionImage.fromMediaImage(img, rotation);
        initDrawingUtils();
        initDetector();
        detectFaces(fbImage);
    }

    private void initDrawingUtils() {
        bitmap = Bitmap.createBitmap(iv.getWidth(), iv.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        dotPaint = new Paint();
        dotPaint.setColor(Color.RED);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setStrokeWidth(2f);
        dotPaint.setAntiAlias(true);
        linePaint = new Paint();
        linePaint.setColor(Color.GREEN);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2f);
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

    private void detectFaces(FirebaseVisionImage fbImage) {
        Bitmap overlay = Bitmap.createBitmap(tv.getWidth(), tv.getHeight(), Bitmap.Config.ARGB_8888);
        Task<List<FirebaseVisionFace>> result = faceDetector
                .detectInImage(fbImage)
                .addOnSuccessListener(firebaseVisionFaces -> {
                    Log.i(TAG, "" + firebaseVisionFaces.size());
                    if (!firebaseVisionFaces.isEmpty()) {
                        processFaces(firebaseVisionFaces);
                    }
                }).addOnFailureListener(e -> {
                    Log.i(TAG, e.getMessage());
                });
    }

    private void processFaces(List<FirebaseVisionFace> faces) {
        int counter;
        for (FirebaseVisionFace face : faces) {
            drawContours(face.getContour(FirebaseVisionFaceContour.FACE).getPoints());
            drawContours(face.getContour(FirebaseVisionFaceContour.LEFT_EYEBROW_BOTTOM).getPoints());
            drawContours(face.getContour(FirebaseVisionFaceContour.RIGHT_EYEBROW_BOTTOM).getPoints());
            drawContours(face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints());
            drawContours(face.getContour(FirebaseVisionFaceContour.RIGHT_EYE).getPoints());
            drawContours(face.getContour(FirebaseVisionFaceContour.LEFT_EYEBROW_TOP).getPoints());
            drawContours(face.getContour(FirebaseVisionFaceContour.RIGHT_EYEBROW_TOP).getPoints());
            drawContours(face.getContour(FirebaseVisionFaceContour.LOWER_LIP_BOTTOM).getPoints());
            drawContours(face.getContour(FirebaseVisionFaceContour.LOWER_LIP_TOP).getPoints());
            drawContours(face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints());
            drawContours(face.getContour(FirebaseVisionFaceContour.UPPER_LIP_TOP).getPoints());
            drawContours(face.getContour(FirebaseVisionFaceContour.NOSE_BRIDGE).getPoints());
            drawContours(face.getContour(FirebaseVisionFaceContour.NOSE_BOTTOM).getPoints());
        }
        iv.setImageBitmap(bitmap);
    }

    private void drawContours(List<FirebaseVisionPoint> points) {
        int counter = 0;
        for (FirebaseVisionPoint point : points) {
            if (counter != points.size() - 1) {
                canvas.drawLine(point.getX(), point.getY(), points.get(counter + 1).getX(), points.get(counter + 1).getY(), linePaint);
            } else {
                canvas.drawLine(point.getX(), point.getY(), points.get(0).getX(), points.get(0).getY(), linePaint);
            }
            counter++;
            canvas.drawCircle(point.getX(), point.getY(), 6, dotPaint);
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

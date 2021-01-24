package com.asmaamir.mlkitdemo.CustomModelDetection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;

public class MLKitDetectionAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "MLKitDetectionAnalyzer";
    private FirebaseVisionFaceDetector faceDetector;
    private TextureView tv;
    private ImageView iv;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint  linePaint;
    private float widthScaleFactor = 1.0f;
    private float heightScaleFactor = 1.0f;
    private float scoreThreshold = 0.5f;
    private FirebaseVisionImage fbImage;
    private CameraX.LensFacing lens;
    FirebaseModelInterpreter interpreter;
    private int DIM_BATCH_SIZE = 1;
    private int DIM_PIXEL_SIZE = 3;
    private int DIM_IMG_SIZE_X = 300;
    private int DIM_IMG_SIZE_Y = 300;
    FirebaseModelInputOutputOptions inputOutputOptions;

    MLKitDetectionAnalyzer(TextureView tv, ImageView iv, CameraX.LensFacing lens, FirebaseModelInterpreter interpreter, FirebaseModelInputOutputOptions inputOutputOptions) {
        this.tv = tv;
        this.iv = iv;
        this.lens = lens;
        this.interpreter = interpreter;
        this.inputOutputOptions = inputOutputOptions;
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
    }

    private void initDetector() {
        try {
            Bitmap bitmap = fbImage.getBitmap();
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, true);
            int batchNum = 0;
            byte[][][][] input = new byte[DIM_BATCH_SIZE][DIM_IMG_SIZE_X][DIM_IMG_SIZE_Y][DIM_PIXEL_SIZE];
            for (int x = 0; x < DIM_IMG_SIZE_X; x++) {
                for (int y = 0; y < DIM_IMG_SIZE_Y; y++) {
                    int pixel = scaledBitmap.getPixel(x, y);
                    input[batchNum][x][y][0] = (byte) (Color.red(pixel));
                    input[batchNum][x][y][1] = (byte) (Color.green(pixel));
                    input[batchNum][x][y][2] = (byte) (Color.blue(pixel));
                }
            }
            FirebaseModelInputs inputs = new FirebaseModelInputs.Builder().add(input).build();
            runDet(inputs);
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
    }


    private void runDet(FirebaseModelInputs inputs) {
        String labels[] = {"person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat", "traffic light",
                "fire hydrant", "???", "stop sign", "parking meter", "bench", "bird", "cat", "dog", "horse", "sheep", "cow",
                "elephant", "bear", "zebra", "giraffe", "???", "backpack", "umbrella", "???", "???", "handbag", "tie",
                "suitcase", "frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove",
                "skateboard", "surfboard", "tennis racket", "bottle", "???", "wine glass", "cup", "fork", "knife", "spoon",
                "bowl", "banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake",
                "chair", "couch", "potted plant", "bed", "???", "dining table", "???", "???", "toilet", "???", "tv", "laptop",
                "mouse", "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator", "???", "book",
                "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"};

        interpreter.run(inputs, inputOutputOptions).addOnSuccessListener(new OnSuccessListener<FirebaseModelOutputs>() {

            @Override
            public void onSuccess(FirebaseModelOutputs firebaseModelOutputs) {
                float[][][] locations = firebaseModelOutputs.getOutput(0);
                float[][] classes = firebaseModelOutputs.getOutput(1);
                float[][] scores = firebaseModelOutputs.getOutput(2);
                int w = fbImage.getBitmap().getWidth();
                int h = fbImage.getBitmap().getHeight();
                for (int i = 0; i < locations[0].length; i++) {
                    if (scores[0][i] > scoreThreshold) {
                        Log.i(TAG, "" + locations[0][i][0] + "  " +
                                locations[0][i][1] + "  " +
                                locations[0][i][2] + "  " +
                                locations[0][i][3]);
                        Log.i(TAG, "class:" + classes[0][i]);
                        Log.i(TAG, "score:" + scores[0][i]);

                        float top = locations[0][i][0] * h;
                        float left = locations[0][i][1] * w;
                        float bottom = locations[0][i][2] * h;
                        float right = locations[0][i][3] * w;
                        Rect box = new Rect((int) translateX(left),
                                (int) translateY(top),
                                (int) translateX(right),
                                (int) translateY(bottom));
                        canvas.drawRect(box, linePaint);

                        canvas.drawText(labels[(int) (classes[0][i])],
                                (int) left,
                                (int) top,
                                linePaint);
                        canvas.drawText(String.valueOf(scores[0][i]),
                                box.centerX(),
                                box.centerY(),
                                linePaint);
                    }

                }
                iv.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }


    private void initDrawingUtils() {
        bitmap = Bitmap.createBitmap(tv.getWidth(), tv.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        linePaint = new Paint();
        linePaint.setColor(Color.RED);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3f);
        linePaint.setTextSize(40);
        widthScaleFactor = canvas.getWidth() / (fbImage.getBitmap().getWidth() * 1.0f);
        heightScaleFactor = canvas.getHeight() / (fbImage.getBitmap().getHeight() * 1.0f);
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

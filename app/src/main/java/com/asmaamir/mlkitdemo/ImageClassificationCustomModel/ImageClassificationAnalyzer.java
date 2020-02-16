package com.asmaamir.mlkitdemo.ImageClassificationCustomModel;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;

import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ImageClassificationAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "MLKitFacesAnalyzer";
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_PIXEL_SIZE = 1;
    private static final int DIM_IMG_SIZE_X = 1;
    private static final int DIM_IMG_SIZE_Y = 1;
    private TextureView tv;
    private ImageView iv;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint dotPaint;
    private float widthScaleFactor = 1.0f;
    private float heightScaleFactor = 1.0f;
    // private FirebaseVisionImage fbImage;
    private CameraX.LensFacing lens;
    private FirebaseModelInterpreter remoteInterpreter;
    private ArrayList<String> labels;
    private ByteBuffer buffer;

    ImageClassificationAnalyzer(TextureView tv, ImageView iv, CameraX.LensFacing lens) {
        this.tv = tv;
        this.iv = iv;
        this.lens = lens;
        this.remoteInterpreter = remoteInterpreter;
        this.labels = labels;
    }


    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
        if (image == null || image.getImage() == null) {
            return;
        }
        buffer = image.getPlanes()[0].getBuffer();

        int rotation = degreesToFirebaseRotation(rotationDegrees);
        //fbImage = FirebaseVisionImage.fromMediaImage(image.getImage(), rotation);
        initDrawingUtils();
        runInference();
    }


    private void runInference() {
        FirebaseCustomRemoteModel remoteModel = new FirebaseCustomRemoteModel
                .Builder("mobilenet").build();
        FirebaseModelManager.getInstance().isModelDownloaded(remoteModel).addOnSuccessListener(aBoolean -> {
            if (aBoolean) {

                try {
                    FirebaseModelInterpreterOptions options = new FirebaseModelInterpreterOptions.Builder(remoteModel).build();
                    FirebaseModelInterpreter interpreter = FirebaseModelInterpreter.getInstance(options);

                    int[] inputs = new int[]{DIM_BATCH_SIZE, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, DIM_PIXEL_SIZE};
                    int[] outputs = new int[]{DIM_BATCH_SIZE, 1001};

                    FirebaseModelInputOutputOptions inputOutputOptions = new FirebaseModelInputOutputOptions
                            .Builder()
                            .setInputFormat(0, FirebaseModelDataType.BYTE, inputs)
                            .setOutputFormat(0, FirebaseModelDataType.BYTE, outputs)
                            .build();
                    FirebaseModelInputs inputImage = new FirebaseModelInputs.Builder().add(buffer).build();
                    if (remoteInterpreter != null) {
                        remoteInterpreter.run(inputImage, inputOutputOptions).addOnSuccessListener(firebaseModelOutputs -> {
                            float[][] modelOutputs = firebaseModelOutputs.getOutput(0);
                            Log.i(TAG, modelOutputs[0].toString());
                        }).addOnFailureListener(e -> {

                        });
                    } else {
                        Log.i(TAG, "No interpreter");
                    }
                } catch (FirebaseMLException e) {

                }


            } else {
                Log.i(TAG, "NO MODEL");
            }
        }).addOnFailureListener(e -> {

        });


    }


    private void initDrawingUtils() {
        bitmap = Bitmap.createBitmap(tv.getWidth(), tv.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        dotPaint = new Paint();
        dotPaint.setColor(Color.RED);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setStrokeWidth(2f);
        dotPaint.setAntiAlias(true);
        //widthScaleFactor = canvas.getWidth() / (b.getBitmap().getWidth() * 1.0f);
        //heightScaleFactor = canvas.getHeight() / (fbImage.getBitmap().getHeight() * 1.0f);
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

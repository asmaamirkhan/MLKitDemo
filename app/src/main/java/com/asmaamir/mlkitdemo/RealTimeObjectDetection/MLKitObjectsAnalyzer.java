package com.asmaamir.mlkitdemo.RealTimeObjectDetection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.TextureView;
import android.widget.ImageView;

import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;

public class MLKitObjectsAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "MLKitObjectsAnalyzer";
    private FirebaseVisionObjectDetector objectDetector;
    private TextureView tv;
    private ImageView iv;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint dotPaint, linePaint;
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
        
    }
}

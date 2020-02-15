package com.asmaamir.mlkitdemo.CameraX;

import android.util.Log;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

public class DemoAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "DemoAnalyzer";

    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
        long currentTimeStamp = System.currentTimeMillis();
        Log.i(TAG, "" + currentTimeStamp);
    }
}

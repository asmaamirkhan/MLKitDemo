package com.asmaamir.mlkitdemo.ImageClassificationLocalModel;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.asmaamir.mlkitdemo.R;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;

import java.nio.ByteBuffer;

public class ImageClassificationLocalModelActivity extends AppCompatActivity {
    private static final String TAG = "ImageClassificationLocalModel";
    public static final int REQUEST_CODE_PERMISSION = 111;
    public static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE"};
    private static final int PICK_IMAGE_CODE = 100;
    private ImageView imageView;
    private ImageView imageViewCanvas;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint dotPaint, linePaint;
    private TextView textView;
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_PIXEL_SIZE = 1;
    private static final int DIM_IMG_SIZE_X = 1;
    private static final int DIM_IMG_SIZE_Y = 1;
    private ByteBuffer buffer;
    FirebaseModelInterpreter interpreter;
    FirebaseModelInputOutputOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_classification_local_model);
        if (allPermissionsGranted()) {
            initViews();
            loadModel();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION);
        }
    }

    private void loadModel() {
        FirebaseCustomLocalModel localModel = new FirebaseCustomLocalModel
                .Builder()
                .setAssetFilePath("mobilenet.tflite")
                .build();

        try {
            FirebaseModelInterpreterOptions interpreterOptions = new FirebaseModelInterpreterOptions.Builder(localModel).build();
            interpreter = FirebaseModelInterpreter.getInstance(interpreterOptions);
            int[] inputs = new int[]{DIM_BATCH_SIZE, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, DIM_PIXEL_SIZE};
            int[] outputs = new int[]{DIM_BATCH_SIZE, 1001};
            options = new FirebaseModelInputOutputOptions
                    .Builder()
                    .setInputFormat(0, FirebaseModelDataType.BYTE, inputs)
                    .setOutputFormat(0, FirebaseModelDataType.BYTE, outputs)
                    .build();
        } catch (FirebaseMLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void initViews() {
        imageView = findViewById(R.id.img_view_pick_local);
        ImageButton imageButton = findViewById(R.id.img_btn_pick_local);
        imageViewCanvas = findViewById(R.id.img_view_pick_canvas_local);
        textView = findViewById(R.id.tv_props_local);
        imageButton.setOnClickListener(v -> pickImage());

    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_CODE) {
            if (data != null) {
                imageView.setImageURI(data.getData());
                textView.setText("Classes: ");
                BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                bitmap = drawable.getBitmap();
                int size = bitmap.getRowBytes() * bitmap.getHeight();
                buffer = ByteBuffer.allocate(size);
                bitmap.copyPixelsToBuffer(buffer);
                //image = FirebaseVisionImage.fromFilePath(this, Objects.requireNonNull(data.getData()));
                Log.i(TAG, data.getData().getPath());
                //BitmapFactory.decodeFile(data.getData().getPath().replace("/raw/", "")).copyPixelsToBuffer(buffer);
                runModel();

            }
        }
    }

    private void runModel() {
        try {

            FirebaseModelInputs inputImage = new FirebaseModelInputs
                    .Builder()
                    .add(buffer)
                    .build();
            Log.i(TAG, "Starting run model");
            interpreter.run(inputImage, options).addOnSuccessListener(firebaseModelOutputs -> {
                float[][] modelOutputs = firebaseModelOutputs.getOutput(0);
                Log.i(TAG, modelOutputs[0].toString());
            }).addOnFailureListener(e -> {
                Log.i(TAG, "No interpreter");
            });
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (allPermissionsGranted()) {
                initViews();
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
}
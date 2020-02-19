package com.asmaamir.mlkitdemo.CustomModelClassification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.asmaamir.mlkitdemo.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CustomModelActivityClassification extends AppCompatActivity {
    private static final String TAG = "CustomModelActivityClassification";
    private static final int PICK_IMAGE_CODE = 100;

    private ImageView imageView;
    private ImageView imageViewCanvas;
    private TextView textView;
    /**
     * Number of results to show in the UI.
     */
    private int RESULTS_TO_SHOW = 3;

    /**
     * Dimensions of inputs.
     */
    private int DIM_BATCH_SIZE = 1;
    private int DIM_PIXEL_SIZE = 3;
    private int DIM_IMG_SIZE_X = 224;
    private int DIM_IMG_SIZE_Y = 224;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_model);
        initViews();
    }

    private void initViews() {
        imageView = findViewById(R.id.img_view_pick_custom);
        ImageButton imageButton = findViewById(R.id.img_btn_pick_custom);
        imageViewCanvas = findViewById(R.id.img_view_pick_canvas_custom);
        textView = findViewById(R.id.tv_props_custom);
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
                FirebaseCustomLocalModel localModel = new FirebaseCustomLocalModel.Builder().setAssetFilePath("mobilenet.tflite").build();
                FirebaseModelInterpreter interpreter;
                try {
                    FirebaseModelInterpreterOptions interpreterOptions = new FirebaseModelInterpreterOptions.Builder(localModel).build();
                    interpreter = FirebaseModelInterpreter.getInstance(interpreterOptions);
                    FirebaseModelInputOutputOptions inputOutputOptions = new FirebaseModelInputOutputOptions
                            .Builder()
                            .setInputFormat(0, FirebaseModelDataType.BYTE, new int[]{DIM_BATCH_SIZE, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, DIM_PIXEL_SIZE})
                            .setOutputFormat(0, FirebaseModelDataType.BYTE, new int[]{DIM_BATCH_SIZE, 1001})
                            .build();
                    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

                    int batchNum = 0;
                    byte[][][][] input = new byte[1][224][224][3];
                    for (int x = 0; x < 224; x++) {
                        for (int y = 0; y < 224; y++) {
                            int pixel = bitmap.getPixel(x, y);
                            // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                            // model. For example, some models might require values to be normalized
                            // to the range [0.0, 1.0] instead.
                            input[batchNum][x][y][0] = (byte) ((Color.red(pixel) - 127) / 128.0);
                            input[batchNum][x][y][1] = (byte) ((Color.green(pixel) - 127) / 128.0);
                            input[batchNum][x][y][2] = (byte) ((Color.blue(pixel) - 127) / 128.0);
                        }
                    }
                    FirebaseModelInputs inputs = new FirebaseModelInputs.Builder().add(input).build();
                    interpreter.run(inputs, inputOutputOptions).addOnSuccessListener(new OnSuccessListener<FirebaseModelOutputs>() {
                        @Override
                        public void onSuccess(FirebaseModelOutputs firebaseModelOutputs) {
                            byte[][] output = firebaseModelOutputs.getOutput(0);
                            byte[] probabilities = output[0];
                            Log.i(TAG, "DONE");
                            Log.i(TAG, output.length + "  " + output[0].length + "   " + probabilities[0]);
                            try {
                                BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(getAssets().open("labels.txt")));
                                for (int i = 0; i < probabilities.length; i++) {
                                    String label = reader.readLine();
                                    if ((probabilities[i] & 0xff) / 255.0f != 0)
                                        Log.i("MLKit", String.format("%s: %1.4f", label, (probabilities[i] & 0xff) / 255.0f));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, e.getMessage());
                        }
                    });
                } catch (FirebaseMLException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
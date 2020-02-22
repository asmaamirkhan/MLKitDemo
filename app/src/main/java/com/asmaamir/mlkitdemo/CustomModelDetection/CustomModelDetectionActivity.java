package com.asmaamir.mlkitdemo.CustomModelDetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

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

public class CustomModelDetectionActivity extends AppCompatActivity {
    private static final String TAG = "CustomModelDetectionActivity";
    private static final int PICK_IMAGE_CODE = 100;
    private int DIM_BATCH_SIZE = 1;
    private int DIM_PIXEL_SIZE = 3;
    private int DIM_IMG_SIZE_X = 300;
    private int DIM_IMG_SIZE_Y = 300;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_model_detection);
        initViews();
    }

    private void initViews() {
        imageView = findViewById(R.id.img_view_pick_detection);
        ImageButton imageButton = findViewById(R.id.img_btn_pick_detection);
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
                FirebaseCustomLocalModel localModel = new FirebaseCustomLocalModel.Builder().setAssetFilePath("detect.tflite").build();
                FirebaseModelInterpreter interpreter;
                try {
                    FirebaseModelInterpreterOptions interpreterOptions = new FirebaseModelInterpreterOptions.Builder(localModel).build();
                    interpreter = FirebaseModelInterpreter.getInstance(interpreterOptions);
                    FirebaseModelInputOutputOptions inputOutputOptions = new FirebaseModelInputOutputOptions
                            .Builder()
                            .setInputFormat(0, FirebaseModelDataType.BYTE, new int[]{DIM_BATCH_SIZE, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, DIM_PIXEL_SIZE})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{DIM_BATCH_SIZE, 10, 4})
                            .build();
                    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    bitmap = Bitmap.createScaledBitmap(bitmap, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, true);

                    int batchNum = 0;
                    byte[][][][] input = new byte[1][DIM_IMG_SIZE_X][DIM_IMG_SIZE_Y][3];
                    for (int x = 0; x < DIM_IMG_SIZE_X; x++) {
                        for (int y = 0; y < DIM_IMG_SIZE_Y; y++) {
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
                            float[][][] output = firebaseModelOutputs.getOutput(0);

                            //float[] probabilities = output[0];
                            /*Log.i(TAG, "DONE");
                            Log.i(TAG, " " + output[0][0][0]);
                            Log.i(TAG, " " + Arrays.toString(output[0][1]));
                            Log.i(TAG, " " + Arrays.toString(output[0]));*/
                            Log.i(TAG, output.length + "  " + output[0].length + "  " + output[0][0].length);
                           /* try {
                                BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(getAssets().open("labels.txt")));
                                for (int i = 0; i < probabilities.length; i++) {
                                    String label = reader.readLine();
                                    if ((probabilities[i] & 0xff) / 255.0f != 0)
                                        Log.i("MLKit", String.format("%s: %1.4f", label, (probabilities[i] & 0xff) / 255.0f));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }*/

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (FirebaseMLException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
package com.asmaamir.mlkitdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.util.List;

public class DrawingView extends View {
    private Context context;

    private List<FirebaseVisionFace> faces;

    public DrawingView(Context context) {
        super(context);
    }

    DrawingView(Context context, List<FirebaseVisionFace> faces) {
        super(context);
        this.context = context;
        this.faces = faces;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        for (FirebaseVisionFace face : faces) {
            paint.setColor(Color.RED);
            paint.setStrokeWidth(8);
            Rect box = face.getBoundingBox();
            canvas.drawRect(box, paint);
        }
    }
}

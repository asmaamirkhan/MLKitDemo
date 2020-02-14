package com.asmaamir.mlkitdemo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class RectOverlay extends GraphicOverlay.Graphic {
    private int RECT_COLOR = Color.RED;
    private float strokeWidth = 4.0f;
    private Paint rectPaint;
    private Rect rect;
    private GraphicOverlay graphicOverlay;

    public RectOverlay(GraphicOverlay graphicOverlay, Rect rect) {
        super(graphicOverlay);
        this.graphicOverlay = graphicOverlay;
        this.rect = rect;
        rectPaint = new Paint();
        rectPaint.setColor(RECT_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(strokeWidth);
        postInvalidate();
    }


    @Override
    public void draw(Canvas canvas) {
        RectF rectF = new RectF(rect);
        rectF.left = translateX(rectF.left);
        rectF.right = translateX(rectF.right);
        rectF.top = translateY(rectF.top);
        rectF.bottom = translateY(rectF.bottom);
        canvas.drawRect(rectF, rectPaint);
    }
}
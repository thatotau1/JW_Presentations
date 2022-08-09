package com.example.pdfreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
;

import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class PaintView extends View {

    private static final float TOUCH_TOLERANCE = 3;

    private Paint mPaint;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private int currentColor;

    private ArrayList<PresentationActivity.FingerPath> paths = new ArrayList<>() ;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);


    private Path mPath;
    private float mX, mY;


    public int brushColour = Color.BLACK;
    public int brushSize = 10;
    public boolean drawingIsEnabled = true;

    public PaintView(Context context, AttributeSet set) {
        super(context, set);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);

    }

    public void init (DisplayMetrics metrics){
        int height = (int) (metrics.heightPixels);
        int width = metrics.widthPixels;


        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        currentColor = Color.GREEN;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        mCanvas.drawColor(Color.TRANSPARENT);

        for(PresentationActivity.FingerPath fp : paths){
            mPaint.setColor(fp.colour);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);
            mCanvas.drawPath(fp.path, mPaint);
        }

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {


        float x = event.getX();
        float y = event.getY();
        Log.d("On Touch","X"+x+"y"+y);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchStart(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x,y);
                Log.d("On Touch","X"+x+"y"+y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
        }
        return true;
    }

    private void touchUp() {
        mPath.lineTo(mX,mY);
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if(dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE){
            mPath.quadTo(mX, mY, (x+mX)/2, (y+mY)/2);
            mX = x;
            mY = y;
        }
        ;
    }

    private void touchStart(float x, float y) {
        mPath = new Path();
        PresentationActivity.FingerPath fp = new PresentationActivity.FingerPath(currentColor, brushSize, mPath);
        paths.add(fp);
        Log.d("Touch start","X"+x+"y"+"y");
        mPath.moveTo(x,y);


        mX = x;
        mY = y;
        
        
    }

    public void clear(){
        paths.clear();
        invalidate();
    }



}

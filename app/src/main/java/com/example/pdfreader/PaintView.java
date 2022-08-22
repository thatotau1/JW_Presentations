package com.example.pdfreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Parcelable;
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
    private static final String EXTRA_EVENT_LIST = "event_list";
    private static final String EXTRA_STATE = "instance_state";
    private ArrayList<MotionEvent> eventList = new ArrayList<MotionEvent>(100);
    private ArrayList<MotionEvent> res_eventList = new ArrayList<MotionEvent>(100);
    private Paint mPaint;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private int currentColor;

    private ArrayList<PresentationActivity.FingerPath> paths = new ArrayList<>() ;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);


    private Path mPath;
    private float mX, mY;

    PresentationActivity refActivity =  (PresentationActivity) (getContext());
    public int brushColour = Color.BLACK;
    public int brushSize = 10;
    public boolean drawingIsEnabled = true;

    public PaintView(Context context, AttributeSet set) {

        super(context, set);
        this.setSaveEnabled(true);
        this.setWillNotDraw(false);
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
        Log.d("On Draw","Drawing.....");

    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        drawingIsEnabled = refActivity.drawEnabled;

        if (drawingIsEnabled == false) {

            return false;
        }
        //getParent().requestDisallowInterceptTouchEvent(true);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                performTouchEvent(event);
                super.invalidate();
        }
        return true;
    }

    public boolean performTouchEvent(MotionEvent event) {


        float x = event.getX();
        float y = event.getY();
        Log.d("On Touch", "X" + x + "y" + y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);

                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                Log.d("On Touch", "X" + x + "y" + y);

                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
        }

        super.invalidate();
        eventList.add(MotionEvent.obtain(event));


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

    }

    private void touchStart(float x, float y) {
        mPath = new Path();
        PresentationActivity.FingerPath fp = new PresentationActivity.FingerPath(currentColor, brushSize, mPath);
        paths.add(fp);
        Log.d("Path", String.valueOf((fp)));
        mPath.moveTo(x,y);


        mX = x;
        mY = y;
        
        
    }

    public void clear(){
        paths.clear();
        invalidate();
    }

    @Override
    public Parcelable onSaveInstanceState()
    {

        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_STATE, super.onSaveInstanceState());
        bundle.putParcelableArrayList(EXTRA_EVENT_LIST, eventList);

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state)
    {


        if (state instanceof Bundle)
        {

            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable(EXTRA_STATE));
            res_eventList = bundle.getParcelableArrayList(EXTRA_EVENT_LIST);

            if (res_eventList == null) {
                res_eventList = new ArrayList<MotionEvent>(100);
            }
            for (MotionEvent event : res_eventList) {

                performTouchEvent(event);



            }
            return;
        }
        super.onRestoreInstanceState(state);
    }



}

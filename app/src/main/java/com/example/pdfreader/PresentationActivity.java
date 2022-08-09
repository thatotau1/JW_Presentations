package com.example.pdfreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.File;

public class PresentationActivity extends AppCompatActivity implements  OnPageChangeListener {
    String pdfPath = "";
    private int mCurrentPage = 0;
    PDFView pdfView;
    private PaintView paintView;
    boolean isDrawInit = false;
    private final static String KEY_CURRENT_PAGE = "current_page";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        {
            mCurrentPage = savedInstanceState.getInt(KEY_CURRENT_PAGE);
        }
        else
        {
            mCurrentPage = -1;
        }
        display();

    }

    private void display() {
        setContentView(R.layout.activity_presentation);

        pdfView = findViewById(R.id.pdfView);
        pdfPath = getIntent().getStringExtra("path");

        File file = new File(pdfPath);
        Uri path = Uri.fromFile(file);
        pdfView.fromUri(path)
                .swipeHorizontal(false)
                .onDrawAll(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
                        Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);

                        Paint paint = new Paint();
                        paint.setColor(Color.BLACK);
                        canvas.drawBitmap(bitmap, 0,0, paint);

                    }
                })
                .defaultPage(mCurrentPage)
                .onRender(new OnRenderListener() {
                    @Override
                    public void onInitiallyRendered(int nbPages, float pageWidth, float pageHeight) {
                        pdfView.fitToWidth(mCurrentPage);


                    }
                })
                .onPageChange(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .enableSwipe(true)
                .enableDoubletap(true)
                .spacing(2)
                .load();

    }



    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentPage = savedInstanceState.getInt(KEY_CURRENT_PAGE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_PAGE, mCurrentPage);

    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        mCurrentPage = page;
        setTitle(String.format("%s %s of %s", "Slide", page + 1, pageCount));
    }

    @Override
    protected void onResume() {
        super.onResume();
            initDraw();
            isDrawInit = true;

    }



    public void onInitDrawClick(View view) {
    }

    private void initDraw(){
        paintView = findViewById(R.id.paintView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        paintView.init(metrics);

    }


    static class FingerPath {
        int colour;
        int strokeWidth;
        Path path;

        FingerPath(int colour, int strokeWidth, Path path) {
            this.colour = colour;
            this.strokeWidth = strokeWidth;
            this.path = path;
            Log.d("On Touch","X"+strokeWidth+"y"+path);
        }
    }
}
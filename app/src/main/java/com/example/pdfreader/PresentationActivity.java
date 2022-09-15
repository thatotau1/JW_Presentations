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
import android.widget.Button;
import android.widget.ImageButton;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.File;

public class PresentationActivity extends AppCompatActivity implements OnPageChangeListener, View.OnClickListener {
    String pdfPath = "";
    private int mCurrentPage = 0;
    PDFView pdfView;
    private PaintView paintView;
    boolean isDrawInit = false;
    private final static String KEY_CURRENT_PAGE = "current_page";
    private final static String DRAW_ENABLED = "draw_Enabled";
    Button drawButton;
    public boolean drawEnabled;


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
        //drawEnabled = savedInstanceState.getBoolean(DRAW_ENABLED);

        setContentView(R.layout.activity_presentation);
        drawButton =(Button) findViewById(R.id.initDraw);

        drawButton.setOnClickListener(this);
        initDraw();
        ImageButton clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(this);

        ImageButton undoButton = findViewById(R.id.undoButton);
        undoButton.setOnClickListener(this);

        ImageButton redoButton = findViewById(R.id.redoButton);
        redoButton.setOnClickListener(this);

        ImageButton styleButton = findViewById(R.id.styleButton);
        styleButton.setOnClickListener(this);


        display();
    }

    private void display() {


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
    public void onPageChanged(int page, int pageCount) {
        mCurrentPage = page;
        setTitle(String.format("%s %s of %s", "Slide", page + 1, pageCount));
    }





    public void onInitDrawClick(View view) {
    }

    private void initDraw(){
        paintView = findViewById(R.id.paintView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        paintView.init(metrics);

    }

    @Override
    public void onClick(View view) {

        int viewID = view.getId();
        if (viewID == R.id.initDraw)
        {
            if (drawEnabled == false) {
                drawButton.setText("Enabled");
                drawEnabled = true;
            } else if (drawEnabled == true) {
                drawButton.setText("Disabled");
                drawEnabled = false;
            }
        }else if (viewID == R.id.clearButton)
        {
            // clear the canvas
            paintView.clear();
        } else if (viewID == R.id.undoButton)
        {
            // undo the most recent drawing action
            paintView.undo();
        } else if (viewID == R.id.redoButton)
        {
            // redraw the most recently undone action
            paintView.redo();
        } else if (viewID == R.id.styleButton)
        {
            ColourPicker dialog = new ColourPicker(PresentationActivity.this, paintView.getColour());
            dialog.setOnDialogOptionSelectedListener(new ColourPicker.ColourPickerSelectedListener() {
                @Override
                public void onColourPickerOptionSelected(int colour) {
                    paintView.setColour(colour);
                }
            });
            dialog.show();

        }
    }


    static class FingerPath {
        int colour;
        int strokeWidth;
        Path path;

        FingerPath(int colour, int strokeWidth, Path path) {
            this.colour = colour;
            this.strokeWidth = strokeWidth;
            this.path = path;

        }
        public int getColour() {
            return colour;
        }

        /**
         * Returns the width of the path to be drawn.
         * @return int - the width of the path.
         */
        public int getWidth() {
            return strokeWidth;
        }

        /**
         * Returns the path object to be drawn.
         * @return Path - the path object.
         */
        public Path getPath() {
            return path;
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {

        super.onRestoreInstanceState(savedInstanceState);
        mCurrentPage = savedInstanceState.getInt(KEY_CURRENT_PAGE);
        drawEnabled = savedInstanceState.getBoolean(DRAW_ENABLED);
        savedInstanceState.putBoolean(DRAW_ENABLED, drawEnabled);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_PAGE, mCurrentPage);
        outState.putBoolean(DRAW_ENABLED, drawEnabled);


    }

}
package com.example.pdfreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.socket.client.Socket;

public class PresentationActivity extends AppCompatActivity implements OnPageChangeListener, View.OnClickListener {
    String pdfPath = "";
    private int mCurrentPage = 0;
    public int currentColour =0;
    PDFView pdfView;
    private PaintView paintView;
    boolean isDrawInit = false;
    private final static String KEY_CURRENT_PAGE = "current_page";
    private final static String PAINT_COLOUR = "current_colour";
    private final static String DRAW_ENABLED = "draw_Enabled";
    private final static String PRESENTING = "presenting";
    Button drawButton;
    Button presentButton;
    Button stopButton;
    public boolean drawEnabled=false;
    private boolean presentiting=false;
    private boolean donePresenting;
    private Socket_Handler socketHandler;
    private Socket mSocket;
    private View presentation;
    private Screenshot screenshot;
    private Bitmap bitmap;
    private String encodedImg;
    public DisplayMetrics metrics;
    public StreamThread thread ;;
    JSONObject dataSent;


    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        {
            mCurrentPage = savedInstanceState.getInt(KEY_CURRENT_PAGE);
            currentColour= savedInstanceState.getInt(PAINT_COLOUR);
            drawEnabled = savedInstanceState.getBoolean(DRAW_ENABLED);
            presentiting = savedInstanceState.getBoolean(PRESENTING);

        }
        else
        {
            mCurrentPage = -1;
        }
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        setContentView(R.layout.activity_presentation);
        drawButton =(Button) findViewById(R.id.initDraw);
        presentButton =(Button)findViewById(R.id.Stream);
        stopButton =(Button)findViewById(R.id.Stop);

        drawButton.setOnClickListener(this);
        presentButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        presentButton.setText("Present");


        initDraw();
        initUI();

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

    private void initUI() {
        if(presentiting==true){
            presentButton.setEnabled(false);
            presentButton.setText("Presenting");
            stopButton.setEnabled(true);

        }else if (presentiting == false){
            presentButton.setEnabled(true);
            presentButton.setText("Present");
            stopButton.setEnabled(false);
        }
        if(drawEnabled==true){
            drawButton.setText("Drawing");
        }else{
            drawButton.setText("Draw");
        }

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
    class StreamThread extends Thread{
        StreamThread(){
            socketHandler = Socket_Handler.getInstance();
            socketHandler.setmSocket();
            mSocket = socketHandler.getmSocket();
        }

        public void terminate() {
            presentiting = false;
            mSocket.disconnect();
        }
        @Override
        public void run() {


            mSocket.connect();
            while (presentiting == true) {
                screenshot = new Screenshot();
                screenshot.takeScreenshot(paintView.refActivity, pdfView);
                bitmap = screenshot.getmBitmap();
                encodedImg = screenshot.encodeImage(bitmap);
                dataSent = new JSONObject();
                try {
                    dataSent.put("imageData", encodedImg);
                    mSocket.emit("image", dataSent);
                    Log.d("I dk", String.valueOf(dataSent));
                    dataSent = null;
                    encodedImg =null;
                    bitmap = null;
                    screenshot = null;
                    Thread.sleep(50);
                } catch (JSONException | InterruptedException e) {

                }
            }
        }


    }

    @Override
    public void onClick(View view) {

        int viewID = view.getId();

        thread = new StreamThread();
        if (viewID == R.id.Stream) {
            presentiting = true;
            thread = new StreamThread();
            presentButton.setText("Presenting");
            stopButton.setEnabled(true);
            presentButton.setEnabled(false);
            if(!thread.isAlive()){
                thread.start();
            }



        }
        else if(viewID==R.id.Stop){
            presentiting = false;
            presentButton.setText("Present");
            stopButton.setEnabled(false);
            presentButton.setEnabled(true);

        }


        else if (viewID == R.id.initDraw)
        {
            if (drawEnabled == false) {
                drawButton.setText("Drawing");
                drawEnabled = true;
            } else if (drawEnabled == true) {
                drawButton.setText("Draw");
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

            ColourPicker dialog = new ColourPicker(PresentationActivity.this, currentColour);
            dialog.setOnDialogOptionSelectedListener(new ColourPicker.ColourPickerSelectedListener() {
                @Override
                public void onColourPickerOptionSelected(int colour) {
                    paintView.setColour(colour);
                    currentColour =colour;
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
        currentColour = savedInstanceState.getInt(PAINT_COLOUR);
        drawEnabled = savedInstanceState.getBoolean(DRAW_ENABLED);
        presentiting = savedInstanceState.getBoolean(PRESENTING);
        savedInstanceState.putBoolean(PRESENTING, presentiting);
        savedInstanceState.putBoolean(DRAW_ENABLED, drawEnabled);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_PAGE, mCurrentPage);
        outState.putInt(PAINT_COLOUR, currentColour);
        outState.putBoolean(DRAW_ENABLED, drawEnabled);
        outState.putBoolean(PRESENTING, presentiting);


    }

}
package com.example.JW_Presentations;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import io.socket.client.Socket;
import io.socket.engineio.parser.Base64;

public class PresentationActivity extends AppCompatActivity implements OnPageChangeListener, View.OnClickListener {
    String pdfPath = "";
    private int mCurrentPage = 0;
    public int currentColour =0;
    PDFView pdfView;
    private PaintView paintView;

    private static final String TAG = PresentationActivity.class.getName();
    private  static final int REQUEST_CODE = 100;

    private MediaProjectionManager mProjectionManager;
    private MediaProjection mProjection;
    private ImageReader mImageReader;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int imagesProduced;
    private long startTimeInMills;

    //Buttons
    private ImageButton presentButton;
    private ImageButton stopButton;
    private ImageButton drawButton;
    private ImageButton clearButton;
    private ImageButton undoButton;
    private ImageButton redoButton;
    private ImageButton styleButton;


    private final static String KEY_CURRENT_PAGE = "current_page";
    private final static String PAINT_COLOUR = "current_colour";
    private final static String DRAW_ENABLED = "draw_Enabled";
    private final static String PRESENTING = "presenting";

    public boolean drawEnabled=false;
    private boolean presentiting=false;

    private Socket_Handler socketHandler;
    private Socket mSocket;



    public DisplayMetrics metrics;


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

        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        setContentView(R.layout.activity_presentation);

        drawButton = (ImageButton) findViewById(R.id.initDraw);
        drawButton.setOnClickListener(this);
        presentButton = (ImageButton) findViewById(R.id.Stream);
        presentButton.setOnClickListener(this);
        stopButton = (ImageButton) findViewById(R.id.Stop);
        stopButton.setOnClickListener(this);
        clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(this);
        undoButton = findViewById(R.id.undoButton);
        undoButton.setOnClickListener(this);
        redoButton = findViewById(R.id.redoButton);
        redoButton.setOnClickListener(this);
        styleButton = findViewById(R.id.styleButton);
        styleButton.setOnClickListener(this);
        initDraw();
        initUI();

        display();

    }


    @SuppressLint("WrongConstant")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // for statistics -- init
                //imagesProduced = 0;
                //startTimeInMills = System.currentTimeMillis();

                mProjection = mProjectionManager.getMediaProjection(resultCode, data);

                if (mProjection != null) {
                    final DisplayMetrics metrics = getResources().getDisplayMetrics();
                    final int density = metrics.densityDpi;
                    final int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
                    final Display display = getWindowManager().getDefaultDisplay();
                    final Point size = new Point();
                    display.getSize(size);
                    final View v = getWindow().getDecorView().getRootView();

                    final int width = v.getWidth();
                    final int height = v.getHeight();


                    mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
                    mProjection.createVirtualDisplay("screencap", width, height, density, flags, mImageReader.getSurface(), new VirtualDisplayCallback(), mHandler);
                    mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {

                        @Override
                        public void onImageAvailable(ImageReader reader) {
                            Image image = null;
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            Bitmap bitmap = null;

                            try {
                                image = mImageReader.acquireLatestImage();
                                if (image != null) {
                                    final Image.Plane[] planes = image.getPlanes();
                                    final Buffer imageBuffer = planes[0].getBuffer().rewind();
                                    // create bitmap
                                    String encImage;

                                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                                    bitmap.copyPixelsFromBuffer(imageBuffer);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                    byte[] b = baos.toByteArray();
                                    encImage = Base64.encodeToString(b, Base64.DEFAULT);
                                    dataSent = new JSONObject();
                                    dataSent.put("imageData", encImage);
                                    mSocket.emit("image", dataSent);
                                    //imagesProduced++;
                                    //final long now = System.currentTimeMillis();
                                    //final long sampleTime = now - startTimeInMills;
                                    //Log.e(TAG, "produced images at rate: " + (imagesProduced / (sampleTime / 1000.0f)) + " per sec");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (baos != null) {
                                    try {
                                        baos.close();
                                    } catch (IOException ioe) {
                                        ioe.printStackTrace();
                                    }
                                }

                                if (bitmap != null)
                                    bitmap.recycle();

                                if (image != null)
                                    image.close();

                            }
                        }

                    }, mHandler);
                }

            }

            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "Persmission denied");
        }
    }

    @SuppressLint("ResourceAsColor")
    private void initUI() {
        if(presentiting==true){
            presentButton.performClick();
            presentButton.setEnabled(false);
            stopButton.setEnabled(true);

        }else if (presentiting == false){
            stopButton.performClick();
            presentButton.setEnabled(true);

            stopButton.setEnabled(false);
        }
        if(drawEnabled==true){
            drawButton.setBackgroundColor(R.color.colorBlueyPurple);
        }else{
            //drawButton.setText("Draw");
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


    private void initDraw(){
        paintView = findViewById(R.id.paintView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        paintView.init(metrics);

    }




    private class VirtualDisplayCallback extends VirtualDisplay.Callback {
        @Override
        public void onPaused() {
            super.onPaused();
            Log.e(TAG, "VirtualDisplayCallback: onPaused");
        }

        @Override
        public void onResumed() {
            super.onResumed();
            Log.e(TAG, "VirtualDisplayCallback: onResumed");
        }

        @Override
        public void onStopped() {
            super.onStopped();
            Log.e(TAG, "VirtualDisplayCallback: onStopped");
        }

    }

    final Thread present = new Thread(new Runnable(){
        @Override
        public void run() {
            Looper.prepare();
            mHandler = new Handler();
            Looper.loop();
        }
    });

    @SuppressLint("ResourceAsColor")
    @Override
    public void onClick(View view) {

        int viewID = view.getId();

        if (viewID == R.id.Stream) {

            //thread = new StreamThread();
            presentiting = true;
            socketHandler = Socket_Handler.getInstance();
            socketHandler.setmSocket();
            mSocket = socketHandler.getmSocket();
            mSocket.connect();
            presentButton.setBackgroundResource(R.drawable.circle_button_focus);
            stopButton.setBackgroundResource(R.drawable.circle_button_no_focus);
            stopButton.setEnabled(true);
            presentButton.setEnabled(false);
           if(!present.isAlive())
           {
               present.start();
               if(mProjection==null) {
                   startProjection();
               }

           }else{
               if(mProjection==null) {
                   startProjection();
               }

           }


        }
        else if(viewID==R.id.Stop){
            presentiting = false;
            presentButton.setBackgroundResource(R.drawable.circle_button_no_focus);
            stopButton.setBackgroundResource(R.drawable.circle_button_focus);
            stopButton.setEnabled(false);
            presentButton.setEnabled(true);
            stopProjection();

        }


        else if (viewID == R.id.initDraw)
        {
            if (drawEnabled == false) {
                drawButton.setBackgroundResource(R.drawable.circle_button_focus);

                drawEnabled = true;
            } else if (drawEnabled == true) {
                drawButton.setBackgroundResource(R.drawable.circle_button_no_focus);
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

    private void stopProjection() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mProjection !=null) {
                    mProjection.stop();
                    mProjection = null;
                }
            }
        });
    }

    private void startProjection() {
        if(mProjection == null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
        }
        else
        {
            final DisplayMetrics metrics = getResources().getDisplayMetrics();
            final int density = metrics.densityDpi;
            final int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
            final Display display = getWindowManager().getDefaultDisplay();
            final Point size = new Point();
            display.getSize(size);
            final int width = size.x;
            final int height = size.y;


            mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 5);
            mProjection.createVirtualDisplay("screencap", width, height, density, flags, mImageReader.getSurface(), new VirtualDisplayCallback(), mHandler);
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {

                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    FileOutputStream fos = null;
                    Bitmap bitmap = null;


                    try {
                        image = mImageReader.acquireLatestImage();
                        if (image != null) {
                            final Image.Plane[] planes = image.getPlanes();
                            if (planes[0].getBuffer() == null) {
                                return;
                            }
                            int width = image.getWidth();
                            int height = image.getHeight();
                            int pixelStride = planes[0].getPixelStride();
                            int rowStride = planes[0].getRowStride();
                            int rowPadding = rowStride - pixelStride * width;
                            byte[] newData = new byte[width * height * 4];

                            int offset = 0;


                            // create bitmap
                            String encImage;

                            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                            ByteBuffer buffer = planes[0].getBuffer();
                            for (int i = 0; i < height; ++i) {
                                for (int j = 0; j < width; ++j) {
                                    int pixel = 0;
                                    pixel |= (buffer.get(offset) & 0xff) << 16;     // R
                                    pixel |= (buffer.get(offset + 1) & 0xff) << 8;  // G
                                    pixel |= (buffer.get(offset + 2) & 0xff);       // B
                                    pixel |= (buffer.get(offset + 3) & 0xff) << 24; // A
                                    bitmap.setPixel(j, i, pixel);
                                    offset += pixelStride;
                                }
                                offset += rowPadding;
                            }

                            // write bitmap to a file
                            //fos = new FileOutputStream(getFilesDir() + "/myscreen.png");

                            /**
                             uncomment this if you want either PNG or JPEG output
                             */
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] b = baos.toByteArray();
                            encImage = Base64.encodeToString(b, Base64.DEFAULT);
                            dataSent = new JSONObject();

                            dataSent.put("imageData", encImage);
                            mSocket.emit("image", dataSent);

                            //bitmap.compress(CompressFormat.PNG, 100, fos);

                            // for statistics
                            imagesProduced++;
                            final long now = System.currentTimeMillis();
                            final long sampleTime = now - startTimeInMills;
                            Log.e(TAG, "produced images at rate: " + (imagesProduced / (sampleTime / 1000.0f)) + " per sec");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (baos != null) {
                            try {
                                baos.close();
                            } catch (IOException ioe) {
                                ioe.printStackTrace();
                            }
                        }

                        if (bitmap != null)
                            bitmap.recycle();

                        if (image != null)
                            image.close();

                    }
                }

            }, mHandler);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mSocket!=null) {
            mSocket.disconnect();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopProjection();

        }

}
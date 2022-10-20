package com.example.JW_Presentations;

import android.app.Activity;
import android.graphics.Bitmap;

import android.view.View;


public class Screenshot {
    private static Bitmap mBitmap;

    private static String encImage = "";

    public void takeScreenshot(Activity refActivity, View v) {

        try {
            View v2 = refActivity.getWindow().getDecorView();
            v2.setDrawingCacheEnabled(true);
            v2.buildDrawingCache(true);
            mBitmap = Bitmap.createBitmap(v2.getDrawingCache());
            v2.setDrawingCacheEnabled(false);
            v2.destroyDrawingCache();
            //Canvas canvas = new Canvas(mBitmap);
            //Drawable bgDrawable = v2.getBackground();
            //if (bgDrawable != null)
              //  bgDrawable.draw(canvas);
            //else
              //  canvas.drawColor(Color.WHITE);
            //v2.draw(canvas);
            //v2.draw(canvas);

        }catch (Throwable e){
            e.printStackTrace();
        }

    }
        public Bitmap getmBitmap(){
            return mBitmap;
        }

        public String encodeImage(Bitmap bitmap){

            if (bitmap != null) {
                //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                //byte[] b = baos.toByteArray();
                //encImage = Base64.encodeToString(b, Base64.DEFAULT);
                return encImage;
            } else {
                return null;
            }

        }
        public void clear(){
        if(mBitmap!=null){
                mBitmap = null;
            }
        encImage="";
        }

    }



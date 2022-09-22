package com.example.pdfreader;

import android.app.Activity;
import android.graphics.Bitmap;

import android.view.View;


import java.io.ByteArrayOutputStream;

import io.socket.engineio.parser.Base64;


public class Screenshot {
    private static Bitmap mBitmap;

    public void takeScreenshot(Activity refActivity, View v) {

        try {
            v = refActivity.getWindow().getDecorView();
            v.setDrawingCacheEnabled(true);
            v.buildDrawingCache(true);
            mBitmap =  Bitmap.createBitmap(v.getDrawingCache());
            v.setDrawingCacheEnabled(false);
            //v.destroyDrawingCache();

        }catch (Throwable e){
            e.printStackTrace();
        }

    }
        public Bitmap getmBitmap(){
            return mBitmap;
        }

        public String encodeImage(Bitmap bitmap){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] b = baos.toByteArray();
            String encImage = Base64.encodeToString(b, Base64.DEFAULT);
            return encImage;
        }

    }



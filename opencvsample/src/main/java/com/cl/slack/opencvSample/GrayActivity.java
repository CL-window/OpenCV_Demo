package com.cl.slack.opencvSample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.cl.slack.opencv.JNIWrapper;


/**
 * 处理图片 灰色 注意图片不能太大，之前没有注意，出现过
 * JNI SetIntArrayRegion called with pending exception java.lang.OutOfMemoryError: Failed to allocate a 74649612 byte allocation with 16777216 free bytes and 24MB until OOM'
 * created by slack
 * on 17/8/11 下午7:00
 */
public class GrayActivity extends AppCompatActivity {

    private ImageView mImageView;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gray);
        mBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.test);
        mImageView = (ImageView) findViewById(R.id.imageView);
    }

    public void srcView(View view) {
        mImageView.setImageBitmap(mBitmap);
    }

    public void grayView(View view) {

        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        int[] pixels = new int[w*h];
        mBitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        //recall JNI
        int[] resultInt = JNIWrapper.getGrayImage(pixels, w, h);
        Bitmap resultImg = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        resultImg.setPixels(resultInt, 0, w, 0, 0, w, h);
        mImageView.setImageBitmap(resultImg);
    }
}

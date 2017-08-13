package com.cl.slack.opencvSample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.cl.slack.opencv.JNIWrapper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("slack", JNIWrapper.stringFromJNI());
    }

    private void startNewActivity(Class zlass) {
        startActivity(new Intent(this, zlass));
    }

    public void openCVGrayClick(View view) {
        startNewActivity(GrayActivity.class);
    }
}

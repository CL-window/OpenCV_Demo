package com.cl.slack.opencvSample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.cl.slack.opencv.JNIWrapper;
import com.cl.slack.opencvSample.facedetect.FaceDetectionActivity;
import com.cl.slack.permission.PermissionsManager;
import com.cl.slack.permission.PermissionsResultAction;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("slack", JNIWrapper.stringFromJNI());
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
                Log.i("slack", "onGranted...");
            }

            @Override
            public void onDenied(String permission) {
                Log.i("slack", "onDenied : " + permission);
            }
        });
    }

    private void startNewActivity(Class zlass) {
        startActivity(new Intent(this, zlass));
    }

    public void openCVGrayClick(View view) {
        startNewActivity(GrayActivity.class);
    }

    public void onMixProcessClick(View view) {
        startNewActivity(MixedProcessingActivity.class);
    }

    public void onFaceDetectClick(View view) {
        startNewActivity(FaceDetectionActivity.class);
    }
}

package com.cl.slack.opencvjava;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cl.slack.opencvjava.cameracalibration.CameraCalibrationActivity;
import com.cl.slack.opencvjava.colorblobdetect.ColorBlobDetectionActivity;
import com.cl.slack.opencvjava.puzzle.Puzzle15Activity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onCameraPreviewClick(View view) {
        startNewActivity(CameraPreviewActivity.class);
    }

    private void startNewActivity(Class zlass) {
        startActivity(new Intent(this, zlass));
    }

    public void onCameraCtrlClick(View view) {
        startNewActivity(CameraControlActivity.class);
    }

    public void onMixProcessClick(View view) {
        startNewActivity(MixedProcessingActivity.class);
    }

    public void onImageManipulationClick(View view) {
        startNewActivity(ImageManipulationsActivity.class);
    }

    public void onPuzzleClick(View view) {
        startNewActivity(Puzzle15Activity.class);
    }

    public void onCameraCalibrationClick(View view) {
        startNewActivity(CameraCalibrationActivity.class);
    }

    public void onColorBlobDetectionClick(View view) {
        startNewActivity(ColorBlobDetectionActivity.class);
    }
}

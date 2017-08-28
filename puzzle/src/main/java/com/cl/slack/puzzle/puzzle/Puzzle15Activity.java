package com.cl.slack.puzzle.puzzle;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.cl.slack.permission.PermissionsManager;
import com.cl.slack.puzzle.R;
import com.cl.slack.puzzle.animation.AlertDialog;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Puzzle15Activity extends AppCompatActivity implements CvCameraViewListener, View.OnTouchListener {

    public static String KEY_DIFFICULTY = "puzzle_difficulty";
    public static int DIFFICULTY_EASY = 3;
    public static int DIFFICULTY_MIDDLE = 4;
    public static int DIFFICULTY_HARDY = 5;
    private static final String  TAG = "Puzzle15";

    enum ViewState {
        STATE_PIC, // 拍照选景
        STATE_PUZZLE; // puzzle 游戏
    }

    private ViewState mViewState = ViewState.STATE_PIC;
    private ImageView mPuzzlePic;

    private JavaCameraView       mOpenCvCameraView;
    private Puzzle15Processor    mPuzzle15;
    private MenuItem             mItemHideNumbers;
    private MenuItem             mItemStartNewGame;
    private MenuItem             mItemReLockView;

    private Chronometer mPuzzleTime;

    private AlertDialog mAlertDialog;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    /* Now enable camera view to start receiving frames */
                    mOpenCvCameraView.setOnTouchListener(Puzzle15Activity.this);
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_puzzle);
        Log.d(TAG, "Creating and setting view");
        mPuzzleTime = (Chronometer) findViewById(R.id.puzzle_game_time);
        mPuzzleTime.setOnChronometerTickListener(mPuzzleTickListener);
        mPuzzleTime.setBase(SystemClock.elapsedRealtime());
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.puzzle_camera_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        int d = DIFFICULTY_MIDDLE;
        Intent intent = getIntent();
        if(intent != null) {
            d = intent.getIntExtra(KEY_DIFFICULTY, DIFFICULTY_MIDDLE);
        }
        mPuzzle15 = new Puzzle15Processor(d);
        mPuzzle15.setPuzzleResult(mPuzzleResult);
        mPuzzle15.prepareNewGame();

        mPuzzlePic = (ImageView) findViewById(R.id.puzzle_camera_btn);
        mPuzzlePic.setOnClickListener(mOnClickListener);

        findViewById(R.id.puzzle_switch_camera).setOnClickListener(mOnClickListener);

        changeViewWithState();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.puzzle_camera_btn:
                    showDelayView();
                    break;
                case R.id.puzzle_switch_camera:
                    mOpenCvCameraView.switchCamera();
                    break;
            }
        }
    };

    private void changeViewWithState() {
        switch (mViewState) {
            case STATE_PIC:
                mPuzzlePic.setVisibility(View.VISIBLE);
                mPuzzleTime.setVisibility(View.GONE);
                break;
            case STATE_PUZZLE:
                mPuzzlePic.setVisibility(View.GONE);
                mPuzzleTime.setVisibility(View.VISIBLE);
                break;
        }
    }

    private Puzzle15Processor.PuzzleResult mPuzzleResult = new Puzzle15Processor.PuzzleResult() {
        @Override
        public void onPuzzleSuccess() {
            showMsg(R.string.puzzle_success);
            mPuzzleTime.stop();
            showAlertDialog();
        }
    };

    private void showAlertDialog() {
        if(mAlertDialog == null) {
            mAlertDialog = new AlertDialog(Puzzle15Activity.this);
        }
        mAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mAlertDialog = null;
            }
        });
        mAlertDialog.show();
    }

    private Chronometer.OnChronometerTickListener mPuzzleTickListener = new Chronometer.OnChronometerTickListener() {
        @Override
        public void onChronometerTick(Chronometer chronometer) {
            // 30 min 超市提示
            if(SystemClock.elapsedRealtime() - chronometer.getBase() > 30 * 60 * 1000) {
                // time out
                chronometer.stop();
                chronometer.setBase(SystemClock.elapsedRealtime());
                showMsg(R.string.puzzle_time_out);
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        if(PermissionsManager.getInstance().hasPermission(this, Manifest.permission.CAMERA)) {
            initOpenCVlibIfNecessary();
        }
    }

    private void initOpenCVlibIfNecessary() {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemHideNumbers = menu.add("Show/hide tile numbers");
        mItemStartNewGame = menu.add("Start new game");
        mItemReLockView = menu.add("Relock View");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "Menu Item selected " + item);
        if (item == mItemStartNewGame) {
            /* We need to start new game */
            mPuzzle15.prepareNewGame();
        } else if (item == mItemHideNumbers) {
            /* We need to enable or disable drawing of the tile numbers */
            mPuzzle15.toggleTileNumbers();
        } else if(item == mItemReLockView) {
            lockView();
        }
        mOpenCvCameraView.updateLockView();
        return true;
    }

    public void onCameraViewStarted(int width, int height) {
        mPuzzle15.prepareGameSize(width, height);
    }

    public void onCameraViewStopped() {
    }

    private boolean mStartPuzzle;
    public boolean onTouch(View view, MotionEvent event) {
        if(!mStartPuzzle) {
            mStartPuzzle = true;
            showMsg(R.string.puzzle_time_start);
            mPuzzleTime.setBase(SystemClock.elapsedRealtime());
            mPuzzleTime.start();
        }
        mPuzzle15.deliverTouchEvent((int) event.getRawX(), (int) event.getRawY());
        mOpenCvCameraView.updateLockView();
        // return false, this method will called only the event's action is ACTION_DOWN
        return false;
    }

    private void lockView() {
        mViewState = ViewState.STATE_PIC;
        mOpenCvCameraView.recoverLockCameraView();
        mStartPuzzle = false;
        mPuzzleTime.stop();
        mPuzzleTime.setBase(SystemClock.elapsedRealtime());
        changeViewWithState();
    }

    private void showDelayView() {
        mOpenCvCameraView.lockCameraView();
        TimeDelay timeDelay = new TimeDelay(this);
        mTimeDelayPWindow = new TimeDelayPWindow(timeDelay);

        timeDelay.setTimeDelayListener(new TimeDelay.onTimeDelayListener() {
            @Override
            public void timeDelayDone() {
                mViewState = ViewState.STATE_PUZZLE;
                changeViewWithState();
                if (mTimeDelayPWindow != null) {
                    mTimeDelayPWindow.dismiss();
                }
                mOpenCvCameraView.updateLockView();
            }
        });

        // 显示在整个屏幕的中央
        mTimeDelayPWindow.showAtLocation(mOpenCvCameraView, Gravity.CENTER, 0, 0);
    }

    private TimeDelayPWindow mTimeDelayPWindow;
    private class TimeDelayPWindow extends PopupWindow {
        private TimeDelay mTimeDelay;
        TimeDelayPWindow(TimeDelay timeDelay) {
            super(timeDelay);
            mTimeDelay = timeDelay;
            this.setWidth(FrameLayout.LayoutParams.MATCH_PARENT);
            this.setHeight(FrameLayout.LayoutParams.MATCH_PARENT);
        }

        @Override
        public void dismiss() {
            try {
                if (mTimeDelay != null) {
                    mTimeDelay.cancelTimeDelay();
                }
                super.dismiss();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Mat onCameraFrame(Mat inputFrame) {
        switch (mViewState) {
            case STATE_PUZZLE:
                return mPuzzle15.puzzleFrame(inputFrame);
            case STATE_PIC:
            default:
                return inputFrame;
        }
    }

    private void showMsg(@StringRes int id) {
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
    }
}
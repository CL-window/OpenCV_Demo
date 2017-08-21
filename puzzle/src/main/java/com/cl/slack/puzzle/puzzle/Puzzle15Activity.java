package com.cl.slack.puzzle.puzzle;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
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
import android.widget.PopupWindow;
import android.widget.Toast;

import com.cl.slack.permission.PermissionsManager;
import com.cl.slack.permission.PermissionsResultAction;
import com.cl.slack.puzzle.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class Puzzle15Activity extends AppCompatActivity implements CvCameraViewListener, View.OnTouchListener {

    public static String KEY_DIFFICULTY = "puzzle_difficulty";
    public static int DIFFICULTY_EASY = 3;
    public static int DIFFICULTY_MIDDLE = 4;
    public static int DIFFICULTY_HARDY = 5;
    private static final String  TAG = "Sample::Puzzle15";

    private JavaCameraView       mOpenCvCameraView;
    private Puzzle15Processor    mPuzzle15;
    private MenuItem             mItemHideNumbers;
    private MenuItem             mItemStartNewGame;
    private MenuItem             mItemReLockView;

    private Chronometer mPuzzleTime;

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

    }

    private Puzzle15Processor.PuzzleResult mPuzzleResult = new Puzzle15Processor.PuzzleResult() {
        @Override
        public void onPuzzleSuccess() {
            showMsg("恭喜恭喜，成功啦");
        }
    };

    private Chronometer.OnChronometerTickListener mPuzzleTickListener = new Chronometer.OnChronometerTickListener() {
        @Override
        public void onChronometerTick(Chronometer chronometer) {
            // 30 min 超市提示
            if(SystemClock.elapsedRealtime() - chronometer.getBase() > 30 * 60 * 1000) {
                // time out
                chronometer.stop();
                chronometer.setBase(SystemClock.elapsedRealtime());
                showMsg("换一局吧，这一局太难了");
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
        mItemReLockView = menu.add("Lock/Relock View");
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
            showMsg("puzzle 开始计时");
            mPuzzleTime.setBase(SystemClock.elapsedRealtime());
            mPuzzleTime.start();
        }
        mPuzzle15.deliverTouchEvent((int) event.getRawX(), (int) event.getRawY());
        mOpenCvCameraView.updateLockView();
        // return false, this method will called only the event's action is ACTION_DOWN
        return false;
    }

    private boolean mLocking;
    private void lockView() {
        if(mLocking) {
            return;
        }
        showMsg("请对准取景窗口，即将锁定游戏屏幕");
        mOpenCvCameraView.recoverLockCameraView();
        mLocking = true;
        mStartPuzzle = false;
        mPuzzleTime.stop();
        mPuzzleTime.setBase(SystemClock.elapsedRealtime());
        showDelayView();
    }

    private void showDelayView() {
        TimeDelay timeDelay = new TimeDelay(this);
        mTimeDelayPWindow = new TimeDelayPWindow(timeDelay);

        timeDelay.setTimeDelayListener(new TimeDelay.onTimeDelayListener() {
            @Override
            public void timeDelayDone() {
                if (mTimeDelayPWindow != null) {
                    mTimeDelayPWindow.dismiss();
                }
                mOpenCvCameraView.lockCameraView();
                mLocking = false;
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
        return mPuzzle15.puzzleFrame(inputFrame);
    }

    private void showMsg(String msg) {
        Toast.makeText(this,msg, Toast.LENGTH_SHORT).show();
    }
}
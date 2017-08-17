package com.cl.slack.opencvSample.facedetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.Objdetect;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.cl.slack.opencvSample.R;

import static org.opencv.imgproc.Imgproc.*;

public class FaceDetectionActivity extends AppCompatActivity implements CvCameraViewListener2 {

    private static final String    TAG                 = "FaceDetectionActivity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    private MenuItem               mItemType;

    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;
    private DetectionBasedTracker  mNativeDetector;

    // eyes
    private CascadeClassifier      mCascadeER, mCascadeEL;
    private Mat                    mResult;
    private Mat                    mZoomWindow;
    private Mat                    mZoomWindow2;
    private Rect 			       mEyearea = new Rect();
    private int 				   mLearn_frames = 0;
    private double 				   mMatch_value;
    private Mat 				   mTeplateR;
    private Mat 				   mTeplateL;
    public  int                    mEyemethod = 3;

    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    private JavaCameraView   mOpenCvCameraView;

    private boolean mFaceDetection = false;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    try {

                       loadDecetorFormXml();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    private void loadDecetorFormXml() throws IOException{

        // --------------------------------- load face cascade file from application resources -----------------------------------
        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
        FileOutputStream os = new FileOutputStream(mCascadeFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        is.close();
        os.close();
        //----------------------------------------------------------------------------------------------------

        // --------------------------------- load left eye classificator -----------------------------------
        InputStream iser = getResources().openRawResource(R.raw.haarcascade_lefteye_2splits);
        File cascadeDirER = getDir("cascadeER", Context.MODE_PRIVATE);
        File cascadeFileER = new File(cascadeDirER, "haarcascade_eye_right.xml");
        FileOutputStream oser = new FileOutputStream(cascadeFileER);

        byte[] bufferER = new byte[4096];
        int bytesReadER;
        while ((bytesReadER = iser.read(bufferER)) != -1) {
            oser.write(bufferER, 0, bytesReadER);
        }
        iser.close();
        oser.close();
        //----------------------------------------------------------------------------------------------------


        // --------------------------------- load right eye classificator ------------------------------------
        InputStream isel = getResources().openRawResource(R.raw.haarcascade_lefteye_2splits);
        File cascadeDirEL = getDir("cascadeEL", Context.MODE_PRIVATE);
        File cascadeFileEL = new File(cascadeDirEL, "haarcascade_eye_left.xml");
        FileOutputStream osel = new FileOutputStream(cascadeFileEL);

        byte[] bufferEL = new byte[4096];
        int bytesReadEL;
        while ((bytesReadEL = isel.read(bufferEL)) != -1) {
            osel.write(bufferEL, 0, bytesReadEL);
        }
        isel.close();
        osel.close();

        // ------------------------------------------------------------------------------------------------------

        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        mCascadeER = new CascadeClassifier(cascadeFileER.getAbsolutePath());
        mCascadeEL = new CascadeClassifier(cascadeFileER.getAbsolutePath());
        if (mJavaDetector.empty() || mCascadeER.empty() || mCascadeEL.empty()) {
            Log.e(TAG, "Failed to load cascade classifier");
            mJavaDetector = null;
            mCascadeER = null;
            mCascadeEL = null;
        } else
            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

        cascadeDir.delete();
        cascadeFileER.delete();
        cascadeDirER.delete();
        cascadeFileEL.delete();
        cascadeDirEL.delete();
    }

    public FaceDetectionActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_face_detection);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        VerticalSeekBar seekBar = (VerticalSeekBar) findViewById(R.id.vertical_seekbar);
        seekBar.setMax(5);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar,
                                          int progress, boolean fromUser) {
                // 0-5
                mEyemethod = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if(!mFaceDetection) {
            return mRgba;
        }

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
            /**
             * detectMultiScale(
             *                  Mat image, //输入图像
             *                  MatOfRect objects, //检测到的Rect[]
             *                  double scaleFactor, //缩放比例，必须大于1
             *                  int minNeighbors, //合并窗口时最小neighbor，每个候选矩阵至少包含的附近元素个数
             *                  int flags,  //检测标记，只对旧格式的分类器有效，与cvHaarDetectObjects的参数flags相同，默认为0，
             *            可能的取值为CV_HAAR_DO_CANNY_PRUNING(CANNY边缘检测)、CV_HAAR_SCALE_IMAGE(缩放图像)、
             *            CV_HAAR_FIND_BIGGEST_OBJECT(寻找最大的目标)、CV_HAAR_DO_ROUGH_SEARCH(做粗略搜索)；
             *            如果寻找最大的目标就不能缩放图像，也不能CANNY边缘检测
             *                  Size minSize, //最小检测目标
             *                  Size maxSize //最大检测目标
             *                  )
             */
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

            if (mZoomWindow == null)
                createAuxiliaryMats();


            Rect[] facesArray = faces.toArray();

            for (int i = 0; i < facesArray.length; i++){
                Rect r = facesArray[i];
                Imgproc.rectangle(mGray, r.tl(), r.br(), new Scalar(0, 255, 0, 255), 3);
                Imgproc.rectangle(mRgba, r.tl(), r.br(), new Scalar(0, 255, 0, 255), 3);

                mEyearea = new Rect(r.x +r.width/8,(int)(r.y + (r.height/4.5)),r.width - 2*r.width/8,(int)( r.height/3.0));
                Imgproc.rectangle(mRgba,mEyearea.tl(),mEyearea.br() , new Scalar(255,0, 0, 255), 2);
                Rect eyearea_right = new Rect(r.x +r.width/16,(int)(r.y + (r.height/4.5)),(r.width - 2*r.width/16)/2,(int)( r.height/3.0));
                Rect eyearea_left = new Rect(r.x +r.width/16 +(r.width - 2*r.width/16)/2,(int)(r.y + (r.height/4.5)),(r.width - 2*r.width/16)/2,(int)( r.height/3.0));
                Imgproc.rectangle(mRgba,eyearea_left.tl(),eyearea_left.br() , new Scalar(255,0, 0, 255), 2);
                Imgproc.rectangle(mRgba,eyearea_right.tl(),eyearea_right.br() , new Scalar(255, 0, 0, 255), 2);

                if(mLearn_frames<5){
                    mTeplateR = getTemplate(mCascadeER,eyearea_right,24);
                    mTeplateL = getTemplate(mCascadeEL,eyearea_left,24);
                    mLearn_frames++;
                }else{

                    mMatch_value = match_eye(eyearea_right,mTeplateR,mEyemethod);
                    mMatch_value = match_eye(eyearea_left,mTeplateL,mEyemethod);

                }
                Imgproc.resize(mRgba.submat(eyearea_left), mZoomWindow2, mZoomWindow2.size());
                Imgproc.resize(mRgba.submat(eyearea_right), mZoomWindow, mZoomWindow.size());

            }

        }
        else if (mDetectorType == NATIVE_DETECTOR) {
            if (mNativeDetector != null)
                mNativeDetector.detect(mGray, faces);
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();
        Log.e(TAG, "recognition size: " + facesArray.length);
        for (int i = 0; i < facesArray.length; i++) {
            /**
             * Mat类型的图上绘制矩形
             * rectangle(Mat img, //图像
             *           Point pt1, //矩形的一个顶点
             *           Point pt2, //矩形对角线上的另一个顶点
             *           Scalar color, //线条颜色 (RGB) 或亮度（灰度图像 ）
             *           int thickness, //组成矩形的线条的粗细程度。取负值时（如 CV_FILLED）函数绘制填充了色彩的矩形
             *           int lineType, //线条的类型
             *           int shift //坐标点的小数点位数
             *           )
             */
            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        }
        return mRgba;
    }

    private void createAuxiliaryMats() {
        if (mGray.empty())
            return;

        int rows = mGray.rows();
        int cols = mGray.cols();

        if (mZoomWindow == null){
            mZoomWindow = mRgba.submat(rows / 2 + rows / 10 ,rows , cols / 2 + cols / 10, cols );
            mZoomWindow2 = mRgba.submat(0, rows / 2 - rows / 10 , cols / 2 + cols / 10, cols );
        }

    }

    private double  match_eye(Rect area, Mat mTemplate,int type){
        Point matchLoc;
        Mat mROI = mGray.submat(area);
        int result_cols =  mGray.cols() - mTemplate.cols() + 1;
        int result_rows = mGray.rows() - mTemplate.rows() + 1;
        if(mTemplate.cols()==0 ||mTemplate.rows()==0){
            return 0.0;
        }
        mResult = new Mat(result_cols,result_rows, CvType.CV_32FC1);

        switch (type){
            case TM_SQDIFF:
                Imgproc.matchTemplate(mROI, mTemplate, mResult, TM_SQDIFF) ;
                break;
            case TM_SQDIFF_NORMED:
                Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_SQDIFF_NORMED) ;
                break;
            case TM_CCORR:
                Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCORR) ;
                break;
            case TM_CCORR_NORMED:
                Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCORR_NORMED) ;
                break;
            case TM_CCOEFF:
                Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCOEFF) ;
                break;
            case TM_CCOEFF_NORMED:
                Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCOEFF_NORMED) ;
                break;

        }

        Core.MinMaxLocResult mmres =  Core.minMaxLoc(mResult);

        if(type == TM_SQDIFF || type == TM_SQDIFF_NORMED)
        { matchLoc = mmres.minLoc; }
        else
        { matchLoc = mmres.maxLoc; }

        Point  matchLoc_tx = new Point(matchLoc.x+area.x,matchLoc.y+area.y);
        Point  matchLoc_ty = new Point(matchLoc.x + mTemplate.cols() + area.x , matchLoc.y + mTemplate.rows()+area.y );

        Imgproc.rectangle(mRgba, matchLoc_tx,matchLoc_ty, new Scalar(255, 255, 0, 255));

        if(type == TM_SQDIFF || type == TM_SQDIFF_NORMED)
        { return mmres.maxVal; }
        else
        { return mmres.minVal; }

    }

    private Mat  getTemplate(CascadeClassifier clasificator, Rect area, int size){
        Mat template = new Mat();
        Mat mROI = mGray.submat(area);
        MatOfRect eyes = new MatOfRect();
        Point iris = new Point();
        Rect eye_template = new Rect();
        clasificator.detectMultiScale(mROI, eyes, 1.15, 2, Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_SCALE_IMAGE, new Size(30,30),new Size());


        Rect[] eyesArray = eyes.toArray();
        for (int i = 0; i < eyesArray.length; i++){
            Rect e = eyesArray[i];
            e.x = area.x + e.x;
            e.y = area.y + e.y;
            Rect eye_only_rectangle = new Rect((int)e.tl().x,(int)( e.tl().y + e.height*0.4),(int)e.width,(int)(e.height*0.6));
            mROI = mGray.submat(eye_only_rectangle);
            Mat vyrez = mRgba.submat(eye_only_rectangle);
            Core.MinMaxLocResult mmG = Core.minMaxLoc(mROI);

            Imgproc.circle(vyrez, mmG.minLoc,2, new Scalar(255, 255, 255, 255),2);
            iris.x = mmG.minLoc.x + eye_only_rectangle.x;
            iris.y = mmG.minLoc.y + eye_only_rectangle.y;
            eye_template = new Rect((int)iris.x-size/2,(int)iris.y-size/2 ,size,size);
            Imgproc.rectangle(mRgba,eye_template.tl(),eye_template.br(),new Scalar(255, 0, 0, 255), 2);
            template = (mGray.submat(eye_template)).clone();
            return template;
        }
        return template;
    }


    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

            if (type == NATIVE_DETECTOR) {
                Log.i(TAG, "Detection Based Tracker enabled");
                mNativeDetector.start();
            } else {
                Log.i(TAG, "Cascade detector enabled");
                mNativeDetector.stop();
            }
        }
    }

    public void switchCamera(View view) {
        mOpenCvCameraView.switchCamera();
    }
}
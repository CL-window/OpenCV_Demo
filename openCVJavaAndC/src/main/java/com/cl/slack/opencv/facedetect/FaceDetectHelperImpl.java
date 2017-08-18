package com.cl.slack.opencv.facedetect;

import android.content.Context;
import android.util.Log;

import com.cl.slack.opencv.facedetect.face.BodyDetect;
import com.cl.slack.opencv.facedetect.face.EyeLeftDetect;
import com.cl.slack.opencv.facedetect.face.EyeRightDetect;
import com.cl.slack.opencv.facedetect.face.FaceDetect;
import com.cl.slack.opencv.facedetect.face.SmileDetect;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
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
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import static org.opencv.imgproc.Imgproc.TM_CCOEFF;
import static org.opencv.imgproc.Imgproc.TM_CCOEFF_NORMED;
import static org.opencv.imgproc.Imgproc.TM_CCORR;
import static org.opencv.imgproc.Imgproc.TM_CCORR_NORMED;
import static org.opencv.imgproc.Imgproc.TM_SQDIFF;
import static org.opencv.imgproc.Imgproc.TM_SQDIFF_NORMED;


/**
 * Created by slack
 * on 17/8/17 下午8:01
 * whole face
 */

public class FaceDetectHelperImpl implements FaceDetectHelper {

    public static FaceDetectHelperImpl instance = new FaceDetectHelperImpl();
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private static final Scalar EYES_RECT_COLOR = new Scalar(255, 0, 0, 255);

    private FaceDetectHelperImpl() {
    }

    private Mat mRgba;
    private Mat mGray;

    private ObjectDetect mFaceDetect;
    private ObjectDetect mEyeLeftDetect;
    private ObjectDetect mEyeRightDetect;
    private ObjectDetect mBodyDetect;
    private ObjectDetect mSmileDetect;


    // eyes
    private Mat mZoomWindow;
    private Mat mZoomWindow2;
    private int mLearn_frames = 0;
    private double mLeftMatchValue,mRightMatchValue;
    private Mat mTeplateR;
    private Mat mTeplateL;
    public int mEyemethod = 3;

    /**
     * call onResume
     *
     * @param context context
     */
    @Override
    public void initFaceDetect(Context context, FaceDetectHelper.LoaderCallback callback) {
        initFaceDetect(context, true, callback);
    }

    @Override
    public void initFaceDetect(Context context, boolean java, FaceDetectHelper.LoaderCallback callback) {
        OpenCVLoaderCallback c = new OpenCVLoaderCallback(context, java, callback);
        if (!OpenCVLoader.initDebug()) {
            Log.d("slack", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, context, c);
        } else {
            Log.d("slack", "OpenCV library found inside package. Using it!");
            c.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    /**
     * face detect gray
     * draw in rgba
     *
     * @param inputFrame src
     */
    @Override
    public Mat detectFace(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mZoomWindow == null)
            createAuxiliaryMats();

        MatOfRect faces = new MatOfRect();

        mFaceDetect.detectObject(mGray, faces);
//        mEyeLeftDetect.detectObject(mGray, faces);
//        mEyeRightDetect.detectObject(mGray, faces);
//        mSmileDetect.detectObject(mGray, faces);

        Rect[] facesArray = faces.toArray();

        for (Rect face : facesArray) {

            Imgproc.rectangle(mRgba, face.tl(), face.br(), FACE_RECT_COLOR, 3);

            // 眼睛范围是估算的一个大概的值，缩小检测范围
            // eye area
//            Rect eyearea = new Rect(face.x + face.width / 8, (int) (face.y + (face.height / 4.5)), face.width - 2 * face.width / 8, (int) (face.height / 3.0));
//            Imgproc.rectangle(mRgba, eyearea.tl(), eyearea.br(), EYES_RECT_COLOR, 2);
            // left eye and right eye
            Rect eyearea_right = new Rect(face.x + face.width / 16, (int) (face.y + (face.height / 4.5)), (face.width - 2 * face.width / 16) / 2, (int) (face.height / 3.0));
            Rect eyearea_left = new Rect(face.x + face.width / 16 + (face.width - 2 * face.width / 16) / 2, (int) (face.y + (face.height / 4.5)), (face.width - 2 * face.width / 16) / 2, (int) (face.height / 3.0));
//            Imgproc.rectangle(mRgba, eyearea_left.tl(), eyearea_left.br(), EYES_RECT_COLOR, 2);
//            Imgproc.rectangle(mRgba, eyearea_right.tl(), eyearea_right.br(), EYES_RECT_COLOR, 2);

            if (mLearn_frames < 5) {
                mTeplateR = getTemplate(mEyeRightDetect, eyearea_right, 24);
                mTeplateL = getTemplate(mEyeLeftDetect, eyearea_left, 24);
                mLearn_frames++;
            } else {
                mRightMatchValue= match_eye(eyearea_right, mTeplateR, mEyemethod);
                mLeftMatchValue = match_eye(eyearea_left, mTeplateL, mEyemethod);
            }
            Imgproc.resize(mRgba.submat(eyearea_left), mZoomWindow2, mZoomWindow2.size());
            Imgproc.resize(mRgba.submat(eyearea_right), mZoomWindow, mZoomWindow.size());


        }

        Log.i("slack", "recognition size: " + facesArray.length + " left eye match: " + mLeftMatchValue + " right eye match: " + mRightMatchValue);
        return mRgba;
    }

    private void createAuxiliaryMats() {
        if (mGray.empty())
            return;

        int rows = mGray.rows();
        int cols = mGray.cols();

        if (mZoomWindow == null) {
            mZoomWindow = mRgba.submat(rows / 2 + rows / 10, rows, cols / 2 + cols / 10, cols);
            mZoomWindow2 = mRgba.submat(0, rows / 2 - rows / 10, cols / 2 + cols / 10, cols);
        }

    }

    /**
     * 可以用来做 识别
     * 模板匹配是一种在图像中定位目标的方法
     * 通过把输入图像在实际图像上逐像素点滑动，计算特征相似性，以此来判断当前滑块图像所在位置是目标图像的概率。
     * 在目标特征变化不是特别快的情况下，跟踪效果还可以
     * matchTemplate(Mat image, 搜索对象图像
     *              Mat templ, 模板图像，小于image，并且和image有相同的数据类型
     *              Mat result, 比较结果
     *              int method 比较算法总共有六种
     *              TM_SQDIFF 平方差匹配法：该方法采用平方差来进行匹配；最好的匹配值为0；匹配越差，匹配值越大。
     *              TM_CCORR 相关匹配法：该方法采用乘法操作；数值越大表明匹配程度越好。
     *              TM_CCOEFF 相关系数匹配法：1表示完美的匹配；-1表示最差的匹配。
     *              TM_SQDIFF_NORMED 归一化平方差匹配法
     *              TM_CCORR_NORMED 归一化相关匹配法
     *              TM_CCOEFF_NORMED 归一化相关系数匹配法
     *              )
     */
    private double match_eye(Rect area, Mat mTemplate, int type) {
        Point matchLoc;
        Mat mROI = mGray.submat(area);
        int result_cols = mGray.cols() - mTemplate.cols() + 1;
        int result_rows = mGray.rows() - mTemplate.rows() + 1;
        if (mTemplate.cols() == 0 || mTemplate.rows() == 0) {
            return 0.0;
        }
        Mat mat = new Mat(result_cols, result_rows, CvType.CV_32FC1);

        switch (type) {
            case TM_SQDIFF:
                Imgproc.matchTemplate(mROI, mTemplate, mat, TM_SQDIFF);
                break;
            case TM_SQDIFF_NORMED:
                Imgproc.matchTemplate(mROI, mTemplate, mat, TM_SQDIFF_NORMED);
                break;
            case TM_CCORR:
                Imgproc.matchTemplate(mROI, mTemplate, mat, TM_CCORR);
                break;
            case TM_CCORR_NORMED:
                Imgproc.matchTemplate(mROI, mTemplate, mat, TM_CCORR_NORMED);
                break;
            case TM_CCOEFF:
                Imgproc.matchTemplate(mROI, mTemplate, mat, TM_CCOEFF);
                break;
            case TM_CCOEFF_NORMED:
                Imgproc.matchTemplate(mROI, mTemplate, mat, TM_CCOEFF_NORMED);
                break;

        }

        Core.MinMaxLocResult mmres = Core.minMaxLoc(mat);

        if (type == TM_SQDIFF || type == TM_SQDIFF_NORMED) {
            matchLoc = mmres.minLoc;
        } else {
            matchLoc = mmres.maxLoc;
        }

        Point matchLoc_tx = new Point(matchLoc.x + area.x, matchLoc.y + area.y);
        Point matchLoc_ty = new Point(matchLoc.x + mTemplate.cols() + area.x, matchLoc.y + mTemplate.rows() + area.y);

        Imgproc.rectangle(mRgba, matchLoc_tx, matchLoc_ty, new Scalar(255, 255, 0, 255));

        if (type == TM_SQDIFF || type == TM_SQDIFF_NORMED) {
            return mmres.maxVal;
        } else {
            return mmres.minVal;
        }

    }

    private Mat getTemplate(ObjectDetect detect, Rect area, int size) {
        Mat template = new Mat();
        Mat mROI = mGray.submat(area);
        MatOfRect eyes = new MatOfRect();
        Point iris = new Point();
        Rect eye_template = new Rect();
        detect.detectObject(mROI, eyes, new Size(30, 30));

        Rect[] eyesArray = eyes.toArray();
        for (int i = 0; i < eyesArray.length; i++) {
            Rect e = eyesArray[i];
            e.x = area.x + e.x;
            e.y = area.y + e.y;
            Rect eye_only_rectangle = new Rect((int) e.tl().x, (int) (e.tl().y + e.height * 0.4), (int) e.width, (int) (e.height * 0.6));
            mROI = mGray.submat(eye_only_rectangle);
            Mat vyrez = mRgba.submat(eye_only_rectangle);
            Core.MinMaxLocResult mmG = Core.minMaxLoc(mROI);

            Imgproc.circle(vyrez, mmG.minLoc, 2, new Scalar(255, 255, 255, 255), 2);
            iris.x = mmG.minLoc.x + eye_only_rectangle.x;
            iris.y = mmG.minLoc.y + eye_only_rectangle.y;
            eye_template = new Rect((int) iris.x - size / 2, (int) iris.y - size / 2, size, size);
            Imgproc.rectangle(mRgba, eye_template.tl(), eye_template.br(), EYES_RECT_COLOR, 2);
//            template = (mGray.submat(eye_template)).clone();
            mGray.submat(eye_template).copyTo(template);
            return template;
        }
        return template;
    }


    private class OpenCVLoaderCallback extends BaseLoaderCallback {

        Context context;
        boolean java;
        LoaderCallback callback;

        OpenCVLoaderCallback(Context appContext, boolean j, LoaderCallback c) {
            super(appContext);
            context = appContext;
            java = j;
            callback = c;
        }

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("slack", "OpenCV loaded successfully");

                    initAllDetect(context, java);

                    if (callback != null) {
                        callback.onResult(true);
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                    if (callback != null) {
                        callback.onResult(false);
                    }
                }
                break;
            }
        }
    }


    private void initAllDetect(Context context, boolean java) {
        if (java) {
            mFaceDetect = new FaceDetect(context);
            mEyeLeftDetect = new EyeLeftDetect(context);
            mEyeRightDetect = new EyeRightDetect(context);
            mBodyDetect = new BodyDetect(context);
            mSmileDetect = new SmileDetect(context);
        }
    }
}

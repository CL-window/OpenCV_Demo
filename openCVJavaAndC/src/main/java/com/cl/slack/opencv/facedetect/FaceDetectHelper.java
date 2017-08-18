package com.cl.slack.opencv.facedetect;

import android.content.Context;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

/**
 * Created by slack
 * on 17/8/17 下午8:14
 * java detect
 */

public interface FaceDetectHelper {

    FaceDetectHelper helper = FaceDetectHelperImpl.instance;

    void initFaceDetect(Context context, LoaderCallback callback);

    /**
     *
     * @param context context
     * @param java use java detect
     * @param callback callback
     */
    void initFaceDetect(Context context, boolean java, LoaderCallback callback);

    /**
     * face detect
     * @param inputFrame src
     * @return rgba Mat
     */
    Mat detectFace(CameraBridgeViewBase.CvCameraViewFrame inputFrame);

    interface LoaderCallback {
        void onResult(boolean success);
    }
}

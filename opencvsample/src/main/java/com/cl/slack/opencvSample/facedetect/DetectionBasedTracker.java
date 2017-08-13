package com.cl.slack.opencvSample.facedetect;

import com.cl.slack.opencv.JNIWrapper;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

public class DetectionBasedTracker
{
    public DetectionBasedTracker(String cascadeName, int minFaceSize) {
        mNativeObj = JNIWrapper.nativeCreateObject(cascadeName, minFaceSize);
    }

    public void start() {
        JNIWrapper.nativeStart(mNativeObj);
    }

    public void stop() {
        JNIWrapper.nativeStop(mNativeObj);
    }

    public void setMinFaceSize(int size) {
        JNIWrapper.nativeSetFaceSize(mNativeObj, size);
    }

    public void detect(Mat imageGray, MatOfRect faces) {
        JNIWrapper.nativeDetect(mNativeObj, imageGray.getNativeObjAddr(), faces.getNativeObjAddr());
    }

    public void release() {
        JNIWrapper.nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    private long mNativeObj = 0;

}

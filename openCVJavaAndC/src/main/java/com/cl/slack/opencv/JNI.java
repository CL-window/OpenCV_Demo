package com.cl.slack.opencv;

/**
 * Created by slack
 * on 17/8/10 下午2:03
 */

public class JNI {

    static {
        System.loadLibrary("opencv-c-lib");
    }

    public static native String stringFromJNI();

    public static native int[] getGrayImage(int[] src, int w, int h);

    public static native void findFeatures(long matAddrGr, long matAddrRgba);


    // face detection
    public static native long nativeCreateObject(String cascadeName, int minFaceSize);
    public static native void nativeDestroyObject(long thiz);
    public static native void nativeStart(long thiz);
    public static native void nativeStop(long thiz);
    public static native void nativeSetFaceSize(long thiz, int size);
    public static native void nativeDetect(long thiz, long inputImage, long faces);

    //
    public static native double nativeFaceRecognition(long thiz, long face1, long face2);

    public static native void rotate(long img, int degree);
}

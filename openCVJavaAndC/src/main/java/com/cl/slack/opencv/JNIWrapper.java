package com.cl.slack.opencv;

import org.opencv.core.Mat;

/**
 * Created by slack
 * on 17/8/11 下午4:06
 */

public class JNIWrapper {

    public static String stringFromJNI(){
        return JNI.stringFromJNI();
    }

    public static int[] getGrayImage(int[] src, int w, int h) {
        return JNI.getGrayImage(src, w, h);
    }

    public static void findFeatures(long matAddrGr, long matAddrRgba) {
        JNI.findFeatures(matAddrGr, matAddrRgba);
    }


    public static long nativeCreateObject(String cascadeName, int minFaceSize){
        return JNI.nativeCreateObject(cascadeName, minFaceSize);
    }
    public static void nativeDestroyObject(long thiz){
        JNI.nativeDestroyObject(thiz);
    }
    public static void nativeStart(long thiz){
        JNI.nativeStart(thiz);
    }
    public static void nativeStop(long thiz){
        JNI.nativeStop(thiz);
    }
    public static void nativeSetFaceSize(long thiz, int size){
        JNI.nativeSetFaceSize(thiz, size);
    }
    public static void nativeDetect(long thiz, long inputImage, long faces){
        JNI.nativeDetect(thiz, inputImage, faces);
    }


    /**
     * 对比两个人脸 相似度
     * @param face1 face Mat
     * @param face2
     */
    public static double nativeFaceRecognition(long thiz, Mat face1, Mat face2) {
        return JNI.nativeFaceRecognition(thiz, face1.getNativeObjAddr(), face2.getNativeObjAddr());
    }


    public static void rotate(Mat img, int degree){
        JNI.rotate(img.getNativeObjAddr(), degree);
    }
}

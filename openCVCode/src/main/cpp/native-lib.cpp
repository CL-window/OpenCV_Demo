#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>

extern "C"
{
JNIEXPORT jstring JNICALL
Java_slack_cl_com_opencv_JNI_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";

    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jintArray JNICALL
Java_slack_cl_com_opencv_JNI_getGrayImage(JNIEnv *env, jclass type, jintArray src_, jint w,
                                          jint h) {
    jint *src = env->GetIntArrayElements(src_, NULL);
    if(src==NULL){
        return NULL;
    }
    cv::Mat imgData(h, w, CV_8UC4, (unsigned char*)src);
    uchar *ptr = imgData.ptr(0);
    for (int i = 0; i < w * h; i++) {
        int grayScale = (int) (ptr[4 * i + 2] * 0.299 + ptr[4 * i + 1] * 0.587
                               + ptr[4 * i + 0] * 0.114);
        ptr[4 * i + 1] = (uchar) grayScale;
        ptr[4 * i + 2] = (uchar) grayScale;
        ptr[4 * i + 0] = (uchar) grayScale;
    }

    int size = w * h;
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, src);

    env->ReleaseIntArrayElements(src_, src, 0);
    return result;

}

}

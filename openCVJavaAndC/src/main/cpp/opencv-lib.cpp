#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include "faceDetect.h"

using namespace cv;
using namespace std;

extern "C"
{
JNIEXPORT jstring JNICALL
Java_com_cl_slack_opencv_JNI_stringFromJNI(JNIEnv *env, jclass type) {

    std::string hello = "Hello from C++";

    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jintArray JNICALL
Java_com_cl_slack_opencv_JNI_getGrayImage(JNIEnv *env, jclass type, jintArray src_, jint w,
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

JNIEXPORT void JNICALL
Java_com_cl_slack_opencv_JNI_findFeatures(JNIEnv *env, jclass type,
                                          jlong addrGray,jlong addrRgba) {

    Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;
    vector<KeyPoint> v;

    Ptr<FeatureDetector> detector = FastFeatureDetector::create(50);
    detector->detect(mGr, v);
    LOGD("findFeatures size:%d ", v.size());
    for( unsigned int i = 0; i < v.size(); i++ )
    {
        const KeyPoint& kp = v[i];
        circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
    }

}




JNIEXPORT jlong JNICALL
Java_com_cl_slack_opencv_JNI_nativeCreateObject(JNIEnv *env, jclass type, jstring cascadeName_,
                                                jint faceSize) {
    const char *cascadeName = env->GetStringUTFChars(cascadeName_, 0);

    string stdFileName(cascadeName);
    jlong result = 0;

    LOGD("nativeCreateObject");

    try
    {
        cv::Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(
                makePtr<CascadeClassifier>(stdFileName));
        cv::Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(
                makePtr<CascadeClassifier>(stdFileName));
        result = (jlong)new DetectorAgregator(mainDetector, trackingDetector);
        if (faceSize > 0)
        {
            mainDetector->setMinObjectSize(Size(faceSize, faceSize));
            //trackingDetector->setMinObjectSize(Size(faceSize, faceSize));
        }
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
        jclass je = env->FindClass("org/opencv/core/CvException");
        if(!je)
            je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeCreateObject caught unknown exception");
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeCreateObject()");
        return 0;
    }

    env->ReleaseStringUTFChars(cascadeName_, cascadeName);
    LOGD("nativeCreateObject exit");
    return result;

}

JNIEXPORT void JNICALL Java_com_cl_slack_opencv_JNI_nativeDestroyObject
        (JNIEnv * jenv, jclass, jlong thiz)
{
    LOGD("nativeDestroyObject");

    try
    {
        if(thiz != 0)
        {
            ((DetectorAgregator*)thiz)->tracker->stop();
            delete (DetectorAgregator*)thiz;
        }
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeestroyObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeDestroyObject caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeDestroyObject()");
    }
    LOGD("nativeDestroyObject exit");
}

JNIEXPORT void JNICALL Java_com_cl_slack_opencv_JNI_nativeStart
        (JNIEnv * jenv, jclass, jlong thiz)
{
    LOGD("nativeStart");

    try
    {
        ((DetectorAgregator*)thiz)->tracker->run();
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeStart caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeStart caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeStart()");
    }
    LOGD("nativeStart exit");
}

JNIEXPORT void JNICALL Java_com_cl_slack_opencv_JNI_nativeStop
        (JNIEnv * jenv, jclass, jlong thiz)
{
    LOGD("nativeStop");

    try
    {
        ((DetectorAgregator*)thiz)->tracker->stop();
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeStop caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeStop caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeStop()");
    }
    LOGD("nativeStop exit");
}

JNIEXPORT void JNICALL Java_com_cl_slack_opencv_JNI_nativeSetFaceSize
        (JNIEnv * jenv, jclass, jlong thiz, jint faceSize)
{
    LOGD("nativeSetFaceSize -- BEGIN");

    try
    {
        if (faceSize > 0)
        {
            ((DetectorAgregator*)thiz)->mainDetector->setMinObjectSize(Size(faceSize, faceSize));
            //((DetectorAgregator*)thiz)->trackingDetector->setMinObjectSize(Size(faceSize, faceSize));
        }
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeStop caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeSetFaceSize caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeSetFaceSize()");
    }
    LOGD("nativeSetFaceSize -- END");
}


JNIEXPORT void JNICALL Java_com_cl_slack_opencv_JNI_nativeDetect
        (JNIEnv * jenv, jclass, jlong thiz, jlong imageGray, jlong faces)
{
    LOGD("nativeDetect");

    try
    {
        vector<Rect> RectFaces;
        ((DetectorAgregator*)thiz)->tracker->process(*((Mat*)imageGray));
        ((DetectorAgregator*)thiz)->tracker->getObjects(RectFaces);
        *((Mat*)faces) = Mat(RectFaces, true);
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeDetect caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code DetectionBasedTracker.nativeDetect()");
    }
    LOGD("nativeDetect END");
}


// 参考 http://blog.csdn.net/nicebooks/article/details/8175002
JNIEXPORT jdouble JNICALL
Java_com_cl_slack_opencv_JNI_nativeFaceRecognition(JNIEnv *env, jclass type, jlong thiz,
                                                   jlong face1, jlong face2) {

    return faceRecognition(thiz, face1, face2);
}

void swap(unsigned char& a, unsigned char& b){
    char c = a;
    a = b;
    b = c;
}

void rotate90(Mat mat) {
    int w = mat.cols;
    int h = mat.rows;
    for (int i = 0; i < w; i++)
    {
        for (int j = 0; j < h; j++)
        {
            int I = h - 1 - j;
            int J = i;
            while ((i*h + j) > (I*w + J))
            {
                int p = I*w + J;
                int tmp_i = p / h;
                int tmp_j = p % h;
                I = h - 1 - tmp_j;
                J = tmp_i;
            }
            swap(*(mat.data + i*h*3 + j*3 + 0), *(mat.data + I*w*3 + J*3 + 0));
            swap(*(mat.data + i*h*3 + j*3 + 1), *(mat.data + I*w*3 + J*3 + 1));
            swap(*(mat.data + i*h*3 + j*3 + 2), *(mat.data + I*w*3 + J*3 + 2));
        }
    }

    mat.cols = h;
    mat.rows = w;
    mat.step = h*3;
}

JNIEXPORT void JNICALL
Java_com_cl_slack_opencv_JNI_rotate(JNIEnv *env, jclass type, jlong img, jint degree) {

    rotate90(*(Mat*) img);

}

}

//
// Created by slack on 17/8/13.
//
#include <string>
#include <opencv2/opencv.hpp>
#include <android/log.h>

#define LOG_TAG "FaceDetection/DetectionBasedTracker"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

using namespace std;
using namespace cv;

inline void vector_Rect_to_Mat(vector<Rect>& v_rect, Mat& mat)
{
    mat = Mat(v_rect, true);
}

class CascadeDetectorAdapter: public DetectionBasedTracker::IDetector
{
public:
    CascadeDetectorAdapter(cv::Ptr<cv::CascadeClassifier> detector):
            IDetector(),
            Detector(detector)
    {
        LOGD("CascadeDetectorAdapter::Detect::Detect");
        CV_Assert(detector);
    }

    void detect(const cv::Mat &Image, std::vector<cv::Rect> &objects)
    {
        LOGD("CascadeDetectorAdapter::Detect: begin");
        LOGD("CascadeDetectorAdapter::Detect: scaleFactor=%.2f, minNeighbours=%d, minObjSize=(%dx%d), maxObjSize=(%dx%d)", scaleFactor, minNeighbours, minObjSize.width, minObjSize.height, maxObjSize.width, maxObjSize.height);
        Detector->detectMultiScale(Image, objects, scaleFactor, minNeighbours, 0, minObjSize, maxObjSize);
        LOGD("CascadeDetectorAdapter::Detect: end");
    }

    virtual ~CascadeDetectorAdapter()
    {
        LOGD("CascadeDetectorAdapter::Detect::~Detect");
    }

private:
    CascadeDetectorAdapter();
    cv::Ptr<cv::CascadeClassifier> Detector;
};

struct DetectorAgregator
{
    cv::Ptr<CascadeDetectorAdapter> mainDetector;
    cv::Ptr<CascadeDetectorAdapter> trackingDetector;

    cv::Ptr<DetectionBasedTracker> tracker;
    DetectorAgregator(cv::Ptr<CascadeDetectorAdapter>& _mainDetector, cv::Ptr<CascadeDetectorAdapter>& _trackingDetector):
            mainDetector(_mainDetector),
            trackingDetector(_trackingDetector)
    {
        CV_Assert(_mainDetector);
        CV_Assert(_trackingDetector);

        DetectionBasedTracker::Parameters DetectorParams;
        tracker = makePtr<DetectionBasedTracker>(mainDetector, trackingDetector, DetectorParams);
    }
};


IplImage* cutImage(IplImage* src, CvRect rect) {
    cvSetImageROI(src, rect);
    IplImage* dst = cvCreateImage(cvSize(rect.width, rect.height),
                                  src->depth,
                                  src->nChannels);

    cvCopy(src,dst,0);
    cvResetImageROI(src);
    return dst;
}


IplImage* detect(jlong thiz, jlong img) {
    vector<Rect> faces;
    Mat mat = *(Mat*) img;
    ((DetectorAgregator*)thiz)->mainDetector->detect(mat, faces);
    for( vector<Rect>::const_iterator r = faces.begin(); r != faces.end(); r++ )
    {
        IplImage image = IplImage(mat);
        IplImage* temp = cutImage(&image, cvRect(r->x, r->y, r->width, r->height));
        return temp;
    }

    return NULL;
}

int HistogramBins = 256;
float HistogramRange1[2]={0,255};
float *HistogramRange[1]={&HistogramRange1[0]};
double compareHist(IplImage* image1, IplImage* image2)
{
    IplImage* srcImage;
    IplImage* targetImage;
    if (image1->nChannels != 1) {
        srcImage = cvCreateImage(cvSize(image1->width, image1->height), image1->depth, 1);
        cvCvtColor(image1, srcImage, CV_BGR2GRAY);
    } else {
        srcImage = image1;
    }

    if (image2->nChannels != 1) {
        targetImage = cvCreateImage(cvSize(image2->width, image2->height), srcImage->depth, 1);
        cvCvtColor(image2, targetImage, CV_BGR2GRAY);
    } else {
        targetImage = image2;
    }

    CvHistogram *Histogram1 = cvCreateHist(1, &HistogramBins, CV_HIST_ARRAY,HistogramRange);
    CvHistogram *Histogram2 = cvCreateHist(1, &HistogramBins, CV_HIST_ARRAY,HistogramRange);

    cvCalcHist(&srcImage, Histogram1);
    cvCalcHist(&targetImage, Histogram2);

    cvNormalizeHist(Histogram1, 1);
    cvNormalizeHist(Histogram2, 1);

    // CV_COMP_CHISQR,CV_COMP_BHATTACHARYYA这两种都可以用来做直方图的比较，值越小，说明图形越相似
    double  result1 = cvCompareHist(Histogram1, Histogram2, CV_COMP_CHISQR);
    double  result2 = cvCompareHist(Histogram1, Histogram2, CV_COMP_BHATTACHARYYA);
    printf("CV_COMP_CHISQR : %.4f\n", result1);
    printf("CV_COMP_BHATTACHARYYA : %.4f\n", result2);


    // CV_COMP_CORREL, CV_COMP_INTERSECT这两种直方图的比较，值越大，说明图形越相似
    double result1_1 = cvCompareHist(Histogram1, Histogram2, CV_COMP_CORREL);
    double result2_1 = cvCompareHist(Histogram1, Histogram2, CV_COMP_INTERSECT);
    printf("CV_COMP_CORREL : %.4f\n", result1_1);
    printf("CV_COMP_INTERSECT : %.4f\n", result2_1);

    cvReleaseHist(&Histogram1);
    cvReleaseHist(&Histogram2);
    if (image1->nChannels != 1) {
        cvReleaseImage(&srcImage);
    }
    if (image2->nChannels != 1) {
        cvReleaseImage(&targetImage);
    }
    return (result1_1 + result2_1) / 2;
}

double faceRecognition(jlong thiz, jlong face1, jlong face2) {
    IplImage* faceImage1 = detect(thiz, face1);
    if (faceImage1 == NULL) {
        return -1;
    }

    IplImage* faceImage2 = detect(thiz, face2);
    if (faceImage2 == NULL) {
        return -1;
    }

    return compareHist(faceImage1, faceImage2);

}

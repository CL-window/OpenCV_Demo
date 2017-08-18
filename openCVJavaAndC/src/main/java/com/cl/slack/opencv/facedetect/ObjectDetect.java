package com.cl.slack.opencv.facedetect;

import android.content.Context;
import android.support.annotation.RawRes;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by slack
 * on 17/8/17 下午7:39
 * object detect base class
 */

public abstract class ObjectDetect {

    private CascadeClassifier       mCascadeClassifier;
    private float                   mRelativeFaceSize   = 0.2f;
    private int                     mAbsoluteFaceSize   = 0;

    public ObjectDetect(Context context) {
        mCascadeClassifier = createDetector(context);
    }

    /**
     * 创建检测器
     * call after OpenCVLoader.initAsync callback
     * @return 检测器
     */
    private CascadeClassifier createDetector(Context context) {
        CascadeClassifier javaDetector;
        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = context.getResources().openRawResource(getRawId());
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, getRawId() + ".xml");
            os = new FileOutputStream(cascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            javaDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());
            if (javaDetector.empty()) {
                javaDetector = null;
            }

            boolean delete = cascadeDir.delete();
            return javaDetector;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
                if (null != os) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置
     * @param faceSize minFace size
     * if user call this, mAbsoluteFaceSize = 0, in detectObject will recalculate
     */
    public void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    /**
     * @param gray src
     * @param result result
     */
    public void detectObject(Mat gray, MatOfRect result){

        if (mAbsoluteFaceSize == 0) {
            int height = gray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        detectObject(gray, result, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize));
    }

    public void detectObject(Mat gray, MatOfRect result, Size minSize){

        /**
         * detectMultiScale(
         *                  Mat image, //输入图像,一般为灰度图
         *                  MatOfRect objects, //检测到的Rect[],存放所有检测出的人脸，每个人脸是一个矩形
         *                  double scaleFactor, //缩放比例,对图片进行缩放，默认为1.1
         *                  int minNeighbors, //合并窗口时最小neighbor，每个候选矩阵至少包含的附近元素个数，默认为3
         *                  int flags,  //检测标记，只对旧格式的分类器有效，与cvHaarDetectObjects的参数flags相同，在3.0版本中没用处, 默认为0，
         *            可能的取值为CV_HAAR_DO_CANNY_PRUNING(CANNY边缘检测)、CV_HAAR_SCALE_IMAGE(缩放图像)、
         *            CV_HAAR_FIND_BIGGEST_OBJECT(寻找最大的目标)、CV_HAAR_DO_ROUGH_SEARCH(做粗略搜索)；
         *            如果寻找最大的目标就不能缩放图像，也不能CANNY边缘检测
         *                  Size minSize, //检测出的人脸最小尺寸
         *                  Size maxSize //检测出的人脸最大尺寸
         *                  )
         */
        mCascadeClassifier.detectMultiScale(gray,
                result,
                1.1,
                3,
                Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_SCALE_IMAGE,
                minSize,
                new Size());
    }


    protected @RawRes abstract int getRawId();
}

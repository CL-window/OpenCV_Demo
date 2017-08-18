package com.cl.slack.opencv.facedetect.face;

import android.content.Context;

import com.cl.slack.opencv.facedetect.ObjectDetect;

import org.opencv.R;
import org.opencv.android.OpenCVLoader;

/**
 * Created by slack
 * on 17/8/17 下午7:34
 * 人脸检测
 * init after OpenCVLoader.initAsync callback
 * {@link OpenCVLoader#initAsync(java.lang.String, android.content.Context, org.opencv.android.LoaderCallbackInterface)}
 */

public class FaceDetect extends ObjectDetect {


    public FaceDetect(Context context) {
        super(context);
    }

    @Override
    protected int getRawId() {
        // ［＊］ 横屏检测效果好
        return R.raw.lbpcascade_frontalface;
        // 检测效果不如上一个
//        return R.raw.lbpcascade_frontalcatface;
        // ［＊］ 横屏检测效果很好
//        return R.raw.lbpcascade_frontalface_improved;
        // 检测人脸 右侧 时，同样是横屏
//        return R.raw.lbpcascade_profileface;

        // 效果不好，太卡
//        return R.raw.haarcascade_frontalcatface;
        // 效果不好，太卡
//        return R.raw.haarcascade_frontalcatface_extended;
        // 检测效果还可以，就是卡
//        return R.raw.haarcascade_frontalface_alt;
        // 检测效果还可以，就是卡
//        return R.raw.haarcascade_frontalface_alt2;
        // 没有效果
//        return R.raw.haarcascade_frontalface_alt_tree;
        // 瞎检测，鼻子嘴巴也认为是脸，不准
//        return R.raw.haarcascade_frontalface_default;
        // 侧脸可以，正脸也可以，就是卡
//        return R.raw.haarcascade_profileface;

        // 目前不清楚做什么使用的
//        return R.raw.haarcascade_licence_plate_rus_16stages;
//        return R.raw.haarcascade_russian_plate_number;
    }


}

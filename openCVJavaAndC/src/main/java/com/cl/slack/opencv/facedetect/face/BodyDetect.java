package com.cl.slack.opencv.facedetect.face;

import android.content.Context;

import com.cl.slack.opencv.facedetect.ObjectDetect;

import org.opencv.R;

/**
 * created by slack
 * on 17/8/18 上午11:32
 *
 * 身体检测
 * - upper body detector (most fun, useful in many scenarios!)
 * - lower body detector
 * - full body detector
 */

public class BodyDetect extends ObjectDetect {


    public BodyDetect(Context context) {
        super(context);
    }

    @Override
    protected int getRawId() {
        // 上半身，下半身，全身 检测 未测试
        return R.raw.haarcascade_fullbody;

//        return R.raw.haarcascade_upperbody;

//        return R.raw.haarcascade_lowerbody;
    }


}

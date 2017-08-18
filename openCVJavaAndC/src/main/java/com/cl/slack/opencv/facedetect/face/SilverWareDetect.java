package com.cl.slack.opencv.facedetect.face;

import android.content.Context;

import com.cl.slack.opencv.facedetect.ObjectDetect;

import org.opencv.R;

/**
 * created by slack
 * on 17/8/18 上午11:32
 * This is 12x80 detector of the silverware (forks, spoons, knives) using LBP features.
 */

public class SilverWareDetect extends ObjectDetect {


    public SilverWareDetect(Context context) {
        super(context);
    }

    @Override
    protected int getRawId() {
        // 检测银器的，身边没有银的，没有测试
        return R.raw.lbpcascade_silverware;
    }


}

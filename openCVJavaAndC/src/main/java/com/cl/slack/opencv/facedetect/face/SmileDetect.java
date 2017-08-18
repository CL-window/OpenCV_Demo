package com.cl.slack.opencv.facedetect.face;

import android.content.Context;

import com.cl.slack.opencv.facedetect.ObjectDetect;

import org.opencv.R;

/**
 * created by slack
 * on 17/8/18 上午11:32
 * Smile detector
 */

public class SmileDetect extends ObjectDetect {


    public SmileDetect(Context context) {
        super(context);
    }

    @Override
    protected int getRawId() {
        // 瞎几把检测，极度不准
        return R.raw.haarcascade_smile;
    }


}

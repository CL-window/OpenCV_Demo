package com.cl.slack.opencv.facedetect.face;

import android.content.Context;

import com.cl.slack.opencv.facedetect.ObjectDetect;

import org.opencv.R;
import org.opencv.android.OpenCVLoader;

/**
 * Created by slack
 * on 17/8/17 下午7:34
 * 右边眼睛检测
 */

public class EyeRightDetect extends ObjectDetect {


    public EyeRightDetect(Context context) {
        super(context);
    }

    @Override
    protected int getRawId() {
        // [＊] 检测右眼 效果看得见
        return R.raw.haarcascade_righteye_2splits;
    }


}

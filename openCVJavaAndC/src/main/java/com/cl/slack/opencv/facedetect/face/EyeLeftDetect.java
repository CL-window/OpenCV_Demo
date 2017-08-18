package com.cl.slack.opencv.facedetect.face;

import android.content.Context;

import com.cl.slack.opencv.facedetect.ObjectDetect;

import org.opencv.R;
import org.opencv.android.OpenCVLoader;

/**
 * Created by slack
 * on 17/8/17 下午7:34
 * 左边眼睛检测
 */

public class EyeLeftDetect extends ObjectDetect {


    public EyeLeftDetect(Context context) {
        super(context);
    }

    @Override
    protected int getRawId() {
        // 只是检测左眼
//        return R.raw.haarcascade_eye;
        // 卡，效果极度不好
//        return R.raw.haarcascade_eye_tree_eyeglasses;
        // [＊] 检测左眼 效果看得见
        return R.raw.haarcascade_lefteye_2splits;
    }


}

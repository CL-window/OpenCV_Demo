package com.cl.slack.puzzle.puzzle;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cl.slack.puzzle.R;

/**
 * <p>Description: 拍照延时 </p>
 * Created by slack on 2016/7/25 14:30 .
 */
public class TimeDelay extends LinearLayout{

    private View view;
    private int timeCount;
    private ImageView imageView;
    private Context context;
    private Handler handler = new Handler();
    private onTimeDelayListener timeDelayListener;

    private int[] delayImg = {R.drawable.count_1,R.drawable.count_2,R.drawable.count_3};

    public TimeDelay(Context context) {
        this(context, 3);
    }

    public TimeDelay(Context context, int timeCount) {
        this(context,null);
        this.timeCount = timeCount;
    }

    public TimeDelay(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TimeDelay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        view = LayoutInflater.from(context).inflate(R.layout.popup_time_delay, this);
        initView();
    }

    private void initView() {
//        timeView = (TextView) view.findViewById(R.id.time_show);
        imageView = (ImageView)findViewById(R.id.time_delay_show);

    }

    public void setTimeDelayListener(onTimeDelayListener timeDelayListener){
        this.timeDelayListener = timeDelayListener;
        handler.post(timeShow);
    }

    Runnable timeShow = new Runnable() {
        @Override
        public void run() {
            if(timeCount > 0){

                imageView.setImageResource(delayImg[timeCount - 1]);
                imageView.startAnimation(AnimationUtils.loadAnimation(context,
                        R.anim.al_sc_time_count));
                timeCount --;
                handler.postDelayed(timeShow,1000);
            }else{
                view.setVisibility(View.GONE);
                if( timeDelayListener != null){
                    timeDelayListener.timeDelayDone();
                }
            }
        }
    };

    public void cancelTimeDelay(){
        handler.removeCallbacks(timeShow);
    }

    public interface onTimeDelayListener{
        void timeDelayDone();
    }
}

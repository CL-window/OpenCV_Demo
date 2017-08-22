package com.cl.slack.puzzle.animation;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.cl.slack.puzzle.R;

/**
 * created by slack
 * on 17/8/22 下午3:11
 */

public class AlertDialog extends Dialog {

    private RelativeLayout mLayout;
    private Button mShareBtn;
    private FllowerAnimation mFllowerAnimation;

    public AlertDialog(Context context) {
        this(context, R.style.selectorDialog);
    }

    public AlertDialog(Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    @Override
    public void show() {
        try {
            super.show();
            mFllowerAnimation.startAnimation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        setContentView(R.layout.popup_alert);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        mLayout = (RelativeLayout) findViewById(R.id.puzzle_animation_layout);
        mShareBtn = (Button) findViewById(R.id.puzzle_share);
        mFllowerAnimation = new FllowerAnimation(getContext());
        mFllowerAnimation.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        mLayout.addView(mFllowerAnimation);
        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareFromLocal();
            }
        });
    }

    private void shareFromLocal() {
        Context context = getContext();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.puzzle_share_msg) + " http://fir.im/7pu5");

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent i = Intent.createChooser(intent, context.getString(R.string.puzzle_share));
        context.startActivity(i);
    }

}

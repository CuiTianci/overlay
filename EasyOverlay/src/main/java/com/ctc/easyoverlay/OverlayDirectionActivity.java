package com.ctc.easyoverlay;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class OverlayDirectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overlay_direction);
        initViews();
        startAnimation();
    }

    FloatingActionButton mFabSwitchToggle;
    View mVSwitchBg;
    ImageView mIvFinger;
    RelativeLayout mRlPanel;

    void initViews() {
        mIvFinger = findViewById(R.id.iv_finger);
        mFabSwitchToggle = findViewById(R.id.fab_switch_toggle);
        mVSwitchBg = findViewById(R.id.v_switch_bg);
        mRlPanel = findViewById(R.id.rl_panel);
        mRlPanel.setOnClickListener(v -> finish());
    }

    int mAnimBounds = -1;
    ValueAnimator mAnimator;

    void startAnimation() {
        if (mAnimBounds == -1)
            mAnimBounds = getResources().getDimensionPixelSize(R.dimen.dp_30);
        mAnimator = ValueAnimator.ofInt(0, mAnimBounds);
        mAnimator.setDuration(1100);
        mAnimator.setRepeatCount(1000);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setStartDelay(800);
        mAnimator.addUpdateListener(animation -> {
            int currentValue = (int) animation.getAnimatedValue();
            // mVSwitchBg的宽度增加
            resetSwitchBgPosition(currentValue);
            // mFabSwitchToggle移动
            resetSwitchTogglePosition(currentValue);
            // mIvFinger移动
            resetFingerPosition(currentValue);
        });
        mAnimator.start();
    }

    float mOriginalSwitchBgPosition = -1;

    void resetSwitchBgPosition(int offset) {
        if (mOriginalSwitchBgPosition == -1)
            mOriginalSwitchBgPosition = mVSwitchBg.getX();

        mVSwitchBg.setX(mOriginalSwitchBgPosition + offset);
    }

    float mOriginalTogglePosition = -1;

    void resetSwitchTogglePosition(int offset) {
        if (mOriginalTogglePosition == -1)
            mOriginalTogglePosition = mFabSwitchToggle.getX();

        mFabSwitchToggle.setX(mOriginalTogglePosition + offset);

        if (offset * 1.5 < mAnimBounds)
            mFabSwitchToggle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.perm_overlay_reminder_close)));
        else
            mFabSwitchToggle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.perm_overlay_reminder_open)));
    }

    float mOriginalFingerPosition = -1;

    void resetFingerPosition(int offset) {
        if (mOriginalFingerPosition == -1)
            mOriginalFingerPosition = mIvFinger.getX();

        mIvFinger.setX(mOriginalFingerPosition + offset);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
    }
}

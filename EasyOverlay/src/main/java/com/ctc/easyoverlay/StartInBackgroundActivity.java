package com.ctc.easyoverlay;

import android.animation.Animator;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

import java.lang.ref.WeakReference;

public class StartInBackgroundActivity extends AppCompatActivity {

    private static final int MSG_DELAY_START_IN_BG = 100;
    private static final int MSG_DELAY_OVERLAY = 101;

    private LottieAnimationView mStartInBgAnim;
    private LottieAnimationView mOverlayAnim;
    private ImageView mStartInBgIcon;
    private ImageView mOverlayIcon;
    private TranslateAnimation mTranslateAnimation;
    private MainHandler mMainHandler;

    private static class MainHandler extends Handler {
        private final WeakReference<StartInBackgroundActivity> weakReference;

        public MainHandler(StartInBackgroundActivity context) {
            weakReference = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            StartInBackgroundActivity context = weakReference.get();
            if (context == null) {
                return;
            }
            switch (msg.what) {
                case MSG_DELAY_START_IN_BG:
                    context.startInBgAnim();
                    break;
                case MSG_DELAY_OVERLAY:
                    context.startOverlayAnim();
                    break;
            }
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_in_backgound);
        mStartInBgIcon = findViewById(R.id.start_in_bg_icon);
        mOverlayIcon = findViewById(R.id.overlay_icon);
        mStartInBgAnim = findViewById(R.id.start_in_bg_anim);
        mOverlayAnim = findViewById(R.id.overlay_anim);
        View mStartInBgLayout = findViewById(R.id.start_in_bg_layout);
        View mOverlayLayout = findViewById(R.id.overlay_layout);
        View mSpacer = findViewById(R.id.spacer);

        View mOverlayAnim = findViewById(R.id.perm_guide_layout);
        mTranslateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        mTranslateAnimation.setDuration(500);
        mTranslateAnimation.setInterpolator(new LinearInterpolator());
        mOverlayAnim.startAnimation(mTranslateAnimation);
        findViewById(R.id.perm_guide_container).setOnClickListener(v -> finish());
        mMainHandler = new MainHandler(this);

        if (!BackgroundLaunchPermissionUtil.Companion.isPermissionGranted(this) &&
                (aboveAndroid() && !hasOverlayPermission(this))) {
            mSpacer.setVisibility(View.VISIBLE);
        } else {
            mSpacer.setVisibility(View.GONE);
        }
        if (!BackgroundLaunchPermissionUtil.Companion.isPermissionGranted(this)) {
            startInBgAnim();
            mStartInBgLayout.setVisibility(View.VISIBLE);
        } else {
            mStartInBgLayout.setVisibility(View.GONE);
        }
        if (aboveAndroid() && !hasOverlayPermission(this)) {
            startOverlayAnim();
            mOverlayLayout.setVisibility(View.VISIBLE);
        } else {
            mOverlayLayout.setVisibility(View.GONE);
        }
    }

    private boolean aboveAndroid() {
        return Build.VERSION.SDK_INT >= 29;
    }

    private boolean hasOverlayPermission(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                Settings.canDrawOverlays(context);
    }

    private void startInBgAnim() {
        mStartInBgIcon.setImageResource(R.drawable.ic_red_on);
        mStartInBgAnim.playAnimation();
        mStartInBgAnim.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mStartInBgIcon.setImageResource(R.drawable.ic_red_off);
                mStartInBgAnim.cancelAnimation();
                mStartInBgAnim.removeAllAnimatorListeners();
                Message message = new Message();
                message.what = MSG_DELAY_START_IN_BG;
                mMainHandler.sendMessageDelayed(message, 1500);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }


    private void startOverlayAnim() {
        mOverlayIcon.setImageResource(R.drawable.ic_red_on);
        mOverlayAnim.playAnimation();
        mOverlayAnim.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mOverlayIcon.setImageResource(R.drawable.ic_red_off);
                mOverlayAnim.cancelAnimation();
                mOverlayAnim.removeAllAnimatorListeners();
                Message message = new Message();
                message.what = MSG_DELAY_OVERLAY;
                mMainHandler.sendMessageDelayed(message, 1500);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTranslateAnimation != null) {
            mTranslateAnimation.cancel();
        }
        if (mStartInBgAnim != null) {
            mStartInBgAnim.cancelAnimation();
            mStartInBgAnim.removeAllAnimatorListeners();
        }
        if (mOverlayAnim != null) {
            mOverlayAnim.cancelAnimation();
            mOverlayAnim.removeAllAnimatorListeners();
        }
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
        }
    }
}

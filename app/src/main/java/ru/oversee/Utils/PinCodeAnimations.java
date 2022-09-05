package ru.oversee.Utils;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.oversee.R;

public class PinCodeAnimations {

    public static void clickAnimationNumbersCircles(ImageView numberCircle, int color) {
        Animation scaleUp = new ScaleAnimation(1, 1.4f, 1, 1.4f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleUp.setDuration(200);
        Animation scaleDown = new ScaleAnimation(1.4f, 1, 1.4f, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleDown.setDuration(200);
        scaleUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                numberCircle.setBackgroundTintList(ColorStateList.valueOf(color));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                numberCircle.startAnimation(scaleDown);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        numberCircle.startAnimation(scaleUp);
    }

    public static void accessPinAnimation(ImageView numberCircle, int color) {
        Animation scaleUp = new ScaleAnimation(1, 1.4f, 1, 1.4f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleUp.setDuration(200);

        Animation scaleDown = new ScaleAnimation(1.4f, 1, 1.4f, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleDown.setDuration(200);

        Animation moveCenterRight = AnimationUtils.loadAnimation(numberCircle.getContext(), R.anim.move_center_right);
        moveCenterRight.setDuration(200);

        scaleUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                numberCircle.setBackgroundTintList(ColorStateList.valueOf(color));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                numberCircle.startAnimation(scaleDown);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        scaleDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                numberCircle.startAnimation(moveCenterRight);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        numberCircle.startAnimation(scaleUp);
    }

    public static void setDefaultNumbersCircles(ArrayList<ImageView> numbersCircles, ArrayList<TextView> numberButtons, boolean fastFinish) {
        for (int i = 0; i < numberButtons.size(); i++) numberButtons.get(i).setEnabled(false);

        new CountDownTimer(400, 400) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                for (int i = 0; i < numbersCircles.size(); i++) {
                    numbersCircles.get(i).clearAnimation();
                    numbersCircles.get(i).setBackgroundTintList(ColorStateList.valueOf(numbersCircles.get(i).getContext().getColor(R.color.whiteTransparent1)));
                }
                for (int i = 0; i < numberButtons.size(); i++) numberButtons.get(i).setEnabled(true);
            }
        }.start();

//        if (fastFinish) countDownTimer.onFinish();
    }

    public static void clickAnimationNumbers(View numberButton, boolean downAction) {
        int colorFrom = numberButton.getContext().getColor(downAction ? R.color.transparent : R.color.whiteTransparent1);
        int colorTo = numberButton.getContext().getColor(downAction ? R.color.whiteTransparent1 : R.color.transparent);

        ObjectAnimator.ofObject(numberButton, "backgroundColor", new ArgbEvaluator(), colorFrom, colorTo)
                .setDuration(200)
                .start();
    }

}

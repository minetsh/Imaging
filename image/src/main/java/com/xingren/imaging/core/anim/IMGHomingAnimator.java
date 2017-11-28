package com.xingren.imaging.core.anim;

import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.xingren.imaging.core.homing.IMGHomingEvaluator;

/**
 * Created by felix on 2017/11/28 下午12:54.
 */

public class IMGHomingAnimator extends ValueAnimator {

    private IMGHomingEvaluator mEvaluator;

    public IMGHomingAnimator() {
        setInterpolator(new AccelerateDecelerateInterpolator());
    }

    @Override
    public void setObjectValues(Object... values) {
        super.setObjectValues(values);
        if (mEvaluator == null) {
            mEvaluator = new IMGHomingEvaluator();
        }
        setEvaluator(mEvaluator);
    }
}

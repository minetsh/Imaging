package me.kareluo.imaging.core.elastic;

import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by felix on 2017/11/27 下午5:22.
 */

public class IMGElasticAnimator extends ValueAnimator {

    private IMGElastic mElastic;

    public IMGElasticAnimator() {
        setEvaluator(new IMGPointFEvaluator());
        setInterpolator(new AccelerateDecelerateInterpolator());
    }

    public IMGElasticAnimator(IMGElastic elastic) {
        this();
        setElastic(elastic);
    }

    public void setElastic(IMGElastic elastic) {
        mElastic = elastic;

        if (mElastic == null) {
            throw new IllegalArgumentException("IMGElastic cannot be null.");
        }
    }

    public void start(float x, float y) {
        setObjectValues(new PointF(x, y), mElastic.getPivot());
        start();
    }
}

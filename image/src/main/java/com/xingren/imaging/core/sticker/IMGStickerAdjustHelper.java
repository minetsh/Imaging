package com.xingren.imaging.core.sticker;

import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by felix on 2017/11/15 下午5:44.
 */

public class IMGStickerAdjustHelper implements View.OnTouchListener {

    private static final String TAG = "IMGStickerAdjustHelper";

    private View mContainer, mView;

    private float mCenterX;
    private float mCenterY;

    private double mRadius;

    private static final Matrix ROTATION = new Matrix();

    public IMGStickerAdjustHelper(View container, View view) {
        mView = view;
        mContainer = container;
        mView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();

                mCenterX = mCenterY = 0;
                float pointX = mView.getX() - mContainer.getPivotX() + x;
                float pointY = mView.getY() - mContainer.getPivotY() + y;

                mRadius = toLength(mCenterX, mCenterY, pointX, pointY);

                ROTATION.reset();
                ROTATION.setTranslate(pointX - x, pointY - y);
                ROTATION.postRotate((float) -toDegrees(pointY, pointX), mCenterX, mCenterY);

                return true;

            case MotionEvent.ACTION_MOVE:

                float[] xy = {event.getX(), event.getY()};
                ROTATION.mapPoints(xy);

                double scale = toLength(mCenterX, mCenterY, xy[0], xy[1]) / mRadius;
                double scaleX = mContainer.getScaleX() * scale;
                double scaleY = mContainer.getScaleY() * scale;

                mContainer.setScaleX((float) scaleX);
                mContainer.setScaleY((float) scaleY);

                mContainer.setRotation((float) ((mContainer.getRotation() + toDegrees(xy[1], xy[0])) % 360f));

                return true;
        }
        return false;
    }

    private static double toDegrees(float v, float v1) {
        return Math.toDegrees(Math.atan2(v, v1));
    }

    private static double toLength(float x1, float y1, float x2, float y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }
}

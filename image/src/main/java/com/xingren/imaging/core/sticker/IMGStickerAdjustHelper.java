package com.xingren.imaging.core.sticker;

import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.xingren.imaging.view.IMGStickerView;

/**
 * Created by felix on 2017/11/15 下午5:44.
 */

public class IMGStickerAdjustHelper implements View.OnTouchListener {

    private static final String TAG = "IMGStickerAdjustHelper";

    private View mView;

    private IMGStickerView mContainer;

    private float mCenterX, mCenterY;

    private double mRadius;

    private Matrix M = new Matrix();

    public IMGStickerAdjustHelper(IMGStickerView container, View view) {
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

                float pointX = (mView.getLeft() + mView.getRight()) / 2f;
                float pointY = (mView.getTop() + mView.getBottom()) / 2f;

                Log.d(TAG, String.format("X=%f, Y=%f, PivotX=%f, PivotY=%f", x, y, pointX, pointY));

                mRadius = toLength(mCenterX, mCenterY, pointX, pointY);

                M.setTranslate(pointX - x, pointY - y);

                Log.d(TAG, String.format("degrees=%f", toDegrees(pointY, pointX)));

                M.postRotate((float) -toDegrees(pointY, pointX), mCenterX, mCenterY);

                return true;

            case MotionEvent.ACTION_MOVE:

                float[] xy = {event.getX(), event.getY()};
                Log.d(TAG, String.format("Raw x=%f, y=%f", xy[0], xy[1]));

                M.mapPoints(xy);

                float scale = (float) (toLength(mCenterX, mCenterY, xy[0], xy[1]) / mRadius);

                Log.d(TAG, "Scale=" + scale);
                mContainer.addScale(scale);

                Log.d(TAG, String.format("x=%f,y=%f", xy[0], xy[1]));
                Log.d(TAG, String.format("dDegrees=%f", toDegrees(xy[1], xy[0])));

                float degrees = (float) ((mContainer.getRotation() + toDegrees(xy[1], xy[0])) % 360f);
                Log.d(TAG, "degrees=" + degrees);
                Log.d(TAG, "  ");
                mContainer.setRotation(degrees);

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

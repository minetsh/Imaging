package com.xingren.imaging.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Created by felix on 2017/11/21 下午10:03.
 */

public class IMGImage {

    private Bitmap mImage;

    private boolean isInit = false;

    private RectF mFrame = new RectF();

    private static final int MIN_SIZE = 200;

    private static final int MAX_SIZE = 10000;

    private static final Matrix MATRIX = new Matrix();

    public IMGImage(Bitmap bitmap) {
        this.mImage = bitmap;
        this.mFrame = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    public void init(float centerX, float centerY, int width, int height) {
        if (!isInit) {
            mFrame.set(0, 0, mImage.getWidth(), mImage.getHeight());
            if (mFrame.width() == 0 || mFrame.height() == 0) return;
            float scale = Math.min(width / mFrame.width(), height / mFrame.height());
            MATRIX.reset();
            MATRIX.setScale(scale, scale, mFrame.centerX(), mFrame.centerY());
            MATRIX.postTranslate(centerX - mFrame.centerX(), centerY - mFrame.centerY());
            MATRIX.mapRect(mFrame);
            isInit = true;
        }
    }

    public void onDraw(Canvas canvas) {
        canvas.clipRect(mFrame);
        canvas.drawBitmap(mImage, null, mFrame, null);
    }

    public void adjust(float centerX, float centerY, int width, int height) {
        MATRIX.reset();
        float scale = Math.min(width / mFrame.width(), height / mFrame.height());
        MATRIX.setScale(scale, scale, mFrame.centerX(), mFrame.centerY());
        MATRIX.postTranslate(centerX - mFrame.centerX(), centerY - mFrame.centerY());
        MATRIX.mapRect(mFrame);
    }

    public void onScaleBegin() {

    }

    public void onScale(float factor, float focusX, float focusY) {

        if (factor == 1f) return;

        if (factor > 1f && Math.max(mFrame.width(), mFrame.height()) >= MAX_SIZE) {
            return;
        } else if (factor < 1f && Math.min(mFrame.width(), mFrame.height()) <= MIN_SIZE) {
            return;
        }

        MATRIX.reset();
        MATRIX.setScale(factor, factor, focusX, focusY);
        MATRIX.mapRect(mFrame);
    }

    public void onScaleEnd() {

    }
}

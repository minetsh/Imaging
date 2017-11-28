package com.xingren.imaging.view;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.xingren.imaging.core.IMGImage;
import com.xingren.imaging.core.IMGMode;
import com.xingren.imaging.core.anim.IMGHomingAnimator;
import com.xingren.imaging.core.homing.IMGHoming;
import com.xingren.imaging.core.sticker.IMGSticker;
import com.xingren.imaging.core.sticker.IMGStickerPortrait;

/**
 * Created by felix on 2017/11/16 下午12:45.
 */

class IMGDelegate implements ScaleGestureDetector.OnScaleGestureListener,
        View.OnLayoutChangeListener, ValueAnimator.AnimatorUpdateListener, IMGStickerPortrait.Callback {

    private static final String TAG = "IMGDelegate";

    private IMGView mView;

    private float mX, mY, mScrollX, mScrollY;

    private IMGImage mImage = new IMGImage();

    private GestureDetector mGestureDetector;

    private ScaleGestureDetector mScaleGestureDetector;

    private IMGHomingAnimator mHomingAnimator;

    IMGDelegate(IMGView view) {
        mView = view;
        mView.addOnLayoutChangeListener(this);
        mGestureDetector = new GestureDetector(view.getContext(), new MoveAdapter());
        mScaleGestureDetector = new ScaleGestureDetector(view.getContext(), this);
    }

    void setImageBitmap(Bitmap image) {
        mImage.setBitmap(image);
        mView.invalidate();
    }

    IMGImage getImage() {
        return mImage;
    }

    /**
     * 是否真正修正归位
     */
    boolean isHoming() {
        return mHomingAnimator != null && mHomingAnimator.isRunning();
    }

    boolean onInterceptTouch(MotionEvent event) {
        return isHoming();
    }

    boolean onTouch(MotionEvent event) {
        if (isHoming()) return false;

        boolean handled = mScaleGestureDetector.onTouchEvent(event);
        IMGMode mode = mImage.getMode();
        if (mode == IMGMode.NONE) {
            handled |= onTouchNONE(event);
        } else {
            switch (mode) {
                case DOODLE:
                    return onTouchDOODLE(event);
            }
        }
        return handled;
    }

    private boolean onTouchNONE(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP:
                onHoming();
                break;
        }

        return mGestureDetector.onTouchEvent(event);
    }

    private boolean onTouchDOODLE(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mImage.onTouch();
                mX = event.getX();
                mY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:

                return true;
        }
        return false;
    }

    private void onHoming() {


        final RectF frame = mImage.getFrame();


        float x = mView.getScrollX();
        float y = mView.getScrollY();
        final RectF startRect = new RectF(mImage.getFrame());
        startRect.offsetTo(x, y);


        float tx = frame.centerX() - mView.getPivotX();
        float ty = frame.centerY() - mView.getPivotY();
        final RectF endRect = new RectF(tx, ty, tx + 500, ty + 500);

        float scale = mImage.getScale();

        final float pivotX = frame.centerX(), pivotY = frame.centerY();

        IMGHoming startHoming = new IMGHoming(startRect.left, startRect.top, scale);
        IMGHoming endHoming = new IMGHoming(endRect.left, endRect.top, scale * endRect.width() / frame.width());


        RectF winr = new RectF(mView.getScrollX(), mView.getScrollY(), mView.getScrollX() + mView.getWidth(), mView.getScrollY() + mView.getHeight());

        startHoming(mImage.getStartHoming(mView.getScrollX(), mView.getScrollY()), mImage.getEndHoming(mView.getScrollX(), mView.getScrollY()));
    }

    private void startHoming(IMGHoming sHoming, IMGHoming eHoming) {
        if (mHomingAnimator == null) {
            mHomingAnimator = new IMGHomingAnimator();
            mHomingAnimator.addUpdateListener(this);
        }
        mHomingAnimator.setObjectValues(sHoming, eHoming);
        mHomingAnimator.start();
    }

    private boolean onScrollBy(float dx, float dy) {
        mView.scrollBy(Math.round(dx), Math.round(dy));
        return true;
    }

    private void onScrollTo(float x, float y) {
        mView.scrollTo(Math.round(x), Math.round(y));
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        mImage.onScaleBegin();
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mImage.onScale(detector.getScaleFactor(), mView.getScrollX() + detector.getFocusX(), mView.getScrollY() + detector.getFocusY());
        mView.invalidate();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        mImage.onScaleEnd();
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
        mImage.onWindowChanged(v.getScrollX() + v.getPivotX(),
                v.getScrollY() + v.getPivotY(), v.getWidth(), v.getHeight());
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        IMGHoming homing = (IMGHoming) animation.getAnimatedValue();
        mImage.setScale(homing.scale);
        onScrollTo(homing.x, homing.y);
    }

    private class MoveAdapter extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            mImage.onTouch();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return onScrollBy(distanceX, distanceY);
        }
    }


    public <V extends View & IMGSticker> void onAddStickerView(V stickerView) {
        if (stickerView != null) {
            stickerView.registerCallback(this);
            mImage.addSticker(stickerView);
        }
    }

    @Override
    public <V extends View & IMGSticker> void onDismiss(V stickerView) {
        mImage.onDismiss(stickerView);
        mView.invalidate();
    }

    @Override
    public <V extends View & IMGSticker> void onShowing(V stickerView) {
        mImage.onShowing(stickerView);
        mView.invalidate();
    }

    @Override
    public <V extends View & IMGSticker> boolean onRemove(V stickerView) {
        if (mImage != null) {
            mImage.onRemoveSticker(stickerView);
        }
        stickerView.unregisterCallback(this);
        mView.removeView(stickerView);
        return true;
    }
}

package com.xingren.imaging.view;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.xingren.imaging.core.IMGImage;
import com.xingren.imaging.core.IMGMode;
import com.xingren.imaging.core.IMGPath;
import com.xingren.imaging.core.anim.IMGHomingAnimator;
import com.xingren.imaging.core.homing.IMGHoming;
import com.xingren.imaging.core.sticker.IMGSticker;
import com.xingren.imaging.core.sticker.IMGStickerPortrait;

/**
 * Created by felix on 2017/11/16 下午12:45.
 */

class IMGDelegate implements ScaleGestureDetector.OnScaleGestureListener,
        ValueAnimator.AnimatorUpdateListener, IMGStickerPortrait.Callback {

    private static final String TAG = "IMGDelegate";

    private IMGView mView;

    private float mX, mY, mScrollX, mScrollY;

    private IMGImage mImage = new IMGImage();

    private GestureDetector mGDetector;

    private ScaleGestureDetector mSGDetector;

    private IMGHomingAnimator mHomingAnimator;

    private Path mPath = new Path();

    private int mPathId = -2;

    private int mPointerCount = 0;

    private static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG);

    static {
        P.setColor(Color.RED);
        P.setStrokeWidth(2);
        P.setStyle(Paint.Style.STROKE);
    }

    IMGDelegate(IMGView view) {
        mView = view;
        mGDetector = new GestureDetector(view.getContext(), new MoveAdapter());
        mSGDetector = new ScaleGestureDetector(view.getContext(), this);
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
        if (isHoming()) {
            // Homing
            return false;
        }

        mPointerCount = event.getPointerCount();

        boolean handled = mSGDetector.onTouchEvent(event);

        IMGMode mode = mImage.getMode();

        if (mode == IMGMode.NONE || mode == IMGMode.CROP) {
            handled |= onTouchNONE(event);
        } else if (mPointerCount > 1) {
            onPathDone();
            handled |= onTouchNONE(event);
        } else {
            handled |= onTouchPath(event);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                onHoming();
                break;
        }

        return handled;
    }

    void onDraw(Canvas canvas) {

        // 图片
        mImage.onDrawImage(canvas);

        // 马赛克
        mImage.onDrawMosaics(canvas);

        // 涂鸦
        mImage.onDrawDoodles(canvas);

        canvas.save();
        canvas.translate(mView.getScrollX(), mView.getScrollY());
        switch (mImage.getMode()) {
            case DOODLE:
                onDrawPath(canvas);
                break;
        }
        canvas.restore();

        mImage.onDrawStickers(canvas);
    }

    private void onDrawPath(Canvas canvas) {
        if (!mPath.isEmpty()) {
            canvas.drawPath(mPath, P);
        }
    }

    private boolean onTouchNONE(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP:
                onHoming();
                break;
        }

        return mGDetector.onTouchEvent(event);
    }

    private boolean onTouchPath(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mImage.onTouch();
                return onPathBegin(event);
            case MotionEvent.ACTION_MOVE:
                return onPathMove(event);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return mPathId == event.getPointerId(0) && onPathDone();
        }
        return false;
    }

    private boolean onPathBegin(MotionEvent event) {
        mPath.reset();
        mPath.moveTo(event.getX(), event.getY());
        mPathId = event.getPointerId(0);
        return true;
    }

    private boolean onPathMove(MotionEvent event) {
        if (mPathId == event.getPointerId(0)) {
            mPath.lineTo(event.getX(), event.getY());
            mView.invalidate();
            return true;
        }
        return false;
    }

    private boolean onPathDone() {
        if (mPath.isEmpty()) {
            return false;
        }

        IMGMode mode = mImage.getMode();
        if (mode == IMGMode.DOODLE) {
            IMGPath path = new IMGPath(new Path(mPath), mode, Color.RED);
            mImage.addDoodle(path, mView.getScrollX(), mView.getScrollY());
        } else if (mode == IMGMode.MOSAIC) {
            IMGPath path = new IMGPath(new Path(mPath), mode);
            mImage.addMosaic(path, mView.getScrollX(), mView.getScrollY());
        }

        mPath.reset();
        mPathId = -2;

        mView.invalidate();
        return true;
    }

    private void onHoming() {
        startHoming(mImage.getStartHoming(mView.getScrollX(), mView.getScrollY()),
                mImage.getEndHoming(mView.getScrollX(), mView.getScrollY()));
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
        if (mPointerCount > 1) {
            mImage.onScaleBegin();
            return true;
        }
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (mPointerCount > 1) {
            mImage.onScale(detector.getScaleFactor(),
                    mView.getScrollX() + detector.getFocusX(),
                    mView.getScrollY() + detector.getFocusY());

            mView.invalidate();
            return true;
        }
        return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        mImage.onScaleEnd();
    }

    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mImage.onWindowChanged(mView.getScrollX() + mView.getPivotX(),
                mView.getScrollY() + mView.getPivotY(), mView.getWidth(), mView.getHeight());
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

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // TODO
            return super.onFling(e1, e2, velocityX, velocityY);
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

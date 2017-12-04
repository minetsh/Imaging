package com.xingren.imaging.view;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

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

    private IMGImage mImage = new IMGImage();

    private GestureDetector mGDetector;

    private ScaleGestureDetector mSGDetector;

    private IMGHomingAnimator mHomingAnimator;

    private Path mPath = new Path();

    private IMGPath mCurrentPath = new IMGPath(mPath);

    private int mPathId = -2;

    private int mPointerCount = 0;

    private Paint mDoodlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mMosaicPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    {
        // 涂鸦画刷
        mDoodlePaint.setStyle(Paint.Style.STROKE);
        mDoodlePaint.setStrokeWidth(IMGPath.BASE_DOODLE_WIDTH);
        mDoodlePaint.setColor(Color.RED);
        mDoodlePaint.setPathEffect(new CornerPathEffect(IMGPath.BASE_DOODLE_WIDTH));
        mDoodlePaint.setStrokeCap(Paint.Cap.ROUND);
        mDoodlePaint.setStrokeJoin(Paint.Join.ROUND);

        // 马赛克画刷
        mMosaicPaint.setStyle(Paint.Style.STROKE);
        mMosaicPaint.setStrokeWidth(IMGPath.BASE_MOSAIC_WIDTH);
        mMosaicPaint.setColor(Color.BLACK);
        mMosaicPaint.setPathEffect(new CornerPathEffect(IMGPath.BASE_MOSAIC_WIDTH));
        mMosaicPaint.setStrokeCap(Paint.Cap.ROUND);
        mMosaicPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    IMGDelegate(IMGView view) {
        mView = view;
        mCurrentPath.setMode(mImage.getMode());
        mGDetector = new GestureDetector(view.getContext(), new MoveAdapter());
        mSGDetector = new ScaleGestureDetector(view.getContext(), this);
    }

    void setImageBitmap(Bitmap image) {
        mImage.setBitmap(image);
        mView.invalidate();
    }

    void setMode(IMGMode mode) {
        mImage.setMode(mode);
        mCurrentPath.setMode(mode);
    }

    IMGMode getMode() {
        return mImage.getMode();
    }

    IMGImage getImage() {
        return mImage;
    }

    boolean isMosaicEmpty() {
        return mImage.isMosaicEmpty();
    }

    boolean isDoodleEmpty() {
        return mImage.isDoodleEmpty();
    }

    void undoDoodle() {
        mImage.undoDoodle();
    }

    void undoMosaic() {
        mImage.undoMosaic();
    }

    Bitmap saveBitmap() {
        RectF frame = mImage.getClipFrame();
        Bitmap bitmap = Bitmap.createBitmap(Math.round(frame.width()), Math.round(frame.height()), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(-frame.left, -frame.top);
        onDraw(canvas);
        return bitmap;
    }

    /**
     * 是否真正修正归位
     */
    boolean isHoming() {
        return mHomingAnimator != null && mHomingAnimator.isRunning();
    }

    void stopHoming() {
        if (mHomingAnimator != null) {
            mHomingAnimator.cancel();
        }
    }

    boolean onInterceptTouch(MotionEvent event) {
        if (isHoming()) {
            stopHoming();
            return true;
        } else if (event.getPointerCount() > 1) {
            // TODO
        }
        return false;
    }

    boolean onTouch(MotionEvent event) {
        Log.d(TAG, "PointerCount=" + event.getPointerCount());

        if (isHoming()) {
            // Homing
            return false;
        }

        mPointerCount = event.getPointerCount();

        boolean handled = mSGDetector.onTouchEvent(event);

        IMGMode mode = mImage.getMode();

        if (mode == IMGMode.NONE || mode == IMGMode.CLIP) {
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
        if (!mImage.isMosaicEmpty() || (mImage.getMode() == IMGMode.MOSAIC && !mPath.isEmpty())) {
            int count = mImage.onDrawMosaicsPath(canvas);
            if (mImage.getMode() == IMGMode.MOSAIC && !mPath.isEmpty()) {
                mDoodlePaint.setStrokeWidth(IMGPath.BASE_MOSAIC_WIDTH);
                canvas.save();
                canvas.translate(mView.getScrollX(), mView.getScrollY());
                canvas.drawPath(mPath, mDoodlePaint);
                canvas.restore();
            }
            mImage.onDrawMosaic(canvas, count);
        }

        // 涂鸦
        mImage.onDrawDoodles(canvas);
        if (mImage.getMode() == IMGMode.DOODLE && !mPath.isEmpty()) {
            mDoodlePaint.setStrokeWidth(IMGPath.BASE_DOODLE_WIDTH * mImage.getScale());
            canvas.save();
            canvas.translate(mView.getScrollX(), mView.getScrollY());
            canvas.drawPath(mPath, mDoodlePaint);
            canvas.restore();
        }

        // 文字贴片
        mImage.onDrawStickers(canvas);

        // 裁剪
        if (mImage.getMode() == IMGMode.CLIP) {
            canvas.save();
            canvas.translate(mView.getScrollX(), mView.getScrollY());
            mImage.onDrawClipWindow(canvas);
            canvas.restore();
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
                mImage.onTouchDown();
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

    private boolean onScroll(float dx, float dy) {
        if (!mImage.onScroll(-dx, -dy)) {
            mView.scrollBy(Math.round(dx), Math.round(dy));
        } else {
            mView.invalidate();
        }
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
        if (!changed) return;
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
            mImage.onTouchDown(e.getX(), e.getY());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return IMGDelegate.this.onScroll(distanceX, distanceY);
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
        ViewParent parent = stickerView.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(stickerView);
        }
        return true;
    }
}

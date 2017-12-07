package com.xingren.imaging.view;

import android.animation.Animator;
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
        ValueAnimator.AnimatorUpdateListener, IMGStickerPortrait.Callback, Animator.AnimatorListener {

    private static final String TAG = "IMGDelegate";

    private IMGView mView;

    private IMGMode mPreMode = IMGMode.NONE;

    private IMGImage mImage = new IMGImage();

    private GestureDetector mGDetector;

    private ScaleGestureDetector mSGDetector;

    private IMGHomingAnimator mHomingAnimator;

    private Pen mPen = new Pen();

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
        mPen.setMode(mImage.getMode());
        mGDetector = new GestureDetector(view.getContext(), new MoveAdapter());
        mSGDetector = new ScaleGestureDetector(view.getContext(), this);
    }

    void setImageBitmap(Bitmap image) {
        mImage.setBitmap(image);
        mView.invalidate();
    }

    void setMode(IMGMode mode) {
        // 保存现在的编辑模式
        mPreMode = mImage.getMode();

        // 设置新的编辑模式
        mImage.setMode(mode);
        mPen.setMode(mode);

        // 矫正区域
        onHoming();
    }

    IMGMode getMode() {
        return mImage.getMode();
    }

    IMGImage getImage() {
        return mImage;
    }

    void setPenColor(int color) {
        mPen.setColor(color);
    }

    boolean isMosaicEmpty() {
        return mImage.isMosaicEmpty();
    }

    boolean isDoodleEmpty() {
        return mImage.isDoodleEmpty();
    }

    void undoDoodle() {
        mImage.undoDoodle();
        mView.invalidate();
    }

    void undoMosaic() {
        mImage.undoMosaic();
        mView.invalidate();
    }

    // TODO
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

        canvas.save();
        // TODO 旋转

        RectF clipFrame = mImage.getClipFrame();
        canvas.rotate(mImage.getRotate(), clipFrame.centerX(), clipFrame.centerY());

        // 图片
        mImage.onDrawImage(canvas);

        // 马赛克
        if (!mImage.isMosaicEmpty() || (mImage.getMode() == IMGMode.MOSAIC && !mPen.isEmpty())) {
            int count = mImage.onDrawMosaicsPath(canvas);
            if (mImage.getMode() == IMGMode.MOSAIC && !mPen.isEmpty()) {
                mDoodlePaint.setStrokeWidth(IMGPath.BASE_MOSAIC_WIDTH);
                canvas.save();
                RectF frame = mImage.getClipFrame();
                canvas.rotate(-mImage.getRotate(), frame.centerX(), frame.centerY());
                canvas.translate(mView.getScrollX(), mView.getScrollY());
                canvas.drawPath(mPen.getPath(), mDoodlePaint);
                canvas.restore();
            }
            mImage.onDrawMosaic(canvas, count);
        }

        // 涂鸦
        mImage.onDrawDoodles(canvas);
        if (mImage.getMode() == IMGMode.DOODLE && !mPen.isEmpty()) {
            mDoodlePaint.setColor(mPen.getColor());
            mDoodlePaint.setStrokeWidth(IMGPath.BASE_DOODLE_WIDTH * mImage.getScale());
            canvas.save();
            RectF frame = mImage.getClipFrame();
            canvas.rotate(-mImage.getRotate(), frame.centerX(), frame.centerY());
            canvas.translate(mView.getScrollX(), mView.getScrollY());
            canvas.drawPath(mPen.getPath(), mDoodlePaint);
            canvas.restore();
        }

        // 文字贴片
        mImage.onDrawStickers(canvas);
        canvas.restore();

        // 裁剪
        if (mImage.getMode() == IMGMode.CLIP) {
            canvas.save();
            canvas.translate(mView.getScrollX(), mView.getScrollY());
            mImage.onDrawClip(canvas);
            canvas.restore();
        }

    }

    private boolean onTouchNONE(MotionEvent event) {
        return mGDetector.onTouchEvent(event);
    }

    private boolean onTouchPath(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mImage.onTouchDown(event.getX(), event.getY());
                return onPathBegin(event);
            case MotionEvent.ACTION_MOVE:
                return onPathMove(event);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return mPen.isIdentity(event.getPointerId(0)) && onPathDone();
        }
        return false;
    }

    private boolean onPathBegin(MotionEvent event) {
        mPen.reset(event.getX(), event.getY());
        mPen.setIdentity(event.getPointerId(0));
        return true;
    }

    private boolean onPathMove(MotionEvent event) {
        if (mPen.isIdentity(event.getPointerId(0))) {
            mPen.lineTo(event.getX(), event.getY());
            mView.invalidate();
            return true;
        }
        return false;
    }

    private boolean onPathDone() {
        if (mPen.isEmpty()) {
            return false;
        }
        mImage.addPath(mPen.toPath(), mView.getScrollX(), mView.getScrollY());
        mPen.reset();
        mView.invalidate();
        return true;
    }

    private void onHoming() {
        mView.invalidate();
        stopHoming();
        startHoming(
                mImage.getStartHoming(mView.getScrollX(), mView.getScrollY()),
                mImage.getEndHoming(mView.getScrollX(), mView.getScrollY())
        );
    }

    private void startHoming(IMGHoming sHoming, IMGHoming eHoming) {
        if (mHomingAnimator == null) {
            mHomingAnimator = new IMGHomingAnimator();
            mHomingAnimator.addUpdateListener(this);
            mHomingAnimator.addListener(this);
        }
        mHomingAnimator.setHomingValues(sHoming, eHoming);
        mHomingAnimator.start();
    }

    private boolean onScroll(float dx, float dy) {
        if (!mImage.onScroll(-dx, -dy)) {
            return onScrollTo(mView.getScrollX() + Math.round(dx), mView.getScrollY() + Math.round(dy));
        }
        mView.invalidate();
        return true;
    }

    private boolean onScrollTo(int x, int y) {
        if (mView.getScrollX() != x || mView.getScrollY() != y) {
            mView.scrollTo(x, y);
            return true;
        }
        return false;
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
        if (changed) {
            mImage.onWindowChanged(right - left, bottom - top);
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        IMGHoming homing = (IMGHoming) animation.getAnimatedValue();
        mImage.setScale(homing.scale);
        mImage.setRotate(homing.rotate);
        if (!onScrollTo(Math.round(homing.x), Math.round(homing.y))) {
            mView.invalidate();
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
        mImage.onHomingStart(mHomingAnimator.isRotate());
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        mImage.onHomingEnd(mHomingAnimator.isRotate());
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        mImage.onHomingCancel(mHomingAnimator.isRotate());
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

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

    void cancelClip() {
        mImage.toBackupClip();
        setMode(mPreMode);
    }

    void resetClip() {
        mImage.resetClip();
        onHoming();
    }

    void doRotate() {
        if (!isHoming()) {
            mImage.rotate(-90);
            onHoming();
        }
    }

    void doClip() {
        mImage.clip(mView.getScrollX(), mView.getScrollY());
        setMode(mPreMode);
        onHoming();
    }

    private static class Pen extends IMGPath {

        private int identity = Integer.MIN_VALUE;

        public void reset() {
            this.path.reset();
            this.identity = Integer.MIN_VALUE;
        }

        public void reset(float x, float y) {
            this.path.reset();
            this.path.moveTo(x, y);
            this.identity = Integer.MIN_VALUE;
        }

        public void setIdentity(int identity) {
            this.identity = identity;
        }

        public boolean isIdentity(int identity) {
            return this.identity == identity;
        }

        public void lineTo(float x, float y) {
            this.path.lineTo(x, y);
        }

        public boolean isEmpty() {
            return this.path.isEmpty();
        }

        public IMGPath toPath() {
            return new IMGPath(new Path(this.path), getMode(), getColor(), getWidth());
        }
    }
}

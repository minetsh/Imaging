package com.xingren.imaging.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.xingren.imaging.core.IMGMode;
import com.xingren.imaging.core.IMGText;
import com.xingren.imaging.core.sticker.IMGSticker;

/**
 * Created by felix on 2017/11/14 下午6:43.
 */
// TODO clip外不加入path
public class IMGView extends FrameLayout implements Runnable {

    private static final String TAG = "IMGView";

    private IMGDelegate mDelegate;

    public IMGView(Context context) {
        this(context, null, 0);
    }

    public IMGView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMGView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        mDelegate = new IMGDelegate(this);
    }

    public void setImageBitmap(Bitmap image) {
        mDelegate.setImageBitmap(image);
    }

    public void setMode(IMGMode mode) {
        mDelegate.setMode(mode);
    }

    public void doRotate() {
        mDelegate.doRotate();
    }

    public void resetClip() {
        mDelegate.resetClip();
    }

    public void doClip() {
        mDelegate.doClip();
    }

    public void cancelClip() {
        mDelegate.cancelClip();
    }

    public void setPenColor(int color) {
        mDelegate.setPenColor(color);
    }

    public boolean isDoodleEmpty() {
        return mDelegate.isDoodleEmpty();
    }

    public void undoDoodle() {
        mDelegate.undoDoodle();
    }

    public boolean isMosaicEmpty() {
        return mDelegate.isMosaicEmpty();
    }

    public void undoMosaic() {
        mDelegate.undoMosaic();
    }

    public IMGMode getMode() {
        return mDelegate.getMode();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mDelegate.onDraw(canvas);
    }

    public Bitmap saveBitmap() {
        return mDelegate.saveBitmap();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mDelegate.onLayout(changed, left, top, right, bottom);
    }

    public <V extends View & IMGSticker> void addStickerView(V stickerView, LayoutParams params) {
        if (stickerView != null) {

            addView(stickerView, params);

            mDelegate.onAddStickerView(stickerView);
        }
    }

    public void addStickerText(IMGText text) {
        IMGStickerTextView textView = new IMGStickerTextView(getContext());

        textView.setText(text);

        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );

        // Center of the drawing window.
        layoutParams.gravity = Gravity.CENTER;

        textView.setX(getScrollX());
        textView.setY(getScrollY());

        addStickerView(textView, layoutParams);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d(TAG, "onInterceptTouchEvent:" + MotionEvent.actionToString(ev.getActionMasked()));
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            return mDelegate.onInterceptTouch(ev) || super.onInterceptTouchEvent(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent:" + MotionEvent.actionToString(event.getActionMasked()));
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                removeCallbacks(this);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                postDelayed(this, 1200);
                break;
        }
        return mDelegate.onTouch(event);
    }

    @Override
    public void run() {
        // 稳定触发
        if (!mDelegate.onSteady()) {
            postDelayed(this, 500);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this);
    }

    public interface IMGEventCallback {

        void onEventBegin(IMGEvent event);

        void onEventEnd(IMGEvent event);
    }

    public enum IMGEvent {

    }
}

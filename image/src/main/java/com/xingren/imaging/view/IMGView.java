package com.xingren.imaging.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.xingren.imaging.R;
import com.xingren.imaging.core.IMGMode;
import com.xingren.imaging.core.IMGText;
import com.xingren.imaging.core.sticker.IMGSticker;

/**
 * Created by felix on 2017/11/14 下午6:43.
 */

public class IMGView extends FrameLayout {

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
        BitmapFactory.Options options = new BitmapFactory.Options();

        mDelegate.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.am, options));
    }

    public void setImageBitmap(Bitmap image) {
        mDelegate.setImageBitmap(image);
    }

    public void setMode(IMGMode mode) {
        mDelegate.setMode(mode);
    }

    public void doRotate() {
        // TODO
    }

    public void resetClip() {
        // TODO
    }

    public void doClip() {
        // TODO
    }

    public void cancelClip() {
        // TODO
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
        return mDelegate.onInterceptTouch(ev) || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDelegate.onTouch(event);
    }

    public interface IMGEventCallback {

        void onEventBegin(IMGEvent event);

        void onEventEnd(IMGEvent event);
    }

    public enum IMGEvent {

    }
}

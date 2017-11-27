package com.xingren.imaging.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.xingren.imaging.R;
import com.xingren.imaging.core.IMGImage;
import com.xingren.imaging.core.IMGSticker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by felix on 2017/11/14 下午6:43.
 */

public class IMGCanvasView extends FrameLayout implements IMGSticker.Callback {

    private static final String TAG = "CanvasView";

    private RectF mImageRect = new RectF();

    private RectF mOriginalRect = new RectF();

    private IMGCanvasDelegate mDelegate;

    private IMGSticker mForeSticker;

    @Nullable
    private IMGImage mImage;

    private List<IMGSticker> mBackStickers = new ArrayList<>();

    private float tPivotX, tPivotY;

    private static final Paint PAINT;

    private static final Matrix MATRIX = new Matrix();

    static {
        PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
        PAINT.setColor(Color.RED);
        PAINT.setStyle(Paint.Style.STROKE);
        PAINT.setStrokeWidth(5);
    }

    public IMGCanvasView(Context context) {
        this(context, null, 0);
    }

    public IMGCanvasView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMGCanvasView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        mImageRect.set(0, 0, 600, 400);
        mOriginalRect.set(0, 0, 600, 400);
        mDelegate = new IMGCanvasDelegate(this);

        mImage = new IMGImage(BitmapFactory.decodeResource(getResources(), R.drawable.am));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mImage != null) {
            mImage.init(getScrollX() + getPivotX(), getScrollY() + getPivotY(), getWidth(), getHeight());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mImage != null) {
            mImage.onDraw(canvas);
        }

        canvas.drawRect(mImageRect, PAINT);
        for (IMGSticker sticker : mBackStickers) {
            if (!sticker.isShowing()) {
                tPivotX = sticker.getX() + sticker.getPivotX();
                tPivotY = sticker.getY() + sticker.getPivotY();
                canvas.save();
                MATRIX.reset();
                MATRIX.setTranslate(sticker.getX(), sticker.getY());
                MATRIX.postScale(sticker.getScaleX(), sticker.getScaleY(), tPivotX, tPivotY);
                MATRIX.postRotate(sticker.getRotation(), tPivotX, tPivotY);
                canvas.concat(MATRIX);
                sticker.onSticker(canvas);
                canvas.restore();
            }
        }
    }

    public <V extends View & IMGSticker> void addStickerView(V stickerView) {
        if (stickerView != null) {
            stickerView.registerCallback(this);
            LayoutParams layoutParams = new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
            );

            layoutParams.gravity = Gravity.CENTER;
            addView(stickerView, layoutParams);
            moveToForeground(stickerView);
        }
    }

    public void moveToForeground(@Nullable IMGSticker stickerView) {
        if (stickerView == null) return;

        moveToBackground(mForeSticker);

        if (stickerView.isShowing()) {
            mForeSticker = stickerView;
            // 从BackStickers中移除
            mBackStickers.remove(stickerView);
        } else stickerView.show();
    }

    public void moveToBackground(@Nullable IMGSticker stickerView) {
        if (stickerView == null) return;

        if (!stickerView.isShowing()) {
            // 加入BackStickers中
            if (!mBackStickers.contains(stickerView)) {
                mBackStickers.add(stickerView);
            }

            if (mForeSticker == stickerView) {
                mForeSticker = null;
            }
        } else stickerView.dismiss();
    }

    @Override
    protected int computeHorizontalScrollRange() {
        return Math.round(mImageRect.width());
    }

    @Override
    protected int computeVerticalScrollRange() {
        return Math.round(mImageRect.height());
    }

    @Override
    public <V extends View & IMGSticker> boolean onRemove(V stickerView) {
        if (mForeSticker == stickerView) {
            mForeSticker = null;
        } else {
            mBackStickers.remove(stickerView);
        }
        stickerView.unregisterCallback(this);
        removeView(stickerView);
        return true;
    }

    @Override
    public <V extends View & IMGSticker> void onDismiss(V stickerView) {
        moveToBackground(stickerView);
        invalidate();
    }

    @Override
    public <V extends View & IMGSticker> void onShowing(V stickerView) {
        if (mForeSticker != stickerView) {
            moveToForeground(stickerView);
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDelegate.onTouch(event);
    }

    public float getScale() {
        return mImageRect.width() / mOriginalRect.width();
    }

    public void onTouch() {
        moveToBackground(mForeSticker);
    }

    public void onScale(float factor, float focusX, float focusY) {

        if (mImage != null) {
            mImage.onScale(factor, focusX + getScrollX(), focusY + getScrollY());
        }

        MATRIX.reset();
        MATRIX.setScale(factor, factor, focusX + getScrollX(), focusY + getScrollY());
        MATRIX.mapRect(mImageRect);

        for (IMGSticker sticker : mBackStickers) {
            MATRIX.mapRect(sticker.getFrame());
            tPivotX = sticker.getX() + sticker.getPivotX();
            tPivotY = sticker.getY() + sticker.getPivotY();
            sticker.setScaleX(sticker.getScaleX() * factor);
            sticker.setScaleY(sticker.getScaleY() * factor);
            sticker.setX(sticker.getX() + sticker.getFrame().centerX() - tPivotX);
            sticker.setY(sticker.getY() + sticker.getFrame().centerY() - tPivotY);
        }

        invalidate();
    }

    public void onScaleEnd(float factor) {

    }
}

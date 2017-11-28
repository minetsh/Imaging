package com.xingren.imaging.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.xingren.imaging.core.clip.IMGClip;
import com.xingren.imaging.core.homing.IMGHoming;
import com.xingren.imaging.core.sticker.IMGSticker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by felix on 2017/11/21 下午10:03.
 */

public class IMGImage {

    private static final String TAG = "IMGImage";

    private Bitmap mImage;

    /**
     * 完整图片边框
     */
    private RectF mFrame = new RectF();

    /**
     * 裁剪图片边框（显示的图片区域）
     */
    private RectF mClipFrame = new RectF();

    /**
     * 图片显示窗口（默认为控件大小，裁剪时为裁剪区域）
     */
    private RectF mHomeFrame = new RectF();

    private IMGClip mClip;

    /**
     * 编辑模式
     */
    private IMGMode mMode = IMGMode.DOODLE;

    private float mWindowPivotX, mWindowPivotY;

    private int mWindowWidth, mWindowHeight;

    /**
     * 是否初始位置
     */
    private boolean isInitial = false;

    /**
     * 当前选中贴片
     */
    private IMGSticker mForeSticker;

    /**
     * 为被选中贴片
     */
    private List<IMGSticker> mBackStickers = new ArrayList<>();

    /**
     * 涂鸦路径
     */
    private List<IMGPath> mDoodles = new ArrayList<>();

    /**
     * 马赛克路径
     */
    private List<IMGPath> mMosaics = new ArrayList<>();

    private static final int MIN_SIZE = 500;

    private static final int MAX_SIZE = 10000;

    private static final Matrix M = new Matrix();

    private static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final boolean DEBUG = true;

    static {
        P.setColor(Color.RED);
        P.setStrokeWidth(2);
        P.setTextSize(36);
        P.setStyle(Paint.Style.STROKE);
    }

    public IMGImage() {

    }

    public void setBitmap(Bitmap bitmap) {
        this.mImage = bitmap;
        onImageChanged();
    }

    public IMGMode getMode() {
        return mMode;
    }

    public void onImageChanged() {
        isInitial = false;
        onWindowChanged(mWindowPivotX, mWindowPivotY, mWindowWidth, mWindowHeight);
    }

    public RectF getFrame() {
        return mFrame;
    }

    public IMGHoming getStartHoming(float scrollX, float scrollY) {
        IMGHoming homing = new IMGHoming();
        homing.x = scrollX;
        homing.y = scrollY;
        homing.scale = getScale();
        return homing;
    }

    public IMGHoming getEndHoming(float scrollX, float scrollY) {
        IMGHoming homing = new IMGHoming(scrollX, scrollY, getScale());
        if (!mClipFrame.contains(scrollX, scrollY,
                scrollX + mWindowWidth, scrollY + mWindowHeight)) {

            if (mClipFrame.width() < mWindowWidth) {
                homing.scale *= mWindowWidth / mClipFrame.width();
                homing.x = mClipFrame.centerX() - mWindowWidth / 2;
            } else if (mClipFrame.left > scrollX) {
                homing.x = mClipFrame.left;
            } else if (mClipFrame.right < scrollX + mWindowWidth) {
                homing.x = mClipFrame.right - mWindowWidth;
            }

            if (mClipFrame.height() < mWindowHeight) {
                homing.y = mClipFrame.centerY() - mWindowHeight / 2;
            } else if (mClipFrame.top > scrollY) {
                homing.y = mClipFrame.top;
            } else if (mClipFrame.bottom < scrollY + mWindowHeight) {
                homing.y = mClipFrame.bottom - mWindowHeight;
            }
        }

        return homing;
    }

    public <S extends IMGSticker> void addSticker(S sticker) {
        if (sticker != null) {
            moveToForeground(sticker);
        }
    }

    public void addDoodle(IMGPath doodle) {
        if (doodle != null) {
            mDoodles.add(doodle);
        }
    }

    private void moveToForeground(IMGSticker sticker) {
        if (sticker == null) return;

        moveToBackground(mForeSticker);

        if (sticker.isShowing()) {
            mForeSticker = sticker;
            // 从BackStickers中移除
            mBackStickers.remove(sticker);
        } else sticker.show();
    }

    private void moveToBackground(IMGSticker sticker) {
        if (sticker == null) return;

        if (!sticker.isShowing()) {
            // 加入BackStickers中
            if (!mBackStickers.contains(sticker)) {
                mBackStickers.add(sticker);
            }

            if (mForeSticker == sticker) {
                mForeSticker = null;
            }
        } else sticker.dismiss();
    }

    public void onDismiss(IMGSticker sticker) {
        moveToBackground(sticker);
    }

    public void onShowing(IMGSticker sticker) {
        if (mForeSticker != sticker) {
            moveToForeground(sticker);
        }
    }

    public void onRemoveSticker(IMGSticker sticker) {
        if (mForeSticker == sticker) {
            mForeSticker = null;
        } else {
            mBackStickers.remove(sticker);
        }
    }

    public void onWindowChanged(float pivotX, float pivotY, int width, int height) {
        // Window's pivot coordinate.
        mWindowPivotX = pivotX;
        mWindowPivotY = pivotY;

        // Window's size.
        mWindowWidth = width;
        mWindowHeight = height;

        if (!isInitial && mImage != null) {

            if (width == 0 || height == 0) {
                // Window not ready.
                return;
            }

            mHomeFrame.set(0, 0, width, height);
            mFrame.set(0, 0, mImage.getWidth(), mImage.getHeight());
            mClipFrame.set(0, 0, mImage.getWidth(), mImage.getHeight());

            if (mFrame.width() == 0 || mFrame.height() == 0) {
                // Bitmap invalidate.
                return;
            }

            float scale = Math.min(
                    width / mFrame.width(),
                    height / mFrame.height()
            );

            // Scale to fit window.
            M.reset();
            M.setScale(scale, scale, mFrame.centerX(), mFrame.centerY());
            M.postTranslate(mWindowPivotX - mFrame.centerX(), mWindowPivotY - mFrame.centerY());
            M.mapRect(mFrame);
            M.mapRect(mClipFrame);

            isInitial = true;
        } else {

            // Pivot to fit window.
            M.reset();
            M.setTranslate(mWindowPivotX - mClipFrame.centerX(), mWindowPivotY - mClipFrame.centerY());
            M.mapRect(mFrame);
            M.mapRect(mClipFrame);
        }
    }

    public PointF getPivot() {
        return new PointF(mFrame.centerX(), mFrame.centerY());
    }

    public void onDrawImage(Canvas canvas) {
//        canvas.clipRect(mClipFrame);
        canvas.drawBitmap(mImage, null, mFrame, null);

        if (DEBUG) {
            canvas.drawText(String.format(Locale.CHINA, "[%.1f, %.1f]",
                    mClipFrame.left, mClipFrame.top), mClipFrame.left, mClipFrame.top, P);
        }

//        canvas.drawRect(mHomeFrame, P);

    }

    public void onDrawDoodles(Canvas canvas) {
        if (!mDoodles.isEmpty()) {
            canvas.save();
            for (IMGPath path : mDoodles) {
                path.onDraw(canvas);
            }
            canvas.restore();
        }
    }

    public void onDrawMosaics(Canvas canvas) {
        if (!mMosaics.isEmpty()) {
            canvas.save();
            for (IMGPath path : mMosaics) {
                path.onDraw(canvas);
            }
            canvas.restore();
        }
    }

    public void onDrawStickers(Canvas canvas) {
        for (IMGSticker sticker : mBackStickers) {
            if (!sticker.isShowing()) {
                float tPivotX = sticker.getX() + sticker.getPivotX();
                float tPivotY = sticker.getY() + sticker.getPivotY();
                canvas.save();
                M.reset();
                M.setTranslate(sticker.getX(), sticker.getY());
                M.postScale(sticker.getScaleX(), sticker.getScaleY(), tPivotX, tPivotY);
                M.postRotate(sticker.getRotation(), tPivotX, tPivotY);
                canvas.concat(M);
                sticker.onSticker(canvas);
                canvas.restore();
            }
        }
    }

    public void onTouch() {
        moveToBackground(mForeSticker);
    }

    public void onScaleBegin() {
        onTouch();
    }

    public float getScale() {
        return 1f * mFrame.width() / mImage.getWidth();
    }

    public void setScale(float scale) {
        setScale(scale, mClipFrame.centerX(), mClipFrame.centerY());
    }

    public void setScale(float scale, float focusX, float focusY) {
        onScale(scale / getScale(), focusX, focusY);
    }

    public void onScale(float factor, float focusX, float focusY) {

        if (factor == 1f) return;

        if (Math.max(mClipFrame.width(), mClipFrame.height()) >= MAX_SIZE
                || Math.min(mClipFrame.width(), mClipFrame.height()) <= MIN_SIZE) {
            factor += (1 - factor) / 2;
        }

        M.reset();
        M.setScale(factor, factor, focusX, focusY);
        M.mapRect(mFrame);
        M.mapRect(mClipFrame);

        for (IMGPath doodle : mDoodles) {
            doodle.transform(M);
        }

        for (IMGSticker sticker : mBackStickers) {
            M.mapRect(sticker.getFrame());
            float tPivotX = sticker.getX() + sticker.getPivotX();
            float tPivotY = sticker.getY() + sticker.getPivotY();
            sticker.setScaleX(sticker.getScaleX() * factor);
            sticker.setScaleY(sticker.getScaleY() * factor);
            sticker.setX(sticker.getX() + sticker.getFrame().centerX() - tPivotX);
            sticker.setY(sticker.getY() + sticker.getFrame().centerY() - tPivotY);
        }

        IMGPath.setStrokeWidthScale(getScale());
    }

    public void onScaleEnd() {

    }
}

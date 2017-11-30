package com.xingren.imaging.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import com.xingren.imaging.core.clip.IMGClip;
import com.xingren.imaging.core.clip.IMGClipWindow;
import com.xingren.imaging.core.homing.IMGHoming;
import com.xingren.imaging.core.sticker.IMGSticker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by felix on 2017/11/21 下午10:03.
 */

public class IMGImage {

    private static final String TAG = "IMGImage";

    // TODO image is null
    private Bitmap mImage, mMosaicImage;

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

    private IMGClip.Anchor mAnchor;

    private IMGClipWindow mClipWin = new IMGClipWindow();

    /**
     * 编辑模式
     */
    private IMGMode mMode = IMGMode.CLIP;

    private float mWindowPivotX, mWindowPivotY;

    private int mWindowWidth, mWindowHeight;

    /**
     * 是否初始位置
     */
    private boolean isInitialHoming = false;

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
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }

        this.mImage = bitmap;

        // 清空马赛克图层
        this.mMosaicImage = null;

        makeMosaicBitmap();

        onImageChanged();
    }

    public IMGMode getMode() {
        return mMode;
    }

    public void setMode(IMGMode mode) {
        this.mMode = mode;

        if (mMode == IMGMode.MOSAIC) {
            makeMosaicBitmap();
        } else if (mMode == IMGMode.CLIP && mImage != null) {
            mClipWin.setImageSize(mImage.getWidth(), mImage.getHeight());
        }
    }

    private void makeMosaicBitmap() {
        if (mMosaicImage != null || mImage == null) {
            return;
        }

        if (mMode == IMGMode.MOSAIC) {

            // TODO
            mMosaicImage = Bitmap.createScaledBitmap(mImage,
                    mImage.getWidth() / 30, mImage.getHeight() / 30, true);
        }
    }

    private void onImageChanged() {
        isInitialHoming = false;
        onWindowChanged(mWindowPivotX, mWindowPivotY, mWindowWidth, mWindowHeight);

        if (mImage != null && mMode == IMGMode.CLIP) {
            mClipWin.setImageSize(mImage.getWidth(), mImage.getHeight());
        }
    }

    public RectF getFrame() {
        return mFrame;
    }

    public IMGHoming getStartHoming(float scrollX, float scrollY) {
        return new IMGHoming(scrollX, scrollY, getScale());
    }

    public IMGHoming getEndHoming(float scrollX, float scrollY) {
        IMGHoming homing = new IMGHoming(scrollX, scrollY, getScale());
        if (mMode == IMGMode.CLIP) {
            RectF frame = new RectF(mClipWin.getFrame());
            frame.offset(scrollX, scrollY);

            if (!mClipFrame.contains(frame)) {
                // TODO
                RectF clipFrame = new RectF();

                float scale = 1f;

                if (mClipFrame.width() < frame.width()) {
                    scale = Math.max(scale, frame.width() / mClipFrame.width());
                }

                if (mClipFrame.height() < frame.height()) {
                    scale = Math.max(scale, frame.height() / mClipFrame.height());
                }

                M.setScale(scale, scale, mClipFrame.centerX(), mClipFrame.centerY());
                M.mapRect(clipFrame, mClipFrame);

                homing.scale *= scale;

                if (clipFrame.left > frame.left) {
                    homing.x += clipFrame.left - frame.left;
                } else if (clipFrame.right < frame.right) {
                    homing.x += clipFrame.right - frame.right;
                }

                if (clipFrame.top > frame.top) {
                    homing.y += clipFrame.top - frame.top;
                } else if (clipFrame.bottom < frame.bottom) {
                    homing.y += clipFrame.bottom - frame.bottom;
                }
            }

        } else {

            // TODO
            if (!mClipFrame.contains(scrollX, scrollY, scrollX + mWindowWidth, scrollY + mWindowHeight)) {

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
        }

        return homing;
    }

    public <S extends IMGSticker> void addSticker(S sticker) {
        if (sticker != null) {
            moveToForeground(sticker);
        }
    }

    public void addDoodle(IMGPath doodle, float sx, float sy) {
        if (doodle != null) {

            float scale = 1f / getScale();
            M.setTranslate(sx - mClipFrame.left, sy - mClipFrame.top);
            M.postScale(scale, scale);
            doodle.transform(M);

            mDoodles.add(doodle);
        }
    }

    public void addMosaic(IMGPath mosaic, float sx, float sy) {
        if (mosaic != null) {
            float scale = 1f / getScale();
            M.setTranslate(sx - mClipFrame.left, sy - mClipFrame.top);
            M.postScale(scale, scale);
            mosaic.transform(M);

            mMosaics.add(mosaic);
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

        if (width == 0 || height == 0) {
            return;
        }

        if (!isInitialHoming && mImage != null) {
            onInitialHoming(pivotX, pivotY, width, height);
        } else {

            // Pivot to fit window.
            M.reset();
            M.setTranslate(mWindowPivotX - mClipFrame.centerX(), mWindowPivotY - mClipFrame.centerY());
            M.mapRect(mFrame);
            M.mapRect(mClipFrame);
        }

        mClipWin.setClipWinSize(width, height);
    }

    private void onInitialHoming(float pivotX, float pivotY, int width, int height) {

        mHomeFrame.set(0, 0, width, height);
        mFrame.set(0, 0, mImage.getWidth(), mImage.getHeight());
        mClipFrame.set(0, 0, mImage.getWidth(), mImage.getHeight());
        mClipWin.setClipWinSize(width, height);

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

        isInitialHoming = true;
        onInitialHomingDone();
    }

    private void onInitialHomingDone() {
        if (mMode == IMGMode.CLIP && mImage != null) {
            mClipWin.setImageSize(mImage.getWidth(), mImage.getHeight());
        }
    }

    public PointF getPivot() {
        return new PointF(mFrame.centerX(), mFrame.centerY());
    }

    public void onDrawImage(Canvas canvas) {
//        canvas.clipRect(mClipFrame);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        canvas.drawBitmap(mImage, null, mClipFrame, null);


    }

    public void onDrawMosaics(Canvas canvas) {
        if (!mMosaics.isEmpty()) {

            int sc = canvas.saveLayer(mClipFrame.left, mClipFrame.top, mClipFrame.right, mClipFrame.bottom, null, Canvas.ALL_SAVE_FLAG);

            canvas.save();
            float scale = getScale();
            canvas.translate(mClipFrame.left, mClipFrame.top);
            canvas.scale(scale, scale);

            for (IMGPath path : mMosaics) {
                path.onDraw(canvas);
            }

            canvas.restore();

            Paint p = new Paint();

            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            canvas.drawBitmap(mMosaicImage, null, mClipFrame, p);

            canvas.restoreToCount(sc);
        }
    }

    public void onDrawDoodles(Canvas canvas) {
        if (!mDoodles.isEmpty()) {

            canvas.save();

            float scale = getScale();
            canvas.translate(mClipFrame.left, mClipFrame.top);
            canvas.scale(scale, scale);

            for (IMGPath path : mDoodles) {
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

    public void onDrawClipWindow(Canvas canvas) {
        if (mMode == IMGMode.CLIP) {
            mClipWin.onDraw(canvas);
        }
    }

    public void onTouchDown(float x, float y) {
        moveToBackground(mForeSticker);
        if (mMode == IMGMode.CLIP) {
            mAnchor = mClipWin.getAnchor(x, y);
        }
    }

    public void onTouchDown() {
        moveToBackground(mForeSticker);
        if (mMode == IMGMode.CLIP) {
//            mAnchor
        }
    }

    public void onTouchUp() {
        mAnchor = null;

    }

    public void onScaleBegin() {
        onTouchDown();
    }

    public boolean onScroll(float dx, float dy) {
        if (mMode == IMGMode.CLIP) {
            if (mAnchor != null) {
                mClipWin.onScroll(mAnchor, dx, dy);
                return true;
            }
        }
        return false;
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

        for (IMGSticker sticker : mBackStickers) {
            M.mapRect(sticker.getFrame());
            float tPivotX = sticker.getX() + sticker.getPivotX();
            float tPivotY = sticker.getY() + sticker.getPivotY();
            sticker.setScaleX(sticker.getScaleX() * factor);
            sticker.setScaleY(sticker.getScaleY() * factor);
            sticker.setX(sticker.getX() + sticker.getFrame().centerX() - tPivotX);
            sticker.setY(sticker.getY() + sticker.getFrame().centerY() - tPivotY);
        }
    }

    public void onScaleEnd() {

    }
}

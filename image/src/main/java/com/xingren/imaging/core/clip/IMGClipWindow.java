package com.xingren.imaging.core.clip;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

import com.xingren.imaging.core.util.IMGUtils;

/**
 * Created by felix on 2017/11/29 下午5:41.
 */

public class IMGClipWindow implements IMGClip {

    /**
     * 裁剪区域
     */
    private RectF mFrame = new RectF();

    /**
     * 裁剪窗口
     */
    private RectF mWinFrame = new RectF();

    private float[] mCells = new float[16];

    private float[] mCorners = new float[32];

    private float[][] mBaseSizes = new float[2][4];

    /**
     * 是否在裁剪中
     */
    private boolean isClipping = false;

    private Matrix M = new Matrix();

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * 垂直窗口比例
     */
    private static final float VERTICAL_RATIO = 0.8f;

    private static final int COLOR_CELL = 0x80FFFFFF;

    private static final int COLOR_FRAME = Color.WHITE;

    private static final int COLOR_CORNER = Color.WHITE;

    {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
        mPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.NORMAL));
    }

    public IMGClipWindow() {

    }

    /**
     * 计算裁剪窗口区域
     */
    public void setClipWinSize(float width, float height) {
        mWinFrame.set(0, 0, width, height * VERTICAL_RATIO);

        if (!mFrame.isEmpty()) {
            IMGUtils.center(mWinFrame, mFrame);
        }
    }

    public void reset(RectF clipImage, float rotate) {
        RectF imgRect = new RectF();
        M.setRotate(rotate, clipImage.centerX(), clipImage.centerY());
        M.mapRect(imgRect, clipImage);
        reset(imgRect.width(), imgRect.height());
    }

    /**
     * 重置裁剪
     */
    private void reset(float imgWidth, float imgHeight) {
        setClipping(false);
        mFrame.set(0, 0, imgWidth, imgHeight);
        IMGUtils.fitCenter(mWinFrame, mFrame, CLIP_MARGIN);

    }

    public boolean isClipping() {
        return isClipping;
    }

    public void setClipping(boolean clipping) {
        isClipping = clipping;
    }

    public RectF getFrame() {
        return mFrame;
    }

    public void onDraw(Canvas canvas) {
        float[] size = {mFrame.width(), mFrame.height()};
        for (int i = 0; i < mBaseSizes.length; i++) {
            for (int j = 0; j < mBaseSizes[i].length; j++) {
                mBaseSizes[i][j] = size[i] * CLIP_SIZE_RATIO[j];
            }
        }

        for (int i = 0; i < mCells.length; i++) {
            mCells[i] = mBaseSizes[i & 1][CLIP_CELL_STRIDES >>> (i << 1) & 3];
        }

        for (int i = 0; i < mCorners.length; i++) {
            mCorners[i] = mBaseSizes[i & 1][CLIP_CORNER_STRIDES >>> i & 1]
                    + CLIP_CORNER_SIZES[CLIP_CORNERS[i] & 3] + CLIP_CORNER_STEPS[CLIP_CORNERS[i] >> 2];
        }

        canvas.translate(mFrame.left, mFrame.top);
        mPaint.setColor(COLOR_CELL);
        mPaint.setStrokeWidth(CLIP_THICKNESS_CELL);
        canvas.drawLines(mCells, mPaint);

        canvas.translate(-mFrame.left, -mFrame.top);
        mPaint.setColor(COLOR_FRAME);
        mPaint.setStrokeWidth(CLIP_THICKNESS_FRAME);
        canvas.drawRect(mFrame, mPaint);

        canvas.translate(mFrame.left, mFrame.top);
        mPaint.setColor(COLOR_CORNER);
        mPaint.setStrokeWidth(CLIP_THICKNESS_SEWING);
        canvas.drawLines(mCorners, mPaint);
    }

    // TODO
    public Anchor getAnchor(float x, float y) {

        int h = -1, v = -1;

        if (Math.abs(mFrame.left - x) < CLIP_CORNER_SIZE) {
            h = Anchor.LEFT.ordinal();
        }

        if (Math.abs(mFrame.right - x) < CLIP_CORNER_SIZE) {
            h = Anchor.RIGHT.ordinal();
        }

        if (Math.abs(mFrame.top - y) < CLIP_CORNER_SIZE) {
            v = Anchor.TOP.ordinal();
        }

        if (Math.abs(mFrame.bottom - y) < CLIP_CORNER_SIZE) {
            v = Anchor.BOTTOM.ordinal();
        }

        if (h >= 0 || v >= 0) {
            int index = 0;

            if (v >= 0) {
                index = v;
            }

            if (h >= 0) {
                index = (index << 1) | h;
            }

            return Anchor.values()[index];
        }

        return null;
    }

    public void onScroll(Anchor anchor, float dx, float dy) {
        anchor.d(mWinFrame, mFrame, dx, dy);
    }
}

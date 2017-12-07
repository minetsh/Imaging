package com.xingren.imaging.core.clip;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
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

    private float mWinWidth, mWinHeight;

    private float[] mCells = new float[16];

    private float[] mCorners = new float[32];

    private float[][] mBaseSizes = new float[2][4];

    /**
     * 是否在裁剪中
     */
    private boolean isClipping = false;

    private boolean isShowShade = false;

    private Matrix M = new Matrix();

    private Path mShadePath = new Path();

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * 垂直窗口比例
     */
    private static final float VERTICAL_RATIO = 0.8f;

    private static final int COLOR_CELL = 0x80FFFFFF;

    private static final int COLOR_FRAME = Color.WHITE;

    private static final int COLOR_CORNER = Color.WHITE;

    private static final int COLOR_SHADE = 0xAA000000;

    {
        mShadePath.setFillType(Path.FillType.EVEN_ODD);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
    }

    public IMGClipWindow() {

    }

    /**
     * 计算裁剪窗口区域
     */
    public void setClipWinSize(float width, float height) {
        mWinWidth = width;
        mWinHeight = height;
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

    public boolean isShowShade() {
        return isShowShade;
    }

    public void setShowShade(boolean showShade) {
        isShowShade = showShade;
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

        onDrawShade(canvas);

        canvas.translate(mFrame.left, mFrame.top);
        mPaint.setStyle(Paint.Style.STROKE);
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

    private void onDrawShade(Canvas canvas) {
        if (!isShowShade) return;

        // 计算遮罩图形
        mShadePath.reset();
        mShadePath.moveTo(0, 0);
        mShadePath.lineTo(mWinWidth, 0);
        mShadePath.lineTo(mWinWidth, mWinHeight);
        mShadePath.lineTo(0, mWinHeight);
        mShadePath.lineTo(0, 0);

        mShadePath.moveTo(mFrame.left, mFrame.top);
        mShadePath.lineTo(mFrame.right, mFrame.top);
        mShadePath.lineTo(mFrame.right, mFrame.bottom);
        mShadePath.lineTo(mFrame.left, mFrame.bottom);
        mShadePath.lineTo(mFrame.left, mFrame.top);

        mPaint.setColor(COLOR_SHADE);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(mShadePath, mPaint);
    }

    public Anchor getAnchor(float x, float y) {
        if (Anchor.isCohesionContains(mFrame, -CLIP_CORNER_SIZE, x, y)
                && !Anchor.isCohesionContains(mFrame, CLIP_CORNER_SIZE, x, y)) {
            int v = 0;
            float[] cohesion = Anchor.cohesion(mFrame, 0);
            float[] pos = {x, y};
            for (int i = 0; i < cohesion.length; i++) {
                if (Math.abs(cohesion[i] - pos[i >> 1]) < CLIP_CORNER_SIZE) {
                    v |= 1 << i;
                }
            }
            return Anchor.valueOf(v);
        }
        return null;
    }

    public void onScroll(Anchor anchor, float dx, float dy) {
        anchor.d(mWinFrame, mFrame, dx, dy);
    }
}

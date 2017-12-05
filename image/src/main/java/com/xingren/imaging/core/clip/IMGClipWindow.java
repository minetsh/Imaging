package com.xingren.imaging.core.clip;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
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

    /**
     * 是否在裁剪中
     */
    private boolean isClipping = false;

    private static final float HP = 0.8f;

    private static final float D = 40f;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    {
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(8);
        mPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.NORMAL));
    }

    public IMGClipWindow() {

    }

    /**
     * 计算裁剪窗口区域
     */
    public void setClipWinSize(float width, float height) {
        mWinFrame.set(0, 0, width, height * HP);

        if (mFrame.isEmpty()) {
            mFrame.set(0, 0, width, height);
        } else {
            IMGUtils.center(mWinFrame, mFrame);
        }
    }

    /**
     * 重置裁剪
     */
    public void reset(float imgWidth, float imgHeight) {
        isClipping = false;
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
        canvas.drawRect(mFrame, mPaint);
    }

    public Anchor getAnchor(float x, float y) {

        int h = -1, v = -1;

        if (Math.abs(mFrame.left - x) < D) {
            h = Anchor.LEFT.ordinal();
        }

        if (Math.abs(mFrame.right - x) < D) {
            h = Anchor.RIGHT.ordinal();
        }

        if (Math.abs(mFrame.top - y) < D) {
            v = Anchor.TOP.ordinal();
        }

        if (Math.abs(mFrame.bottom - y) < D) {
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

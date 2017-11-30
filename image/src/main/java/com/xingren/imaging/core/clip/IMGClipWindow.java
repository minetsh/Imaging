package com.xingren.imaging.core.clip;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Created by felix on 2017/11/29 下午5:41.
 */

public class IMGClipWindow implements IMGClip {

    private RectF mFrame = new RectF();

    private RectF mWinFrame = new RectF();

    private float mClipWidth, mClipHeight;

    private static final float HP = 0.8f;

    private static final float D = 40f;

    private static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG);

    static {
        P.setColor(Color.WHITE);
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(8);
        P.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.NORMAL));
    }

    public void setClipWinSize(float w, float h) {
        mClipWidth = w;
        mClipHeight = h;

        mWinFrame.set(0, 0, w, h * HP);
        if (mFrame.isEmpty()) {
            mFrame.set(0, 0, w, h);
        }
    }

    public void setImageSize(int width, int height) {

        // 可用宽高
        float w = mWinFrame.width() - 2 * CLIP_MARGIN;
        float h = mWinFrame.height() - 2 * CLIP_MARGIN;

        float scale = Math.min(w / width, h / height);

        mFrame.set(0, 0, width * scale, height * scale);

        mFrame.offset(mClipWidth / 2 - mFrame.centerX(), mClipHeight * HP / 2 - mFrame.centerY());

    }

    public RectF getFrame() {
        return mFrame;
    }

    public void onDraw(Canvas canvas) {
        canvas.drawRect(mFrame, P);
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

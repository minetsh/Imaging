package com.xingren.imaging.core.clip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by felix on 2017/11/29 下午5:12.
 */

public class IMGClipView extends View implements IMGClip {

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    {
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    public IMGClipView(Context context) {
        super(context);

        setBackgroundColor(0x44ff0000);
    }

    public IMGClipView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IMGClipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(new RectF(0, 0, 100, 100), mPaint);
    }
}

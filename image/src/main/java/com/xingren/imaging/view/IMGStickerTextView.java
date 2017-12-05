package com.xingren.imaging.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.TextView;

import com.xingren.imaging.core.sticker.IMGSticker;
import com.xingren.imaging.core.sticker.IMGStickerHelper;

/**
 * Created by felix on 2017/12/5 下午12:08.
 */
// TODO
public class IMGStickerTextView extends TextView implements IMGSticker {

    private IMGStickerHelper<IMGStickerTextView> mHelper;

    {
        mHelper = new IMGStickerHelper<>(this);
    }

    public IMGStickerTextView(Context context) {
        super(context);
    }

    public IMGStickerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IMGStickerTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean show() {
        return mHelper.show();
    }

    @Override
    public boolean remove() {
        return mHelper.remove();
    }

    @Override
    public boolean dismiss() {
        return mHelper.dismiss();
    }

    @Override
    public boolean isShowing() {
        return mHelper.isShowing();
    }

    @Override
    public RectF getFrame() {
        return mHelper.getFrame();
    }

    @Override
    public void onSticker(Canvas canvas) {

    }

    @Override
    public void registerCallback(Callback callback) {
        mHelper.registerCallback(callback);
    }

    @Override
    public void unregisterCallback(Callback callback) {
        mHelper.unregisterCallback(callback);
    }
}

package me.minetsh.imaging.core.sticker;

import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by felix on 2017/11/17 下午6:08.
 */

public class IMGStickerMoveHelper {

    private static final String TAG = "IMGStickerMoveHelper";

    private View mView;

    private float mX, mY;

    private static final Matrix M = new Matrix();

    public IMGStickerMoveHelper(View view) {
        mView = view;
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mX = event.getX();
                mY = event.getY();
                M.reset();
                Log.i("xxxxx", v.getTranslationX() + "" + mView.getTranslationX());
                M.setRotate(v.getRotation());
                return true;
            case MotionEvent.ACTION_MOVE:
                float[] dxy = {event.getX() - mX, event.getY() - mY};
                M.mapPoints(dxy);
                v.setTranslationX(mView.getTranslationX() + dxy[0]);
                v.setTranslationY(mView.getTranslationY() + dxy[1]);
                return true;
        }
        return false;
    }
}

package com.xingren.imaging.view;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.xingren.imaging.core.IMGMode;

/**
 * Created by felix on 2017/11/16 下午12:45.
 */

class IMGCanvasDelegate implements ScaleGestureDetector.OnScaleGestureListener {

    private static final String TAG = "IMGCanvasDelegate";

    private IMGCanvasView mView;

    private IMGMode mMode = IMGMode.NONE;

    private ScaleGestureDetector mScaleGestureDetector;

    public IMGCanvasDelegate(IMGCanvasView view) {
        mView = view;
        mScaleGestureDetector = new ScaleGestureDetector(view.getContext(), this);
    }

    public boolean onTouch(MotionEvent event) {
        if (!mScaleGestureDetector.onTouchEvent(event)) {
            switch (mMode) {
                case NONE:
                    return onTouchNONE(event);
                case DOODLE:
                    return onTouchDOODLE(event);
            }
        }
        return false;
    }

    private boolean onTouchNONE(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mView.onTouch();
                return true;
        }
        return false;
    }

    private boolean onTouchDOODLE(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mView.onTouch();
                return true;
        }
        return false;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        mView.scrollBy(Math.round(distanceX), Math.round(distanceY));
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mView.onScale(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        mView.onTouch();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        mView.onScaleEnd(detector.getScaleFactor());
    }
}

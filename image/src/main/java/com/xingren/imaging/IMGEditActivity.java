package com.xingren.imaging;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;

import com.xingren.imaging.core.IMGMode;
import com.xingren.imaging.core.IMGText;
import com.xingren.imaging.core.util.IMGUtils;

import java.io.File;

/**
 * Created by felix on 2017/11/14 下午2:26.
 */

public class IMGEditActivity extends IMGEditBaseActivity {

    public static final String EXTRA_IMAGE_PATH = "EXTRA_IMAGE_PATH";

    private static final int MAX_WIDTH = 8000;

    private static final int MAX_HEIGHT = 8000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Bitmap getBitmap() {
//        Intent intent = getIntent();
//        if (intent == null) {
//            return null;
//        }
//
//        String path = intent.getStringExtra(EXTRA_IMAGE_PATH);
//        if (TextUtils.isEmpty(path)) {
//            return null;
//        }
//
//        File file = new File(path);
//        if (!file.exists()) {
//            return null;
//        }
//
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 1;
//        options.inJustDecodeBounds = true;
//
//        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
//
//        if (options.outWidth > MAX_WIDTH) {
//            options.inSampleSize = IMGUtils.inSampleSize(Math.round(1f * options.outWidth / MAX_WIDTH));
//        }
//
//        if (options.outHeight > MAX_HEIGHT) {
//            options.inSampleSize = Math.max(options.inSampleSize,
//                    IMGUtils.inSampleSize(Math.round(1f * options.outHeight / MAX_HEIGHT)));
//        }
//
//        if (bitmap != null) {
//            bitmap.recycle();
//        }
//        options.inJustDecodeBounds = false;
//
//        bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
//        if (bitmap == null) {
//            return null;
//        }

//        return bitmap;

        return BitmapFactory.decodeResource(getResources(), R.drawable.g);
    }

    @Override
    public void onText(IMGText text) {
        mImgView.addStickerText(text);
    }

    @Override
    public void onModeClick(IMGMode mode) {
        IMGMode cm = mImgView.getMode();
        if (cm == mode) {
            mode = IMGMode.NONE;
        }
        mImgView.setMode(mode);
        updateModeUI();

        if (mode == IMGMode.CLIP) {
            setOpDisplay(OP_CLIP);
        }
    }

    @Override
    public void onUndoClick() {
        IMGMode mode = mImgView.getMode();
        if (mode == IMGMode.DOODLE) {
            mImgView.undoDoodle();
        } else if (mode == IMGMode.MOSAIC) {
            mImgView.undoMosaic();
        }
    }

    @Override
    public void onCancelClick() {
        finish();
    }

    @Override
    public void onDoneClick() {
        // TODO


        Bitmap bitmap = mImgView.saveBitmap();


        setResult(RESULT_OK, new Intent().putExtra("IMAGE", bitmap));


        finish();
    }

    @Override
    public void onCancelClipClick() {
        mImgView.cancelClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    @Override
    public void onDoneClipClick() {
        mImgView.doClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    @Override
    public void onResetClipClick() {
        mImgView.resetClip();
    }

    @Override
    public void onRotateClipClick() {
        mImgView.doRotate();
    }

    @Override
    public void onColorChanged(int checkedColor) {
        mImgView.setPenColor(checkedColor);
    }
}

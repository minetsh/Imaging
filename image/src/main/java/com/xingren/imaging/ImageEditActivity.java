package com.xingren.imaging;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.ViewSwitcher;

import com.xingren.imaging.core.IMGMode;
import com.xingren.imaging.core.IMGText;
import com.xingren.imaging.view.IMGView;

/**
 * Created by felix on 2017/11/14 下午2:26.
 */

public class ImageEditActivity extends Activity implements View.OnClickListener,
        ImageTextDialog.Callback {

    private IMGView mImageView;

    private RadioGroup mModeGroup;

    private ViewSwitcher mClipSwitcher;

    private ImageTextDialog mTextDialog;

    private ViewSwitcher mPathOpSwitcher;

    private View mDoodleAndMosaicLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_edit_activity);

        mClipSwitcher = findViewById(R.id.vs_edit_op);

        mModeGroup = findViewById(R.id.rg_modes);

        mDoodleAndMosaicLayout = findViewById(R.id.layout_dm_op);
        mPathOpSwitcher = findViewById(R.id.vs_dm_op);

        mImageView = findViewById(R.id.image_canvas);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.rb_doodle) {
            onModeClick(IMGMode.DOODLE);
        } else if (vid == R.id.btn_text) {
            onTextClick();
        } else if (vid == R.id.rb_mosaic) {
            onModeClick(IMGMode.MOSAIC);
        } else if (vid == R.id.btn_clip) {
            onModeClick(IMGMode.CLIP);
        } else if (vid == R.id.btn_undo) {
            onUndoClick();
        } else if (vid == R.id.tv_done) {
            onDoneClick();
        } else if (vid == R.id.tv_cancel) {
            onCancelClick();
        }
    }

    private void onModeClick(IMGMode mode) {
        IMGMode cm = mImageView.getMode();
        if (cm == mode) {
            mode = IMGMode.NONE;
        }
        mImageView.setMode(mode);
        updateModeUI();

        if (mode == IMGMode.CLIP) {
            mClipSwitcher.setDisplayedChild(1);
        }
    }

    private void onTextClick() {
        if (mTextDialog == null) {
            mTextDialog = new ImageTextDialog(this, this);
        }
        mTextDialog.show();
    }

    private void updateModeUI() {
        IMGMode mode = mImageView.getMode();
        switch (mode) {
            case DOODLE:
                mModeGroup.check(R.id.rb_doodle);
                mPathOpSwitcher.setDisplayedChild(0);
                mDoodleAndMosaicLayout.setVisibility(View.VISIBLE);
                break;
            case MOSAIC:
                mModeGroup.check(R.id.rb_mosaic);
                mPathOpSwitcher.setDisplayedChild(1);
                mDoodleAndMosaicLayout.setVisibility(View.VISIBLE);
                break;
            case NONE:
                mModeGroup.clearCheck();
                mDoodleAndMosaicLayout.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void onText(IMGText text) {
        mImageView.addStickerText(text);
    }

    private void onUndoClick() {
        IMGMode mode = mImageView.getMode();
        if (mode == IMGMode.DOODLE) {
            mImageView.undoDoodle();
        } else if (mode == IMGMode.MOSAIC) {
            mImageView.undoMosaic();
        }
    }

    private void onDoneClick() {
        Bitmap bitmap = mImageView.saveBitmap();
        setResult(RESULT_OK, new Intent().putExtra("IMAGE", bitmap));
        finish();
    }

    private void onCancelClick() {
        finish();
    }
}

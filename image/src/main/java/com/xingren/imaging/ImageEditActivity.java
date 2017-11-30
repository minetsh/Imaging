package com.xingren.imaging;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.ViewSwitcher;

import com.xingren.imaging.core.IMGMode;
import com.xingren.imaging.view.IMGStickerTextView;
import com.xingren.imaging.view.IMGView;

/**
 * Created by felix on 2017/11/14 下午2:26.
 */

public class ImageEditActivity extends Activity implements View.OnClickListener {

    private IMGView mImageView;

    private RadioGroup mModeGroup;

    private ViewSwitcher mClipSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_edit_activity);

        mClipSwitcher = findViewById(R.id.vs_edit_op);

        mModeGroup = findViewById(R.id.rg_modes);
        findViewById(R.id.rb_a).setOnClickListener(this);
        findViewById(R.id.rb_b).setOnClickListener(this);
        findViewById(R.id.rb_c).setOnClickListener(this);
        findViewById(R.id.rb_d).setOnClickListener(this);

        mImageView = findViewById(R.id.image_canvas);

        IMGStickerTextView sticker = new IMGStickerTextView(getApplicationContext());

        mImageView.addStickerView(sticker);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.rb_a) {
            onModeClick(IMGMode.DOODLE);
        } else if (vid == R.id.rb_b) {
            onTextClick();
        } else if (vid == R.id.rb_c) {
            onModeClick(IMGMode.MOSAIC);
        } else if (vid == R.id.rb_d) {
            onModeClick(IMGMode.CLIP);
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
        // TODO
    }

    private void updateModeUI() {
        IMGMode mode = mImageView.getMode();
        switch (mode) {
            case DOODLE:
                mModeGroup.check(R.id.rb_a);
                break;
            case MOSAIC:
                mModeGroup.check(R.id.rb_c);
                break;
            case CLIP:
                mModeGroup.check(R.id.rb_d);
                break;
            case NONE:
                mModeGroup.clearCheck();
                break;
        }
    }
}

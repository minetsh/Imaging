package com.xingren.imaging;

import android.app.Activity;
import android.os.Bundle;

import com.xingren.imaging.view.IMGStickerTextView;
import com.xingren.imaging.view.IMGView;

/**
 * Created by felix on 2017/11/14 下午2:26.
 */

public class ImageEditActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_edit_activity);

        IMGView view = findViewById(R.id.image_canvas);

        IMGStickerTextView sticker = new IMGStickerTextView(getApplicationContext());

        view.addStickerView(sticker);
    }
}

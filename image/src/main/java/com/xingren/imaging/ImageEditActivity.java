package com.xingren.imaging;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.xingren.imaging.view.IMGCanvasView;
import com.xingren.imaging.view.IMGStickerTextView;

/**
 * Created by felix on 2017/11/14 下午2:26.
 */

public class ImageEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_edit_activity);

        IMGCanvasView view = findViewById(R.id.image_canvas);

        IMGStickerTextView sticker = new IMGStickerTextView(getApplicationContext());

//        sticker.setRotation(30);
        view.addStickerView(sticker);
    }
}

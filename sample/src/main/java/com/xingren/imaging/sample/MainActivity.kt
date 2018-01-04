package com.xingren.imaging.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.facebook.drawee.backends.pipeline.Fresco
import kotlinx.android.synthetic.main.activity_main.*

const val REQ_IMAGE_EDIT = 1

const val REQ_IMAGE_CHOOSE = 2

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Fresco.initialize(this)

        setContentView(R.layout.activity_main)

        btn_edit.setOnClickListener {
            startActivity(Intent(
                    this, ImageEditSampleActivity::class.java
            ))
        }

        btn_gallery.setOnClickListener {
            startActivity(Intent(
                    this, GallerySampleActivity::class.java
            ))
        }
    }
}

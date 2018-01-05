package me.kareluo.imaging.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

const val REQ_IMAGE_EDIT = 1

const val REQ_IMAGE_CHOOSE = 2

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sdv_image_edit.setOnClickListener {
            startActivity(Intent(
                    this, ImageEditSampleActivity::class.java
            ))
        }

        sdv_image_album.setOnClickListener {
            startActivity(Intent(
                    this, GallerySampleActivity::class.java
            ))
        }
    }
}

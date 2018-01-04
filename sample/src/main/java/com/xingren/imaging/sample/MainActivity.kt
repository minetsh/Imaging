package com.xingren.imaging.sample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.facebook.drawee.backends.pipeline.Fresco
import com.xingren.imaging.IMGEditActivity
import com.xingren.imaging.IMGGalleryActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private val REQ_EDIT_IMAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Fresco.initialize(this)

        setContentView(R.layout.activity_main)

        btn_edit.setOnClickListener {

            val intent = Intent(this, IMGEditActivity::class.java)
            intent.putExtra(IMGEditActivity.EXTRA_IMAGE_URI, Uri.parse("asset:///g.jpeg"))
            intent.putExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH, File(cacheDir, "text.jpg").absolutePath)
            startActivityForResult(intent, REQ_EDIT_IMAGE)
        }

        btn_edit.performClick()

        btn_gallery.setOnClickListener {

            val intent = Intent(this, IMGGalleryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        sdv_image.setImageURI("file://" + File(cacheDir, "text.jpg").absolutePath)
    }
}

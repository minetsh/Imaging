package com.xingren.imaging.sample

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.xingren.imaging.IMGEditActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val REQ_EDIT_IMAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_edit.setOnClickListener {

            val intent = Intent(this, IMGEditActivity::class.java)
            intent.putExtra(IMGEditActivity.EXTRA_IMAGE_PATH, "/android_asset/g.jpeg")
            startActivityForResult(intent, REQ_EDIT_IMAGE)
        }

        btn_edit.performClick()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_EDIT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                val bitmap = data?.getParcelableExtra<Bitmap>("IMAGE")
                iv_image.setImageBitmap(bitmap)
            }
        }
    }
}

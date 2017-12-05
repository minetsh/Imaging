package com.xingren.imaging.sample

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.xingren.imaging.IMGEditActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val REQ_EDIT_IMAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_edit.setOnClickListener {
            startActivityForResult(Intent(this, IMGEditActivity::class.java), REQ_EDIT_IMAGE)
        }
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

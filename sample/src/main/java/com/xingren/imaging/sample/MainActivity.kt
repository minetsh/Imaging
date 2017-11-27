package com.xingren.imaging.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.xingren.imaging.ImageEditActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivity(Intent(this, ImageEditActivity::class.java))

        findViewById<View>(R.id.tv_hello).setOnClickListener {
            startActivity(Intent(this, ImageEditActivity::class.java))
        }
    }
}

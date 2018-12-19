package me.kareluo.imaging.sample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

const val REQ_IMAGE_EDIT = 1

const val REQ_IMAGE_CHOOSE = 2

const val REQ_PERMISSION_GALLERY_STORE = 3

const val REQ_PERMISSION_EDIT_STORE = 4

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sdv_image_edit.setOnClickListener {
            toEditActivity()
        }

        sdv_image_album.setOnClickListener {
            toGalleryActivity()
        }
    }

    private fun toEditActivity() {
        if (PermissionUtil.checkOrRequestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, REQ_PERMISSION_EDIT_STORE)) {
            startActivity(Intent(
                    this, ImageEditSampleActivity::class.java
            ))
        }
    }

    private fun toGalleryActivity() {
        if (PermissionUtil.checkOrRequestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, REQ_PERMISSION_EDIT_STORE)) {
            startActivity(Intent(
                    this, GallerySampleActivity::class.java
            ))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_PERMISSION_EDIT_STORE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    toEditActivity()
                } else {
                    Toast.makeText(this, "请打开存储权限", Toast.LENGTH_SHORT).show()
                }
            }
            REQ_PERMISSION_GALLERY_STORE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    toGalleryActivity()
                } else {
                    Toast.makeText(this, "请打开存储权限", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

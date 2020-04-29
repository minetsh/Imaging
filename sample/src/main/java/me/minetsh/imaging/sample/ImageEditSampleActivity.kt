package me.minetsh.imaging.sample

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_image_edit_sample.*
import me.minetsh.imaging.IMGEditActivity
import me.minetsh.imaging.IMGGalleryActivity
import me.minetsh.imaging.gallery.model.IMGChooseMode
import me.minetsh.imaging.gallery.model.IMGImageInfo
import java.io.File
import java.util.*

/**
 * Created by felix on 2018/1/4 下午5:59.
 */

class ImageEditSampleActivity : AppCompatActivity() {

    private var mImageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_edit_sample)

        btn_choose_image.setOnClickListener {
            chooseImages()
        }
    }

    private fun chooseImages() {
        startActivityForResult(
                IMGGalleryActivity.newIntent(this, IMGChooseMode.Builder()
                        .setSingleChoose(true)
                        .build()),
                REQ_IMAGE_CHOOSE
        )
    }

    private fun onChooseImages(images: List<IMGImageInfo>?) {
        val image = images?.get(0)
        if (image != null) {

            sdv_image.setImageURI(image.uri, null)

            mImageFile = File(cacheDir, UUID.randomUUID().toString() + ".jpg")

            startActivityForResult(
                    Intent(this, IMGEditActivity::class.java)
                            .putExtra(IMGEditActivity.EXTRA_IMAGE_URI, image.uri)
                            .putExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH, mImageFile?.absolutePath),
                    REQ_IMAGE_EDIT
            )
        }
    }

    private fun onImageEditDone() {
        sdv_image_edit.setImageURI(Uri.fromFile(mImageFile), null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQ_IMAGE_CHOOSE -> {
                if (resultCode == Activity.RESULT_OK) {
                    onChooseImages(IMGGalleryActivity.getImageInfos(data))
                }
            }
            REQ_IMAGE_EDIT -> {
                if (resultCode == Activity.RESULT_OK) {
                    onImageEditDone()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
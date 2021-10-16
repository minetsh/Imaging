package me.minetsh.imaging.sample

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_image_edit_sample.*
import me.minetsh.imaging.IMGEditActivity
import me.minetsh.imaging.gallery.IMGGalleryActivity
import me.minetsh.imaging.gallery.core.model.IMGChooseMode
import me.minetsh.imaging.gallery.core.model.IMGImageInfo
import java.io.File
import java.util.*

/**
 * Created by felix on 2018/1/4 下午5:59.
 */

class ImageEditSampleActivity : AppCompatActivity() {

    private var mImageFile: File? = null

    private val TAG = "ImageEditSampleActivity"

    private lateinit var launcher: ActivityResultLauncher<Intent>

    private lateinit var editLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_edit_sample)

        btn_choose_image.setOnClickListener {
            launcher.launch(IMGGalleryActivity.newIntent(this, IMGChooseMode.Builder()
                    .setSingleChoose(true)
                    .build()))
        }

        this.launcher = this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                onChooseImages(IMGGalleryActivity.getImageInfos(it.data))
            }
        }

        this.editLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                onImageEditDone()
            }
        }
    }

    private fun onChooseImages(images: List<IMGImageInfo>?) {
        val image = images?.get(0)
        Log.i("+++xx", image.toString())
        if (image != null) {

            sdv_image.setImageURI(image.uri, null)

            mImageFile = File(cacheDir, UUID.randomUUID().toString() + ".jpg")

            Log.i("+++xx", mImageFile.toString())

            this.editLauncher.launch(Intent(this, IMGEditActivity::class.java)
                    .putExtra(IMGEditActivity.EXTRA_IMAGE_URI, image.uri)
                    .putExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH, mImageFile?.absolutePath))
        }
    }

    private fun onImageEditDone() {
        sdv_image_edit.setImageURI(Uri.fromFile(mImageFile), null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQ_IMAGE_CHOOSE -> {
                Log.i("+++++", "" + requestCode)
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

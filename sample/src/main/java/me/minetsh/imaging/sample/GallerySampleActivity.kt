package me.minetsh.imaging.sample

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.common.RotationOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import kotlinx.android.synthetic.main.activity_gallery_sample.*
import me.minetsh.imaging.IMGGalleryActivity
import me.minetsh.imaging.gallery.model.IMGChooseMode
import me.minetsh.imaging.gallery.model.IMGImageInfo

/**
 * Created by felix on 2018/1/4 下午4:36.
 */

class GallerySampleActivity : AppCompatActivity() {

    private var mAdapter: GalleryImageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_sample)

        mAdapter = GalleryImageAdapter(this)
        rv_images.adapter = mAdapter

        btn_choose.setOnClickListener {
            chooseImages()
        }

        btn_custom.setOnClickListener {
            chooseImagesFromCustomAlbum()
        }
    }

    private fun chooseImages() {
        val builder = IMGChooseMode.Builder()

        builder.setSingleChoose(rg_modes.checkedRadioButtonId == R.id.rb_single)
        builder.setMaxChooseCount(et_max_count.text.toString().toInt())

        startActivityForResult(
                IMGGalleryActivity.newIntent(this, builder.build()),
                REQ_IMAGE_CHOOSE
        )
    }

    private fun chooseImagesFromCustomAlbum() {
        val builder = IMGChooseMode.Builder()

        builder.setSingleChoose(rg_modes.checkedRadioButtonId == R.id.rb_single)
        builder.setMaxChooseCount(et_max_count.text.toString().toInt())

        startActivityForResult(
                IMGGalleryActivity.newIntent(this, builder.build())
                        .setClass(this, CustomGalleryActivity::class.java),
                REQ_IMAGE_CHOOSE
        )
    }

    private fun onChooseImages(images: List<IMGImageInfo>?) {
        mAdapter?.mImages = images
        mAdapter?.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQ_IMAGE_CHOOSE -> {
                if (resultCode == Activity.RESULT_OK) {
                    onChooseImages(IMGGalleryActivity.getImageInfos(data))
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}

class GalleryImageAdapter(private val context: Context) : RecyclerView.Adapter<GalleryImageViewHolder>() {

    var mImages: List<IMGImageInfo>? = null

    private val layoutInflater by lazy {
        LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryImageViewHolder {
        return GalleryImageViewHolder(layoutInflater.inflate(R.layout.layout_image, parent, false))
    }

    override fun onBindViewHolder(holder: GalleryImageViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.update(item)
        }
    }

    private fun getItem(position: Int): IMGImageInfo? {
        return if (position in 0..(itemCount - 1)) mImages?.get(position) else null
    }

    override fun getItemCount(): Int {
        return mImages?.size ?: 0
    }
}

class GalleryImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val imageView: SimpleDraweeView = itemView.findViewById(R.id.sdv_image)

    fun update(image: IMGImageInfo) {
        val request = ImageRequestBuilder.newBuilderWithSource(image.uri)
                .setLocalThumbnailPreviewsEnabled(true)
                .setResizeOptions(ResizeOptions(300, 300))
                .setRotationOptions(RotationOptions.autoRotate())
                .build()

        val controller = Fresco.newDraweeControllerBuilder()
                .setOldController(imageView.controller)
                .setImageRequest(request)
                .build()

        imageView.controller = controller
    }
}
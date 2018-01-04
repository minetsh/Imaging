package com.xingren.imaging;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.xingren.imaging.gallery.IMGChooseMode;
import com.xingren.imaging.gallery.IMGScanTask;
import com.xingren.imaging.gallery.IMGScanner;
import com.xingren.imaging.model.IMGImageViewModel;

import java.util.List;
import java.util.Map;

/**
 * Created by felix on 2017/11/14 上午11:30.
 */

public class IMGGalleryActivity extends Activity {

    private ImageAdapter mAdapter;

    private RecyclerView mRecyclerView;

    private IMGChooseMode mGalleryMode;

    private Map<String, List<IMGImageViewModel>> mImages;

    private static final String EXTRA_CHOOSE_MODE = "CHOOSE_MODE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_gallery_activity);

        mGalleryMode = getIntent().getParcelableExtra(EXTRA_CHOOSE_MODE);
        if (mGalleryMode == null) {
            mGalleryMode = new IMGChooseMode();
        }

        mRecyclerView = findViewById(R.id.rv_images);
        mRecyclerView.setAdapter(mAdapter = new ImageAdapter());

        new IMGScanTask(this).execute();
    }

    public void onImages(Map<String, List<IMGImageViewModel>> images) {
        mImages = images;
        if (images != null) {
            mAdapter.setModels(images.get(IMGScanner.ALL_IMAGES));
            mAdapter.notifyDataSetChanged();
        }
    }

    public void onQuicklyImages(List<IMGImageViewModel> images) {
        mAdapter.setModels(images);
        mAdapter.notifyDataSetChanged();
    }

    public static Intent buildIntent(Context context, IMGChooseMode mode) {
        return new Intent(context, IMGGalleryActivity.class)
                .putExtra(EXTRA_CHOOSE_MODE, mode);
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {

        private List<IMGImageViewModel> models;

        private void setModels(List<IMGImageViewModel> models) {
            this.models = models;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ImageViewHolder(getLayoutInflater()
                    .inflate(R.layout.image_layout_image, parent, false));
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            holder.update(models.get(position), mGalleryMode);
        }

        @Override
        public int getItemCount() {
            return models != null ? models.size() : 0;
        }
    }

    private static class ImageViewHolder extends RecyclerView.ViewHolder {

        private CheckBox mCheckBox;

        private SimpleDraweeView mImageView;

        private ImageViewHolder(View itemView) {
            super(itemView);
            mCheckBox = itemView.findViewById(R.id.cb_box);
            mImageView = itemView.findViewById(R.id.sdv_image);
        }

        private void update(IMGImageViewModel model, IMGChooseMode mode) {
            mCheckBox.setChecked(model.isSelected());
            mImageView.setImageURI(model.getUri());

            mCheckBox.setVisibility(mode.isSingleChoose() ? View.GONE : View.VISIBLE);

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(model.getUri())
                    .setLocalThumbnailPreviewsEnabled(true)
                    .disableDiskCache()
                    .setResizeOptions(new ResizeOptions(300, 300))
                    .setRotationOptions(RotationOptions.autoRotate())
                    .build();

            AbstractDraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(mImageView.getController())
                    .setImageRequest(request)
                    .build();

            mImageView.setController(controller);
        }
    }
}

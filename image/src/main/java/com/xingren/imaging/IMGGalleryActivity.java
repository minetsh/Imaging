package com.xingren.imaging;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.facebook.drawee.view.SimpleDraweeView;
import com.xingren.imaging.model.IMGImageViewModel;

import java.util.List;

/**
 * Created by felix on 2017/11/14 上午11:30.
 */

public class IMGGalleryActivity extends Activity {

    private ImageAdapter mAdapter;

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_gallery_activity);
        mRecyclerView = findViewById(R.id.rv_images);

        mAdapter = new ImageAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {

        private List<IMGImageViewModel> models;

        public void setModels(List<IMGImageViewModel> models) {
            this.models = models;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ImageViewHolder(getLayoutInflater()
                    .inflate(R.layout.image_layout_image, parent, false));
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            holder.update(models.get(position));
        }

        @Override
        public int getItemCount() {
            return models != null ? models.size() : 0;
        }
    }

    private static class ImageViewHolder extends RecyclerView.ViewHolder {

        private CheckBox mCheckBox;

        private SimpleDraweeView mDraweeView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            mCheckBox = itemView.findViewById(R.id.cb_box);
            mDraweeView = itemView.findViewById(R.id.sdv_image);
        }

        public void update(IMGImageViewModel model) {
            mCheckBox.setChecked(model.isSelected());
            mDraweeView.setImageURI(model.getUri());
        }
    }
}

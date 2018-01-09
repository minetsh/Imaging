package me.kareluo.imaging;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.kareluo.imaging.gallery.IMGGalleryMenuWindow;
import me.kareluo.imaging.gallery.IMGScanTask;
import me.kareluo.imaging.gallery.IMGScanner;
import me.kareluo.imaging.gallery.model.IMGChooseMode;
import me.kareluo.imaging.gallery.model.IMGImageInfo;
import me.kareluo.imaging.gallery.model.IMGImageViewModel;
import me.kareluo.imaging.widget.IMGGalleryHolderCallback;

/**
 * Created by felix on 2017/11/14 上午11:30.
 */

public class IMGGalleryActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageAdapter mAdapter;

    private RecyclerView mRecyclerView;

    private IMGChooseMode mGalleryMode;

    private TextView mAlbumFolderView;

    private View mFooterView;

    private IMGGalleryMenuWindow mGalleryMenuWindow;

    private Map<String, List<IMGImageViewModel>> mImages;

    private List<IMGImageViewModel> mChooseImages = new ArrayList<>();

    private static final String EXTRA_IMAGES = "IMAGES";

    private static final String EXTRA_CHOOSE_MODE = "CHOOSE_MODE";

    private static final int[] ATTRS = new int[]{
            R.attr.image_gallery_span_count,
            R.attr.image_gallery_select_shade
    };

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

        mFooterView = findViewById(R.id.layout_footer);

        mAlbumFolderView = findViewById(R.id.tv_album_folder);
        mAlbumFolderView.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_menu_gallery, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.image_menu_done) {
            onDone();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onImages(Map<String, List<IMGImageViewModel>> images) {
        mImages = images;
        if (images != null) {
            mAdapter.setModels(images.get(IMGScanner.ALL_IMAGES));
            mAdapter.notifyDataSetChanged();

            IMGGalleryMenuWindow window = getGalleryMenuWindow();
            Set<String> keys = images.keySet();
            List<String> items = new ArrayList<>(keys);
            if (!items.isEmpty() && !IMGScanner.ALL_IMAGES.equals(items.get(0))) {
                items.remove(IMGScanner.ALL_IMAGES);
                items.add(0, IMGScanner.ALL_IMAGES);
            }
            window.setMenuItems(items);
        }
    }

    public void onQuicklyImages(List<IMGImageViewModel> images) {
        mAdapter.setModels(images);
        mAdapter.notifyDataSetChanged();
    }

    private void onImageCheckClick(int position) {
        IMGImageViewModel item = mAdapter.getItem(position);
        if (item != null) {
            if (!item.isSelected()) {
                if (mChooseImages.size() >= mGalleryMode.getMaxChooseCount()) {
                    // TODO 达到最大限度
                    mAdapter.notifyItemChanged(position, true);
                    return;
                }
            }

            item.toggleSelected();
            if (item.isSelected()) {
                mChooseImages.add(item);
            } else {
                mChooseImages.remove(item);
            }

            mAdapter.notifyItemChanged(position, true);
        }
    }

    private void onImageClick(int position) {
        IMGImageViewModel item = mAdapter.getItem(position);
        if (item != null) {
            if (mGalleryMode.isSingleChoose()) {
                mChooseImages.clear();
                item.setSelected(true);
                mChooseImages.add(item);
                onDone();
            }
        }
    }

    private void onDone() {
        ArrayList<IMGImageInfo> infos = new ArrayList<>();
        for (IMGImageViewModel model : mChooseImages) {
            infos.add(new IMGImageInfo(model));
        }
        setResult(RESULT_OK, new Intent().putParcelableArrayListExtra(EXTRA_IMAGES, infos));
        finish();
    }

    private IMGGalleryMenuWindow getGalleryMenuWindow() {
        if (mGalleryMenuWindow == null) {
            mGalleryMenuWindow = new IMGGalleryMenuWindow(this);
        }
        return mGalleryMenuWindow;
    }

    public static ArrayList<IMGImageInfo> getImageInfos(Intent intent) {
        if (intent != null) {
            return intent.getParcelableArrayListExtra(EXTRA_IMAGES);
        }
        return null;
    }

    public static Intent newIntent(Context context, IMGChooseMode mode) {
        return new Intent(context, IMGGalleryActivity.class)
                .putExtra(EXTRA_CHOOSE_MODE, mode);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_album_folder) {
            showGalleryMenu();
        }
    }

    private void showGalleryMenu() {
        IMGGalleryMenuWindow window = getGalleryMenuWindow();
        if (window != null) {
            window.show(mFooterView);
        }
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder>
            implements IMGGalleryHolderCallback {

        private List<IMGImageViewModel> models;

        private void setModels(List<IMGImageViewModel> models) {
            this.models = models;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ImageViewHolder(getLayoutInflater().inflate(
                    R.layout.image_layout_image, parent, false), this);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            holder.update(models.get(position), mGalleryMode);
        }

        @Override
        public int getItemCount() {
            return models != null ? models.size() : 0;
        }

        private IMGImageViewModel getItem(int position) {
            if (position >= 0 && position < getItemCount()) {
                return models.get(position);
            }
            return null;
        }

        @Override
        public void onViewHolderClick(RecyclerView.ViewHolder holder) {
            onImageClick(holder.getAdapterPosition());
        }

        @Override
        public void onCheckClick(RecyclerView.ViewHolder holder) {
            onImageCheckClick(holder.getAdapterPosition());
        }
    }

    private static class ImageViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private CheckBox mCheckBox;

        private SimpleDraweeView mImageView;

        private IMGGalleryHolderCallback mCallback;

        private static Drawable FORE_DRAWABLE = null;

        private ImageViewHolder(View itemView, IMGGalleryHolderCallback callback) {
            super(itemView);
            mCallback = callback;

            mCheckBox = itemView.findViewById(R.id.cb_box);
            mImageView = itemView.findViewById(R.id.sdv_image);

            mCheckBox.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        private void update(IMGImageViewModel model, IMGChooseMode mode) {
            mCheckBox.setChecked(model.isSelected());
            mCheckBox.setVisibility(mode.isSingleChoose() ? View.GONE : View.VISIBLE);

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(model.getUri())
                    .setLocalThumbnailPreviewsEnabled(true)
                    .setResizeOptions(new ResizeOptions(300, 300))
                    .setRotationOptions(RotationOptions.autoRotate())
                    .build();

            AbstractDraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(mImageView.getController())
                    .setImageRequest(request)
                    .build();

            mImageView.setController(controller);
        }

        @Override
        public void onClick(View v) {
            if (mCallback != null) {
                if (v.getId() == R.id.cb_box) {
                    mCallback.onCheckClick(this);
                } else {
                    mCallback.onViewHolderClick(this);
                }
            }
        }
    }
}

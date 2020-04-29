package me.minetsh.imaging.gallery;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import me.minetsh.imaging.R;
import me.minetsh.imaging.widget.IMGViewHolderCallback;

/**
 * Created by felix on 2018/1/5 下午1:51.
 */

public class IMGGalleryMenuWindow extends PopupWindow {

    private Context mContext;

    private MenuAdapter mAdapter;

    private RecyclerView mMenuRecyclerView;

    private List<GalleryMenuItemViewModel> mItemModels;

    private LayoutInflater mLayoutInflater;

    public IMGGalleryMenuWindow(Context context) {
        super(context);
        mContext = context;

        View contentView = getLayoutInflater().inflate(
                R.layout.image_layout_gallery_pop, null, false);

        setContentView(contentView);

        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        setHeight(Math.round(metrics.heightPixels * 0.76f));

        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(0x60000000));

        mAdapter = new MenuAdapter();
        mMenuRecyclerView = contentView.findViewById(R.id.image_rv_menu);
        mMenuRecyclerView.setAdapter(mAdapter);
    }

    private LayoutInflater getLayoutInflater() {
        if (mLayoutInflater == null) {
            mLayoutInflater = LayoutInflater.from(mContext);
        }
        return mLayoutInflater;
    }

    public void setMenuItems(List<String> menuItems) {
        if (menuItems != null) {
            if (mItemModels == null) {
                mItemModels = new ArrayList<>();
            }
            mItemModels.clear();

            GalleryMenuItemViewModel _all = null;
            for (String item : menuItems) {
                GalleryMenuItemViewModel model = new GalleryMenuItemViewModel(
                        null, item, IMGScanner.ALL_IMAGES.equals(item)
                );
                if (IMGScanner.ALL_IMAGES.equals(item)) {
                    _all = model;
                }
                mItemModels.add(model);
            }

            ensureSingleItemSelected(_all);
        }
    }

    private void ensureSingleItemSelected(GalleryMenuItemViewModel model) {
        if (mItemModels != null) {
            for (GalleryMenuItemViewModel m : mItemModels) {
                m.isSelected = m == model;
            }
        }
    }

    private void onItemSelected(int position) {
        GalleryMenuItemViewModel item = mAdapter.getItem(position);
        if (item != null) {
            ensureSingleItemSelected(item);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void show(View parent) {
        showAsDropDown(parent, 0, 0);
    }

    private class MenuAdapter extends RecyclerView.Adapter<MenuItemViewHolder>
            implements IMGViewHolderCallback {

        @Override
        public MenuItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MenuItemViewHolder(getLayoutInflater().inflate(
                    R.layout.image_layout_gallery_menu_item, parent, false), this);
        }

        @Override
        public void onBindViewHolder(MenuItemViewHolder holder, int position) {
            holder.update(getItem(position));
        }

        public GalleryMenuItemViewModel getItem(int position) {
            if (position >= 0 && position < getItemCount()) {
                return mItemModels.get(position);
            }
            return null;
        }

        @Override
        public int getItemCount() {
            return mItemModels == null ? 0 : mItemModels.size();
        }

        @Override
        public void onViewHolderClick(RecyclerView.ViewHolder holder) {
            onItemSelected(holder.getAdapterPosition());
        }
    }

    private static class MenuItemViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private SimpleDraweeView imageView;

        private TextView textView;

        private RadioButton radioButton;

        private IMGViewHolderCallback callback;

        public MenuItemViewHolder(View itemView, IMGViewHolderCallback callback) {
            super(itemView);

            this.callback = callback;

            imageView = itemView.findViewById(R.id.sdv_image);
            textView = itemView.findViewById(R.id.tv_name);
            radioButton = itemView.findViewById(R.id.rb_select);

            itemView.setOnClickListener(this);
        }

        public void update(GalleryMenuItemViewModel model) {
            textView.setText(model.name);
            radioButton.setChecked(model.isSelected);
        }

        @Override
        public void onClick(View v) {
            if (callback != null) {
                callback.onViewHolderClick(this);
            }
        }
    }

    public static class GalleryMenuItemViewModel {

        private Uri uri;

        private String name;

        private boolean isSelected;

        public GalleryMenuItemViewModel(Uri uri, String name, boolean isSelected) {
            this.uri = uri;
            this.name = name;
            this.isSelected = isSelected;
        }
    }
}

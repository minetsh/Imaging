package me.minetsh.imaging.gallery.widget;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by felix on 2018/1/4 下午3:53.
 */

public interface IMGGalleryHolderCallback extends IMGViewHolderCallback {

    void onCheckClick(RecyclerView.ViewHolder holder);
}

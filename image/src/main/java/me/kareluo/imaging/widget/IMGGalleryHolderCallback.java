package me.kareluo.imaging.widget;

import android.support.v7.widget.RecyclerView;

/**
 * Created by felix on 2018/1/4 下午3:53.
 */

public interface IMGGalleryHolderCallback extends IMGViewHolderCallback {

    void onCheckClick(RecyclerView.ViewHolder holder);
}

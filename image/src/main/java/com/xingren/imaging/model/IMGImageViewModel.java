package com.xingren.imaging.model;

import android.net.Uri;

/**
 * Created by felix on 2017/12/26 上午11:21.
 */

public class IMGImageViewModel {

    private Uri uri;

    private boolean isSelected;

    private boolean isOriginal;

    public IMGImageViewModel(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public boolean isOriginal() {
        return isOriginal;
    }

    public void setOriginal(boolean original) {
        isOriginal = original;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

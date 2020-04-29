package me.minetsh.imaging.gallery;

import android.os.AsyncTask;

import me.minetsh.imaging.IMGGalleryActivity;
import me.minetsh.imaging.gallery.model.IMGImageViewModel;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

/**
 * Created by felix on 2018/1/4 下午2:26.
 */

public class IMGScanTask extends AsyncTask<Void, List<IMGImageViewModel>, Map<String, List<IMGImageViewModel>>> {

    private WeakReference<IMGGalleryActivity> mActivity;

    public IMGScanTask(IMGGalleryActivity activity) {
        this.mActivity = new WeakReference<>(activity);
    }

    @Override
    protected Map<String, List<IMGImageViewModel>> doInBackground(Void... contexts) {
        if (mActivity != null && mActivity.get() != null) {
            return IMGScanner.getImages14(mActivity.get(), 64, new IMGScanner.Callback() {
                @Override
                public void onImages(List<IMGImageViewModel> images) {
                    publishProgress(images);
                }
            });
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(List<IMGImageViewModel>[] values) {
        if (mActivity != null) {
            IMGGalleryActivity activity = mActivity.get();
            if (activity != null) {
                if (values != null && values.length > 0) {
                    activity.onQuicklyImages(values[0]);
                }
            }
        }
    }

    @Override
    protected void onPostExecute(Map<String, List<IMGImageViewModel>> images) {
        if (mActivity != null) {
            IMGGalleryActivity activity = mActivity.get();
            if (activity != null) {
                activity.onImages(images);
            }
        }
    }
}
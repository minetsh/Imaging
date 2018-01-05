package me.kareluo.imaging.gallery;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import me.kareluo.imaging.gallery.model.IMGImageViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by felix on 2018/1/4 下午1:27.
 */

public class IMGScanner {

    private static final String[] SELECTION_ARGS = new String[]{
            "image/jpeg", "image/png", "image/webp"
    };

    private static final String SELECTION = String.format(
            "%s=? or %s=? or %s=?",
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.MIME_TYPE
    );

    public static final String ALL_IMAGES = "所有图片";

    public static Map<String, List<IMGImageViewModel>> getImages14(Context context, int count, Callback callback) {
        LinkedHashMap<String, List<IMGImageViewModel>> images = new LinkedHashMap<>();

        ContentResolver contentResolver = context.getContentResolver();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        Cursor cursor = null;

        try {

            cursor = contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Images.Media.DATA,
                            MediaStore.Images.Media.SIZE
                    },
                    SELECTION, SELECTION_ARGS,
                    MediaStore.Images.Media.DATE_MODIFIED + " desc"
            );

            List<IMGImageViewModel> allInfos = new ArrayList<>();
            images.put(ALL_IMAGES, allInfos);
            if (cursor == null) {
                return images;
            }
            while (cursor.moveToNext()) {

                long size = cursor.getLong(
                        cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
                );

                String path = cursor.getString(
                        cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                );

                if (size == 0 || TextUtils.isEmpty(path)) {
                    continue;
                }

                File file = new File(path);
                if (file.exists()) {
                    // 通过读取文件获取图片宽高
                    BitmapFactory.decodeFile(path, options);
                    IMGImageViewModel model = new IMGImageViewModel(Uri.fromFile(file));
                    model.setWidth(options.outWidth);
                    model.setHeight(options.outHeight);
                    model.setSize(size);
                    File parentFile = file.getParentFile();
                    if (parentFile != null && parentFile.exists()) {
                        String folderName = file.getParentFile().getName();
                        List<IMGImageViewModel> infos = images.get(folderName);
                        if (infos == null) {
                            infos = new ArrayList<>();
                            images.put(folderName, infos);
                        }
                        infos.add(model);
                        allInfos.add(model);
                        if (callback != null && allInfos.size() >= count) {
                            callback.onImages(allInfos);
                        }
                    }
                }
            }
            return images;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public interface Callback {

        void onImages(List<IMGImageViewModel> images);
    }
}

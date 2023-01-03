package me.minetsh.imaging.core.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.spi.FileSystemProvider;
import java.util.UUID;

/**
 * Created by felix on 2017/12/26 下午3:07.
 */

public class IMGContentDecoder extends IMGDecoder {

    private Context mContext;

    public IMGContentDecoder(Context context, Uri uri) {
        super(uri);
        this.mContext = context;
    }

    @Override
    public Bitmap decode(BitmapFactory.Options options) {
        Uri uri = getUri();
        if (uri == null) {
            return null;
        }

        File file = new File(mContext.getCacheDir(), UUID.randomUUID().toString());
        Uri u = IMGProvider.getUriForFile(this.mContext, mContext.getPackageName(), file);

        if (file.exists()) {
            return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        }

        return null;
    }
}

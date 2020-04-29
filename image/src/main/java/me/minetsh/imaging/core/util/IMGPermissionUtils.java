package me.minetsh.imaging.core.util;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class IMGPermissionUtils {

    private IMGPermissionUtils() {

    }

    public static boolean isPermissionGranted(Context context, String permission) {
        return checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static int checkSelfPermission(Context context, String permission) {
        return ActivityCompat.checkSelfPermission(context, permission);
    }

    public static void requestPermissions() {

    }
}

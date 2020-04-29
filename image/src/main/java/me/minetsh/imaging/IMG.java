package me.minetsh.imaging;

import android.content.Context;

/**
 * Created by felix on 2017/12/26 下午1:34.
 */

public class IMG {

    private static Context mApplicationContext;

    public static void initialize(Context context) {
        mApplicationContext = context.getApplicationContext();

    }

    public static class Config {

        private boolean isSave;

    }
}

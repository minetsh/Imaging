package me.minetsh.imaging.sample

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco

/**
 * Created by felix on 2018/1/5 下午12:34.
 */

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(applicationContext)
    }
}
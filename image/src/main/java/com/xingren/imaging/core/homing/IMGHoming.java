package com.xingren.imaging.core.homing;

/**
 * Created by felix on 2017/11/28 下午4:14.
 */

public class IMGHoming {

    public float x, y;

    public float scale;

    public IMGHoming() {

    }

    public IMGHoming(float x, float y, float scale) {
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    public void set(float x, float y, float scale) {
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    @Override
    public String toString() {
        return "IMGHoming{" +
                "x=" + x +
                ", y=" + y +
                ", scale=" + scale +
                '}';
    }
}

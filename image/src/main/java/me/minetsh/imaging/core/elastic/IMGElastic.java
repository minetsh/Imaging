package me.minetsh.imaging.core.elastic;

import android.graphics.PointF;

/**
 * Created by felix on 2017/11/27 下午6:43.
 */

public class IMGElastic {

    private float width, height;

    private PointF pivot = new PointF();

    public IMGElastic() {

    }

    public IMGElastic(float x, float y) {
        pivot.set(x, y);
    }

    public IMGElastic(float x, float y, float width, float height) {
        pivot.set(x, y);
        this.width = width;
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getX() {
        return pivot.x;
    }

    public void setX(float x) {
        pivot.x = x;
    }

    public float getY() {
        return pivot.y;
    }

    public void setY(float y) {
        pivot.y = y;
    }

    public void setXY(float x, float y) {
        pivot.set(x, y);
    }

    public PointF getPivot() {
        return pivot;
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void set(float x, float y, float width, float height) {
        pivot.set(x, y);
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return "IMGElastic{" +
                "width=" + width +
                ", height=" + height +
                ", pivot=" + pivot +
                '}';
    }
}

package com.xingren.imaging.core;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by felix on 2017/11/22 下午6:13.
 */

public class IMGPath {

    private Path path;

    private int color = Color.RED;

    private Matrix matrix = new Matrix();

    private IMGMode mode = IMGMode.DOODLE;

    private static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final float SW_BASE = 3f;

    static {
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(SW_BASE);
    }

    public IMGPath(Path path, IMGMode mode, int color) {
        this.path = path;
        this.mode = mode;
        this.color = color;
    }

    public void onDraw(Canvas canvas) {
        if (mode == IMGMode.DOODLE) {
            onDrawDoodle(canvas);
        } else if (mode == IMGMode.MOSAIC) {

        }
    }

    private void onDrawDoodle(Canvas canvas) {
        P.setColor(color);
        // rewind
        //
        canvas.save();
        canvas.concat(matrix);
        canvas.drawPath(path, P);
        canvas.restore();
    }

    public void transform(Matrix matrix) {
        this.matrix.postConcat(matrix);
    }

    public static void setStrokeWidth(float width) {
        P.setStrokeWidth(width);
    }

    public static void setStrokeWidthScale(float scale) {
        setStrokeWidth(SW_BASE * scale);
    }
}

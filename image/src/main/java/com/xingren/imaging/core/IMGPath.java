package com.xingren.imaging.core;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by felix on 2017/11/22 下午6:13.
 */

public class IMGPath {

    private Path path;

    private int color = Color.RED;

    private IMGMode mode = IMGMode.DOODLE;

    private static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG);

    static {
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(20f);
        P.setPathEffect(new CornerPathEffect(20));
        P.setStrokeCap(Paint.Cap.ROUND);
        P.setStrokeJoin(Paint.Join.ROUND);
    }

    public IMGPath(Path path, IMGMode mode, int color) {
        this.path = path;
        this.mode = mode;
        this.color = color;
        if (mode == IMGMode.MOSAIC) {
            path.setFillType(Path.FillType.EVEN_ODD);
        }
    }

    public IMGPath(Path path, IMGMode mode) {
        this.path = path;
        this.mode = mode;
        if (mode == IMGMode.MOSAIC) {
            path.setFillType(Path.FillType.EVEN_ODD);
        }
    }

    public void onDraw(Canvas canvas) {
        if (mode == IMGMode.DOODLE) {
            onDrawDoodle(canvas);
        } else if (mode == IMGMode.MOSAIC) {
            onDrawMosaic(canvas);
        }
    }

    private void onDrawDoodle(Canvas canvas) {
        P.setColor(color);
        // rewind
        canvas.drawPath(path, P);
    }

    private void onDrawMosaic(Canvas canvas) {
        P.setColor(Color.BLACK);
        canvas.drawPath(path, P);
    }

    public void transform(Matrix matrix) {
        path.transform(matrix);
    }
}

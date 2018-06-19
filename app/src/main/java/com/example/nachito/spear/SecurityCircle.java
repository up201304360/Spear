package com.example.nachito.spear;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SecurityCircle extends SurfaceView {


    static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    static SurfaceHolder surfaceHolder;


    public SecurityCircle(Context context) {
        super(context);
        surfaceHolder = getHolder();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
    }
}

package com.example.nachito.spear;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ines on 5/18/17.
 */

public class StopTeleop extends View {
    int color = Color.parseColor("#ff0000"), pressed_color = Color.parseColor("#568203");
    private StopListener listener;

    public StopTeleop(Context context) {
        super(context);
    }

    public StopTeleop(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StopTeleop(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setOnStop(StopListener listener) {
        this.listener = listener;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.setBackgroundColor(pressed_color);
        int actionType = event.getAction();

        if (listener != null)
            listener.stop();

        invalidate();
        if (actionType == MotionEvent.ACTION_UP) {
            release();
        }

        return true;
    }


    public void release() {

        this.setBackgroundColor(color);
        if (listener != null) {
            listener.OnReleaseStop();
        }
    }
}


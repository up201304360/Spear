package com.example.nachito.spear;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 *
 * Created by nachito on 17/05/17.
 */

public class Accelerate  extends View {
    int color = Color.parseColor("#39B7CD"), pressed_color = Color.parseColor("#568203");
    private AccelListener listener;

    public Accelerate(Context context) {
        super(context);
    }

    public Accelerate(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Accelerate(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnAccelerate(AccelListener listener) {
        this .listener = listener;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        this.setBackgroundColor(pressed_color);
        int actionType = event.getAction();

        if (listener != null)
            listener.accelerate();
        invalidate();
        if(actionType == MotionEvent.ACTION_UP) {
            performClick();
            release();}
        return true;
    }



    public void release(){


        this.setBackgroundColor(color);
        if (listener != null) {
            listener.OnReleaseAcc();
        }
    }

}

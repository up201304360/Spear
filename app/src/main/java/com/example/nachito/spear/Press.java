package com.example.nachito.spear;

import android.content.Context;

import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 *
 * Created by ines on 8/22/17.
 */
public class Press extends View  {
    private PressListener listener;


    public Press(Context context) {
        super(context);
    }

    public Press(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Press(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setonPress(PressListener listener) {
        this .listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent v) {
        if (listener != null) {

            double x = v.getX()  ;
            double y = (v.getY());




            listener.onLongPress(x,y);
        }


        return true;
    }


}
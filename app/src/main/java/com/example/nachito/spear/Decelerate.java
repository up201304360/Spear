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

public class Decelerate extends View {
    int color = Color.parseColor("#39B7CD"), pressed_color = Color.parseColor("#568203");
    private DecListener listener;


    public Decelerate(Context context) {
        super(context);
    }

    public Decelerate(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Decelerate(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnDec(DecListener listener) {
        this.listener = listener;
    }


    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int actionType = event.getAction();
        this.setBackgroundColor(pressed_color);
        if (listener != null)
            listener.dec();

        invalidate();
        if (actionType == MotionEvent.ACTION_UP) {
            release();
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                performClick();
                return true;
        }
        return true;
    }


    public void release() {

        this.setBackgroundColor(color);
        if (listener != null) {
            listener.OnReleaseDec();
        }
    }

}

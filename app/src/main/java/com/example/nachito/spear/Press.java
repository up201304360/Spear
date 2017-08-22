package com.example.nachito.spear;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.maps.model.MarkerOptions;

import org.androidannotations.annotations.EFragment;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;

/**
 * Created by ines on 8/22/17.
 */
public class Press extends View  {
    private PressListener listener;
    GeoPoint p;




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

            double x =  (v.getX()* 1E6);
                double y = (v.getY()* 1E6);

                System.out.println("x: " + x + " y: " + y);




           listener.onLongPress(x,y);
            }


        return true;
    }


}
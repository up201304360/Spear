package com.example.nachito.spear;


import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.InfoWindow;


import java.util.ArrayList;

/**
 * Created by ines on 8/14/17.
 */

public class Line extends MainActivity implements  MapEventsReceiver{
    MapView map;
    ArrayList<GeoPoint> markerPoints = new ArrayList<>();
    Marker nodeMarker;
    float mGroundOverlayBearing = 0.0f;
ArrayList<ArrayList<GeoPoint>> points;
    Polyline polyline;
    Button done;



    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(final GeoPoint p) {

        markerPoints.add(p);
        System.out.println(markerPoints);
        GroundOverlay myGroundOverlay = new GroundOverlay();
        myGroundOverlay.setPosition(p);
        myGroundOverlay.setDimensions(2000.0f);
        myGroundOverlay.setBearing(mGroundOverlayBearing);
        mGroundOverlayBearing += 20.0f;
        map.getOverlays().add(myGroundOverlay);
        map.invalidate();


        Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);
        nodeMarker = new Marker(map);
        nodeMarker.setPosition(p);
        nodeMarker.setIcon(nodeIcon);
        nodeMarker.isDraggable();
        nodeMarker.setDraggable(true);
        nodeMarker.setTitle("lat/lon:" + p);
        map.getOverlays().add(nodeMarker);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (markerPoints.size() < 1) {

                    //////////////////////////

                } else if (markerPoints.size() > 1) {


                    GeoPoint origin =  markerPoints.get(markerPoints.size() - 2);
                    GeoPoint dest =  markerPoints.get(markerPoints.size() - 1);
                    drawLine(origin, dest,  markerPoints);


                }
            }
        });

        return true;
    }





    public void drawLine(GeoPoint origin, GeoPoint dest, ArrayList<GeoPoint> markerPoints) {
        points = new ArrayList<>();
        points.add(markerPoints);
        points = new ArrayList<>();
        points.add(markerPoints);
        polyline = new Polyline();
        polyline.setWidth(7);
        polyline.setGeodesic(true);
        for (int i = 0; i < points.size(); i++)
            polyline.setPoints(points.get(i));
        polyline.setInfoWindow(new BasicInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, map));
        polyline.setTitle("Origin on " + origin + " Dest on " + dest);
        map.getOverlayManager().add(polyline);
        map.invalidate();

    }


}



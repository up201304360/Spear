package com.example.nachito.spear;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.InfoWindow;


import java.util.ArrayList;

/**
 * Created by ines on 8/14/17.
 */

public class Line extends Activity implements PressListener {
    MapView map;
    ArrayList<GeoPoint> markerPoints = new ArrayList<>();
    Marker nodeMarker;
    ArrayList<ArrayList<GeoPoint>> points;
    Button done;
    InfoWindow infoWindow;
    Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        map = (MapView) findViewById(R.id.map);
        done = (Button) findViewById(R.id.done);



        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (markerPoints.size() < 1) {

                    //////////////////////////


                } else if (markerPoints.size() > 1) {


                    GeoPoint origin = markerPoints.get(markerPoints.size() - 2);
                    GeoPoint dest = markerPoints.get(markerPoints.size() - 1);
                    drawLine(origin, dest, markerPoints);

                }
            }
        });
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
    }



    float mGroundOverlayBearing = 0.0f;



    public void drawLine(GeoPoint origin, GeoPoint dest, ArrayList<GeoPoint> markerPoints) {

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


    @Override
    public void onLongPress() {





        markerPoints.add(p);






        final ArrayList<OverlayItem> items2 = new ArrayList<OverlayItem>();

        OverlayItem marker2 = new OverlayItem("markerTitle", "markerDescription", p);
        marker2.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
        items2.add(marker2);


        Bitmap source2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.marker_node);


        Drawable marker_ = new BitmapDrawable(getResources(), source2);
        ItemizedIconOverlay markersOverlay2 = new ItemizedIconOverlay<>(items2, marker_, null, this);
        map.getOverlays().add(markersOverlay2);






/*
        map = (MapView) findViewById(R.id.map);
        markerPoints.add(p);
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
        map.invalidate();*/

       /* nodeMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                infoWindow = marker.getInfoWindow();
                if (infoWindow.isOpen()) {
                    infoWindow.close();
                    marker.remove(map);
                    markerPoints.remove(p);

                    if (polyline != null)
                        polyline.setPoints(markerPoints);
                    map.invalidate();
                } else {
                    marker.showInfoWindow();
                    marker.getPosition();
                }
                return false;

            }
        });*/


    }
}


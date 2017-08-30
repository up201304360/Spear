package com.example.nachito.spear;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.util.constants.MapViewConstants;
import org.slf4j.helpers.MarkerIgnoringBase;

import java.util.ArrayList;
import pt.lsts.imc.FollowPath;
import pt.lsts.imc.Goto;

/**
 * Created by ines on 8/14/17.
 */
public class Line extends MainActivity implements  PressListener, MapViewConstants {

    float mGroundOverlayBearing = 0.0f;
    ArrayList<ArrayList<GeoPoint>> points;
    InfoWindow infoWindow;
    IMCGlobal imc;
    GeoPoint p;

    public void setImc(IMCGlobal imc) {
        this.imc = imc;
        imc.register(this);
    }


    @Override
    public void onLongPress(double x, double y) {

            IGeoPoint p2 = map.getProjection().fromPixels((int) x, (int) y);

            p = new GeoPoint(p2.getLatitude(), p2.getLongitude());

            markerPoints.add(p);

            GroundOverlay myGroundOverlay = new GroundOverlay();
            myGroundOverlay.setPosition(p);
            myGroundOverlay.setDimensions(2000.0f);
            myGroundOverlay.setBearing(mGroundOverlayBearing);
            mGroundOverlayBearing += 20.0f;
            map.getOverlays().add(myGroundOverlay);
            map.getOverlayManager().add(myGroundOverlay);
            map.invalidate();


            nodeMarker = new Marker(map);
            nodeMarker.setPosition(p);
            nodeMarker.setIcon(nodeIcon);
            nodeMarker.isDraggable();
            nodeMarker.setDraggable(true);
            nodeMarker.setTitle("lat/lon:" + p);
            map.getOverlays().add(nodeMarker);


            nodeMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
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
            });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (markerPoints.size() <= 2) {
                    if (imc.selectedvehicle == null) {

                        System.out.println("No vehicles");
                    }else{
                        Go(p);
                    }


                } else if (markerPoints.size() > 2) {

                    GeoPoint origin = markerPoints.get(markerPoints.size() - 2);
                    GeoPoint dest=markerPoints.get(markerPoints.size()-1);
                    drawLine(p, origin, dest, markerPoints);
                    trans.setVisibility(View.INVISIBLE);

                }
            }
        });
    }


    public void drawLine(GeoPoint p, GeoPoint origin, GeoPoint dest, ArrayList<GeoPoint> markerPoints) {
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
        if (imc.selectedvehicle == null) {
            System.out.println("No vehicle selected");


        } else {
            follow(markerPoints, p);
        }

    }

    public void follow(ArrayList markerPoints, GeoPoint p) {

        FollowPath m = new FollowPath();
        double lat = Math.toRadians(p.getLatitude());
        double lon = Math.toRadians(p.getLongitude());
        m.setLat(lat);
        m.setLon(lon);
        m.setZ(depth);
        m.setZUnits(FollowPath.Z_UNITS.DEPTH);
        m.setSpeed(speed);
        m.setSpeedUnits(FollowPath.SPEED_UNITS.RPM);
        m.setPoints(markerPoints);
        String planid = "FollowLine";

        startManeuver(planid, m);

            finish();
    }

    public void Go(GeoPoint p) {


        Goto go = new Goto();
        double lat = Math.toRadians(p.getLatitude());
        double lon = Math.toRadians(p.getLongitude());
        go.setLat(lat);
        go.setLon(lon);
        go.setZ(depth);
        go.setZUnits(Goto.Z_UNITS.DEPTH);
        go.setSpeed(speed);
        go.setSpeedUnits(Goto.SPEED_UNITS.RPM);
        String planid = "Goto";
        startManeuver(planid, go);
        finish();

    }


    public void finish() {
        map.getOverlayManager().clear();
        map.invalidate();
        done.setVisibility(View.INVISIBLE);
        imc.unregister(this);

    }



}



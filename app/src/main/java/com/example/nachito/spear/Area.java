package com.example.nachito.spear;

import android.view.View;
import android.widget.Toast;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.util.constants.MapViewConstants;

import java.util.ArrayList;

import pt.lsts.imc.Goto;

/**
 * Created by ines on 8/23/17.
 */

public class Area extends MainActivity implements  PressListener, MapViewConstants {
    ArrayList<GeoPoint> markerPoints = new ArrayList<>();
    float mGroundOverlayBearing = 0.0f;
    Polygon circle;
    InfoWindow infoWindow;


    @Override
    public void onLongPress(double x, double y) {
        Projection proj = map.getProjection();
        IGeoPoint p2 = proj.fromPixels((int) x, (int) y);

        final GeoPoint p = new GeoPoint(p2.getLatitude(), p2.getLongitude());

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

                    if (circle != null)
                        circle.setPoints(markerPoints);
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

//TODO !
                if (markerPoints.size() < 1) {
                    if (imc.selectedvehicle == null) {
                        Toast.makeText(Area.this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
                        return;}
                        Go(p);


                } else if (markerPoints.size() > 1) {


                    GeoPoint origin = (GeoPoint) markerPoints.get(markerPoints.size() - 2);
                    GeoPoint dest = (GeoPoint) markerPoints.get(markerPoints.size() - 1);
                    drawArea(origin, markerPoints);
                    trans.setVisibility(View.INVISIBLE);

                }
            }
        });


    }



    public void drawArea(GeoPoint origin, ArrayList<GeoPoint> markerPoints) {
        BoundingBox bb = BoundingBox.fromGeoPoints(markerPoints);
        map.zoomToBoundingBox(bb, true);
        circle = new Polygon();
        circle.getOutlinePaint();
        circle.isVisible();
        circle.setStrokeWidth(7);
        circle.setPoints(markerPoints);
        circle.setInfoWindow(new BasicInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, map));
        circle.setTitle("Centered on " + origin.getLatitude() + "," + origin.getLongitude());
        map.getOverlays().add(circle);
        map.invalidate();


    }


    public  void Go(GeoPoint p){
        Goto go = new Goto();
        double lat = Math.toRadians(p.getLatitude());
        double lon = Math.toRadians(p.getLongitude());
        go.setLat(lat);
        go.setLon(lon);
        go.setZ(depth);
        go.setZUnits(Goto.Z_UNITS.DEPTH);
        go.setSpeed(speed);
        go.setSpeedUnits(Goto.SPEED_UNITS.RPM);
        String planid = "GoToPoint";
        startManeuver(planid, go);
    }
}



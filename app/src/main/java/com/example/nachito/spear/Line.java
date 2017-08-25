package com.example.nachito.spear;


import android.view.View;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.util.constants.MapViewConstants;
import java.util.ArrayList;
import pt.lsts.imc.FollowPath;

/**
 * Created by ines on 8/14/17.
 */
public class Line extends MainActivity implements  PressListener, MapViewConstants{
    ArrayList<GeoPoint> markerPoints = new ArrayList<>();
    float mGroundOverlayBearing = 0.0f;
    ArrayList<ArrayList<GeoPoint>> points;
    Polyline polyline;
    InfoWindow infoWindow;
IMCGlobal imc;

    public void setImc(IMCGlobal imc) {
        this.imc = imc;
        imc.register(this);
    }



    @Override
    public void onLongPress(double x, double y) {
        Projection proj = map.getProjection();
         IGeoPoint p2 =  proj.fromPixels((int)x,(int)y);

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

                        if(polyline!=null)
                            polyline.setPoints(markerPoints);
                        map.invalidate();
                    } else {
                        marker.showInfoWindow();
                        marker.getPosition();
                    }
                    return false;

                }
            });





        MainActivity.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if (markerPoints.size() <= 2) {

                   if (imc.selectedvehicle == null) {
                       System.out.println("No vehicles");
//                        Toast.makeText(line.getApplicationContext(), "No vehicles available", Toast.LENGTH_SHORT).show();

                    } else {
                        Go(p);
                    }
                }
                else if (markerPoints.size() > 2) {


                    GeoPoint origin =  markerPoints.get(markerPoints.size() - 2);
                    GeoPoint dest =  markerPoints.get(markerPoints.size() - 1);
                    drawLine(origin, dest,  markerPoints);
                    trans.setVisibility(View.INVISIBLE);

                }
            }
        });





    }

    //TODO Se carrego em Area apagar os marcadores fazer o mesmo na area




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


    public  void Go(GeoPoint p){

        FollowPath go = new FollowPath();
        double lat = Math.toRadians(p.getLatitude());
        double lon = Math.toRadians(p.getLongitude());
        go.setLat(lat);
        go.setLon(lon);
        go.setZ(depth);
        go.setZUnits(FollowPath.Z_UNITS.DEPTH);
        go.setSpeed(speed);
        go.setSpeedUnits(FollowPath.SPEED_UNITS.RPM);
        String planid = "FollowPath";
        startManeuver(planid, go);
    }


    public void finish() {

        map.getOverlays().remove(nodeMarker);
        nodeMarker.remove(map);
        map.invalidate();
        imc.unregister(this);

    }



}



package com.example.nachito.spear;


import android.content.Context;
import android.content.Intent;
import android.view.View;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.util.constants.MapViewConstants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import pt.lsts.imc.Goto;
import pt.lsts.imc.Maneuver;
import pt.lsts.neptus.messages.listener.Periodic;
import pt.lsts.util.PlanUtilities;


/**
 * Created by ines on 8/14/17.
 */
public class Line extends MainActivity implements  PressListener, MapViewConstants {

    float mGroundOverlayBearing = 0.0f;
    ArrayList<ArrayList<GeoPoint>> points;
    InfoWindow infoWindow;
    IMCGlobal imc;
    GeoPoint p;
    Boolean doneClicked=false;
    Goto follow;
    public void setImc(IMCGlobal imc) {
        this.imc = imc;
        imc.register(this);
    }


    @Override
    public void onLongPress(double x, double y) {
        if (!doneClicked) {
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

            lineMarker = new Marker(map);
            lineMarker.setPosition(p);
            lineMarker.setIcon(lineIcon);
            lineMarker.isDraggable();
            lineMarker.setDraggable(true);
            lineMarker.setTitle("lat/lon:" + p);
            map.getOverlays().add(lineMarker);

            lineMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
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
                done.setVisibility(View.VISIBLE);
                erase.setVisibility(View.VISIBLE);
                done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doneClicked = true;

                        if (markerPoints.size() <= 2) {
                            if (imc.selectedvehicle == null) {

                                System.out.println("No vehicles");
                            } else {
                                Go(p);
                            }


                        } else if (markerPoints.size() > 2) {

                            GeoPoint origin = markerPoints.get(markerPoints.size() - 2);
                            GeoPoint dest = markerPoints.get(markerPoints.size() - 1);
                            drawLine(origin, dest);
                            trans.setVisibility(View.INVISIBLE);

                        }
                    }
                });

        }
            erase.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View v) {
                    for (int i = 0; i < markerPoints.size(); i++) {
                        lineMarker.remove(map);


                        map.invalidate();
                    }
                    markerPoints.clear();

                    if (polyline != null)
                        polyline.setPoints(markerPoints);
                    map.getOverlays().clear();
                    trans.setVisibility(View.VISIBLE);

                }

            });
        }



    public void drawLine( GeoPoint origin, GeoPoint dest) {
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
            followPoints();
        }

    }

    public void followPoints() {
        LinkedHashSet<String> lhs = new LinkedHashSet<String>();
        Iterator<GeoPoint> it = markerPoints.iterator();

        while(it.hasNext()) {
            String val = it.next().toString();
            if (lhs.contains(val)) {
                it.remove();
            }
            else
                lhs.add(val);
        }

        ArrayList<Maneuver> maneuvers = new ArrayList<>();

        for (GeoPoint p : markerPoints) {

            follow = new Goto();
            double lat = Math.toRadians((p.getLatitude()));
            double lon = Math.toRadians((p.getLongitude()));
             follow.setLat(lat);
                follow.setLon(lon);
                follow.setZ(depth);
                follow.setZUnits(Goto.Z_UNITS.DEPTH);
                follow.setSpeed(speed);
            if(!showrpm) {
                follow.setSpeedUnits(Goto.SPEED_UNITS.METERS_PS);
            } else{
                follow.setSpeedUnits(Goto.SPEED_UNITS.RPM);}
            maneuvers.add(follow);

        }

    startBehaviour("SpearFollowPoints" , PlanUtilities.createPlan("followPoints"+imc.selectedvehicle, maneuvers.toArray(new Maneuver[0])));
        wayPoints(follow);

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
        if(!showrpm) {
            go.setSpeedUnits(Goto.SPEED_UNITS.METERS_PS);
        } else{
            go.setSpeedUnits(Goto.SPEED_UNITS.RPM);}
        String planid = "SpearGoto-"+imc.selectedvehicle;
        startBehaviour(planid, go);
        wayPoints(go);


    }

    public void finish() {
        map.getOverlayManager().clear();
        map.invalidate();
        done.setVisibility(View.INVISIBLE);
        erase.setVisibility(View.INVISIBLE);
        vel2.setVisibility(View.INVISIBLE);
        imc.unregister(this);
    }


}


//depois do done passar para o main
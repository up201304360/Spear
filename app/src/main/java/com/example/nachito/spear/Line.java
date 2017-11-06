package com.example.nachito.spear;


import android.view.View;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import pt.lsts.imc.Goto;
import pt.lsts.imc.Maneuver;
import pt.lsts.imc.def.SpeedUnits;
import pt.lsts.imc.def.ZUnits;
import pt.lsts.util.PlanUtilities;


/**
 *
 * Created by ines on 8/14/17.
 */
public class Line extends MainActivity implements  PressListener {

    IMCGlobal imc;
    Boolean doneClicked = false;
    Goto follow;
    float mGroundOverlayBearing = 0.0f;
    ArrayList<ArrayList<GeoPoint>> points;

    public void setImc(IMCGlobal imc) {
        this.imc = imc;
        imc.register(this);
    }

    @Override
    public void onBackPressed() {
        doneClicked=false;
        Line.super.onBackPressed();
        imc.unregister(this);

    }

    @Override
    public void onLongPress(double x, double y) {

        if (!doneClicked) {

            IGeoPoint p2 = map.getProjection().fromPixels((int) x, (int) y);
            final GeoPoint clickedLocation = new GeoPoint(p2.getLatitude(), p2.getLongitude());

            markerPoints.add(clickedLocation);
            GroundOverlay myGroundOverlay = new GroundOverlay();
            myGroundOverlay.setPosition(clickedLocation);
            myGroundOverlay.setDimensions(2000.0f);
            myGroundOverlay.setBearing(mGroundOverlayBearing);
            mGroundOverlayBearing += 20.0f;
            map.getOverlays().add(myGroundOverlay);
            map.getOverlayManager().add(myGroundOverlay);
            map.invalidate();
            lineMarker = new Marker(map);
            lineMarker.setPosition(clickedLocation);
            lineMarker.setIcon(lineIcon);
            lineMarker.isDraggable();
            lineMarker.setDraggable(true);
            lineMarker.setTitle("lat/lon:" + clickedLocation);
            map.getOverlays().add(lineMarker);


            done.setVisibility(View.VISIBLE);
            erase.setVisibility(View.VISIBLE);

            done.setOnClickListener(v -> {
                doneClicked = true;
                if (markerPoints.size() <= 2) {
                    if (imc.selectedvehicle == null) {

                        System.out.println("No vehicles");
                    } else {
                        Go(clickedLocation);

                    }


                } else if (markerPoints.size() > 2) {

                    GeoPoint origin = markerPoints.get(markerPoints.size() - 2);
                    GeoPoint dest = markerPoints.get(markerPoints.size() - 1);
                    drawLine(origin, dest);
                    trans.setVisibility(View.INVISIBLE);

                }
            });

        }

        erase.setOnClickListener(v -> {

            for (int i = 0; i < markerPoints.size(); i++) {
                lineMarker.remove(map);


                map.invalidate();
            }
            markerPoints.clear();


            if (polyline != null)
                polyline.setPoints(markerPoints);
            if(circle!=null)
                circle.setPoints(markerPoints);
            map.getOverlays().clear();

            if(doneClicked) {
                trans.setVisibility(View.INVISIBLE);
                doneClicked=false;

            }
            trans.setVisibility(View.VISIBLE);

        });

    }


    public void drawLine(GeoPoint origin, GeoPoint dest) {


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
            doneClicked=false;


        } else {
            followPoints();
        }

    }

    public void followPoints() {

        LinkedHashSet<String> noRepetitions = new LinkedHashSet<>();
        Iterator<GeoPoint> iterator = markerPoints.iterator();

        while (iterator.hasNext()) {
            String val = iterator.next().toString();
            if (noRepetitions.contains(val)) {
                iterator.remove();
            } else
                noRepetitions.add(val);
        }

        ArrayList<Maneuver> maneuvers = new ArrayList<>();

        for (GeoPoint point : markerPoints) {

            follow = new Goto();
            double lat = Math.toRadians((point.getLatitude()));
            double lon = Math.toRadians((point.getLongitude()));
            follow.setLat(lat);
            follow.setLon(lon);
            follow.setZ(depth);
            follow.setZUnits(ZUnits.DEPTH);
            follow.setSpeed(speed);
            if (!showrpm) {
                follow.setSpeedUnits(SpeedUnits.METERS_PS);
            } else {
                follow.setSpeedUnits(SpeedUnits.RPM);
            }
            maneuvers.add(follow);


        }

        startBehaviour("followPoints" + imc.selectedvehicle, PlanUtilities.createPlan("followPoints" + imc.selectedvehicle, maneuvers.toArray(new Maneuver[0])));
        previous = "M";
        setEstadoVeiculo(" ");

    }


    public void Go(GeoPoint p) {
        Goto go = new Goto();
        double lat = Math.toRadians(p.getLatitude());
        double lon = Math.toRadians(p.getLongitude());
        go.setLat(lat);
        go.setLon(lon);
        go.setZ(depth);
        go.setZUnits(ZUnits.DEPTH);
        go.setSpeed(speed);
        if (!showrpm) {
            go.setSpeedUnits(SpeedUnits.METERS_PS);
        } else {
            go.setSpeedUnits(SpeedUnits.RPM);
        }
        String planid = "SpearGoto";
        startBehaviour(planid, go);
        previous = "M";
        setEstadoVeiculo(" ");
        trans.setVisibility(View.INVISIBLE);


    }


    //TODO
    public void finish() {

        done.setVisibility(View.INVISIBLE);
        erase.setVisibility(View.INVISIBLE);
        imc.unregister(this);
    }




}
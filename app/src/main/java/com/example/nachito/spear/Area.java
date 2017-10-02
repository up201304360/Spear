package com.example.nachito.spear;


import android.view.View;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.util.constants.MapViewConstants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import pt.lsts.coverage.GeoCoord;
import pt.lsts.imc.Goto;
import pt.lsts.imc.Maneuver;
import pt.lsts.util.PlanUtilities;
import static pt.lsts.coverage.AreaCoverage.computeCoveragePath;

/**
 * Created by ines on 8/23/17.
 *
 */

public class Area extends MainActivity implements  PressListener, MapViewConstants {
    IMCGlobal imc;
    Goto area2;
    Boolean doneClicked=false;
    float mGroundOverlayBearing = 0.0f;


    public void setImc(IMCGlobal imc) {
        this.imc = imc;
        imc.register(this);
    }
    public void finish() {
        map.getOverlayManager().clear();
        map.invalidate();
        done.setVisibility(View.INVISIBLE);
        erase.setVisibility(View.INVISIBLE);
        velocityTextView.setVisibility(View.INVISIBLE);
        imc.unregister(this);


    }

    @Override
    public void onLongPress(double x, double y) {
        if (!doneClicked) {
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


            lineMarker = new Marker(map);
            lineMarker.setPosition(p);
            lineMarker.setIcon(lineIcon);
            lineMarker.isDraggable();
            lineMarker.setDraggable(true);
            lineMarker.setTitle("lat/lon:" + p);
            map.getOverlays().add(lineMarker);




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

                        drawArea();

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


                if (circle != null)
                    circle.setPoints(markerPoints);
                map.getOverlays().clear();
                trans.setVisibility(View.VISIBLE);
            }
        });



    }

    public void drawArea() {

        circle = new Polygon();
        circle.getOutlinePaint();
        circle.isVisible();
        circle.setStrokeWidth(7);
        circle.setPoints(markerPoints);
        circle.setInfoWindow(new BasicInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, map));
        map.getOverlays().add(circle);
        map.invalidate();
        if (imc.selectedvehicle == null) {
            System.out.println("No vehicle selected");


        } else {

            followArea();


        }
    }

    public void followArea(){


        LinkedHashSet<String> noRepetitions = new LinkedHashSet<>();
        Iterator<GeoPoint> it = markerPoints.iterator();
        while(it.hasNext()) {
            String val = it.next().toString();
            if (noRepetitions.contains(val)) {
                it.remove();
            }
            else
                noRepetitions.add(val);
        }

        ArrayList<GeoCoord> coords = new ArrayList<>();
        ArrayList<Maneuver> maneuvers = new ArrayList<>();

        for(int i=0; i<markerPoints.size(); i++ ){

            coords.add(new GeoCoord(markerPoints.get(i).getLatitude(), markerPoints.get(i).getLongitude()));

        }


        for (GeoCoord coord : computeCoveragePath(coords, swath_width)) {

//FollowPath
            area2 = new Goto();
            double lat = Math.toRadians(coord.latitudeDegs);
            double lon = Math.toRadians(coord.longitudeDegs);
            area2.setLat(lat);
            area2.setLon(lon);
            area2.setZ(depth);
            area2.setZUnits(Goto.Z_UNITS.DEPTH);
            area2.setSpeed(speed);

            if(!showrpm) {
                area2.setSpeedUnits(Goto.SPEED_UNITS.METERS_PS);
            } else{
                area2.setSpeedUnits(Goto.SPEED_UNITS.RPM);}

            maneuvers.add(area2);

        }
        startBehaviour("SpearArea" , PlanUtilities.createPlan("SpearArea-"+imc.selectedvehicle, maneuvers.toArray(new Maneuver[0])));
        wayPoints(area2);
        setEstadoVeiculo(" ");
        previous="M";
        trans.setVisibility(View.INVISIBLE);


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
        if(!showrpm) {
            go.setSpeedUnits(Goto.SPEED_UNITS.METERS_PS);
        } else{
            go.setSpeedUnits(Goto.SPEED_UNITS.RPM);}
        String planid = "SpearGoto-"+imc.selectedvehicle;
        startBehaviour(planid, go);
        wayPoints(go);
        setEstadoVeiculo(" ");
        previous="M";
        trans.setVisibility(View.INVISIBLE);


    }


}



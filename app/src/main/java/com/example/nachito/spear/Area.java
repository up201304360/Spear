package com.example.nachito.spear;

import android.view.View;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.util.constants.MapViewConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import pt.lsts.coverage.GeoCoord;
import pt.lsts.imc.CoverArea;
import pt.lsts.imc.Goto;
import pt.lsts.imc.Loiter;
import pt.lsts.imc.Maneuver;
import pt.lsts.imc.PolygonVertex;
import pt.lsts.util.PlanUtilities;

import static pt.lsts.coverage.AreaCoverage.computeCoveragePath;


/**
 * Created by ines on 8/23/17.
 *
 */

public class Area extends MainActivity implements  PressListener, MapViewConstants {
    float mGroundOverlayBearing = 0.0f;
    InfoWindow infoWindow;
    IMCGlobal imc;

    public void setImc(IMCGlobal imc) {
        this.imc = imc;
        imc.register(this);
    }
    public void finish() {
        imc.unregister(this);
        map.getOverlayManager().clear();
        map.invalidate();
        done.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onLongPress(double x, double y) {

        Projection proj = map.getProjection();
        IGeoPoint p2 = proj.fromPixels((int) x, (int) y);

        final GeoPoint p = new GeoPoint(p2.getLatitude(), p2.getLongitude());

        markerPoints.add(p);
        System.out.println(p);

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
            done.setVisibility(View.VISIBLE);
            erase.setVisibility(View.VISIBLE);
            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (markerPoints.size() <= 2) {
                        if (imc.selectedvehicle == null) {

                            System.out.println("No vehicles");
                        } else {
                            Go(p);
                        }


                    } else if (markerPoints.size() > 2) {

                        GeoPoint origin = markerPoints.get(markerPoints.size() - 2);
                        drawArea(p, origin);
                        trans.setVisibility(View.INVISIBLE);

                    }
                }
            });



    }

    public void drawArea(GeoPoint p,GeoPoint origin) {
        circle = new Polygon();
        circle.getOutlinePaint();
        circle.isVisible();
        circle.setStrokeWidth(7);
        circle.setPoints(markerPoints);
        circle.setInfoWindow(new BasicInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, map));
        circle.setTitle("Centered on " + origin.getLatitude() + "," + origin.getLongitude());
        map.getOverlays().add(circle);
        map.invalidate();
        if (imc.selectedvehicle == null) {

            System.out.println("No vehicle selected");


        } else {


            LinkedHashSet<String> lhs = new LinkedHashSet<>();
            Iterator<GeoPoint> it = markerPoints.iterator();

            while(it.hasNext()) {
                String val = it.next().toString();
                if (lhs.contains(val)) {
                    it.remove();
                }
                else
                    lhs.add(val);
            }


            ArrayList<GeoCoord> coords = new ArrayList<>();
            ArrayList<Maneuver> maneuvers = new ArrayList<>();

     for(int i=0; i<markerPoints.size()-1; i++ ){

         coords.add(new GeoCoord(markerPoints.get(i).getLatitude(), markerPoints.get(i).getLongitude()));
         System.out.println(": "+coords);

     }

            for (GeoCoord coord : computeCoveragePath(coords, swath_width)){
                System.out.println(coord.latitudeDegs+", "+coord.longitudeDegs);


           Collection< PolygonVertex> poly =null;
    



                CoverArea area;
                area = new CoverArea();
                double lat = Math.toRadians((coord.latitudeDegs));
                double lon = Math.toRadians((coord.longitudeDegs));

                area.setLat(lat);
                area.setLon(lon);
                area.setZ(depth);
                area.setZUnits(CoverArea.Z_UNITS.DEPTH);
                area.setSpeed(speed);
                area.setPolygon( poly);
                if(!showrpm) {
                    area.setSpeedUnits(CoverArea.SPEED_UNITS.METERS_PS);
                } else{
                    area.setSpeedUnits(CoverArea.SPEED_UNITS.RPM);}
                maneuvers.add(area);
            }

            startBehaviour("SpearCoverArea" , PlanUtilities.createPlan("CoverArea", maneuvers.toArray(new Maneuver[0])));


        }


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
        String planid = "SpearGoto";
        startBehaviour(planid, go);
    }


}



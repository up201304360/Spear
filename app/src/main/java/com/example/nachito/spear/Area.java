package com.example.nachito.spear;

import android.support.annotation.NonNull;
import android.util.Log;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import pt.lsts.coverage.GeoCoord;
import pt.lsts.imc.CoverArea;
import pt.lsts.imc.Goto;
import pt.lsts.imc.Maneuver;
import pt.lsts.imc.PolygonVertex;
import pt.lsts.neptus.messages.listener.Periodic;
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
    CoverArea area2;

    Collection<PolygonVertex> e= new Collection<PolygonVertex>() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @NonNull
        @Override
        public Iterator<PolygonVertex> iterator() {
            return null;
        }

        @NonNull
        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @NonNull
        @Override
        public <T> T[] toArray(@NonNull T[] ts) {
            return null;
        }

        @Override
        public boolean add(PolygonVertex polygonVertex) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(@NonNull Collection<?> collection) {
            return false;
        }

        @Override
        public boolean addAll(@NonNull Collection<? extends PolygonVertex> collection) {
            return false;
        }

        @Override
        public boolean removeAll(@NonNull Collection<?> collection) {
            return false;
        }

        @Override
        public boolean retainAll(@NonNull Collection<?> collection) {
            return false;
        }

        @Override
        public void clear() {

        }
    };
    PolygonVertex poly = new PolygonVertex();

    public void setImc(IMCGlobal imc) {
        this.imc = imc;
        imc.register(this);
    }
    public void finish() {
        imc.unregister(this);
        map.getOverlayManager().clear();
        map.invalidate();
        done.setVisibility(View.INVISIBLE);
        vel2.setVisibility(View.INVISIBLE);


    }

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
           velc();
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

     }

            for (GeoCoord coord : computeCoveragePath(coords, swath_width)){

                poly.setLon(Math.toRadians(coord.longitudeDegs));
                poly.setLat(Math.toRadians(coord.latitudeDegs));
                e.add(poly);
                area2 = new CoverArea();
                double lat = Math.toRadians((coord.latitudeDegs));
                double lon = Math.toRadians((coord.longitudeDegs));
                area2.setLat(lat);
                area2.setLon(lon);
                area2.setZ(depth);
                area2.setZUnits(CoverArea.Z_UNITS.DEPTH);
                area2.setSpeed(speed);
                area2.setPolygon(e);
                if(!showrpm) {
                    area2.setSpeedUnits(CoverArea.SPEED_UNITS.METERS_PS);
                } else{
                    area2.setSpeedUnits(CoverArea.SPEED_UNITS.RPM);}
                maneuvers.add(area2);
            }
            startBehaviour("SpearCoverArea" , PlanUtilities.createPlan("CoverArea", maneuvers.toArray(new Maneuver[0])));
            wayPoints(area2);
        }


    }
    @Periodic
    public void velc() {


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                vel2.setText("Speed:" + " " + vel + " " + "m/s" + "\n" + "Depth:" + " " + dept + "\n" + imc.getSelectedvehicle());

            }
        });
    }
    public  void Go(GeoPoint p){
        velc();

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
        wayPoints(go);
    }


}



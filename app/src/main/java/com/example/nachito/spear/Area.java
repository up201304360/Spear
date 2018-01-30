package com.example.nachito.spear;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import org.androidannotations.annotations.EActivity;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import pt.lsts.coverage.GeoCoord;
import pt.lsts.imc.Goto;
import pt.lsts.imc.Maneuver;
import pt.lsts.imc.def.SpeedUnits;
import pt.lsts.imc.def.ZUnits;
import pt.lsts.neptus.messages.listener.Periodic;
import pt.lsts.util.PlanUtilities;

import static com.example.nachito.spear.MainActivity.depth;
import static com.example.nachito.spear.MainActivity.returnAreaPoints;
import static com.example.nachito.spear.MainActivity.setVehicleStateString;
import static com.example.nachito.spear.MainActivity.showrpm;
import static com.example.nachito.spear.MainActivity.speed;
import static com.example.nachito.spear.MainActivity.startBehaviour;
import static com.example.nachito.spear.MainActivity.swath_width;
import static pt.lsts.coverage.AreaCoverage.computeCoveragePath;

/**
 *
 * Created by ines on 11/13/17.
 */
@EActivity
public class Area  extends AppCompatActivity {

    IMapController mapController;
    static double lat;
    static double lon;
    Button done;
    MapView map;
    Button erase;
    int numPontos;
    Drawable nodeIcon;
    static boolean iscircleDrawn;
    Marker startMarker;
    ArrayList<GeoPoint> otherVehiclesPosition;
    ArrayList<GeoPoint> areaPoints = returnAreaPoints();
    ArrayList<GeoPoint> linePoints = MainActivity.returnLinePoints();
    Marker nodeMarkers = MainActivity.getPointsMain();
    GeoPoint centerInSelectedVehicle;
    Goto area2;
    static Polyline polyline;
    static Polygon circle;
    static ArrayList<GeoPoint> markers = new ArrayList<>();
    boolean doneClicked=false;
    private Handler mHandler;
    Button eraseAll;
    String selected;
    String previous= MainActivity.getPrevious();
    static ArrayList<Maneuver> maneuverArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.area);

        map =  findViewById(R.id.mapArea);
        done = findViewById(R.id.doneArea);
        nodeIcon = getResources().getDrawable(R.drawable.orangeled);
        erase =   findViewById(R.id.eraseArea);
        eraseAll =  findViewById(R.id.eraseAllArea);
        map.setMultiTouchControls(true);
        Toast.makeText(this, " Long click on the map to choose an area", Toast.LENGTH_SHORT).show();
        getIntentSelected();
        mapController = map.getController();
        mapController.setZoom(16);
        centerInSelectedVehicle = MainActivity.getVariables();
        mapController.setCenter(centerInSelectedVehicle);
        drawRed();
        drawBlue();
        drawGreen();
        if (areaPoints != null) {
            Set<GeoPoint> hs = new HashSet<>();
            hs.addAll(areaPoints);
            areaPoints.clear();
            areaPoints.addAll(hs);
            for (int i = 0; i < areaPoints.size(); i++) {
                Marker markerArea = new Marker(map);
                markerArea.setPosition(areaPoints.get(i));
                markerArea.setIcon(nodeIcon);
                map.getOverlays().add(markerArea);
            }
        }

        if (linePoints != null) {
            Set<GeoPoint> hs = new HashSet<>();
            hs.addAll(linePoints);
            linePoints.clear();
            linePoints.addAll(hs);

            for (int i = 0; i < linePoints.size(); i++) {
                Marker markerLine = new Marker(map);
                markerLine.setPosition(linePoints.get(i));
                markerLine.setIcon(nodeIcon);
                map.getOverlays().add(markerLine);
            }

        }
        if (nodeMarkers != null) {
            map.getOverlays().add(nodeMarkers);
        }

        if (polyline != null) {
            map.getOverlays().add(polyline);
        }

        if (MainActivity.returnCircle()) {
            circle = new Polygon();
            circle.setPoints(markers);
            map.getOverlays().add(circle);

        }
        if(MainActivity.returnPoly()) {
            polyline = new Polyline();
            polyline.setPoints(markers);
            map.getOverlays().add(polyline);

        }
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                lat = p.getLatitude();
                lon = p.getLongitude();
                markers.add(p);
                startMarker = new Marker(map);
                startMarker.setPosition(p);
                map.getOverlays().add(startMarker);
                startMarker.setIcon(getResources().getDrawable(R.drawable.orangeled));
                startMarker.setTitle(p.toString());
                map.invalidate();
                numPontos++;
                erase.setOnClickListener(v -> {
                    if(!doneClicked) {
                        for (int i = 0; i < numPontos; i++) {
                            map.getOverlays().remove(startMarker);
                            startMarker.remove(map);
                            markers.remove(startMarker.getPosition());
                            numPontos--;
                        }

                        if (polyline != null)
                            polyline.setPoints(markers);
                        if (circle != null)
                            circle.setPoints(markers);
                        map.invalidate();
                        erase.setClickable(false);
                    }
                });



                eraseAll.setOnClickListener(v-> erasePointsArea());

                done.setOnClickListener(v -> {
                    if (markers.size() <= 1) {

                        if (selected == null) {
                            Toast.makeText(Area.this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
                        } else {
                            Go(p);
                            doneClicked=true;
                            startRepeatingTask();
                        }
                    } else if (markers.size() > 1) {
                        if (selected == null) {
                            Toast.makeText(Area.this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
                        } else {
                            drawArea();
                            doneClicked = true;
                            iscircleDrawn = true;
                            startRepeatingTask();
                        }
                    }
                });
                return false;
            }
        };

        MapEventsOverlay OverlayEventos = new MapEventsOverlay(this.getBaseContext(), mReceive);
        map.getOverlays().add(OverlayEventos);
        //Refreshing the map to draw the new overlay
        map.invalidate();
        mHandler = new Handler();
    }

    public void erasePointsArea(){
        if(!doneClicked) {
            eraseAll.setClickable(false);
            map.getOverlayManager().clear();
            map.setMultiTouchControls(true);
            markers.clear();
            if (polyline != null)
                polyline.setPoints(markers);
            if (circle != null)
                circle.setPoints(markers);
            numPontos = 0;
            map.invalidate();
            drawGreen();
            drawBlue();
            drawRed();
            onBackPressed();
        }
    }
    @NonNull
    public void getIntentSelected(){
        Intent intent = getIntent();
        selected = intent.getExtras().getString("selected");
    }

    public static ArrayList<GeoPoint>  getPointsArea(){
        return markers;
    }
    public static  boolean getCircle(){
        return iscircleDrawn;
    }
        Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                map.getOverlayManager().clear();
                if(markers.size()!=0){
                    for (int i = 0; i < markers.size(); i++) {
                        startMarker = new Marker(map);
                        startMarker.setPosition(markers.get(i));
                        startMarker.setIcon(nodeIcon);
                        startMarker.isDraggable();
                        startMarker.setDraggable(true);
                        startMarker.setTitle("lat/lon:" + markers.get(i));
                        map.getOverlays().add(startMarker);
                    }
                    if (circle != null){

                        circle.setPoints(markers);
                        circle.setInfoWindow(new BasicInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, map));
                        map.getOverlays().add(circle);
                        map.invalidate();}

                }
                if(polyline!=null)
                    map.getOverlays().add(polyline);
                drawRed();
                drawGreen();
                drawBlue();
                //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                int mInterval = 5000;
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }

    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }
    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    public void drawArea() {
        circle = new Polygon();
        circle.isVisible();
        circle.setStrokeWidth(7);
        circle.setPoints(markers);
        circle.setInfoWindow(new BasicInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, map));
        map.getOverlays().add(circle);
        map.invalidate();
        iscircleDrawn =true;
        followArea();
    }

    public void followArea(){
        LinkedHashSet<String> noRepetitions = new LinkedHashSet<>();
        Iterator<GeoPoint> it = markers.iterator();
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
        for(int i=0; i<markers.size(); i++ ){
            coords.add(new GeoCoord(markers.get(i).getLatitude(), markers.get(i).getLongitude()));
        }
        for (GeoCoord coord : computeCoveragePath(coords, swath_width)) {
            area2 = new Goto();
            double lat = Math.toRadians(coord.latitudeDegs);
            double lon = Math.toRadians(coord.longitudeDegs);
            area2.setLat(lat);
            area2.setLon(lon);
            area2.setZ(depth);
            area2.setZUnits(ZUnits.DEPTH);
            area2.setSpeed(speed);
            if(!showrpm) {
                area2.setSpeedUnits(SpeedUnits.METERS_PS);
            } else{
                area2.setSpeedUnits(SpeedUnits.RPM);}
            maneuvers.add(area2);
            maneuverArrayList = new ArrayList<>();
            maneuverArrayList.addAll( maneuvers);

        }
        startBehaviour("SpearArea-"+selected , PlanUtilities.createPlan("SpearArea-"+selected, maneuvers.toArray(new Maneuver[0])));
        onBackPressed();
        setVehicleStateString(" ");
        previous="M";

    }

    public static List<Maneuver> sendmList(){

        return maneuverArrayList;
    }



    public  void Go(GeoPoint p){
        Goto go = new Goto();
        double lat = Math.toRadians(p.getLatitude());
        double lon = Math.toRadians(p.getLongitude());
        go.setLat(lat);
        go.setLon(lon);
        go.setZ(depth);
        go.setZUnits(ZUnits.DEPTH);
        go.setSpeed(speed);
        if(!showrpm) {
            go.setSpeedUnits(SpeedUnits.METERS_PS);
        } else{
            go.setSpeedUnits(SpeedUnits.RPM);}
        String planid = "SpearGoto-"+selected;
        startBehaviour(planid, go);
        setVehicleStateString(" ");
        previous="M";
        onBackPressed();
    }







    final OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", centerInSelectedVehicle);
    @Periodic
    public void drawRed() {
        final GeoPoint loc = MainActivity.localizacao();
        final ArrayList<OverlayItem> items2 = new ArrayList<>();
        final OverlayItem marker2 = new OverlayItem("markerTitle", "markerDescription", loc);
        marker.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
        items2.add(marker2);
        Bitmap newMarker2 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.arrowred), 70, 70, false);
        float orientation2 = MainActivity.orientation();
        int ori2 = (int) Math.round(Math.toDegrees(orientation2));
        ori2 = ori2 - 180;
        Bitmap target = MainActivity.RotateMyBitmap(newMarker2,ori2);
        Drawable markerLoc = new BitmapDrawable(getResources(), target);
        final ItemizedIconOverlay markersOverlay2 = new ItemizedIconOverlay<>(items2, markerLoc, null, this);
        map.getOverlays().add(markersOverlay2);
    }
    @Periodic
    public void drawBlue(){
        otherVehiclesPosition = MainActivity.drawPosicaoOutrosVeiculos();
        Set<GeoPoint> hs = new HashSet<>();
        hs.addAll(otherVehiclesPosition);
        otherVehiclesPosition.clear();
        otherVehiclesPosition.addAll(hs);
        for(int i = 0; i< otherVehiclesPosition.size(); i++) {
            if(otherVehiclesPosition.get(i)!= centerInSelectedVehicle) {
                final ArrayList<OverlayItem> itemsPoints = new ArrayList<>();
                OverlayItem markerPoints = new OverlayItem("markerTitle", "markerDescription", otherVehiclesPosition.get(i));
                markerPoints.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
                itemsPoints.add(markerPoints);
                Bitmap source2 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.downarrow), 70, 70, false);
                float orientation2 = MainActivity.orientation();
                int ori2 = (int) Math.round(Math.toDegrees(orientation2));
                ori2 = ori2 - 180;
                Bitmap target = MainActivity.RotateMyBitmap(source2,ori2);
                Drawable marker_ = new BitmapDrawable(getResources(), target);
                ItemizedIconOverlay markersOverlay_ = new ItemizedIconOverlay<>(itemsPoints, marker_, null, this);
                map.getOverlays().add(markersOverlay_);
            }
        }
    }
    @Periodic
    public void drawGreen() {
        System.out.println("green");
        if (centerInSelectedVehicle != null) {
            final ArrayList<OverlayItem> items = new ArrayList<>();
            final OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", centerInSelectedVehicle);
            marker.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
            items.add(marker);
            Bitmap newMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.arrowgreen), 70, 70, false);
            float orientation2 = MainActivity.orientation();
            int ori2 = (int) Math.round(Math.toDegrees(orientation2));
            ori2 = ori2 - 180;
            Bitmap target = MainActivity.RotateMyBitmap(newMarker,ori2);
            Drawable markerLoc = new BitmapDrawable(getResources(), target);
            final ItemizedIconOverlay markersOverlay2 = new ItemizedIconOverlay<>(items, markerLoc, null, this);
            map.getOverlays().add(markersOverlay2);

        }
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onStop() {
        stopRepeatingTask();
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        stopRepeatingTask();
        super.onDestroy();

    }

}
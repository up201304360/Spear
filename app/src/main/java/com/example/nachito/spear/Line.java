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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import pt.lsts.imc.Goto;
import pt.lsts.imc.Maneuver;
import pt.lsts.imc.def.SpeedUnits;
import pt.lsts.imc.def.ZUnits;
import pt.lsts.neptus.messages.listener.Periodic;
import pt.lsts.util.PlanUtilities;

import static com.example.nachito.spear.MainActivity.depth;
import static com.example.nachito.spear.MainActivity.setVehicleStateString;
import static com.example.nachito.spear.MainActivity.showrpm;
import static com.example.nachito.spear.MainActivity.speed;
import static com.example.nachito.spear.MainActivity.startBehaviour;

/**
 *
 * Created by ines on 11/14/17.
 */

public class Line extends AppCompatActivity{


    IMapController mapController;
    static double lat;
    static double lon;
    Button done;
    MapView map;
    Button erase;
    int numberOfPoints;
    static boolean isPolylineDrawn;
    Drawable nodeIcon;
    ArrayList<ArrayList<GeoPoint>> points;
    Goto follow;
    Marker startMarker;
    ArrayList<GeoPoint> posicaoOutrosVeiculos;
    ArrayList<GeoPoint> areaPoints = MainActivity.returnAreaPoints();
    ArrayList<GeoPoint> linePoints = MainActivity.returnLinePoints();
    Marker nodeMarkers = MainActivity.getPointsMain();

    GeoPoint selectedVehiclesPosition;
    static Polyline polyline;
    static ArrayList<GeoPoint> markers = new ArrayList<>();
    static Polygon circle;
    Button eraseAll;
    private Handler mHandler;
    boolean isDoneClicked =false;
    String selected;
    String previous= MainActivity.getPrevious();
    static ArrayList<Maneuver> lineListManeuvers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.line);

        map = (MapView) findViewById(R.id.mapLine);
        done = (Button) findViewById(R.id.doneLine);
        nodeIcon = getResources().getDrawable(R.drawable.orangeled);
        erase = (Button) findViewById(R.id.eraseLine);
        eraseAll=(Button) findViewById(R.id.eraseAllLine);
        map.setMultiTouchControls(true);
        Toast.makeText(this, " Long click on the map to choose a line", Toast.LENGTH_SHORT).show();
        getIntentSelected();
        mapController = map.getController();
        mapController.setZoom(16);
        selectedVehiclesPosition = MainActivity.getVariables();
        mapController.setCenter(selectedVehiclesPosition);
        drawRed();
        drawBlue();
        drawGreen();

        if(areaPoints!=null){
            Set<GeoPoint> hs = new HashSet<>();
            hs.addAll(areaPoints);
            areaPoints.clear();
            areaPoints.addAll(hs);
            for(int i=0; i<areaPoints.size(); i++) {
                Marker markerArea = new Marker(map);
                markerArea.setPosition(areaPoints.get(i));
                markerArea.setIcon(nodeIcon);
                map.getOverlays().add(markerArea);
            }

        }

        if(linePoints!=null){
            Set<GeoPoint> hs = new HashSet<>();
            hs.addAll(linePoints);
            linePoints.clear();
            linePoints.addAll(hs);
            for(int i=0; i<linePoints.size(); i++) {
                Marker markerLine = new Marker(map);
                markerLine.setPosition(linePoints.get(i));
                markerLine.setIcon(nodeIcon);
                map.getOverlays().add(markerLine);
            }
        }
        if(nodeMarkers!=null){
            map.getOverlays().add(nodeMarkers);
        }

        if(polyline!=null){
            map.getOverlays().add(polyline);
        }
        if (MainActivity.returnCircle()) {
            circle = new Polygon();
            circle.setPoints(markers);
            map.getOverlays().add(circle);

        }
        if(MainActivity.returnPoly()){
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
                startMarker.isDraggable();
                map.getOverlays().add(startMarker);
                startMarker.setIcon(getResources().getDrawable(R.drawable.orangeled));
                startMarker.setTitle(p.toString());
                map.invalidate();
                numberOfPoints++;
                erase.setOnClickListener(v -> {
                    if(!isDoneClicked) {
                        for (int i = 0; i < numberOfPoints; i++) {
                            map.getOverlays().remove(startMarker);
                            startMarker.remove(map);
                            markers.remove(startMarker.getPosition());
                            numberOfPoints--;
                        }

                        if (polyline != null)
                            polyline.setPoints(markers);
                        if (circle != null)
                            circle.setPoints(markers);
                        map.invalidate();
                        erase.setClickable(false);
                    }
                });

                eraseAll.setOnClickListener(v-> {
                    if (!isDoneClicked) {
                        eraseAll.setClickable(false);
                        map.getOverlayManager().clear();
                        map.setMultiTouchControls(true);
                        markers.clear();
                        if (polyline != null)
                            polyline.setPoints(markers);
                        if (circle != null)
                            circle.setPoints(markers);
                        numberOfPoints = 0;
                        map.invalidate();
                        drawGreen();
                        drawBlue();
                        drawRed();
                        onBackPressed();
                    }
                });
                done.setOnClickListener(v -> {
                    if (markers.size() <= 1) {
                        if (selected == null) {
                            Toast.makeText(Line.this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
                        } else {
                            Go(p);
                            isDoneClicked =true;
                            startRepeatingTask();
                        }
                    } else if (markers.size() > 1) {
                        if (selected == null) {
                            Toast.makeText(Line.this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
                        } else {
                            drawLine();
                            startRepeatingTask();
                            isDoneClicked =true;
                            isPolylineDrawn =true;
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

    public static ArrayList<GeoPoint>  getPointsLine(){
        return markers;
    }

    public static  Polyline getPolyline(){
        return polyline;
    }


    public static  boolean getPoly(){
        return isPolylineDrawn;
    }

    public void getIntentSelected(){
        Intent intent = getIntent();
        selected = intent.getExtras().getString("selected");
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                map.getOverlayManager().clear();
                if(markers.size()!=0) {
                    for (int i = 0; i < markers.size(); i++) {
                        startMarker = new Marker(map);
                        startMarker.setPosition(markers.get(i));
                        startMarker.setIcon(nodeIcon);
                        startMarker.isDraggable();
                        startMarker.setDraggable(true);
                        startMarker.setTitle("lat/lon:" + markers.get(i));
                        map.getOverlays().add(startMarker);
                    }
                    if (polyline != null) {
                        map.getOverlays().add(polyline);
                    }

                    if (circle != null) {

                        circle.setPoints(markers);
                        map.getOverlays().add(circle);
                        map.invalidate();
                    }
                }
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


    public void drawLine() {
        points = new ArrayList<>();
        points.add(markers);
        polyline = new Polyline();
        polyline.setWidth(7);
        polyline.setGeodesic(true);
        for (int i = 0; i < points.size(); i++)
            polyline.setPoints(points.get(i));
        map.getOverlayManager().add(polyline);
        map.invalidate();
        isPolylineDrawn =true;
        if (selected == null) {
            System.out.println("No vehicle selected");
        } else {
            followPoints();
        }

    }

    public void followPoints() {
        LinkedHashSet<String> noRepetitions = new LinkedHashSet<>();
        Iterator<GeoPoint> iterator = markers.iterator();
        while (iterator.hasNext()) {
            String val = iterator.next().toString();
            if (noRepetitions.contains(val)) {
                iterator.remove();
            } else
                noRepetitions.add(val);
        }

        ArrayList<Maneuver> maneuvers = new ArrayList<>();
        for (GeoPoint point : markers) {
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
            lineListManeuvers = new ArrayList<>();
            lineListManeuvers.addAll( maneuvers);        }
        startBehaviour("followPoints" + selected, PlanUtilities.createPlan("followPoints" + selected, maneuvers.toArray(new Maneuver[0])));
        previous = "M";
        setVehicleStateString(" ");
        onBackPressed();
    }

    public static List<Maneuver> sendmList(){
        return lineListManeuvers;


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


    final OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", selectedVehiclesPosition);
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
        posicaoOutrosVeiculos = MainActivity.drawPosicaoOutrosVeiculos();
        Set<GeoPoint> hs = new HashSet<>();
        hs.addAll(posicaoOutrosVeiculos);
        posicaoOutrosVeiculos.clear();
        posicaoOutrosVeiculos.addAll(hs);
        for(int i = 0 ; i<posicaoOutrosVeiculos.size();i++) {
            if(posicaoOutrosVeiculos.get(i)!= selectedVehiclesPosition) {
                final ArrayList<OverlayItem> itemsPoints = new ArrayList<>();
                OverlayItem markerPoints = new OverlayItem("markerTitle", "markerDescription", posicaoOutrosVeiculos.get(i));
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
        if (selectedVehiclesPosition != null) {
            final ArrayList<OverlayItem> items = new ArrayList<>();
            final OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", selectedVehiclesPosition);
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
        stopRepeatingTask();
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

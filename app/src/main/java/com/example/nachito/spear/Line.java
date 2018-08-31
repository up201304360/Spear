package com.example.nachito.spear;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import pt.lsts.imc.Goto;
import pt.lsts.imc.Maneuver;
import pt.lsts.imc.def.SpeedUnits;
import pt.lsts.imc.def.ZUnits;
import pt.lsts.neptus.messages.listener.Periodic;
import pt.lsts.util.PlanUtilities;

import static android.os.Build.VERSION_CODES.M;
import static com.example.nachito.spear.MainActivity.altitude;
import static com.example.nachito.spear.MainActivity.depth;
import static com.example.nachito.spear.MainActivity.isDepthSelected;
import static com.example.nachito.spear.MainActivity.isRPMSelected;
import static com.example.nachito.spear.MainActivity.isStopPressed;
import static com.example.nachito.spear.MainActivity.localizacao;
import static com.example.nachito.spear.MainActivity.speed;
import static com.example.nachito.spear.MainActivity.startBehaviour;
import static com.example.nachito.spear.MainActivity.zoomLevel;

/**
 * Created by ines on 11/14/17.
 */

public class Line extends AppCompatActivity {


    static double lat;
    static double lon;
    static boolean isPolylineDrawn;
    static Polyline polyline;
    static ArrayList<GeoPoint> markers = new ArrayList<>();
    static Polygon circle;
    static ArrayList<Maneuver> lineListManeuvers;
    IMapController mapController;
    Button done;
    Button preview;

    MapView map;
    Button erase;
    int numberOfPoints;
    Drawable nodeIcon;
    ArrayList<ArrayList<GeoPoint>> points;
    Goto follow;
    Marker startMarker;
    ArrayList<GeoPoint> otherVehiclesPosition;
    GeoPoint selectedVehiclePosition;
    final OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", selectedVehiclePosition);
    Button eraseAll;
    boolean isDoneClicked = false;
    String selected;
    Boolean isPreviewPressed = false;

    List<Marker> markerList = new ArrayList<>();
    private Handler mHandler;
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                map.getOverlayManager().clear();
                if (markers.size() != 0) {
                    for (int i = 0; i < markers.size(); i++) {
                        startMarker = new Marker(map);
                        startMarker.setPosition(markers.get(i));
                        startMarker.setIcon(nodeIcon);
                        startMarker.isDraggable();
                        startMarker.setDraggable(true);
                        startMarker.setTitle("lat/lon:" + markers.get(i));
                        map.getOverlays().add(startMarker);
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

    public static ArrayList<GeoPoint> getPointsLine() {
        return markers;
    }


    public static boolean getPoly() {
        return isPolylineDrawn;
    }

    public static List<Maneuver> sendmList() {
        return lineListManeuvers;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.line);

        map = findViewById(R.id.mapLine);
        done = findViewById(R.id.doneLine);
        nodeIcon = getResources().getDrawable(R.drawable.orangeled);
        erase = findViewById(R.id.eraseLine);
        eraseAll = findViewById(R.id.eraseAllLine);
        preview = findViewById(R.id.previewLine);

        map.setMultiTouchControls(true);
        Toast.makeText(this, " Long click on the map to choose a line", Toast.LENGTH_SHORT).show();
        getIntentSelected();
        if (MainActivity.isOfflineSelected) {
            map.setTileSource(new XYTileSource("4uMaps", 2, 18, 256, ".png", new String[]{}));
        }
        mapController = map.getController();
        mapController.setZoom(zoomLevel);
        selectedVehiclePosition = MainActivity.getVariables();

        if (selectedVehiclePosition != null)
            mapController.setCenter(selectedVehiclePosition);
        else
            mapController.setCenter(localizacao());

        drawRed();
        drawBlue();
        drawGreen();

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
                markerList.add(startMarker);
                map.invalidate();
                numberOfPoints++;
                points = new ArrayList<>();

                polyline = new Polyline();
                erase.setOnClickListener(v -> {
                    if (!isDoneClicked) {
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

                eraseAll.setOnClickListener(v -> {
                    if (!isDoneClicked) {
                        for (Marker m : markerList) {
                            m.remove(map);
                            map.invalidate();
                        }
                        markerList.clear();
                        markers.clear();
                        numberOfPoints = 0;
                        if (polyline != null) {
                            polyline.setPoints(markers);
                            isPolylineDrawn = false;
                        }
                        drawGreen();
                        drawBlue();
                        drawRed();
                    }


                });
                done.setOnClickListener(v -> {
                    if (markers.size() <= 1) {
                        if (selected == null) {
                            Toast.makeText(Line.this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
                        } else {
                            Go(p);
                            isDoneClicked = true;
                            startRepeatingTask();
                        }
                    } else if (markers.size() > 1) {
                        if (selected == null) {
                            Toast.makeText(Line.this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
                        } else {
                            isDoneClicked = true;
                            isPolylineDrawn = true;
                            drawLine();
                            startRepeatingTask();


                        }
                    }
                });


                preview.setOnClickListener(v -> {

                    isPreviewPressed = true;
                    if (markers.size() == 1 || markers.isEmpty()) {
                        Toast.makeText(Line.this, "Add more points", Toast.LENGTH_SHORT).show();
                    } else {
                        if (selected == null) {
                            Toast.makeText(Line.this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
                        } else {
                            drawLine();

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

    public void getIntentSelected() {
        Intent intent = getIntent();
        selected = Objects.requireNonNull(intent.getExtras()).getString("selected");
    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    public void drawLine() {


        if (!isDoneClicked && isPreviewPressed) {
            points.add(markers);

            polyline.setWidth(7);
            polyline.setGeodesic(true);
            for (int i = 0; i < points.size(); i++)
                polyline.setPoints(points.get(i));
            map.getOverlayManager().add(polyline);
            map.invalidate();
        } else if (isDoneClicked) {

            points.add(markers);

            polyline.setWidth(7);
            polyline.setGeodesic(true);
            for (int i = 0; i < points.size(); i++)
                polyline.setPoints(points.get(i));
            map.getOverlayManager().add(polyline);
            map.invalidate();
            if (selected == null) {
                System.out.println("No vehicle selected");
            } else {
                followPoints();
            }

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
            if (isDepthSelected) {
                follow.setZ(depth);
                follow.setZUnits(ZUnits.DEPTH);
            } else {
                follow.setZ(altitude);
                follow.setZUnits(ZUnits.ALTITUDE);
            }
            follow.setSpeed(speed);
            if (!isRPMSelected) {
                follow.setSpeedUnits(SpeedUnits.METERS_PS);
            } else {
                follow.setSpeedUnits(SpeedUnits.RPM);
            }
            maneuvers.add(follow);
        }

        isPolylineDrawn = true;
        MainActivity.hasEnteredServiceMode = false;
        isStopPressed = true;
        startBehaviour("followPoints" + selected, PlanUtilities.createPlan("followPoints" + selected, maneuvers.toArray(new Maneuver[0])));
        onBackPressed();


    }

    public void Go(GeoPoint p) {
        Goto go = new Goto();
        double lat = Math.toRadians(p.getLatitude());
        double lon = Math.toRadians(p.getLongitude());
        go.setLat(lat);
        go.setLon(lon);
        if (isDepthSelected) {
            go.setZ(depth);
            go.setZUnits(ZUnits.DEPTH);
        } else {
            go.setZ(altitude);
            go.setZUnits(ZUnits.ALTITUDE);
        }
        go.setSpeed(speed);
        if (!isRPMSelected) {
            go.setSpeedUnits(SpeedUnits.METERS_PS);
        } else {
            go.setSpeedUnits(SpeedUnits.RPM);
        }
        String planid = "SpearGoto-" + selected;
        MainActivity.hasEnteredServiceMode = false;
        startBehaviour(planid, go);
        onBackPressed();


    }

    @Periodic
    public void drawRed() {
        final GeoPoint loc = MainActivity.localizacao();
        final ArrayList<OverlayItem> items2 = new ArrayList<>();
        final OverlayItem marker2 = new OverlayItem("markerTitle", "markerDescription", loc);
        marker.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
        items2.add(marker2);
        Resources resources = this.getResources();

        Bitmap newMarker2;
        if (android.os.Build.VERSION.SDK_INT <= M) {

            newMarker2 = Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.ico_ccu));

        } else {

            newMarker2 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.ico_ccu), 50, 50, true);

        }
        Drawable markerLoc = new BitmapDrawable(getResources(), newMarker2);
        final ItemizedIconOverlay markersOverlay2 = new ItemizedIconOverlay<>(items2, markerLoc, null, this);
        map.getOverlays().add(markersOverlay2);

    }

    @Periodic
    public void drawBlue() {
        otherVehiclesPosition = MainActivity.drawOtherVehicles();

        for (int i = 0; i < otherVehiclesPosition.size(); i++) {
            if (otherVehiclesPosition.get(i) != selectedVehiclePosition) {
                final ArrayList<OverlayItem> itemsPoints = new ArrayList<>();
                OverlayItem markerPoints = new OverlayItem("markerTitle", "markerDescription", otherVehiclesPosition.get(i));
                markerPoints.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
                itemsPoints.add(markerPoints);
                Bitmap source2;
                Resources resources = this.getResources();

                if (android.os.Build.VERSION.SDK_INT <= M) {
                    source2 = Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.downarrow2));

                } else

                    source2 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.downarrow2), 50, 50, true);

                Bitmap target = MainActivity.RotateMyBitmap(source2, MainActivity.orientationOtherVehicles.get(i));
                Drawable marker_ = new BitmapDrawable(getResources(), target);
                ItemizedIconOverlay markersOverlay_ = new ItemizedIconOverlay<>(itemsPoints, marker_, null, this);
                map.getOverlays().add(markersOverlay_);
            }
        }
    }

    @Periodic
    public void drawGreen() {
        if (selectedVehiclePosition != null) {
            final ArrayList<OverlayItem> items = new ArrayList<>();
            final OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", selectedVehiclePosition);
            marker.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
            items.add(marker);
            Bitmap newMarker;
            Resources resources = this.getResources();

            if (android.os.Build.VERSION.SDK_INT <= M) {
                newMarker = Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrowgreen2));

            } else

                newMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrowgreen2), 50, 50, true);

            Bitmap target = MainActivity.RotateMyBitmap(newMarker, MainActivity.orientationSelected);
            Drawable markerLoc = new BitmapDrawable(getResources(), target);
            ItemizedIconOverlay markersOverlay2 = new ItemizedIconOverlay<>(items, markerLoc, null, this);
            map.getOverlays().add(markersOverlay2);

        }
    }


    @Override
    public void onBackPressed() {
        stopRepeatingTask();
        for (Marker m : markerList) {
            m.remove(map);
            map.invalidate();
        }
        markerList.clear();
        numberOfPoints = 0;
        getPointsLine().clear();

        super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        stopRepeatingTask();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        stopRepeatingTask();
        super.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();
        mapController.setZoom(zoomLevel);
    }
}

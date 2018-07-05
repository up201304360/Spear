package com.example.nachito.spear;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import org.androidannotations.annotations.EActivity;
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
import java.util.Vector;

import pt.lsts.coverage.GeoCoord;
import pt.lsts.imc.FollowPath;
import pt.lsts.imc.Goto;
import pt.lsts.imc.Maneuver;
import pt.lsts.imc.PathPoint;
import pt.lsts.imc.def.SpeedUnits;
import pt.lsts.imc.def.ZUnits;
import pt.lsts.neptus.messages.listener.Periodic;
import pt.lsts.util.PlanUtilities;

import static android.os.Build.VERSION_CODES.M;
import static com.example.nachito.spear.MainActivity.altitude;
import static com.example.nachito.spear.MainActivity.areaIcon;
import static com.example.nachito.spear.MainActivity.depth;
import static com.example.nachito.spear.MainActivity.imc;
import static com.example.nachito.spear.MainActivity.isDepthSelected;
import static com.example.nachito.spear.MainActivity.isRPMSelected;
import static com.example.nachito.spear.MainActivity.localizacao;
import static com.example.nachito.spear.MainActivity.planWaypointPolyline;
import static com.example.nachito.spear.MainActivity.planWaypoints;
import static com.example.nachito.spear.MainActivity.speed;
import static com.example.nachito.spear.MainActivity.startBehaviour;
import static com.example.nachito.spear.MainActivity.swath_width;
import static com.example.nachito.spear.MainActivity.zoomLevel;
import static pt.lsts.coverage.AreaCoverage.computeCoveragePath;

/**
 *
 * Created by ines on 11/13/17.
 */
@EActivity
public class Area extends AppCompatActivity {
    static double lat;
    static double lon;
    static boolean iscircleDrawn;
    static Polyline polyline;
    static Polygon circle;
    static ArrayList<GeoPoint> markers = new ArrayList<>();
    static ArrayList<Maneuver> maneuverArrayList;
    static ArrayList<GeoPoint> nullArray = new ArrayList<>();
    final ArrayList seletedItems = new ArrayList();
    IMapController mapController;
    Button done;
    Button preview;
    MapView mapArea;
    Button erase;
    Drawable nodeIcon2;
    int numberOfPointsPressed;
    Drawable nodeIcon;
    Marker startMarker;
    ArrayList<GeoPoint> otherVehiclesPosition;
    GeoPoint centerInSelectedVehicle = MainActivity.getVariables();

    final OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", centerInSelectedVehicle);
    boolean isdoneClicked = false;
    Button eraseAll;
    String selected;
    List<Marker> markerList = new ArrayList<>();
    List<Marker> markerArea = new ArrayList<>();
    List<Polyline> poliList = new ArrayList<>();
    Marker pointsFromArea;
    Polyline areaWaypointPolyline;
    ArrayList<Maneuver> maneuvers;
    Boolean isPreviewPressed = false;
    ArrayList<String> sensorList;
    AlertDialog dialog;
    boolean hasMultibeam;
    boolean hasCamera;
    boolean hasSidescan;

    public static ArrayList<GeoPoint> getPointsArea() {
        return markers;
    }


    public static List<Maneuver> sendmList() {
        return maneuverArrayList;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.area);

        mapArea = findViewById(R.id.mapArea);
        done = findViewById(R.id.doneArea);
        nodeIcon = getResources().getDrawable(R.drawable.orangeled);
        erase = findViewById(R.id.eraseArea);
        eraseAll = findViewById(R.id.eraseAllArea);
        isdoneClicked = false;
        if (pointsFromArea != null) {
            for (Marker l : markerArea) {
                l.remove(mapArea);
                mapArea.invalidate();
            }
        }

        if (areaWaypointPolyline != null) {

            areaWaypointPolyline.setPoints(nullArray);
            mapArea.getOverlays().remove(areaWaypointPolyline);
            mapArea.invalidate();
        }


        if (maneuverArrayList != null)
            maneuverArrayList.clear();
        markerList.clear();
        markerArea.clear();
        markers.clear();
        numberOfPointsPressed = 0;
        getIntentSelected();
        nodeIcon2 = getResources().getDrawable(R.drawable.reddot);
        preview = findViewById(R.id.previewArea);

        if (MainActivity.isOfflineSelected) {
            mapArea.setTileSource(new XYTileSource("4uMaps", 2, 18, 256, ".png", new String[]{}));
        }
        mapController = mapArea.getController();
        mapArea.setMultiTouchControls(true);
        mapController.setZoom(zoomLevel);
        if (centerInSelectedVehicle != null)
            mapController.setCenter(centerInSelectedVehicle);
        else
            mapController.setCenter(localizacao());

        drawRed();
        drawBlue();
        drawGreen();
        if(!imc.connectedVehicles().isEmpty()) {
            if (imc.allSensores() != null)
                if (imc.allSensores().size() == 0) {
                    Toast.makeText(this, "No sensors detected", Toast.LENGTH_SHORT).show();
                } else {

                    sensorList = new ArrayList<>();
                    for (int i = 0; i < imc.allSensores().size(); i++) {
                        sensorList.addAll(imc.allSensores());

                    }
                    LinkedHashSet<String> withoutRepetitions = new LinkedHashSet<>();

                    Iterator<String> it = sensorList.iterator();
                    while (it.hasNext()) {
                        String val = it.next();
                        if (withoutRepetitions.contains(val)) {
                            it.remove();
                        } else
                            withoutRepetitions.add(val);
                    }
                    final CharSequence[] items = sensorList.toArray(new CharSequence[0]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Select Profile");
                    // indexSelected contains the index of item (of which checkbox checked)
                    builder.setMultiChoiceItems(items, null,
                            (dialog, indexSelected, isChecked) -> {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    // write your code when user checked the checkbox
                                    Toast.makeText(this, "Choose only one ", Toast.LENGTH_SHORT).show();

                                    if (seletedItems.size() == 0) {
                                        seletedItems.add(indexSelected);
                                        if (sensorList.get(indexSelected).equals("Camera")) {
                                            hasCamera = true;
                                        }
                                        if (sensorList.get(indexSelected).equals("Multibeam")) {
                                            hasMultibeam = true;
                                        }
                                        if (sensorList.get(indexSelected).equals("Sidescan")) {
                                            hasSidescan = true;
                                        }
                                    } else {
                                        Toast.makeText(this, "Choose only one ", Toast.LENGTH_SHORT).show();

                                        seletedItems.remove(Integer.valueOf(indexSelected));

                                    }


                                } else if (seletedItems.contains(indexSelected)) {
                                    // Else, if the item is already in the array, remove it
                                    // write your code when user Uchecked the checkbox
                                    seletedItems.remove(Integer.valueOf(indexSelected));
                                }
                            })
                            // Set the action buttons
                            .setPositiveButton("OK", (dialog, id) -> {
                                //  Your code when user clicked on OK
                                //  You can write the code  to save the selected item here

                            })
                            .setNegativeButton("Cancel", (dialog, id) -> {
                                //  Your code when user clicked on Cancel

                            });

                    dialog = builder.create();//AlertDialog dialog; create like this outside onClick
                    dialog.show();


                }
        }
        Toast.makeText(this, " Long click on the map to choose an area", Toast.LENGTH_SHORT).show();

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
                startMarker = new Marker(mapArea);
                startMarker.setPosition(p);
                mapArea.getOverlays().add(startMarker);
                startMarker.setIcon(getResources().getDrawable(R.drawable.orangeled));


                markerList.add(startMarker);
                mapArea.invalidate();
                numberOfPointsPressed++;

                for (Marker l : markerList) {
                    l.setOnMarkerClickListener((marker, mapView) -> {
                        mapArea.getOverlays().remove(marker);
                        marker.remove(mapArea);
                        markerList.remove(marker);
                        markerArea.remove(marker);
                        markers.remove(marker.getPosition());

                        numberOfPointsPressed--;
                        mapArea.invalidate();
                        return false;
                    });
                }

                erase.setOnClickListener(v -> {
                    if (!isdoneClicked) {
                        for (int i = 0; i < numberOfPointsPressed; i++) {
                            mapArea.getOverlays().remove(startMarker);
                            startMarker.remove(mapArea);
                            markers.remove(startMarker.getPosition());
                            numberOfPointsPressed--;
                        }

                        if (polyline != null)
                            polyline.setPoints(markers);
                        if (circle != null)
                            circle.setPoints(markers);
                        mapArea.invalidate();
                        erase.setClickable(false);
                    }
                });


                eraseAll.setOnClickListener(v -> {
                    if (planWaypoints != null) {
                        for (Marker l : markerArea) {
                            l.remove(mapArea);
                            mapArea.invalidate();
                            planWaypoints.clear();
                        }
                    }


                    if (planWaypointPolyline != null) {

                        planWaypointPolyline.setPoints(nullArray);
                        mapArea.invalidate();


                    }

                    if (maneuverArrayList != null)
                        maneuverArrayList.clear();
                    if (maneuvers != null)
                        maneuvers.clear();

                    if (!isdoneClicked) {
                        for (Marker m : markerList) {
                            m.remove(mapArea);
                            mapArea.invalidate();

                        }
                        markerList.clear();
                        markerArea.clear();
                        markers.clear();
                        numberOfPointsPressed = 0;
                        drawGreen();
                        drawBlue();
                        drawRed();

                    }
                });

                done.setOnClickListener(v -> {
                    if (markers.size() <= 1) {

                        if (selected == null) {
                            Toast.makeText(Area.this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
                        } else {
                            Go(p);
                            isdoneClicked = true;
                        }
                    } else if (markers.size() > 1) {
                        if (selected == null) {
                            Toast.makeText(Area.this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
                        } else {
                            isdoneClicked = true;
                            iscircleDrawn = true;
                            if(hasCamera) {
                                followAreaCamera();
                            }else if(hasMultibeam){
                                followAreaMultibeam();}
                                else if(hasSidescan){
                                followAreaSidescan();}else {
                            followArea();}
                        }
                    }
                });


                preview.setOnClickListener(v -> {
                    if (isPreviewPressed) {
                        if (planWaypoints != null) {
                            for (Marker l : markerArea) {
                                l.remove(mapArea);
                                mapArea.invalidate();
                                planWaypoints.clear();
                            }
                        }


                        if (planWaypointPolyline != null) {

                            planWaypointPolyline.setPoints(nullArray);
                            mapArea.invalidate();


                        }


                    }


                    isPreviewPressed = true;
                    if (markers.size() == 1 || markers.size() == 0) {
                        Toast.makeText(Area.this, "Add more points", Toast.LENGTH_SHORT).show();
                    } else {
                        if (selected == null) {
                            Toast.makeText(Area.this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
                        } else {
                            if(hasCamera) {
                                followAreaCamera();
                            }else if(hasMultibeam){
                                followAreaMultibeam();}
                            else if(hasSidescan){
                                followAreaSidescan();}else {
                                followArea();}


                        }
                    }
                });
                return false;
            }
        };
        MapEventsOverlay OverlayEventos = new MapEventsOverlay(this.getBaseContext(), mReceive);
        mapArea.getOverlays().add(OverlayEventos);
        //Refreshing the map to draw the new overlay
        mapArea.invalidate();

    }

    public void getIntentSelected() {
        Intent intent = getIntent();
        selected = intent.getExtras().getString("selected");
    }


    public void followArea() {

        LinkedHashSet<String> noRepetitions = new LinkedHashSet<>();
        Iterator<GeoPoint> it = markers.iterator();
        while (it.hasNext()) {
            String val = it.next().toString();
            if (noRepetitions.contains(val)) {
                it.remove();
            } else
                noRepetitions.add(val);
        }


            ArrayList<GeoCoord> coords = new ArrayList<>();
            maneuvers = new ArrayList<>();
            for (int i = 0; i < markers.size(); i++) {
                coords.add(new GeoCoord(markers.get(i).getLatitude(), markers.get(i).getLongitude()));
            }
            Vector<PathPoint> points = new Vector<>();
            GeoCoord primPonto = new GeoCoord(markers.get(0).getLatitude(), markers.get(0).getLongitude());
            for (GeoCoord coord : computeCoveragePath(coords, swath_width)) {
                double[] offsets = coord.getOffsetFrom(primPonto);
                PathPoint pt = new PathPoint();
                pt.setX(offsets[0]);
                pt.setY(offsets[1]);
                pt.setZ(offsets[2]);
                points.add(pt);

            }


            FollowPath area = new FollowPath();
            double lat = Math.toRadians(markers.get(0).getLatitude()); //primeiro
            double lon = Math.toRadians(markers.get(0).getLongitude());
            area.setLat(lat);
            area.setLon(lon);
            area.setSpeed(speed);
            if (!isRPMSelected) {
                area.setSpeedUnits(SpeedUnits.METERS_PS);
            } else {
                area.setSpeedUnits(SpeedUnits.RPM);
            }
            if (isDepthSelected) {
                area.setZ(depth);
                area.setZUnits(ZUnits.DEPTH);
            } else {
                area.setZ(altitude);
                area.setZUnits(ZUnits.ALTITUDE);
            }
            area.setPoints(points);
            for (int i = 0; i < area.getPoints().size(); i++) {
                maneuvers.add(area);
                maneuverArrayList = new ArrayList<>();
                maneuverArrayList.addAll(maneuvers);

            }
            if (isdoneClicked) {
                MainActivity.areNewWaypointsFromAreaUpdated = false;
                MainActivity.hasEnteredServiceMode = false;
                startBehaviour("SpearArea-" + selected, PlanUtilities.createPlan("SpearArea-" + selected, maneuvers.toArray(new Maneuver[0])));
                MainActivity.updateWaypoints();
                onBackPressed();
            } else {
                DrawWaypoints.callWaypoint(maneuverArrayList);

                if (planWaypoints.size() != 0) {
                    Marker pointsFromPlan;
                    for (int i = 0; i < planWaypoints.size(); i++) {
                        pointsFromPlan = new Marker(mapArea);
                        if (planWaypoints.size() != 0) {
                            pointsFromPlan.setPosition(planWaypoints.get(i));
                            pointsFromPlan.setIcon(areaIcon);
                            pointsFromPlan.setDraggable(true);
                            mapArea.getOverlays().add(pointsFromPlan);
                            markerArea.add(pointsFromPlan);
                            mapArea.invalidate();

                        }
                        if (planWaypointPolyline != null) {
                            mapArea.getOverlays().add(planWaypointPolyline);
                            poliList.add(planWaypointPolyline);

                            mapArea.invalidate();

                        }
                    }

                }

            }
        }



    public void followAreaCamera() {
        LinkedHashSet<String> noRepetitions = new LinkedHashSet<>();
        Iterator<GeoPoint> it = markers.iterator();
        while (it.hasNext()) {
            String val = it.next().toString();
            if (noRepetitions.contains(val)) {
                it.remove();
            } else
                noRepetitions.add(val);
        }


        ArrayList<GeoCoord> coords = new ArrayList<>();
        maneuvers = new ArrayList<>();
        for (int i = 0; i < markers.size(); i++) {
            coords.add(new GeoCoord(markers.get(i).getLatitude(), markers.get(i).getLongitude()));
        }
        Vector<PathPoint> points = new Vector<>();
        GeoCoord primPonto = new GeoCoord(markers.get(0).getLatitude(), markers.get(0).getLongitude());
        for (GeoCoord coord : computeCoveragePath(coords, 10)) {
            double[] offsets = coord.getOffsetFrom(primPonto);
            PathPoint pt = new PathPoint();
            pt.setX(offsets[0]);
            pt.setY(offsets[1]);
            pt.setZ(offsets[2]);
            points.add(pt);

        }


        FollowPath area = new FollowPath();
        double lat = Math.toRadians(markers.get(0).getLatitude()); //primeiro
        double lon = Math.toRadians(markers.get(0).getLongitude());
        area.setLat(lat);
        area.setLon(lon);
        area.setSpeed(1);
        area.setSpeedUnits(SpeedUnits.METERS_PS);


            area.setZ(2.5);
            area.setZUnits(ZUnits.ALTITUDE);

        area.setPoints(points);
        for (int i = 0; i < area.getPoints().size(); i++) {
            maneuvers.add(area);
            maneuverArrayList = new ArrayList<>();
            maneuverArrayList.addAll(maneuvers);

        }
        if (isdoneClicked) {
            MainActivity.areNewWaypointsFromAreaUpdated = false;
            MainActivity.hasEnteredServiceMode = false;
            startBehaviour("SpearArea-" + selected, PlanUtilities.createPlan("SpearArea-" + selected, maneuvers.toArray(new Maneuver[0])));
            MainActivity.updateWaypoints();
            onBackPressed();
        } else {
            DrawWaypoints.callWaypoint(maneuverArrayList);

            if (planWaypoints.size() != 0) {
                Marker pointsFromPlan;
                for (int i = 0; i < planWaypoints.size(); i++) {
                    pointsFromPlan = new Marker(mapArea);
                    if (planWaypoints.size() != 0) {
                        pointsFromPlan.setPosition(planWaypoints.get(i));
                        pointsFromPlan.setIcon(areaIcon);
                        pointsFromPlan.setDraggable(true);
                        mapArea.getOverlays().add(pointsFromPlan);
                        markerArea.add(pointsFromPlan);
                        mapArea.invalidate();

                    }
                    if (planWaypointPolyline != null) {
                        mapArea.getOverlays().add(planWaypointPolyline);
                        poliList.add(planWaypointPolyline);

                        mapArea.invalidate();

                    }
                }
            }
        }
            System.out.println("Camera");

    }

    public void followAreaMultibeam() {

        LinkedHashSet<String> noRepetitions = new LinkedHashSet<>();
        Iterator<GeoPoint> it = markers.iterator();
        while (it.hasNext()) {
            String val = it.next().toString();
            if (noRepetitions.contains(val)) {
                it.remove();
            } else
                noRepetitions.add(val);
        }


        ArrayList<GeoCoord> coords = new ArrayList<>();
        maneuvers = new ArrayList<>();
        for (int i = 0; i < markers.size(); i++) {
            coords.add(new GeoCoord(markers.get(i).getLatitude(), markers.get(i).getLongitude()));
        }
        Vector<PathPoint> points = new Vector<>();
        GeoCoord primPonto = new GeoCoord(markers.get(0).getLatitude(), markers.get(0).getLongitude());
        for (GeoCoord coord : computeCoveragePath(coords, 20)) {
            double[] offsets = coord.getOffsetFrom(primPonto);
            PathPoint pt = new PathPoint();
            pt.setX(offsets[0]);
            pt.setY(offsets[1]);
            pt.setZ(offsets[2]);
            points.add(pt);

        }


        FollowPath area = new FollowPath();
        double lat = Math.toRadians(markers.get(0).getLatitude()); //primeiro
        double lon = Math.toRadians(markers.get(0).getLongitude());
        area.setLat(lat);
        area.setLon(lon);
        area.setSpeed(1);

            area.setSpeedUnits(SpeedUnits.METERS_PS);

        if (isDepthSelected) {
            area.setZ(depth);
            area.setZUnits(ZUnits.DEPTH);
        } else {
            area.setZ(altitude);
            area.setZUnits(ZUnits.ALTITUDE);
        }
        area.setPoints(points);
        for (int i = 0; i < area.getPoints().size(); i++) {
            maneuvers.add(area);
            maneuverArrayList = new ArrayList<>();
            maneuverArrayList.addAll(maneuvers);

        }
        if (isdoneClicked) {
            MainActivity.areNewWaypointsFromAreaUpdated = false;
            MainActivity.hasEnteredServiceMode = false;
            startBehaviour("SpearArea-" + selected, PlanUtilities.createPlan("SpearArea-" + selected, maneuvers.toArray(new Maneuver[0])));
            MainActivity.updateWaypoints();
            onBackPressed();
        } else {
            DrawWaypoints.callWaypoint(maneuverArrayList);

            if (planWaypoints.size() != 0) {
                Marker pointsFromPlan;
                for (int i = 0; i < planWaypoints.size(); i++) {
                    pointsFromPlan = new Marker(mapArea);
                    if (planWaypoints.size() != 0) {
                        pointsFromPlan.setPosition(planWaypoints.get(i));
                        pointsFromPlan.setIcon(areaIcon);
                        pointsFromPlan.setDraggable(true);
                        mapArea.getOverlays().add(pointsFromPlan);
                        markerArea.add(pointsFromPlan);
                        mapArea.invalidate();

                    }
                    if (planWaypointPolyline != null) {
                        mapArea.getOverlays().add(planWaypointPolyline);
                        poliList.add(planWaypointPolyline);

                        mapArea.invalidate();

                    }
                }

            }

        }

        System.out.println("Multibeam");
    }

    public void followAreaSidescan() {
        LinkedHashSet<String> noRepetitions = new LinkedHashSet<>();
        Iterator<GeoPoint> it = markers.iterator();
        while (it.hasNext()) {
            String val = it.next().toString();
            if (noRepetitions.contains(val)) {
                it.remove();
            } else
                noRepetitions.add(val);
        }


        ArrayList<GeoCoord> coords = new ArrayList<>();
        maneuvers = new ArrayList<>();
        for (int i = 0; i < markers.size(); i++) {
            coords.add(new GeoCoord(markers.get(i).getLatitude(), markers.get(i).getLongitude()));
        }
        Vector<PathPoint> points = new Vector<>();
        GeoCoord primPonto = new GeoCoord(markers.get(0).getLatitude(), markers.get(0).getLongitude());
        for (GeoCoord coord : computeCoveragePath(coords, 40)) {
            double[] offsets = coord.getOffsetFrom(primPonto);
            PathPoint pt = new PathPoint();
            pt.setX(offsets[0]);
            pt.setY(offsets[1]);
            pt.setZ(offsets[2]);
            points.add(pt);

        }


        FollowPath area = new FollowPath();
        double lat = Math.toRadians(markers.get(0).getLatitude()); //primeiro
        double lon = Math.toRadians(markers.get(0).getLongitude());
        area.setLat(lat);
        area.setLon(lon);
        area.setSpeed(1);

            area.setSpeedUnits(SpeedUnits.METERS_PS);


            area.setZ(5);
            area.setZUnits(ZUnits.ALTITUDE);

        area.setPoints(points);
        for (int i = 0; i < area.getPoints().size(); i++) {
            maneuvers.add(area);
            maneuverArrayList = new ArrayList<>();
            maneuverArrayList.addAll(maneuvers);

        }
        if (isdoneClicked) {
            MainActivity.areNewWaypointsFromAreaUpdated = false;
            MainActivity.hasEnteredServiceMode = false;
            startBehaviour("SpearArea-" + selected, PlanUtilities.createPlan("SpearArea-" + selected, maneuvers.toArray(new Maneuver[0])));
            MainActivity.updateWaypoints();
            onBackPressed();
        } else {
            DrawWaypoints.callWaypoint(maneuverArrayList);

            if (planWaypoints.size() != 0) {
                Marker pointsFromPlan;
                for (int i = 0; i < planWaypoints.size(); i++) {
                    pointsFromPlan = new Marker(mapArea);
                    if (planWaypoints.size() != 0) {
                        pointsFromPlan.setPosition(planWaypoints.get(i));
                        pointsFromPlan.setIcon(areaIcon);
                        pointsFromPlan.setDraggable(true);
                        mapArea.getOverlays().add(pointsFromPlan);
                        markerArea.add(pointsFromPlan);
                        mapArea.invalidate();

                    }
                    if (planWaypointPolyline != null) {
                        mapArea.getOverlays().add(planWaypointPolyline);
                        poliList.add(planWaypointPolyline);

                        mapArea.invalidate();

                    }
                }

            }

        }


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


    @Periodic()
    public void drawBlue() {
        otherVehiclesPosition = MainActivity.drawOtherVehicles();

        for (int i = 0; i < otherVehiclesPosition.size(); i++) {
            if (otherVehiclesPosition.get(i) != centerInSelectedVehicle) {
                final ArrayList<OverlayItem> itemsPoints = new ArrayList<>();
                OverlayItem markerPoints = new OverlayItem("markerTitle", "markerDescription", otherVehiclesPosition.get(i));
                markerPoints.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
                itemsPoints.add(markerPoints);
                Resources resources = this.getResources();
                Bitmap source2;
                if (android.os.Build.VERSION.SDK_INT <= M) {
                    source2 = Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.downarrow2));

                } else

                    source2 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.downarrow2), 50, 50, false);

                Bitmap target = MainActivity.RotateMyBitmap(source2, MainActivity.orientationOtherVehicles.get(i));
                Drawable marker_ = new BitmapDrawable(getResources(), target);
                ItemizedIconOverlay markersOverlay_ = new ItemizedIconOverlay<>(itemsPoints, marker_, null, this);
                mapArea.getOverlays().add(markersOverlay_);

            }
        }
    }

    @Periodic()
    public void drawRed() {
        final GeoPoint loc = MainActivity.localizacao();
        final ArrayList<OverlayItem> items2 = new ArrayList<>();
        final OverlayItem marker2 = new OverlayItem("markerTitle", "markerDescription", loc);
        marker.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
        items2.add(marker2);
        Resources resources = this.getResources();

        Bitmap newMarker2;
        if (android.os.Build.VERSION.SDK_INT <= M) {

            newMarker2 = Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrowred2));

        } else {

            newMarker2 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.arrowred2), 50, 50, true);

        }
        Bitmap target = MainActivity.RotateMyBitmap(newMarker2, MainActivity.bearingMyLoc);
        Drawable markerLoc = new BitmapDrawable(getResources(), target);
        final ItemizedIconOverlay markersOverlay2 = new ItemizedIconOverlay<>(items2, markerLoc, null, this);
        mapArea.getOverlays().add(markersOverlay2);

    }


    @Periodic()
    public void drawGreen() {
        if (centerInSelectedVehicle != null) {
            final ArrayList<OverlayItem> items = new ArrayList<>();
            final OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", centerInSelectedVehicle);
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
            final ItemizedIconOverlay markersOverlay2 = new ItemizedIconOverlay<>(items, markerLoc, null, this);
            mapArea.getOverlays().add(markersOverlay2);


        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();


        if (!isdoneClicked) {
            if (planWaypoints != null)
                planWaypoints.clear();
            if (planWaypointPolyline != null)
                planWaypointPolyline.setPoints(nullArray);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapController.setZoom(zoomLevel);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
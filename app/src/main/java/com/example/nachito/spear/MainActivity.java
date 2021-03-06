package com.example.nachito.spear;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.util.constants.MapViewConstants;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.lsts.imc.DesiredSpeed;
import pt.lsts.imc.DesiredZ;
import pt.lsts.imc.EstimatedState;
import pt.lsts.imc.FollowReference;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.Loiter;
import pt.lsts.imc.Maneuver;
import pt.lsts.imc.PlanControl;
import pt.lsts.imc.PlanDB;
import pt.lsts.imc.Reference;
import pt.lsts.imc.Rpm;
import pt.lsts.imc.StationKeeping;
import pt.lsts.imc.Teleoperation;
import pt.lsts.imc.VehicleState;
import pt.lsts.imc.def.SpeedUnits;
import pt.lsts.imc.def.ZUnits;
import pt.lsts.imc.net.Consume;
import pt.lsts.neptus.messages.listener.Periodic;
import pt.lsts.util.PlanUtilities;
import pt.lsts.util.WGS84Utilities;

import static android.os.Build.VERSION_CODES.M;
import static com.example.nachito.spear.PlanList.planBeingExecuted;
import static com.example.nachito.spear.R.id.wifiImage;
import static pt.lsts.util.WGS84Utilities.WGS84displace;


@EActivity

public class MainActivity extends AppCompatActivity
        implements MapViewConstants, OnLocationChangedListener, SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static boolean isOfflineSelected;
    @Bean
    static IMCGlobal imc;
    @ViewById(R.id.map)
    //Setting values
    static MapView map;
    static double speed;
    static int duration;
    static double radius;
    static double depth;
    static double swath_width;
    //Drawables
    @org.androidannotations.annotations.res.DrawableRes(R.drawable.orangeled)
    static Drawable nodeIcon;
    @org.androidannotations.annotations.res.DrawableRes(R.drawable.reddot)
    static Drawable areaIcon;
    @org.androidannotations.annotations.res.DrawableRes(R.drawable.blueled)
    static Drawable lineIcon;
    //Determine if speed is in RPM or meters
    static boolean isRPMSelected;
    //Strings for the panel
    @SuppressLint("StaticFieldLeak")
    static String velocityString;
    static String previous = null;
    static double orientationCompass;
    //points we choose in Activity Line
    static ArrayList<GeoPoint> pointsLine = Line.getPointsLine();
    static GeoPoint selectedVehiclePosition;
    static GeoPoint myPosition;
    static ArrayList<GeoPoint> otherVehiclesPositionList = new ArrayList<>();
    static float vehicleOrientation;
    static Collection<PlanUtilities.Waypoint> waypointsFromPlan;
    static Bitmap bitmapArrow;
    static boolean isPolylineDrawn = Line.getPoly();
    static double latVehicle;
    static String vehicleName;
    static double lonVehicle;
    static boolean isStopPressed = false;
    static boolean areNewWaypointsFromAreaUpdated = false;
    static ArrayList<GeoPoint> planWaypoints = new ArrayList<>();
    static boolean hasEnteredServiceMode = false;
    static boolean wasPlanChanged = false;
    static int zoomLevel;
    static List<Integer> orientationOtherVehicles;
    static int orientationSelected;
    static double altitude;
    static boolean isDepthSelected;
    static List<Maneuver> maneuverList;
    static Polyline planWaypointPolyline;
    static HashMap<String, List<String>> allErrorsList;
    static double latitudeAndroid;
    static double longitudeAndroid;
    final LinkedHashMap<String, EstimatedState> estates = new LinkedHashMap<>();
    final LinkedHashMap<String, Rpm> rpmValues = new LinkedHashMap<>();

    protected PowerManager.WakeLock mWakeLock;
    Marker markerFromLine;
    String connectedState;
    @ViewById(R.id.dive)
    Button dive;
    @ViewById(R.id.minus)
    Button minus;
    @ViewById(R.id.plus)
    Button plus;
    @ViewById(R.id.unlock)
    Button unlock;
    @ViewById(R.id.near)
    Button comeNear;
    @ViewById(R.id.startplan)
    Button startPlan;
    @ViewById(R.id.keepStation)
    Button keepStation;
    @ViewById(R.id.servicebar)
    TextView serviceBar;
    @ViewById(wifiImage)
    ImageView wifiDrawable;
    @ViewById(R.id.noWifiImage)
    ImageView noWifiImage;
    @ViewById(R.id.accelerate)
    Accelerate accelerate;
    @ViewById(R.id.decelerate)
    Decelerate decelerate;
    @ViewById(R.id.stopTeleop)
    StopTeleop stopTeleop;
    @ViewById(R.id.decelerateTV)
    TextView txt2;
    @ViewById(R.id.accelerateTV)
    TextView txt4;
    @ViewById(R.id.textView5)
    TextView txt5;
    @ViewById(R.id.teleopText)
    TextView txtmap;
    @ViewById(R.id.mainTV)
    TextView mainTV;
    TeleOperation teleOperation;
    List<VehicleState> vehicleStateList;
    MyLocationNewOverlay myLocationOverlay;
    @ViewById(R.id.teleOperationButton)
    Button teleOperationButton;
    @ViewById(R.id.stop)
    Button stopPlan;
    CompassOverlay mCompassOverlay;
    LocationManager locationManager;
    IMapController mapController;
    OverlayItem lastPosition = null;
    OsmMapsItemizedOverlay mItemizedOverlay;
    Double currSpeed;
    @ViewById(R.id.bottomsheet)
    LinearLayout bottom;
    Marker pointsFromPlan;
    Location location;
    List<String> vehicleList;
    List<String> planList;
    int listSize;
    List<String> stateList;
    SendSms sendSms;
    List<Marker> markerListSMS = new ArrayList<>();
    Marker markerSMS;
    ScaleBarOverlay scaleBarOverlay;
    @ViewById(R.id.joyLeft)
    Button joyLeft;
    ItemizedIconOverlay markersOverlay2;
    android.content.res.Resources resources;
    ArrayList<GeoPoint> nullArray = new ArrayList<>();
    float selectedVehicleOrientation;
    AISPlot ais;
    boolean isAISSelected;
    GPSConvert gpsConvert = new GPSConvert();
    RipplesPosition ripples;
    boolean isRipplesSelected = false;
    boolean followMeOn;
    double n;
    double e;
    GeoPoint geo;
    String previousVehicle;
    Polygon circle2;
    String planExecuting;
    ArrayList<String> errorsList;
    @ViewById(R.id.joyRight)
    Button joyRight;
    boolean detach;
    boolean myPosSelected;
    AppLocationService appLocationService;
    Location gpsLocation;
    Location nwLocation;
    private Context context;
    short vehicleRpm;
    String distFinal;
    MapEventsReceiver mReceive = new MapEventsReceiver() {
        @Override
        public boolean singleTapConfirmedHelper(GeoPoint p) {

            return false;
        }

        @Override
        public boolean longPressHelper(GeoPoint p) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder
                    .setMessage("Lock Map in ")
                    .setCancelable(true)
                    .setPositiveButton((imc.selectedVehicle), (dialog, id) -> {
                        if (imc.getSelectedvehicle() != null) {

                            myPosSelected = false;
                            mapController.setCenter(selectedVehiclePosition);
                            detach = true;
                            unlock.setVisibility(View.VISIBLE);

                            dialog.dismiss();
                        } else {
                            dialog.dismiss();

                            warning();

                        }
                    })
                    .setNegativeButton("My Pos", (dialog, id) -> {

                        if (myPosition != null) {
                            mapController.setCenter(myPosition);
                            detach = true;
                            unlock.setVisibility(View.VISIBLE);
                            myPosSelected = true;

                            dialog.cancel();
                        } else
                            Toast.makeText(context, "Turn Location on", Toast.LENGTH_SHORT).show();
                        dialog.cancel();

                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();


            return false;
        }

    };
    private Handler customHandlerAIS;
    private Marker startMarkerAIS[];
    private GeoPoint systemPosAIS;
    private int timeoutAISPull = 10;
    private int countAisTime = 0;
    private Handler customHandlerRipples;
    private Marker startMarkerRipples[];
    private String UrlRipples = "http://ripples.lsts.pt/api/v1/systems/active";
    private boolean newRipplesData = false;
    private RipplesPosition.SystemInfo systemInfo;
    private RipplesPosition.SystemInfo backSystemInfo;
    private GeoPoint systemPosRipples;
    private boolean firstRunRipplesPull = true;
    private int timeoutRipplesPull = 10;
    private Runnable updateTimerThreadGarbagde = new Runnable() {
        public void run() {
            Handler customHandlerGarbagde = new Handler();
            customHandlerGarbagde.postDelayed(updateTimerThreadGarbagde, 100);

            customHandlerGarbagde.postDelayed(this, 20000);
            System.gc();
            Runtime.getRuntime().gc();
        }
    };
    private Runnable updateTimerThreadAIS = new Runnable() {
        @Periodic
        @SuppressLint("SetTextI18n")
        public void run() {


            customHandlerAIS.postDelayed(this, timeoutAISPull * 1000);

            timeoutAISPull = Integer.parseInt("12");

        }

    };
    //Run task periodically - Ripples
    private Runnable updateTimerThreadRipples = new Runnable() {
        @SuppressLint("SetTextI18n")
        public void run() {
            if (isRipplesSelected) {
                customHandlerRipples.postDelayed(this, timeoutRipplesPull * 1000);
                if (timeoutRipplesPull != 1) {
                    if (ripples.PullData(UrlRipples)) {
                        systemInfo = ripples.GetSystemInfoRipples();
                        newRipplesData = true;
                    }

                }
                timeoutRipplesPull = Integer.parseInt("12");

            }
        }
    };

    public static GeoPoint getVariables() {
        return selectedVehiclePosition;
    }

    public static GeoPoint localizacao() {
        return myPosition;


    }

    public static ArrayList<GeoPoint> drawOtherVehicles() {
        return otherVehiclesPositionList;
    }

    @Nullable
    public static Bitmap RotateMyBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        if (bitmapArrow != null)
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        else
            return null;
    }

    public static void startBehaviour(String planid, IMCMessage what) {
        PlanControl pc = new PlanControl();
        pc.setArg(what);
        pc.setType(PlanControl.TYPE.REQUEST);
        pc.setOp(PlanControl.OP.START);
        pc.setFlags(0);
        pc.setRequestId(0);
        pc.setPlanId(planid);

        isStopPressed = false;
        wasPlanChanged = false;
        previous = "S";
        MainActivity.hasEnteredServiceMode = false;
        imc.sendMessage(pc);

    }

    public static void updateWaypoints() {

        areNewWaypointsFromAreaUpdated = true;

        if (Area.sendmList() != null) {
            List<Maneuver> areaWaypoints = new ArrayList<>();
            for (int i = 0; i < Area.sendmList().size() - 1; i++) {
                areaWaypoints.add(Area.sendmList().get(i));
                DrawWaypoints.callWaypoint(areaWaypoints);

            }


        }


    }

    //if a plan is changed without stopping the plan that was executing
    @Periodic()
    public void changePlans() {
        if ((!isStopPressed && planBeingExecuted != null && !PlanList.previousPlan.equals(".") && !PlanList.previousPlan.equals(planBeingExecuted)) || (!isStopPressed && planBeingExecuted != null && wasPlanChanged)) {
//wasPlannedChanged -> selecting a new Plan pressing the StartPlan button without stopping the previous button
            cleanMap();
            wasPlanChanged = false;
        }
    }

    public void updatePosition(GeoPoint aPoint) {
        if (mItemizedOverlay == null) {
            return;
        }
        OverlayItem overlayItem;
        overlayItem = new OverlayItem("Center", "Center", aPoint);
        lastPosition = overlayItem;
        mItemizedOverlay.addOverlay(overlayItem);
        map.getOverlays().add(mItemizedOverlay);
        map.getController().animateTo(aPoint);
    }

    //Run task periodically - AIS

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public void checkLocationPermission() {


        if (ContextCompat.checkSelfPermission(this,
                permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user  timer.startPlan()
                // sees the explanation, try again to request the permission.
                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    public void showRipplesPos() {

        if (isRipplesSelected) {

            if (newRipplesData) {
                newRipplesData = false;
                firstRunRipplesPull = false;
                backSystemInfo = systemInfo;
                for (int i = 0; i < systemInfo.systemSize; i++) {
                    systemPosRipples.setCoords(systemInfo.coordinates[i].getLatitude(), systemInfo.coordinates[i].getLongitude());
                    startMarkerRipples[i].setPosition(systemPosRipples);
                    startMarkerRipples[i].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                    if (systemInfo.sysName[i].contains("lauv"))
                        startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_auv));
                    else if (systemInfo.sysName[i].contains("ccu"))
                        startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_ccu));
                    else if (systemInfo.sysName[i].contains("manta"))
                        startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_manta));
                    else if (systemInfo.sysName[i].contains("spot"))
                        startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.spot_icon));
                    else
                        startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_unknown));

                    if (!(systemInfo.sysName[i].equals(vehicleList.get(i)))) {//TODO
                        startMarkerRipples[i].setTitle(systemInfo.sysName[i] + "\n" +
                                gpsConvert.latLonToDM(systemInfo.coordinates[i].getLatitude(), systemInfo.coordinates[i].getLongitude()));
                        map.getOverlays().add(startMarkerRipples[i]);
                    }
                }
            } else if (!newRipplesData && !firstRunRipplesPull) {
                for (int i = 0; i < backSystemInfo.systemSize; i++) {
                    systemPosRipples.setCoords(backSystemInfo.coordinates[i].getLatitude(), backSystemInfo.coordinates[i].getLongitude());
                    startMarkerRipples[i].setPosition(systemPosRipples);
                    startMarkerRipples[i].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    if (backSystemInfo.sysName[i].contains("lauv")) {


                        if (!(imc.connectedVehicles().toString().contains(backSystemInfo.sysName[i])))
                            startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_auv));
                    } else if (backSystemInfo.sysName[i].contains("ccu"))
                        startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_ccu));
                    else if (backSystemInfo.sysName[i].contains("manta"))
                        startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_manta));
                    else if (backSystemInfo.sysName[i].contains("spot"))
                        startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.spot_icon));
                    else
                        startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_unknown));

                    startMarkerRipples[i].setTitle(backSystemInfo.sysName[i] + "\n" + gpsConvert.latLonToDM(backSystemInfo.coordinates[i].getLatitude(), backSystemInfo.coordinates[i].getLongitude()));
                    map.getOverlays().add(startMarkerRipples[i]);
                }
            }


        }
    }

    public void onResume() {
        super.onResume();
        if (selectedVehiclePosition != null) {
            mapController.setCenter(selectedVehiclePosition);
            mapController.setZoom(zoomLevel);
        } else
            mapController.setCenter(new GeoPoint(41.1496100, -8.6109900));
        mapController.setZoom(zoomLevel);


        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    public void onClick(View v) {
        registerForContextMenu(serviceBar);
        openContextMenu(serviceBar);
    }

    //get all the values
    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String rpmKey = sharedPreferences.getString(getString(R.string.pref_show_rpmms_key), "speedControl");

        if (rpmKey.equals("RPM")) {
            setShowRPM(true);
        } else
            setShowRPM(false);

        String zControl = sharedPreferences.getString(getString(R.string.pref_show_depth_key), "zControl");

        if (zControl.equals("Depth")) {
            setShowDepth(true);
        } else
            setShowDepth(false);


        String chosenTileSource = sharedPreferences.getString(getString(R.string.pref_show_map_key), "MapTileSource");


        if (chosenTileSource.equals("Offline")) {
            setOfflineMap(true);
        } else
            setOfflineMap(false);


        loadFromPrefs(sharedPreferences);


        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void loadFromPrefs(SharedPreferences sharedPreferences) {
        try {
            speed = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_speed_key), getString(R.string.pref_speed_default)));
            currSpeed = speed;
            if ((isRPMSelected && currSpeed.intValue() < 100) || (isRPMSelected && currSpeed.toString().indexOf(".") < 3) || (!isRPMSelected && currSpeed.toString().indexOf(".") > 2))
                Toast.makeText(this, "Check speed value", Toast.LENGTH_LONG).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Speed number not accepted INVALID FORMAT", Toast.LENGTH_LONG).show();
            speed = Float.parseFloat(getString(R.string.pref_speed_default));

        }
        try {
            duration = (int) Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_duration_key), getString(R.string.pref_duration_default)));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Duration number not accepted INVALID FORMAT", Toast.LENGTH_LONG).show();
            duration = (int) Float.parseFloat(getString(R.string.pref_duration_default));
        }
        try {
            radius = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_radius_key), getString(R.string.pref_radius_default)));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Radius number not accepted INVALID FORMAT", Toast.LENGTH_LONG).show();
            radius = Float.parseFloat(getString(R.string.pref_radius_default));
        }
        try {
            depth = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_depth_key), getString(R.string.pref_depth_default)));

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Depth number not accepted INVALID FORMAT", Toast.LENGTH_LONG).show();
            depth = Float.parseFloat(getString(R.string.pref_depth_default));
        }
        try {
            swath_width = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_width_key), "25"));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Swath Width number not accepted INVALID FORMAT", Toast.LENGTH_LONG).show();
            swath_width = Float.parseFloat("25");

        }
        try {
            altitude = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_altitude_key), getString(R.string.pref_altitude_default)));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Altitude number not accepted INVALID FORMAT", Toast.LENGTH_LONG).show();
            altitude = Float.parseFloat(getString(R.string.pref_altitude_default));

        }
    }

    public void setShowRPM(boolean showrpm) {
        MainActivity.isRPMSelected = showrpm;


    }

    public void setShowDepth(boolean showDepth) {
        MainActivity.isDepthSelected = showDepth;


    }

    public void setOfflineMap(boolean isOfflineMap) {
        isOfflineSelected = isOfflineMap;

        if (isOfflineSelected) {

            map.setUseDataConnection(false);

        } else

            map.setTileSource(TileSourceFactory.MAPNIK);
    }


    @SuppressLint("ResourceType")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

//update the screen if the shared preferences change

        String chosenKey = sharedPreferences.getString(getString(R.string.pref_show_rpmms_key), "speedControl");

        if (chosenKey.equals("RPM")) {
            setShowRPM(true);
        } else
            setShowRPM(false);


        String chosenTileSource = sharedPreferences.getString(getString(R.string.pref_show_map_key), "MapTileSource");


        if (chosenTileSource.equals("Offline")) {
            setOfflineMap(true);
        } else
            setOfflineMap(false);


        if (key.equals(getString(R.string.pref_speed_key))) {
            loadFromPrefs(sharedPreferences);

        } else if (key.equals(getString(R.string.pref_duration_key))) {
            loadFromPrefs(sharedPreferences);

        } else if (key.equals(getString(R.string.pref_radius_key))) {
            loadFromPrefs(sharedPreferences);
        } else if (key.equals(getString(R.string.pref_depth_key))) {
            loadFromPrefs(sharedPreferences);
        } else if (key.equals(getString(R.string.pref_altitude_key))) {
            loadFromPrefs(sharedPreferences);
        }

        String zControlValue = sharedPreferences.getString(getString(R.string.pref_show_depth_key), "zControl");
        if (zControlValue.equals("Depth")) {
            setShowDepth(true);
        } else
            setShowDepth(false);

    }

    @UiThread
    public void init() {

        teleOperationButton.setOnClickListener(v -> {
            if (imc.selectedVehicle == null) {
                warning();
            } else {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder
                        .setMessage("Connect to " + imc.selectedVehicle + "?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", (dialog, id) -> {
                            if (teleOperation == null)
                                teleOperation = new TeleOperation();
                            teleOperation.setImc(imc);
                            Joystick joystick = findViewById(R.id.joystick);
                            joystick.setOnJoystickMovedListener(teleOperation);
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.activity_maps, teleOperation).addToBackStack("tag").commit();
                            dive.setVisibility(View.INVISIBLE);
                            teleOperationButton.setVisibility(View.INVISIBLE);
                            startPlan.setVisibility(View.INVISIBLE);
                            comeNear.setVisibility(View.INVISIBLE);
                            serviceBar.setVisibility(View.INVISIBLE);
                            keepStation.setVisibility(View.INVISIBLE);
                            accelerate.setVisibility(View.VISIBLE);
                            txt2.setVisibility(View.VISIBLE);
                            txt4.setVisibility(View.VISIBLE);
                            txt5.setVisibility(View.VISIBLE);
                            accelerate.setOnAccelerate(teleOperation);
                            decelerate.setVisibility(View.VISIBLE);
                            decelerate.setOnDec(teleOperation);
                            stopPlan.setVisibility(View.INVISIBLE);
                            StopTeleop stopTeleop = findViewById(R.id.stopTeleop);
                            stopTeleop.setVisibility(View.VISIBLE);
                            stopTeleop.setOnStop(teleOperation);
                            joystick.setVisibility(View.VISIBLE);
                            joyLeft.setVisibility(View.VISIBLE);

                            joyRight.setVisibility(View.VISIBLE);

                            PlanControl pc = new PlanControl();
                            Teleoperation teleoperationMsg = new Teleoperation();
                            teleoperationMsg.setCustom("src=" + imc.getLocalId());
                            pc.setArg(teleoperationMsg);
                            pc.setType(PlanControl.TYPE.REQUEST);
                            pc.setOp(PlanControl.OP.START);
                            pc.setFlags(0);
                            pc.setRequestId(0);
                            pc.setPlanId("SpearTeleoperation-" + imc.selectedVehicle);
                            imc.sendMessage(pc);

                        })
                        .setNegativeButton("No", (dialog, id) -> dialog.cancel());
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }

        });


        startPlan.setOnClickListener(v -> {
            if (imc.selectedVehicle == null) {
                warning();
            } else {
                requestPlans();
            }
        });


        dive.setOnClickListener(v -> {
            if (imc.selectedVehicle == null) {
                warning();
            } else {
                dive();
            }
        });

        comeNear.setOnClickListener(v -> {
            if (imc.selectedVehicle == null) {
                warning();
            } else {
                near();
            }
        });

        stopPlan.setOnClickListener(v -> {
            if (imc.selectedVehicle == null) {
                warning();
            } else {
                stopPlan();
                otherVehiclesPositionList.clear();
            }
        });

        keepStation.setOnClickListener(v -> {
            if (imc.selectedVehicle == null) {
                warning();
            } else {
                keepStation();
            }
        });


    }

    private void requestForSpecificPermission() {
        int PERMISSION_ALL = 101;
        String[] PERMISSIONS = {
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.ACCESS_WIFI_STATE,
                android.Manifest.permission.VIBRATE,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.INTERNET};

        hasPermissions(this, PERMISSIONS);
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
    }

    public void hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                ActivityCompat.checkSelfPermission(context, permission);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        boolean result_permission = true;
        for (int grantResult : grantResults) {
            if (grantResult == -1)
                result_permission = false;
        }
        if (result_permission) {

            checkConnections();
        }
    }

    @Override
    public void onBackPressed() {
        if (teleOperation != null) {
            if (TeleOperation.isTeleOpSelected) {


                teleOperation.finishTeleOp();
                getFragmentManager().popBackStack();
                dive.setVisibility(View.VISIBLE);
                teleOperationButton.setVisibility(View.VISIBLE);
                startPlan.setVisibility(View.VISIBLE);
                comeNear.setVisibility(View.VISIBLE);
                keepStation.setVisibility(View.VISIBLE);
                stopPlan.setVisibility(View.VISIBLE);
                accelerate.setVisibility(View.INVISIBLE);
                decelerate.setVisibility(View.INVISIBLE);
                stopTeleop.setVisibility(View.INVISIBLE);
                txt2.setVisibility(View.INVISIBLE);
                txt4.setVisibility(View.INVISIBLE);
                txt5.setVisibility(View.INVISIBLE);
                Joystick joystick = findViewById(R.id.joystick);
                serviceBar.setVisibility(View.VISIBLE);

                joystick.setVisibility(View.INVISIBLE);
                joyRight.setVisibility(View.INVISIBLE);
                joyLeft.setVisibility(View.INVISIBLE);
                teleOperation = null;
                TeleOperation.isTeleOpSelected = false;
            } else {

                getFragmentManager().popBackStack();
                dive.setVisibility(View.VISIBLE);
                serviceBar.setVisibility(View.VISIBLE);
                teleOperationButton.setVisibility(View.VISIBLE);
                startPlan.setVisibility(View.VISIBLE);
                comeNear.setVisibility(View.VISIBLE);
                keepStation.setVisibility(View.VISIBLE);
                stopPlan.setVisibility(View.VISIBLE);
                accelerate.setVisibility(View.INVISIBLE);
                decelerate.setVisibility(View.INVISIBLE);
                stopTeleop.setVisibility(View.INVISIBLE);
                txt4.setVisibility(View.INVISIBLE);
                txt2.setVisibility(View.INVISIBLE);
                txt5.setVisibility(View.INVISIBLE);
                Joystick joystick = findViewById(R.id.joystick);
                joystick.setVisibility(View.INVISIBLE);
                joyRight.setVisibility(View.INVISIBLE);
                joyLeft.setVisibility(View.INVISIBLE);
                teleOperation = null;

            }


        } else if (sendSms != null) {
            map.setMultiTouchControls(true);
        } else {
            Intent setIntent = new Intent(Intent.ACTION_MAIN);

            setIntent.addCategory(Intent.CATEGORY_HOME);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(setIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void showAIS() {
        if (isAISSelected) {
            if (countAisTime >= 5) {

                AISPlot.SystemInfoAIS mAIS = ais.GetDataAIS();
                if (mAIS.systemSizeAIS > 0) {
                    for (int i = 0; i < mAIS.systemSizeAIS; i++) {
                        if (((System.currentTimeMillis() / 1000L) - (mAIS.lastUpdateAisShip.get(i) / 1000L)) < 3600) {
                            systemPosAIS.setCoords(mAIS.shipLocation.get(i).getLatitude(), mAIS.shipLocation.get(i).getLongitude());
                            startMarkerAIS[i].remove(map);

                            startMarkerAIS[i].setPosition(systemPosAIS);
                            startMarkerAIS[i].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            startMarkerAIS[i].setIcon(getResources().getDrawable(R.drawable.ship_icon));
                            startMarkerAIS[i].setTitle(mAIS.shipName.get(i) + "\n" + ais.parseTime(mAIS.lastUpdateAisShip.get(i)) + "\nHeading: " + mAIS.headingAisShip.get(i) + " | Speed: " + mAIS.speedAisShip.get(i) + " m/s" + '\n' + gpsConvert.latLonToDM(mAIS.shipLocation.get(i).getLatitude(), mAIS.shipLocation.get(i).getLongitude()));
                        }
                        map.getOverlays().add(startMarkerAIS[i]);

                    }
                }

                countAisTime = -1;
            } else {

                if (ais.GetNumberShipsAIS() != 0) {

                    for (int i = 0; i < ais.GetNumberShipsAIS(); i++) {
                        map.getOverlays().add(startMarkerAIS[i]);

                    }
                }
            }

            countAisTime++;
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT <= 19) {
            checkConnections();
        } else {
            requestForSpecificPermission();
        }

        if (isNetworkAvailable()) {
            map.setTileSource(TileSourceFactory.MAPNIK);
            isOfflineSelected = false;


        } else

        {

            isOfflineSelected = true;
        }


        android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
        ais = new AISPlot(ctx);
        customHandlerAIS = new Handler();
        customHandlerAIS.postDelayed(updateTimerThreadAIS, 2000);

        customHandlerRipples = new Handler();
        customHandlerRipples.postDelayed(updateTimerThreadRipples, 100);

        planWaypointPolyline = new Polyline();

        startMarkerRipples = new Marker[2048];
        for (int i = 0; i < 2048; i++)
            startMarkerRipples[i] = new Marker(map);


        startMarkerAIS = new Marker[10024];
        for (int i = 0; i < 10024; i++)
            startMarkerAIS[i] = new Marker(map);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);

        resources = getResources();
        context = this;
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        orientationOtherVehicles = new ArrayList<>();

        map.setMultiTouchControls(true);
        map.setClickable(true);


        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), map);
        myLocationOverlay.enableMyLocation();

        map.getOverlays().add(this.myLocationOverlay);


        mCompassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context), map);
        mCompassOverlay.enableCompass();
        map.getOverlays().add(mCompassOverlay);
        mainTV.setVisibility(View.VISIBLE);
        txtmap.bringToFront();

        setupSharedPreferences();

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        init();


        accelerate.setVisibility(View.INVISIBLE);

        decelerate.setVisibility(View.INVISIBLE);
        Joystick joystick = findViewById(R.id.joystick);
        joystick.setVisibility(View.INVISIBLE);
        joyRight.setVisibility(View.INVISIBLE);
        joyLeft.setVisibility(View.INVISIBLE);
        noWifiImage.setVisibility(View.INVISIBLE);

        txt2.setVisibility(View.INVISIBLE);
        txt4.setVisibility(View.INVISIBLE);
        txt5.setVisibility(View.INVISIBLE);

        stopTeleop.setVisibility(View.INVISIBLE);

        imc.register(this);
        minus.setOnClickListener(v -> mapController.zoomOut());
        plus.setOnClickListener(v -> mapController.zoomIn());
        unlock.setVisibility(View.INVISIBLE);


        if (android.os.Build.VERSION.SDK_INT >= M) {
            checkLocationPermission();

        }
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            appLocationService = new AppLocationService(MainActivity.this);
            gpsLocation = appLocationService.getLocation(LocationManager.GPS_PROVIDER);

            nwLocation = appLocationService
                    .getLocation(LocationManager.NETWORK_PROVIDER);

        } catch (Exception ignored) {
        }



        /* location manager */
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (location == null) {
            location = new Location(LocationManager.GPS_PROVIDER);
        }
        myLocationOverlay.enableMyLocation();
        map.getOverlays().add(this.myLocationOverlay);

        GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(getBaseContext());
        gpsMyLocationProvider.setLocationUpdateMinDistance(100); // [m]  // Set the minimum distance for location updates
        gpsMyLocationProvider.setLocationUpdateMinTime(10000);   // [ms] // Set the minimum time interval for location updates
        myLocationOverlay = new MyLocationNewOverlay(map);
        myLocationOverlay.setDrawAccuracyEnabled(true);


        map.invalidate();
        mapController = map.getController();
        mapController.setZoom(12);
        zoomLevel = 12;
        mapController.setCenter(new GeoPoint(location));


        scaleBarOverlay = new ScaleBarOverlay(map);
        List<Overlay> overlays = map.getOverlays();

        overlays.add(scaleBarOverlay);


        ReceiveSms.bindListener(new SmsListener() {
            @Override
            public void messageReceived(String messageText) {
                Pattern p = Pattern.compile(getString(R.string.patternMessageReceived));


                Matcher matcher = p.matcher(messageText);
                if (!matcher.matches()) {
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "SMS message not understood: " + messageText);

                    return;
                }

                String type = matcher.group(1);
                String vehicle = matcher.group(2);
                String timeOfDay = matcher.group(3);
                String latMins = matcher.group(4);
                String lonMins = matcher.group(5);
                GregorianCalendar date = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                String[] timeParts = timeOfDay.split(":");
                date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
                date.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
                date.set(Calendar.SECOND, Integer.parseInt(timeParts[2]));
                String latParts[] = latMins.split(" ");
                String lonParts[] = lonMins.split(" ");

                double lat = Double.parseDouble(latParts[0]);
                lat += (lat > 0) ? Double.parseDouble(latParts[1]) / 60.0 : -Double.parseDouble(latParts[1]) / 60.0;
                double lon = Double.parseDouble(lonParts[0]);
                lon += (lon > 0) ? Double.parseDouble(lonParts[1]) / 60.0 : -Double.parseDouble(lonParts[1]) / 60.0;


                GeoPoint coordSMS = new GeoPoint(lat, lon);

                System.out.println(coordSMS + " coordinates from sms");

                mapController.setCenter(coordSMS);
                mapController.setZoom(12);
                zoomLevel = 12;
                markerSMS = new Marker(map);
                markerSMS.setPosition(coordSMS);
                if (previousVehicle != null) {
                    if (vehicle.equals(previousVehicle))
                        markerSMS.setIcon(nodeIcon);
                    else
                        markerSMS.setIcon(areaIcon);
                } else
                    markerSMS.setIcon(nodeIcon);

                markerSMS.setTitle(vehicle + "\n" + "Lat: " + lat + '\n' + "Lon: " + lon + "\n" + "Hour: " + timeParts[0] + ":" + timeParts[1] + ":" + timeParts[2]);
                map.getOverlays().add(markerSMS);
                markerListSMS.add(markerSMS);

                previousVehicle = vehicle;


            }

        });


    }

    @Override
    protected void onDestroy() {
        this.mWakeLock.release();
        super.onDestroy();
        // Unregister MainActv as an OnPreferenceChangedListener to avoid any memory leaks.
        android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);

        imc.stop();
    }

    @Background
    @Consume
    public void receive(final EstimatedState state) {
        synchronized (estates) {
            estates.put(state.getSourceName(), state);

        }
    }

    @Background
    @Consume
    public void receiveRPM(final Rpm rpmValue) {
        synchronized (rpmValues) {
            rpmValues.put(rpmValue.getSourceName(), rpmValue);

        }
    }

    @Background
    @Override
    public void onLocationChanged(Location location) {


        if (gpsLocation != null) {
            latitudeAndroid = Math.toRadians(gpsLocation.getLatitude());
            longitudeAndroid = Math.toRadians(gpsLocation.getLongitude());
            myPosition = new GeoPoint(gpsLocation.getLatitude(), gpsLocation.getLongitude());

        } else if (nwLocation != null) {
            latitudeAndroid = Math.toRadians(nwLocation.getLatitude());
            longitudeAndroid = Math.toRadians(nwLocation.getLongitude());
            myPosition = new GeoPoint(nwLocation.getLatitude(), nwLocation.getLongitude());

        }


        final ArrayList<OverlayItem> items = new ArrayList<>();
        OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", myPosition);
        marker.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
        items.add(marker);
        Bitmap newMarker;
        if (android.os.Build.VERSION.SDK_INT <= M) {

            newMarker = Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.ico_ccu));

        } else {

            newMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.ico_ccu), 50, 50, true);

        }


        Drawable marker3 = new BitmapDrawable(getResources(), newMarker);
        ItemizedIconOverlay markersOverlay = new ItemizedIconOverlay<>(items, marker3, null, context);
        map.getOverlays().add(markersOverlay);

        MapEventsOverlay OverlayEventos = new MapEventsOverlay(this.getBaseContext(), mReceive);


        map.getOverlays().add(OverlayEventos);
        //Refreshing the map to draw the new overlay

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            try {
                startActivity(new Intent(this, SettingsActivity.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;


        } else if (id == R.id.ais) {


            if (!isAISSelected) {
                ais = new AISPlot(this.context);
                systemPosAIS = new GeoPoint(0, 0);
                ais.getAISInfo();
                isAISSelected = true;
            } else {
                isAISSelected = false;

            }


        } else if (id == R.id.sysinter) {
            try {
                Intent i = new Intent(this, SysInteractions.class);
                i.putExtra("selected", imc.selectedVehicle);
                startActivity(i);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (id == R.id.ripples) {

            if (!isRipplesSelected) {
                ripples = new RipplesPosition(UrlRipples);
                systemPosRipples = new GeoPoint(0, 0);
                isRipplesSelected = true;
            } else
                isRipplesSelected = false;


        } else if (id == R.id.followme) {

            followme();
        } else if (id == R.id.error) {
            try {
                startActivity(new Intent(this, Errors.class));
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else if (id == R.id.downloadTiles) {
            if (!isOfflineSelected) {
                CacheManager cm = new CacheManager(map);

                BoundingBox bbox = map.getBoundingBox();
                cm.downloadAreaAsync(this, bbox, map.getZoomLevel() - 3, map.getZoomLevel() + 1);


            } else {
                Toast.makeText(this, "Go to Settings -> Online mode", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Background
    public void getRpmValue(final Rpm rpmValue) {
        if (imc.stillConnected() != null) {

            if (imc.selectedVehicle != null) {
                if ((imc.stillConnected().contains(rpmValue.getSourceName())))
                    if (imc.getSelectedvehicle().equals(rpmValue.getSourceName()))
                        vehicleRpm = rpmValue.getValue();
            }
        }
    }


    public void calculateDistance() {

        final int R = 6371; // Radius of the earth
        double latDistance = latVehicle - (latitudeAndroid);
        double lonDistance = lonVehicle - (longitudeAndroid);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos((latitudeAndroid)) * Math.cos(latVehicle)
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        DecimalFormat twoDForm = new DecimalFormat("#.##");
        distFinal = (twoDForm.format(distance));

    }

    @SuppressLint("SetTextI18n")
    @Background
    public void paintState(final EstimatedState state) {
        final String vname = state.getSourceName();
        if (imc.stillConnected() != null) {

            if (imc.selectedVehicle != null) {
                if (!(imc.stillConnected().contains(imc.selectedVehicle)))
                    runOnUiThread(() -> {
                        serviceBar.setText(" Lost Connection");

                        txtmap.setVisibility(View.INVISIBLE);
                        //retirar icon
                        selectedVehiclePosition = null;


                    });
            }
            if (imc.stillConnected().contains(vname)) {
                double[] lld = WGS84Utilities.toLatLonDepth(state);
                final ArrayList<OverlayItem> items2 = new ArrayList<>();
                if (!vname.equals(imc.getSelectedvehicle()))
                    otherVehiclesPositionList.add(new GeoPoint(lld[0], lld[1]));
                OverlayItem marker2 = new OverlayItem("markerTitle", "markerDescription", new GeoPoint((lld[0]), (lld[1])));
                marker2.setMarkerHotspot(HotspotPlace.TOP_CENTER);
                items2.add(marker2);

                vehicleOrientation = (float) state.getPsi();

                int ori2 = (int) Math.toDegrees(vehicleOrientation);
                ori2 = ori2 - 180;
                if (!orientationOtherVehicles.contains(ori2))
                    orientationOtherVehicles.add(ori2);


                if (context == MainActivity.this) {

                    if (android.os.Build.VERSION.SDK_INT <= M) {
                        bitmapArrow = Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.downarrow2));

                    } else {

                        bitmapArrow = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.downarrow2), 50, 50, false);

                    }
                }
                if (vname.equals(imc.getSelectedvehicle())) {


                    selectedVehiclePosition = new GeoPoint(lld[0], lld[1]);
                    selectedVehicleOrientation = (float) state.getPsi();
                    int ori = (int) (Math.toDegrees(selectedVehicleOrientation));
                    ori = ori - 180;
                    orientationSelected = ori;
                    if (selectedVehiclePosition != null) {
                        latVehicle = Math.toRadians(selectedVehiclePosition.getLatitude());
                        lonVehicle = Math.toRadians(selectedVehiclePosition.getLongitude());
                    }
                    if (context == MainActivity.this) {
                        runOnUiThread(() -> {

                            // Stuff that updates the UI
                            serviceBar.setText(imc.getSelectedvehicle());


                        });


                        if (android.os.Build.VERSION.SDK_INT <= M) {
                            bitmapArrow = Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrowgreen2));

                        } else {

                            bitmapArrow = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrowgreen2), 50, 50, false);

                        }
                    }

                    DecimalFormat df2 = new DecimalFormat("#.##");
                    velocityString = df2.format(Math.sqrt((state.getVx() * state.getVx()) + (state.getVy() * state.getVy()) + (state.getVz() * state.getVz())));
                    calculateDistance();


                    if (planBeingExecuted == null) {
                        planExecuting = "";
                    } else {
                        planExecuting = planBeingExecuted;
                        String[] getPlanName = planExecuting.split("-");
                        planExecuting = getPlanName[0];
                    }


                    if (teleOperation == null) {
                        runOnUiThread(() -> {

                            txtmap.setVisibility(View.INVISIBLE);
                            mainTV.setVisibility(View.VISIBLE);
                            mainTV.setText(getString(R.string.speedstring) + " " + velocityString + " " + getString(R.string.meterspersecond) + "\n" + " RPM: " + vehicleRpm + "\n" + connectedState + " \n" + planExecuting + "Dist: " + distFinal + " m");
                            if (errorsList != null) {
                                mainTV.setText(getString(R.string.speedstring) + " " + velocityString + " " + getString(R.string.meterspersecond) + "\n" + " RPM: " + vehicleRpm + "\n" + connectedState + "\n" + errorsList.toString().replace(",", "\n").replace("[", "").replace("]", "") + "\n" + planExecuting + "\n" + "Dist: " + distFinal + " m");

                            }
                        });


                    } else {
                        runOnUiThread(() -> {
                            mainTV.setVisibility(View.INVISIBLE);
                            txtmap.setVisibility(View.VISIBLE);
                            txtmap.setText(getString(R.string.speedstring) + " " + velocityString + " " + getString(R.string.meterspersecond) + " RPM: " + vehicleRpm + " " + connectedState + "Dist: " + distFinal + " m");
                        });

                    }

                }

                Bitmap target = RotateMyBitmap(bitmapArrow, ori2);

                Drawable marker_ = new BitmapDrawable(resources, target);
                markersOverlay2 = new ItemizedIconOverlay<>(items2, marker_, null, context);

                map.getOverlays().add(markersOverlay2);

            }
        }

    }

    @Background
    @Periodic
    public void updateState() {
        stateList = new ArrayList<>();
        errorsList = new ArrayList<>();
        allErrorsList = new HashMap<>();

        vehicleStateList = imc.connectedVehicles();
        List<String> errorsEnts = new ArrayList<>();


        for (VehicleState state : vehicleStateList) {
            if (imc.selectedVehicle != null)
                if (imc.selectedVehicle.equals(state.getSourceName())) {
                    stateList.add(state.getOpModeStr());
                    errorsList.add(state.getErrorEnts());
                }

            errorsEnts.add(state.getErrorEnts());
            allErrorsList.put(state.getSourceName(), errorsEnts);
        }
        if (stateList.size() != 0) {
            for (int i = 0; i < stateList.size(); i++) {
                connectedState = stateList.toString();

                if (!isStopPressed) {
                    //Se o veiculo entrar em service mode sem ser por parar o plano
                    if ((previous != null) && !hasEnteredServiceMode && connectedState.charAt(1) == 'S') {
                        hasEnteredServiceMode = true;
                        followMeOn = false;

                        previous = null;
                        areNewWaypointsFromAreaUpdated = false;
                        isPolylineDrawn = false;
                        otherVehiclesPositionList.clear();
                        wasPlanChanged = false;
                        cleanMap();


                    }


                }


            }
        }
    }


    //Run task periodically - garbage collection

    @SuppressLint("ClickableViewAccessibility")
    @Background
    @Periodic(500)
    public void updateMap() {
        System.gc();
        otherVehiclesPositionList.clear();
        map.getOverlays().remove(mCompassOverlay);
        map.getOverlays().clear();
        onLocationChanged(location);

        if (context == MainActivity.this) {
            drawCompass();
        }
        synchronized (estates) {
            for (EstimatedState state : estates.values()) {
                paintState(state);
            }

        }


        synchronized (rpmValues) {
            for (Rpm rpmValue : rpmValues.values()) {
                getRpmValue(rpmValue);
            }

        }



        if (detach) {

            if (!myPosSelected) {
                if (imc.selectedVehicle != null)
                    mapController.setCenter(selectedVehiclePosition);
            } else {
                if (localizacao() != null) {
                    mapController.setCenter(localizacao());

                }
            }

            map.setOnTouchListener((y, event) -> {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // if you want to fire another event
                }

                // Is detached mode is active all other touch handler
                // should not be invoked, so just return true
                return true;


            });


            unlock.setOnClickListener(v -> {
                detach = false;
                map.setOnTouchListener((y, event) -> {
                    y.clearFocus();
                    y.setFocusable(true);
                    y.setClickable(true);

                    return false;
                });
                unlock.setVisibility(View.INVISIBLE);
            });

        }
        if (isAISSelected)
            showAIS();
        if (isRipplesSelected)
            showRipplesPos();


        if (pointsLine.size() > 1) {

            for (int i = 0; i < pointsLine.size(); i++) {
                markerFromLine = new Marker(map);
                if (pointsLine.size() > 1) {

                    markerFromLine.setPosition(pointsLine.get(i));
                    markerFromLine.setIcon(lineIcon);

                    map.getOverlays().add(markerFromLine);
                }
            }


        }
        if (pointsLine.size() == 1) {


            markerFromLine = new Marker(map);
            if (pointsLine.size() == 1) {
                markerFromLine.setPosition(pointsLine.get(0));
                markerFromLine.setIcon(lineIcon);
                map.getOverlays().add(markerFromLine);
            }
        }

        if (Line.getPoly()) {
            if (pointsLine.size() > 1) {

                planWaypointPolyline.setPoints(pointsLine);
                map.getOverlays().add(planWaypointPolyline);
                isPolylineDrawn = true;

            }
        }
        if (followMeOn)
            afterChoice();
        if (circle2 != null) {
            map.getOverlays().add(circle2);
            map.getOverlayManager().add(circle2);
        }
        if (!isStopPressed && !hasEnteredServiceMode) {

            if (planWaypoints.size() != 0) {
                for (int i = 0; i < planWaypoints.size(); i++) {
                    pointsFromPlan = new Marker(map);
                    if (planWaypoints.size() != 0) {
                        pointsFromPlan.setPosition(planWaypoints.get(i));
                        pointsFromPlan.setIcon(areaIcon);
                        map.getOverlays().add(pointsFromPlan);
                    }
                    if (planWaypointPolyline != null)
                        map.getOverlays().add(planWaypointPolyline);
                }


            }
        }

        if (!markerListSMS.isEmpty()) {
            for (int i = 0; i < markerListSMS.size(); i++)
                map.getOverlays().add(markerListSMS.get(i));
        }
        if (!areNewWaypointsFromAreaUpdated) {
            if (connectedState != null && connectedState.charAt(1) != 'S')
                if (Area.sendmList() != null) {

                    updateWaypoints();
                }

        }
    }

    public void drawCompass() {
        if (mCompassOverlay != null) {
            map.getOverlays().add(mCompassOverlay);
            orientationCompass = mCompassOverlay.getOrientation();


        } else {
            mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), map);
            mCompassOverlay.enableCompass();

            map.getOverlays().add(mCompassOverlay);
            orientationCompass = mCompassOverlay.getOrientation();


        }

        if (scaleBarOverlay != null) {

            scaleBarOverlay.setAlignRight(true);
            scaleBarOverlay.setScaleBarOffset(40, 30);
            map.getOverlays().add(scaleBarOverlay);
        } else {

            scaleBarOverlay = new ScaleBarOverlay(map);
            scaleBarOverlay.setAlignRight(true);
            scaleBarOverlay.setScaleBarOffset(40, 30);
            map.getOverlays().add(scaleBarOverlay);

        }


        if (SendSms.pontoSMS() != null) {
            GeoPoint ponto = SendSms.pontoSMS();


            markerSMS = new Marker(map);
            markerSMS.setPosition(ponto);
            markerSMS.setIcon(areaIcon);
            map.getOverlays().add(markerSMS);

        }


    }

    public void dive() {
        Loiter dive = new Loiter();
        dive.setLon(lonVehicle);
        dive.setLat(latVehicle);
        dive.setZ(depth);
        dive.setType(Loiter.TYPE.CIRCULAR);
        if (isDepthSelected) {
            dive.setZ(depth);
            dive.setZUnits(ZUnits.DEPTH);
        } else {
            dive.setZ(altitude);
            dive.setZUnits(ZUnits.ALTITUDE);
        }
        dive.setSpeed(speed);

        if (!isRPMSelected) {
            dive.setSpeedUnits(SpeedUnits.METERS_PS);
        } else {
            dive.setSpeedUnits(SpeedUnits.RPM);
        }
        dive.setRadius(radius);
        dive.setDuration(duration);
        dive.setBearing(0);
        String planid = "SpearDive-" + imc.selectedVehicle;
        startBehaviour(planid, dive);
    }

    public void keepStation() {
        StationKeeping stationKeepingmsg = new StationKeeping();
        stationKeepingmsg.setLat(latVehicle);
        stationKeepingmsg.setLon(lonVehicle);
        stationKeepingmsg.setSpeed(speed);
        if (!isRPMSelected) {
            stationKeepingmsg.setSpeedUnits(SpeedUnits.METERS_PS);
        } else {
            stationKeepingmsg.setSpeedUnits(SpeedUnits.RPM);
        }
        stationKeepingmsg.setDuration(duration);
        stationKeepingmsg.setRadius(radius);
        if (isDepthSelected) {
            stationKeepingmsg.setZ(depth);
            stationKeepingmsg.setZUnits(ZUnits.DEPTH);
        } else {
            stationKeepingmsg.setZ(altitude);
            stationKeepingmsg.setZUnits(ZUnits.ALTITUDE);
        }
        String planid = " SpearStationKeeping-" + imc.selectedVehicle;
        startBehaviour(planid, stationKeepingmsg);


    }

    public void followme() {
        if (imc.selectedVehicle == null) {
            Toast.makeText(this, "Select a vehicle first", Toast.LENGTH_SHORT).show();

        } else {
            FollowReference go = new FollowReference();
            go.setAltitudeInterval(1);
            go.setControlSrc(imc.getLocalId());
            go.setLoiterRadius(0);
            go.setTimeout(30);

            String planid = "SpearFollowMe-" + imc.selectedVehicle;
            followMeOn = true;
            startReference();

            startBehaviour(planid, go);
        }

    }

    public void near() {


        StationKeeping comeNear = new StationKeeping();


        comeNear.setLat(latitudeAndroid);
        comeNear.setLon(longitudeAndroid);

        comeNear.setSpeed(speed);
        if (!isRPMSelected) {
            comeNear.setSpeedUnits(SpeedUnits.METERS_PS);
        } else {
            comeNear.setSpeedUnits(SpeedUnits.RPM);
        }
        comeNear.setDuration(duration);
        comeNear.setRadius(radius);
        if (isDepthSelected) {
            comeNear.setZ(depth);
            comeNear.setZUnits(ZUnits.DEPTH);
        } else {
            comeNear.setZ(altitude);
            comeNear.setZUnits(ZUnits.ALTITUDE);
        }


        String planid = "SpearComeNear-" + imc.selectedVehicle;
        startBehaviour(planid, comeNear);


    }

    public void startReference() {
        if (followMeOn) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder
                    .setMessage("North or east?")
                    .setCancelable(true)
                    .setPositiveButton("North", (dialog, id) -> {
                        n = 0.6;
                        e = 0;
                        afterChoice();

                    })
                    .setNegativeButton("East", (dialog, id) -> {
                        e = 1;
                        n = 0;
                        afterChoice();


                        dialog.cancel();
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }


    }

    public void afterChoice() {
        if (followMeOn) {
            Reference ref = new Reference();
            double[] latlonDisplace = WGS84displace(latitudeAndroid, longitudeAndroid, depth, n, e, 0);

            ref.setLat(latlonDisplace[0]);
            ref.setLon(latlonDisplace[1]);
            geo = new GeoPoint(Math.toDegrees(latlonDisplace[0]), Math.toDegrees(latlonDisplace[1]));


            DesiredSpeed ds = new DesiredSpeed();
            if (!isRPMSelected) {
                ds.setSpeedUnits(SpeedUnits.METERS_PS);
            } else {
                ds.setSpeedUnits(SpeedUnits.RPM);
            }
            ds.setValue(speed);
            ref.setSpeed(ds);
            DesiredZ dz = new DesiredZ();
            dz.setValue(0);
            if (isDepthSelected) {
                dz.setZUnits(ZUnits.DEPTH);
            } else {
                dz.setZUnits(ZUnits.ALTITUDE);
            }
            ref.setZ(dz);

            imc.sendMessage(ref);


        }


    }


    public void stopPlan() {
        PlanControl pc = new PlanControl();
        pc.setType(PlanControl.TYPE.REQUEST);
        pc.setOp(PlanControl.OP.STOP);
        pc.setRequestId(1);
        pc.setFlags(0);
        pc.setPlanId("stopPlan-" + imc.selectedVehicle);
        imc.sendMessage(pc);

        MainActivity.previous = "S";
        hasEnteredServiceMode = true;
        isStopPressed = true;
        areNewWaypointsFromAreaUpdated = false;
        isPolylineDrawn = false;
        otherVehiclesPositionList.clear();
        wasPlanChanged = false;


        followMeOn = false;

        cleanMap();


    }

    public void cleanMap() {
        if (planWaypointPolyline != null) {
            planWaypointPolyline.setPoints(nullArray);
            map.getOverlays().remove(planWaypointPolyline);
        }
        connectedState = null;


        if (planWaypoints != null) {
            planWaypoints.clear();
            if (pointsLine != null)
                pointsLine.clear();
        }

        if (waypointsFromPlan != null) {
            waypointsFromPlan.clear();
        }


        if (Area.getPointsArea() != null) {
            Area.getPointsArea().clear();
        }


        if (pointsFromPlan != null) {
            pointsFromPlan.remove(map);
            map.getOverlays().remove(pointsFromPlan);
        }

        if (maneuverList != null) {
            maneuverList.clear();

        }


        if (Line.getPoly()) {
            isPolylineDrawn = false;

            nullArray.clear();

        }


        if (Line.getPointsLine() != null) {
            Line.getPointsLine().clear();
            pointsLine.clear();
        }


        if (Area.sendmList() != null) {

            if (Area.maneuverArrayList != null)
                Area.maneuverArrayList.clear();


            Area.sendmList().clear();
        }


        if (Line.sendmList() != null) {
            Line.lineListManeuvers.clear();
            Line.sendmList().clear();
        }


        if (SendSms.pontoSMS() != null) {
            map.getOverlays().remove(markerSMS);


        }
        runOnUiThread(() -> {
            map.invalidate();

        });
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    //if there is wifiDrawable show an imageview, when the wifiDrawable is off change it to another
    private void checkConnections() {
        final boolean[] connectedWifi = {false};

        if (isWifiAvailable() || isNetworkAvailable()) {
            new CountDownTimer(10000, 1000) {
                public void onFinish() {
                    // When timer is finished
                    // Execute your code here
                    connectedWifi[0] = true;

                }

                public void onTick(long millisUntilFinished) {
                }
            }.start();
        } else {
            connectedWifi[0] = false;
        }
    }

    private boolean isWifiAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifi = null;
        if (cm != null) {
            wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        }
        return wifi != null && wifi.isConnected();

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    //if there is wifiDrawable show an imageview, when the wifiDrawable is off change it to another


    //Set a timer to check if is connected to a Wifi Network
    @Periodic(5000)
    public void drawWifiSignal() {
        // Check if is connected to a Wifi Network, if not popups a informative toast
        runOnUiThread(() -> {
            if (!isWifiAvailable()) {
                if (serviceBar != null) {
                    wifiDrawable.setVisibility(View.INVISIBLE);
                    noWifiImage.setVisibility(View.VISIBLE);

                }
            } else {
                if (serviceBar != null) {
                    noWifiImage.setVisibility(View.INVISIBLE);
                    wifiDrawable.setVisibility(View.VISIBLE);

                }
            }
        });
    }


    public void requestPlans() {
        if (imc.selectedVehicle == null) {
            warning();

        } else {
            PlanDB pdb = new PlanDB();
            pdb.setOp(PlanDB.OP.GET_INFO);
            imc.sendMessage(pdb);
            registerForContextMenu(startPlan);
            openContextMenu(startPlan);
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.startplan) {

            planList = new ArrayList<>();

            if (imc.allPlans() == null) {
                Toast.makeText(this, "No plans available", Toast.LENGTH_SHORT).show();
                return;
            }
            for (int i = 0; i < imc.allPlans().size(); i++) {
                planList.addAll(imc.allPlans());

                menu.add(i, i, i, planList.get(i));
                menu.setHeaderTitle("Plan List");


            }

            listSize = planList.size();

        } else if (v.getId() == R.id.servicebar) {

            if (teleOperation == null) {
                vehicleList = new ArrayList<>();
                maneuverList = new ArrayList<>();


                vehicleStateList = imc.connectedVehicles();
                if (imc.connectedVehicles() == null)
                    warning();
                for (VehicleState state : vehicleStateList) {
                    vehicleList.add(state.getSourceName() + ":" + state.getOpMode());


                }
                for (int i = 0; i < vehicleList.size(); i++) {

                    String connectedvehicles = vehicleList.toString();
                    String[] getName = connectedvehicles.split(",");
                    getName[0] = getName[0].substring(1);
                    getName[getName.length - 1] = getName[getName.length - 1].substring(0, getName[getName.length - 1].length() - 1);
                    String selectedName = getName[i];
                    menu.add(i, i, i, selectedName);


                }

            }
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!(item.toString().contains(":"))) {

            PlanControl pc = new PlanControl();
            pc.setType(PlanControl.TYPE.REQUEST);
            pc.setOp(PlanControl.OP.START);
            pc.setFlags(0);
            pc.setRequestId(0);
            pc.setPlanId(item.toString());
            imc.sendMessage(pc);
            wasPlanChanged = connectedState.charAt(1) != 'S';


            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (imc.allManeuvers() == null) {
                    Toast.makeText(MainActivity.this, "No plan specification available", Toast.LENGTH_SHORT).show();
                } else {
                    previous = "M";
                    isStopPressed = false;
                    hasEnteredServiceMode = false;
                    maneuverList.addAll(imc.allManeuvers());
                    DrawWaypoints.callWaypoint(maneuverList);

                }
            }, 3000);
        } else {
            String selected = item.toString();
            String[] getName2 = selected.split(":");
            String selectedName2 = getName2[0];
            imc.setSelectedvehicle(selectedName2.trim());
            previous = null;

            vehicleName = selectedName2;

        }
        if (teleOperation != null) {
            teleOperation.finishTeleOp();
        }

        return super.onContextItemSelected(item);
    }


    public void warning() {
        Toast.makeText(this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
    }
}


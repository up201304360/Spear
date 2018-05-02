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
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;
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
import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.Loiter;
import pt.lsts.imc.Maneuver;
import pt.lsts.imc.PlanControl;
import pt.lsts.imc.PlanDB;
import pt.lsts.imc.Reference;
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
import static com.example.nachito.spear.R.id.wifiImage;
import static pt.lsts.util.WGS84Utilities.WGS84displace;


@EActivity

public class MainActivity extends AppCompatActivity
        implements MapViewConstants, OnLocationChangedListener, LocationListener, SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {

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
    static String depthString;
    static String velocityString;
    //previous- when Spear starts it doesn't enter the updateState() method
    static String previous = null;
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
    static double lonVehicle;
    static boolean isStopPressed = false;
    static boolean areNewWaypointsFromAreaUpdated = false;
    static ArrayList<GeoPoint> planWaypoints = new ArrayList<>();
    static boolean hasEnteredServiceMode = false;
    static boolean wasPlanChanged = false;
    static String stateconnected;
    static int zoomLevel;
    static List<Integer> orientationOtherVehicles;
    static int orientationSelected;
    static float bearingMyLoc;
    static double altitude;
    static boolean isDepthSelected;
    static List<Maneuver> maneuverList;
    static Polyline planWaypointPolyline;
    final LinkedHashMap<String, EstimatedState> estates = new LinkedHashMap<>();
    @ViewById(R.id.dive)
    Button dive;
    @ViewById(R.id.minus)
    Button minus;
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
    @ViewById(R.id.textView2)
    TextView txt2;
    @ViewById(R.id.textView4)
    TextView txt4;
    @ViewById(R.id.textView5)
    TextView txt5;
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
    static  double latitudeAndroid;
    static double longitudeAndroid;
    @ViewById(R.id.velocity)
    TextView velocity;
    @ViewById(R.id.bottomsheet)
    LinearLayout bottom;
    Marker pointsFromPlan;
    Location location;
    List<String> vehicleList;
    List<String> planList;
    int listSize;
    Marker markerFromLine;
    OSMHandler updateHandler;
    List<String> stateList;
    SendSms sendSms;
    Marker markerSMS;
    ScaleBarOverlay scaleBarOverlay;
    @ViewById(R.id.location)
    Button centerLocation;
    ItemizedIconOverlay markersOverlay2;
    android.content.res.Resources resources;
    Polyline polyline = Line.getPolyline();
    ArrayList<GeoPoint> nullArray = new ArrayList<>();
    float selectedVehicleOrientation;
    private Context context;
    AISPlot ais;
    boolean isAISSelected;
    private Handler customHandlerAIS;
    private Marker startMarkerAIS[];
    private GeoPoint systemPosAIS;
    private int timeoutAISPull = 10;
    private int countAisTime = 0;
    GPSConvert gpsConvert = new GPSConvert();
    RipplesPosition ripples;
    private Handler customHandlerRipples;
    private Marker startMarkerRipples[];
    private String UrlRipples = "http://ripples.lsts.pt/api/v1/systems/active";
    private boolean newRipplesData = false;
    private RipplesPosition.SystemInfo systemInfo;
    private RipplesPosition.SystemInfo backSystemInfo;
    private GeoPoint systemPosRipples;
    private boolean firstRunRipplesPull = true;
    private int timeoutRipplesPull = 10;
boolean isRipplesSelected;
    private Handler customHandlerGarbagde;
    boolean comeNearOn;
    double n;
    double e;




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

        if (isWifiAvailable()) {
            map.setTileSource(TileSourceFactory.MAPNIK);
            isOfflineSelected = false;

            ripples = new RipplesPosition(this, UrlRipples);
            systemPosRipples = new GeoPoint(0,0);
            isRipplesSelected=true;

        } else

        {
          //  map.setTileSource(new XYTileSource("4uMaps", 0, 18, 256, ".png", new String[]{}));
            isOfflineSelected = true;
        }
        map.setTilesScaledToDpi(true);


        android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }

        customHandlerAIS = new Handler();
        customHandlerAIS.postDelayed(updateTimerThreadAIS, 2000);

        customHandlerRipples = new Handler();
        customHandlerRipples.postDelayed(updateTimerThreadRipples, 100);

            startMarkerRipples = new Marker[2048];
            for (int i = 0; i < 2048; i++)
                startMarkerRipples[i] = new Marker(map);

            startMarkerAIS = new Marker[10024];
            for (int i = 0; i < 10024; i++)
                startMarkerAIS[i] = new Marker(map);

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
        velocity.setVisibility(View.VISIBLE);
        velocity.bringToFront();

        setupSharedPreferences();

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        init();
        planWaypointPolyline = new Polyline();


        accelerate.setVisibility(View.INVISIBLE);

        decelerate.setVisibility(View.INVISIBLE);
        Joystick joystick = findViewById(R.id.joystick);
        joystick.setVisibility(View.INVISIBLE);
        noWifiImage.setVisibility(View.INVISIBLE);

        txt2.setVisibility(View.INVISIBLE);
        txt4.setVisibility(View.INVISIBLE);
        txt5.setVisibility(View.INVISIBLE);

        stopTeleop.setVisibility(View.INVISIBLE);

        imc.register(this);
        minus.setOnClickListener(v -> mapController.zoomOut());


        if (android.os.Build.VERSION.SDK_INT >= M) {
            checkLocationPermission();
        }
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        } catch (Exception ignored) {
        }

        /* location manager */
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        updateHandler = new OSMHandler(this);

        for (String provider : locationManager.getProviders(true)) {
            location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                locationManager.requestLocationUpdates(provider, 0, 0, updateHandler);
                break;
            }
        }
        if (location == null) {
            location = new Location(LocationManager.GPS_PROVIDER);
        }
        map.getOverlays().add(this.myLocationOverlay);
        myLocationOverlay.enableMyLocation();
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

                int source = IMCDefinition.getInstance().getResolver().resolve(vehicle);

                if (source == -1) {
                    System.err.println("Received report from unknown system name: " + vehicle);
                    return;
                }

                GeoPoint coordSMS = new GeoPoint(lat, lon);

                System.out.println(coordSMS + " coordinates from sms");

                mapController.setCenter(coordSMS);
                mapController.setZoom(12);
                zoomLevel = 12;
            }

        });
    }

    //if a plan is changed without stopping the plan that was executing
    @Periodic()
    public void changePlans() {
        if ((!isStopPressed && PlanList.planBeingExecuted != null && !PlanList.previousPlan.equals(".") && !PlanList.previousPlan.equals(PlanList.planBeingExecuted)) || (!isStopPressed && PlanList.planBeingExecuted != null && wasPlanChanged)) {
//wasPlannedChanged -> selecting a new Plan pressing the StartPlan button without stopping the previous button
            cleanMap();
            updateMap();
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

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public boolean checkLocationPermission() {


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
            return false;
        } else {
            return true;
        }
    }
    //TODO  - atualizar o mais recente
    public void showRipplesPos(){
if(isRipplesSelected) {

    if(newRipplesData){
        newRipplesData = false;
        firstRunRipplesPull = false;
        backSystemInfo = systemInfo;
        for(int i = 0; i < systemInfo.systemSize; i++){
            systemPosRipples.setCoords(systemInfo.coordinates[i].getLatitude(), systemInfo.coordinates[i].getLongitude());
            startMarkerRipples[i].setPosition(systemPosRipples);
            startMarkerRipples[i].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            if(systemInfo.sysName[i].contains("lauv"))
                startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_auv));
            else if(systemInfo.sysName[i].contains("ccu"))
                startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_ccu));
            else if(systemInfo.sysName[i].contains("manta"))
                startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_manta));
            else if(systemInfo.sysName[i].contains("spot"))
                startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.spot_icon));
            else
                startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_unknown));

            startMarkerRipples[i].setTitle(systemInfo.sysName[i]+"\n"+
                    gpsConvert.latLonToDM(systemInfo.coordinates[i].getLatitude(), systemInfo.coordinates[i].getLongitude()));
            map.getOverlays().add(startMarkerRipples[i]);
        }
    }
    else if(!newRipplesData && !firstRunRipplesPull){
        for(int i = 0; i < backSystemInfo.systemSize; i++){
            systemPosRipples.setCoords(backSystemInfo.coordinates[i].getLatitude(), backSystemInfo.coordinates[i].getLongitude());
            startMarkerRipples[i].setPosition(systemPosRipples);
            startMarkerRipples[i].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            if(backSystemInfo.sysName[i].contains("lauv")){


                if(!(imc.connectedVehicles().toString().contains(backSystemInfo.sysName[i])))
                startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_auv));
            }
            else if(backSystemInfo.sysName[i].contains("ccu"))
                startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_ccu));
            else if(backSystemInfo.sysName[i].contains("manta"))
                startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_manta));
            else if(backSystemInfo.sysName[i].contains("spot"))
                startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.spot_icon));
            else
                startMarkerRipples[i].setIcon(getResources().getDrawable(R.drawable.ico_unknown));

            startMarkerRipples[i].setTitle(backSystemInfo.sysName[i]+"\n"+ gpsConvert.latLonToDM(backSystemInfo.coordinates[i].getLatitude(), backSystemInfo.coordinates[i].getLongitude()));
            map.getOverlays().add(startMarkerRipples[i]);
        }
    }


}
    }
    public void showAIS(){

        if(isAISSelected) {

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
                                  startMarkerAIS[i].setTitle(mAIS.shipName.get(i) + "\n" + gpsConvert.latLonToDM(mAIS.shipLocation.get(i).getLatitude(), mAIS.shipLocation.get(i).getLongitude()) +
                                          "\n" + ais.parseTime(mAIS.lastUpdateAisShip.get(i)) + "\nHeading: " + mAIS.headingAisShip.get(i) + " | Speed: " + mAIS.speedAisShip.get(i) + " m/s");
                                  map.getOverlays().add(startMarkerAIS[i]);
                              }

                        }
                    }

                    countAisTime = -1;
                } else {
                    for (int i = 0; i < ais.GetNumberShipsAIS(); i++)
                        map.getOverlays().add(startMarkerAIS[i]);
                }

                countAisTime++;
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

        setShowRPM(sharedPreferences.getBoolean(getString(R.string.pref_show_rpmms_key), getResources().getBoolean((R.bool.pref_show_rpmms_default))));
        setOfflineMap(sharedPreferences.getBoolean(getString(R.string.pref_show_offline_key), getResources().getBoolean(R.bool.pref_show_offline_default)));
        setShowDepth(sharedPreferences.getBoolean(getString(R.string.pref_show_depth_key), getResources().getBoolean(R.bool.pref_show_depth_default)));
        loadFromPrefs(sharedPreferences);


        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void loadFromPrefs(SharedPreferences sharedPreferences) {
        speed = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_speed_key), getString(R.string.pref_speed_default)));
        duration = (int) Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_duration_key), getString(R.string.pref_duration_default)));
        radius = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_radius_key), getString(R.string.pref_radius_default)));
        depth = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_depth_key), getString(R.string.pref_depth_default)));
        swath_width = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_width_key), "25"));
        altitude = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_altitude_key), getString(R.string.pref_altitude_default)));

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
            map.setTileSource(new XYTileSource("4uMaps", 2, 18, 256, ".png", new String[]{}));
        } else {
            map.setTileSource(TileSourceFactory.MAPNIK);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

//update the screen if the shared preferences change

        if (key.equals(getString(R.string.pref_show_rpmms_key))) {


            setShowRPM(sharedPreferences.getBoolean(key, getResources().getBoolean((R.bool.pref_show_rpmms_default))));

        } else if (key.equals(getString(R.string.pref_show_offline_key))) {
            setOfflineMap(sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.pref_show_offline_default)));
        } else if (key.equals(getString(R.string.pref_speed_key))) {
            loadFromPrefs(sharedPreferences);

        } else if (key.equals(getString(R.string.pref_duration_key))) {
            loadFromPrefs(sharedPreferences);

        } else if (key.equals(getString(R.string.pref_radius_key))) {
            loadFromPrefs(sharedPreferences);
        } else if (key.equals(getString(R.string.pref_depth_key))) {
            loadFromPrefs(sharedPreferences);
        } else if (key.equals(getString(R.string.pref_altitude_key))) {
            loadFromPrefs(sharedPreferences);
        } else if (key.equals(getString(R.string.pref_show_depth_key))) {
            setShowDepth(sharedPreferences.getBoolean(key, getResources().getBoolean((R.bool.pref_show_depth_default))));

        }
    }

    @UiThread
    public void init() {

        teleOperationButton.setOnClickListener(v -> {
            if (imc.selectedvehicle == null) {
                warning();
            } else {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder
                        .setMessage("Connect to " + imc.selectedvehicle + "?")
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
                            PlanControl pc = new PlanControl();
                            Teleoperation teleoperationMsg = new Teleoperation();
                            teleoperationMsg.setCustom("src=" + imc.getLocalId());
                            pc.setArg(teleoperationMsg);
                            pc.setType(PlanControl.TYPE.REQUEST);
                            pc.setOp(PlanControl.OP.START);
                            pc.setFlags(0);
                            pc.setRequestId(0);
                            pc.setPlanId("SpearTeleoperation-" + imc.selectedvehicle);
                            imc.sendMessage(pc);
                        })
                        .setNegativeButton("No", (dialog, id) -> dialog.cancel());
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }

        });


        startPlan.setOnClickListener(v -> {
            if (imc.selectedvehicle == null) {
                warning();
            } else {
                requestPlans();
            }
        });


        dive.setOnClickListener(v -> {
            if (imc.selectedvehicle == null) {
                warning();
            } else {
                dive();
            }
        });

        comeNear.setOnClickListener(v -> {
            if (imc.selectedvehicle == null) {
                warning();
            } else {
                near();
            }
        });

        stopPlan.setOnClickListener(v -> {
            if (imc.selectedvehicle == null) {
                warning();
            } else {
                stopPlan();
                updateMap();
                otherVehiclesPositionList.clear();
            }
        });

        keepStation.setOnClickListener(v -> {
            if (imc.selectedvehicle == null) {
                warning();
            } else {
                keepStation();
            }
        });


        centerLocation.setOnClickListener(v -> {
            if (myPosition != null)
                mapController.setCenter(myPosition);
            else
                Toast.makeText(context, "Turn Location on", Toast.LENGTH_SHORT).show();

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
            teleOperation.finish();
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
            Joystick joystick = findViewById(R.id.joystick);
            joystick.setVisibility(View.INVISIBLE);
            teleOperation = null;
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
        } else if (id == R.id.sms) {
            Intent i = new Intent(this, SendSms.class);
            startActivity(i);

            return true;
        } else if (id == R.id.area) {
            Intent i = new Intent(this, Area_.class);
            i.putExtra("selected", imc.selectedvehicle);
            startActivity(i);

        } else if (id == R.id.line) {
            Intent i = new Intent(this, Line.class);
            i.putExtra("selected", imc.selectedvehicle);
            startActivity(i);

        }else if (id == R.id.ais) {




            ais = new AISPlot(this.context);
            systemPosAIS = new GeoPoint(0,0);
            ais.getAISInfo();
             isAISSelected = true;

        }else if (id == R.id.ripples) {


            ripples = new RipplesPosition(this, UrlRipples);
            systemPosRipples = new GeoPoint(0,0);
            isRipplesSelected=true;
        }else if (id == R.id.compass) {

            Intent i = new Intent(this, Compass.class);
            startActivity(i);
        }


            return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
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
    @Override
    public void onLocationChanged(Location location) {
        latitudeAndroid = Math.toRadians(location.getLatitude());
        longitudeAndroid = Math.toRadians(location.getLongitude());
        myPosition = new GeoPoint(location.getLatitude(), location.getLongitude());
        bearingMyLoc = location.getBearing();
        final ArrayList<OverlayItem> items = new ArrayList<>();
        OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", myPosition);
        marker.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
        items.add(marker);
        Bitmap newMarker;
        if (android.os.Build.VERSION.SDK_INT <= M) {

            newMarker = Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrowred2));

        } else {

            newMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.arrowred2), 0, 0, true);

        }



        Drawable marker3 = new BitmapDrawable(getResources(), newMarker);
        ItemizedIconOverlay markersOverlay = new ItemizedIconOverlay<>(items, marker3, null, context);
        map.getOverlays().add(markersOverlay);


    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Background
    public void paintState(final EstimatedState state) {
        final String vname = state.getSourceName();

        if (imc.stillConnected() != null) {

            if (imc.selectedvehicle != null) {
                if (!(imc.stillConnected().contains(imc.selectedvehicle)))
                    runOnUiThread(() -> {

                        serviceBar.setText(" ");
                        velocity.setText(" ");
                        //retirar icon
                        selectedVehiclePosition = null;
                        if (map.getOverlays().contains(markersOverlay2))
                            map.getOverlays().remove(markersOverlay2);

                    });
            }
            if (imc.stillConnected().contains(vname)) {
                double[] lld = WGS84Utilities.toLatLonDepth(state);
                final ArrayList<OverlayItem> items2 = new ArrayList<>();
                if (!vname.equals(imc.getSelectedvehicle()))
                    otherVehiclesPositionList.add(new GeoPoint(lld[0], lld[1]));
                OverlayItem marker2 = new OverlayItem("markerTitle", "markerDescription", new GeoPoint(lld[0], lld[1]));
                marker2.setMarkerHotspot(HotspotPlace.TOP_CENTER);
                items2.add(marker2);


                vehicleOrientation = (float) state.getPsi();
                int ori2 = (int) Math.round(Math.toDegrees(vehicleOrientation));
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
                    int ori = (int) Math.round(Math.toDegrees(selectedVehicleOrientation));
                    ori = ori - 180;
                    orientationSelected = ori;
                    if (selectedVehiclePosition != null) {
                        latVehicle = Math.toRadians(selectedVehiclePosition.getLatitude());
                        lonVehicle = Math.toRadians(selectedVehiclePosition.getLongitude());
                    }
                    if (context == MainActivity.this) {
                        if (android.os.Build.VERSION.SDK_INT <= M) {
                            bitmapArrow = Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrowgreen2));

                        } else {

                            bitmapArrow = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrowgreen2), 50, 50, false);

                        }
                    }
                    DecimalFormat df2 = new DecimalFormat("#.##");
                    velocityString = df2.format(Math.sqrt((state.getVx() * state.getVx()) + (state.getVy() * state.getVy()) + (state.getVz() * state.getVz())));
                    depthString = df2.format(state.getDepth());
                    if (velocity != null)
                        runOnUiThread(() -> velocity.setText(getString(R.string.speedstring) + " " + velocityString + " " + getString(R.string.meterspersecond) + "\n" + getString(R.string.depthstring) + " " + depthString + "\n" + stateconnected));
                }

                Bitmap target = RotateMyBitmap(bitmapArrow, ori2);

                Drawable marker_ = new BitmapDrawable(resources, target);
                markersOverlay2 = new ItemizedIconOverlay<>(items2, marker_, null, context);

                map.getOverlays().add(markersOverlay2);
            }
        }

    }

    public void zoomVehicle(final EstimatedState state) {
        if (imc.getSelectedvehicle().equals(state.getSourceName())) {
            double[] lld = WGS84Utilities.toLatLonDepth(state);

            GeoPoint posicaoVeiculo2 = new GeoPoint(lld[0], lld[1]);
            mapController.setZoom(14);
            zoomLevel = 14;
            mapController.setCenter(posicaoVeiculo2);
        }
    }

    @Background
    @Periodic
    public void updateState() {
        stateList = new ArrayList<>();
        vehicleStateList = imc.connectedVehicles();



        for (VehicleState state : vehicleStateList) {
            if (imc.selectedvehicle != null)
                if (imc.selectedvehicle.equals(state.getSourceName()))
                    stateList.add(state.getOpModeStr());


        }
        if (stateList.size() != 0) {
            for (int i = 0; i < stateList.size(); i++) {

                stateconnected = stateList.toString();

                if (!isStopPressed) {
                    //Se o veiculo entrar em service mode sem ser por parar o plano
                    if ((previous != null) && !hasEnteredServiceMode && stateconnected.charAt(1) == 'S') {
                        hasEnteredServiceMode = true;
                            comeNearOn=false;

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

    @Background
    @Periodic(500)
    public void updateMap() {
        System.gc();
        otherVehiclesPositionList.clear();
        map.getOverlays().remove(mCompassOverlay);
        map.getOverlays().clear();

        showAIS();
        showRipplesPos();
        map.setMultiTouchControls(true);
        if (maneuverList != null)

            if (context == MainActivity.this) {
                drawWifiSignal();
                drawCompass();
            }


        synchronized (estates) {
            for (EstimatedState state : estates.values()) {
                paintState(state);
            }

        }


        if (location != null)
            onLocationChanged(location);

        if (!isStopPressed && !hasEnteredServiceMode) {
            if (Line.getPointsLine().size() != 0) {
                for (int i = 0; i < Line.getPointsLine().size(); i++) {
                    markerFromLine = new Marker(map);
//java.lang.IndexOutOfBoundsException: Invalid index 0, size is 0
                    if (Line.getPointsLine().size() != 0) {
                        markerFromLine.setPosition(Line.getPointsLine().get(i));
                        markerFromLine.setIcon(lineIcon);
                        map.getOverlays().add(markerFromLine);
                    }
                }
            }

        }
        if (!isStopPressed && Line.getPoly() && !hasEnteredServiceMode) {
            if (pointsLine.size() != 0) {
                polyline = new Polyline();
                polyline.setPoints(pointsLine);
                map.getOverlays().add(polyline);
                isPolylineDrawn = true;

            }
        }
    afterChoice();

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

        if (!areNewWaypointsFromAreaUpdated) {
            if (stateconnected != null && stateconnected.charAt(1) != 'S')
            if (Area.sendmList() != null) {

                updateWaypoints();
            }

        }
    }

    //Run task periodically - garbage collection

    private Runnable updateTimerThreadGarbagde = new Runnable() {
        public void run() {
            customHandlerGarbagde = new Handler();
            customHandlerGarbagde.postDelayed(updateTimerThreadGarbagde, 100);

            customHandlerGarbagde.postDelayed(this, 20000);
            System.gc();
            Runtime.getRuntime().gc();
        }
    };

//TODO - parser ver qual mensagem mais recente e mostrar essa entre wifi e iridium - ver exemplo ACM SOIActivity


    //Run task periodically - AIS
    private Runnable updateTimerThreadAIS = new Runnable() {
        @SuppressLint("SetTextI18n")
        public void run() {
            if(isAISSelected) {



                customHandlerAIS.postDelayed(this, timeoutAISPull * 1000);
                //if(timeoutAISPull != 1) {
                //    showError.showErrorLogcat("MEU", "size ais: "+ais.GetNumberShipsAIS());
                //}
                timeoutAISPull = Integer.parseInt("12");

            }
        }
    };


    //Run task periodically - Ripples
    private Runnable updateTimerThreadRipples = new Runnable() {
        @SuppressLint("SetTextI18n")
        public void run() {
            if (isRipplesSelected) {

                customHandlerRipples.postDelayed(this, timeoutRipplesPull * 1000);
                if(timeoutRipplesPull != 1) {
                    if (ripples.PullData(UrlRipples)) {
                        systemInfo = ripples.GetSystemInfoRipples();
                        newRipplesData = true;
                    }

                }
                timeoutRipplesPull = Integer.parseInt("12");

            }
        }
    };







    public void drawCompass() {
        if (mCompassOverlay != null) {
            map.getOverlays().add(mCompassOverlay);

        } else {
            mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), map);
            mCompassOverlay.enableCompass();

            map.getOverlays().add(mCompassOverlay);
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
            markerSMS.setIcon(lineIcon);
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
        String planid = "SpearDive-" + imc.selectedvehicle;
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
        String planid = " SpearStationKeeping-" + imc.selectedvehicle;
        startBehaviour(planid, stationKeepingmsg);


    }

    public void near() {

        FollowReference go = new FollowReference();
        go.setAltitudeInterval(1);
        go.setControlSrc(imc.getLocalId());
        go.setLoiterRadius(0);
        go.setTimeout(30);

        String planid = "SpearComeNear-" + imc.selectedvehicle;
        comeNearOn=true;
        startReference();

        startBehaviour(planid, go);


    }

    public void startReference() {
        if (comeNearOn) {



                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder
                            .setMessage("North or east?")
                            .setCancelable(true)
                            .setPositiveButton("North", (dialog, id) -> {
                                n = 1.5;
                                e = 0;
                                afterChoice();
                            })
                            .setNegativeButton("East", (dialog, id) -> {
                                e = 1.5;
                                n = 0;
                                afterChoice();

                                dialog.cancel();
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();



        }


    }
    public void afterChoice() {
        if (comeNearOn) {
            Reference ref = new Reference();
System.out.println(" comenear");
            double[] latlonDisplace = WGS84displace(latitudeAndroid, longitudeAndroid, depth, n, e, 0);

            ref.setLat(latlonDisplace[0]);
            ref.setLon(latlonDisplace[1]);
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
        pc.setPlanId("stopPlan-" + imc.selectedvehicle);
        imc.sendMessage(pc);

        MainActivity.previous = "S";
        hasEnteredServiceMode = true;
        isStopPressed = true;
        areNewWaypointsFromAreaUpdated = false;
        isPolylineDrawn = false;
        otherVehiclesPositionList.clear();
        wasPlanChanged = false;
            comeNearOn=false;

        cleanMap();


    }

    public void cleanMap() {
        if (planWaypointPolyline != null) {
            planWaypointPolyline.setPoints(nullArray);
            map.getOverlays().remove(planWaypointPolyline);
        }
        stateconnected = null;


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
            map.getOverlays().remove(polyline);
            nullArray.clear();
            if (polyline != null)
                polyline.setPoints(nullArray);

        }



        if (Line.getPointsLine() != null) {
            map.getOverlays().remove(markerFromLine);
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
            updateMap();


        }
        runOnUiThread(() -> {
            map.invalidate();
            //map.getOverlays().clear();
            updateMap();
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
            new CountDownTimer(5000, 1000) {
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
    public void drawWifiSignal() {
        // Check if is connected to a Wifi Network, if not popups a informative toast
        runOnUiThread(() -> {
            if (!isWifiAvailable()) {
                if (serviceBar != null) {
                    wifiDrawable.setVisibility(View.INVISIBLE);
                    noWifiImage.setVisibility(View.VISIBLE);
                    isRipplesSelected=false;

                }
            } else {
                if (serviceBar != null) {
                    noWifiImage.setVisibility(View.INVISIBLE);
                    wifiDrawable.setVisibility(View.VISIBLE);
                    isRipplesSelected=true;

                }
            }
        });
    }


    public void requestPlans() {
        if (imc.selectedvehicle == null) {
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
            wasPlanChanged = stateconnected.charAt(1) != 'S';


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
            serviceBar.setText(selectedName2);
            previous = null;


            synchronized (estates) {
                for (EstimatedState state : estates.values()) {
                    zoomVehicle(state);
                }


            }
        }
        if (teleOperation != null) {
            teleOperation.finish();
        }

        return super.onContextItemSelected(item);
    }



    public void warning() {
        Toast.makeText(this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
    }
}


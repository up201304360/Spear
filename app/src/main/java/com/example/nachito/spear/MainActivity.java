package com.example.nachito.spear;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import org.jetbrains.annotations.Contract;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.lsts.imc.EstimatedState;
import pt.lsts.imc.Goto;
import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.Loiter;
import pt.lsts.imc.Maneuver;
import pt.lsts.imc.PlanControl;
import pt.lsts.imc.PlanDB;
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


@EActivity

public class MainActivity extends AppCompatActivity
        implements MapViewConstants,  OnLocationChangedListener,  LocationListener, SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {

    private Context context;
    @ViewById(R.id.dive)
    Button dive;
    @ViewById(R.id.near)
    Button comeNear;
    @ViewById(R.id.startplan)
    Button startPlan;
    @ViewById(R.id.keepStation)
    Button keepStation;
    @ViewById(R.id.servicebar)
    TextView serviceBar;
    @Bean
    static IMCGlobal imc;
    @ViewById(wifiImage)
    ImageView wifiDrawable;
    @ViewById(R.id.noWifiImage)
    ImageView noWifiImage;
    TeleOperation teleOperation;
    List<Maneuver> maneuverList;
    List<VehicleState> vehicleStateList;
    @ViewById(R.id.map)
    static MapView map;
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
    double latitude;
    double longitude;
    @ViewById(R.id.velocity)
    TextView velocity;
    static double speed;
    static int duration;
    static double radius;
    static double depth;
    static double swath_width;
    int color = Color.parseColor("#39B7CD"), pressed_color = Color.parseColor("#568203");
    @ViewById(R.id.bottomsheet)
    LinearLayout bottom;
    @org.androidannotations.annotations.res.DrawableRes(R.drawable.orangeled)
    static Drawable nodeIcon;
    @org.androidannotations.annotations.res.DrawableRes(R.drawable.reddot)
    static Drawable redIcon;
    @org.androidannotations.annotations.res.DrawableRes(R.drawable.blueled)
    static Drawable lineIcon;
    Marker pointsPlans;
    static Marker lineMarker;
    static  boolean showrpm;
    //static - so Area and Line can pass the waypointsFromPlan clicked
    @SuppressLint("StaticFieldLeak")
    static String depthString;
    static String vehicleStateString;
    static String velocityString;
    static Marker nodeMarkerWaypoints;
    Location location;
    static String previous=null;
    Double valueOfLatitude;
    Double valueOfLongitude;
    List<String> vehicleList;
    List<String> planList;
    int listSize;
    OSMHandler updateHandler;
    List<String> stateList;
    static GeoPoint vehiclePosition;
    static GeoPoint myPosition;
    static  ArrayList<GeoPoint> otherVehiclesPositionList = new ArrayList<>();
    static float vehicleOrientation;
    SendSms sendsms;
    Marker markerSMS;
    ScaleBarOverlay scaleBarOverlay;
    static Maneuver maneuverFromPlan;
    @ViewById(R.id.localizacao)
    Button centerLocation;
    ItemizedIconOverlay markersOverlay2;
    static Collection<PlanUtilities.Waypoint> waypointsFromPlan;
    android.content.res.Resources resources;

    static Bitmap bitmapArrow;
    ArrayList<GeoPoint> pointsArea= Area.getPointsArea();

    Polyline polyline = Line.getPolyline();
    static Boolean isCircleDrawn = Area.getCircle();
    static Boolean isPolylineDrawn = Line.getPoly();
    List<Maneuver> maneuverListFromArea = Area.sendmList();
static double latVeiculo;
static double lonVeiculo;
    static boolean stopPressed=false;
    Polygon circle;
    static ArrayList<GeoPoint> pointsLine= Line.getPointsLine();
    Marker markerArea;
    Marker markerLine;
    Polyline planWaypointPolyline;
    ArrayList<GeoPoint> pontosAreaMarker = new ArrayList<>();
    static ArrayList<GeoPoint> planWaypoints = new ArrayList<>();
    ArrayList<GeoPoint> nullArray = new ArrayList<>();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));



        setContentView(R.layout.activity_main);
        map.setTileSource(TileSourceFactory.MAPNIK);
        android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }

        resources = getResources();
        context = this;
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);



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

        Accelerate accelerate =(Accelerate)  findViewById(R.id.accelerate);
        accelerate.setVisibility(View.INVISIBLE);
        Decelerate decelerate = (Decelerate) findViewById(R.id.decelerate);
        decelerate.setVisibility(View.INVISIBLE);
        Joystick joystick =  (Joystick)findViewById(R.id.joystick);
        joystick.setVisibility(View.INVISIBLE);
        noWifiImage.setVisibility(View.INVISIBLE);
        StopTeleop stopTeleop =  (StopTeleop)findViewById(R.id.stopTeleop);
        stopTeleop.setVisibility(View.INVISIBLE);


        imc.register(this);
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
        mapController.setCenter(new GeoPoint(location));


        scaleBarOverlay = new ScaleBarOverlay( map);
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

                GeoPoint coordSMS= new GeoPoint(lat, lon);

                System.out.println(coordSMS + " coordinates from sms");

                mapController.setCenter(coordSMS);
                mapController.setZoom(18);
            }

        });
    }

    public static GeoPoint getVariables(){
        return vehiclePosition;
    }

    public static String getPrevious(){return previous;}

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

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

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

    public void onResume() {
        super.onResume();

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
        loadFromPrefs(sharedPreferences);

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }


    private void loadFromPrefs(SharedPreferences sharedPreferences) {
        speed = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_speed_key), getString(R.string.pref_speed_default)));
        duration = (int) Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_duration_key), getString(R.string.pref_duration_default)));
        radius = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_radius_key), getString(R.string.pref_radius_default)));
        depth = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_depth_key), getString(R.string.pref_depth_default)));
        swath_width = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_width_key), "25"));

    }

    public void setShowRPM(boolean showrpm) {
        MainActivity.showrpm = showrpm;

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

//update the screen if the shared preferences change

        if (key.equals(getString(R.string.pref_show_rpmms_key))) {


            setShowRPM(sharedPreferences.getBoolean(key, getResources().getBoolean((R.bool.pref_show_rpmms_default))));

        } else if (key.equals(getString(R.string.pref_speed_key))) {
            loadFromPrefs(sharedPreferences);

        } else if (key.equals(getString(R.string.pref_duration_key))) {
            loadFromPrefs(sharedPreferences);

        } else if (key.equals(getString(R.string.pref_radius_key))) {
            loadFromPrefs(sharedPreferences);
        } else if (key.equals(getString(R.string.pref_depth_key))) {
            loadFromPrefs(sharedPreferences);
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
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialog, id) -> {
                            if (teleOperation == null)
                                teleOperation = new TeleOperation();
                            teleOperation.setImc(imc);
                            Joystick joystick = (Joystick) findViewById(R.id.joystick);
                            joystick.setOnJoystickMovedListener(teleOperation);
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.activity_maps, teleOperation).addToBackStack("tag").commit();
                            dive.setVisibility(View.INVISIBLE);
                            teleOperationButton.setVisibility(View.INVISIBLE);
                            startPlan.setVisibility(View.INVISIBLE);
                            comeNear.setVisibility(View.INVISIBLE);
                            keepStation.setVisibility(View.INVISIBLE);
                            Accelerate accelerate = (Accelerate) findViewById(R.id.accelerate);
                            accelerate.setVisibility(View.VISIBLE);
                            accelerate.setOnAccelerate(teleOperation);
                            Decelerate decelerate = (Decelerate) findViewById(R.id.decelerate);
                            decelerate.setVisibility(View.VISIBLE);
                            decelerate.setOnDec(teleOperation);
                            stopPlan.setVisibility(View.INVISIBLE);
                            StopTeleop stopTeleop = (StopTeleop) findViewById(R.id.stopTeleop);
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
                            setVehicleStateString(" ");
                        })
                        .setNegativeButton("No", (dialog, id) -> dialog.cancel());
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }

        });


        startPlan.setOnClickListener(v -> {
            startPlan.setBackgroundColor(pressed_color);

            if (imc.selectedvehicle == null) {
                warning();
                startPlan.setBackgroundColor(color);

            } else {
                requestPlans();
                startPlan.setBackgroundColor(color);

            }

        });


        dive.setOnClickListener(v -> {
            dive.setBackgroundColor(pressed_color);

            if (imc.selectedvehicle == null) {
                warning();
                dive.setBackgroundColor(color);

            } else {
                dive();
                dive.setBackgroundColor(color);

            }
        });

        comeNear.setOnClickListener(v -> {
            comeNear.setBackgroundColor(pressed_color);

            if (imc.selectedvehicle == null) {
                warning();
                comeNear.setBackgroundColor(color);

            } else {
                near();
                comeNear.setBackgroundColor(color);

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
            keepStation.setBackgroundColor(pressed_color);

            if (imc.selectedvehicle == null) {
                warning();
                keepStation.setBackgroundColor(color);

            } else {
                keepStation();
                keepStation.setBackgroundColor(color);
            }
        });



        centerLocation.setOnClickListener(v -> {
            if(myPosition !=null)
                mapController.setCenter(myPosition);
            else
                Toast.makeText(context, "Turn Location on", Toast.LENGTH_SHORT).show();

        });



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
            Accelerate accelerate = (Accelerate) findViewById(R.id.accelerate);
            accelerate.setVisibility(View.INVISIBLE);
            Decelerate decelerate =(Decelerate) findViewById(R.id.decelerate);
            decelerate.setVisibility(View.INVISIBLE);
            StopTeleop stopTeleop =  (StopTeleop)findViewById(R.id.stopTeleop);
            stopTeleop.setVisibility(View.INVISIBLE);
            Joystick joystick = (Joystick) findViewById(R.id.joystick);
            joystick.setVisibility(View.INVISIBLE);
            teleOperation =null;

        } else if (sendsms != null) {
//


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
        }
        else if(id==R.id.sms){
            Intent i = new Intent(this, SendSms.class);
            i.putExtra("selected", imc.selectedvehicle);
            startActivity(i);

            return true;
        }
        else if (id == R.id.area) {
            Intent i = new Intent(this, Area_.class);
            i.putExtra("selected", imc.selectedvehicle);
            startActivity(i);

        } else if (id == R.id.line) {
            Intent i = new Intent(this, Line.class);
            i.putExtra("selected", imc.selectedvehicle);
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

    final LinkedHashMap<String, EstimatedState> estates = new LinkedHashMap<>();

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
        latitude = Math.toRadians(location.getLatitude());
        longitude = Math.toRadians(location.getLongitude());
        myPosition = new GeoPoint(location.getLatitude(), location.getLongitude());

        final ArrayList<OverlayItem> items = new ArrayList<>();

        OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", myPosition);
        marker.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
        items.add(marker);

        Bitmap newMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.arrowred), 70, 70, false);
        Drawable marker3 = new BitmapDrawable(getResources(), newMarker);
        ItemizedIconOverlay markersOverlay = new ItemizedIconOverlay<>(items, marker3, null, context);
        map.getOverlays().add(markersOverlay);


    }

    public static GeoPoint localizacao(){
        return myPosition;


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

            if(imc.selectedvehicle!=null){
                if(!(imc.stillConnected().contains(imc.selectedvehicle)))
                    runOnUiThread(() -> {

                        serviceBar.setText(" ");
                        velocity.setText(" ");
                        //retirar icon
                        if(map.getOverlays().contains(markersOverlay2))
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
                if(context==MainActivity.this)
                    bitmapArrow = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.downarrow), 70, 70, false);


                if (vname.equals(imc.getSelectedvehicle())) {

                    vehiclePosition = new GeoPoint(lld[0], lld[1]);

                    latVeiculo= Math.toRadians(vehiclePosition.getLatitude());
                    lonVeiculo= Math.toRadians(vehiclePosition.getLongitude());
                    System.out.println(latVeiculo + "lat");

                    if(context==MainActivity.this)

                        bitmapArrow = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrowgreen), 70, 70, false);

                    DecimalFormat df2 = new DecimalFormat("#.##");
                    velocityString = df2.format(Math.sqrt((state.getVx() * state.getVx()) + (state.getVy() * state.getVy()) + (state.getVz() * state.getVz())));
                    depthString = df2.format(state.getDepth());
                    if(velocity!=null)
                        runOnUiThread(() -> velocity.setText("Speed:" + " " + velocityString + " " + "m/s" + "\n" + "Depth:" + " " + depthString + "\n" + vehicleStateString + "\n"));

                }


                Bitmap target = RotateMyBitmap(bitmapArrow, ori2);
                Drawable marker_ = new BitmapDrawable(resources, target);
                markersOverlay2 = new ItemizedIconOverlay<>(items2, marker_, null, context);
                map.getOverlays().add(markersOverlay2);
            }
        }
    }

    @Contract(pure = true)
    public static ArrayList<GeoPoint> drawPosicaoOutrosVeiculos(){
        return otherVehiclesPositionList;
    }

    @Contract(pure = true)
    public  static float orientation(){
        return vehicleOrientation;

    }
    public void zoomVehicle(final EstimatedState state) {
        if (imc.getSelectedvehicle().equals(state.getSourceName())) {
            double[] lld = WGS84Utilities.toLatLonDepth(state);

            GeoPoint posicaoVeiculo2 = new GeoPoint(lld[0], lld[1]);
            mapController.setZoom(16);
            mapController.setCenter(posicaoVeiculo2);

        }
    }

    @Nullable
    public static Bitmap RotateMyBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        if(bitmapArrow!=null)
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        else
            return null;
    }


    @Background
    @Periodic
    public void updateState() {
        stateList = new ArrayList<>();
        vehicleStateList = imc.connectedVehicles();


        for (VehicleState state : vehicleStateList) {
            if(imc.selectedvehicle!=null)
                if(imc.selectedvehicle.equals(state.getSourceName()))
                    stateList.add(state.getOpModeStr());

        }

        for (int i = 0; i < stateList.size(); i++) {
            String stateconncected;
            stateconncected= stateList.toString();



            if (previous!=null && stateconncected.charAt(1) == 'S') {

                map.getOverlays().remove(lineMarker);
                map.getOverlays().remove(nodeMarkerWaypoints);
                if(waypointsFromPlan !=null)
                    waypointsFromPlan.clear();
                waypointsFromPlan =null;
                if(maneuverList !=null)
                    maneuverList.clear();
                updateMap();
                otherVehiclesPositionList.clear();
                if(planWaypoints !=null)
                    planWaypoints.clear();
                if(planWaypointPolyline !=null){
                    planWaypointPolyline.setPoints(nullArray);
                    map.getOverlays().remove(planWaypointPolyline);}


            }

        }
    }



    public static void setVehicleStateString(String e){

        vehicleStateString =e;
    }

    @Nullable
    @Contract(pure = true)
    public static Marker getPointsMain(){
        return nodeMarkerWaypoints;

    }
    public static boolean returnCircle(){
        return isCircleDrawn;

    }
    public static boolean returnPoly(){
        return isPolylineDrawn;

    }
    @Nullable
    public static ArrayList<GeoPoint> returnLinePoints() {
        Line.markers.addAll(Line.getPointsLine());
        Area.markers.addAll(Line.getPointsLine());
        return Line.getPointsLine();


    }

    @Nullable
    public static ArrayList<GeoPoint> returnAreaPoints(){
        Line.markers.addAll(Area.getPointsArea());
        Area.markers.addAll(Area.getPointsArea());

        return Area.getPointsArea();


    }


    @Background
    @Periodic(500)
    public  void updateMap() {
        otherVehiclesPositionList.clear();
        map.getOverlays().remove(mCompassOverlay);
        map.getOverlays().clear();
        System.out.println(latVeiculo + "lat");

        map.setMultiTouchControls(true);


        if (context == MainActivity.this) {
            drawWifiSignal();
            drawCompass();
        }


        synchronized (estates) {
            for (EstimatedState state : estates.values()) {
                paintState(state);
            }

        }



        if (Line.getPolyline() != null) {
            map.getOverlays().add(polyline);
        }


        if (Area.getCircle()) {
            if (pointsArea.size() != 0) {
                circle = new Polygon();
                circle.setStrokeWidth(7);
                circle.setPoints(pointsArea);
                map.getOverlays().add(circle);
                isCircleDrawn = true;
            }
        }

        if (Line.getPoly()) {
            if (pointsLine.size() != 0) {
                polyline = new Polyline();
                polyline.setPoints(pointsLine);
                map.getOverlays().add(polyline);
                isPolylineDrawn = true;
            }
        }

        if (location != null)
            onLocationChanged(location);


        if(Area.sendmList()!=null || Line.sendmList()!=null)
             updateWaypoints();




    }

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


        if (pointsArea.size() != 0) {


            for (int i = 0; i < pointsArea.size(); i++) {
                markerArea = new Marker(map);

                markerArea.setPosition(pointsArea.get(i));
                markerArea.setIcon(lineIcon);
                map.getOverlays().add(markerArea);

            }
        }

        if (pointsLine.size() != 0) {
            for (int i = 0; i < pointsLine.size(); i++) {
                markerLine = new Marker(map);

                markerLine.setPosition(pointsLine.get(i));
                markerLine.setIcon(lineIcon);
                map.getOverlays().add(markerLine);
            }
        }


        if (planWaypoints.size() != 0) {

            for (int i = 0; i < planWaypoints.size(); i++) {
                pointsPlans = new Marker(map);
                if (!stopPressed)
                    pointsPlans.setPosition(planWaypoints.get(i));
                pointsPlans.setIcon(redIcon);
                map.getOverlays().add(pointsPlans);
                if(planWaypointPolyline !=null)
                    map.getOverlays().remove(planWaypointPolyline);
                map.getOverlays().add(planWaypointPolyline);
            }





        }
        if (SendSms.pontoSMS() != null) {
            GeoPoint ponto = SendSms.pontoSMS();


            markerSMS = new Marker(map);
            markerSMS.setPosition(ponto);
            markerSMS.setIcon(lineIcon);
            map.getOverlays().add(markerSMS);

        }



    }

    public  void updateWaypoints() {


        if (Area.sendmList() != null) {

            for (int i = 0; i < Area.maneuverArrayList.size() - 1; i++) {
                callWaypoint(Area.maneuverArrayList);
            }


        }
        else if(Line.sendmList()!=null){

            for (int i = 0; i < Line.lineListManeuvers.size() - 1; i++) {
                callWaypoint(Line.lineListManeuvers);
            }
        }
    }




    public void dive() {
        Loiter dive = new Loiter();
            dive.setLon(lonVeiculo);
            dive.setLat(latVeiculo);
            System.out.println(dive.getLat() + "dive " + latVeiculo);
            dive.setZ(depth);
            dive.setType(Loiter.TYPE.CIRCULAR);

        System.out.println(dive.getZ() + "depth " + depth);

        dive.setZUnits(ZUnits.DEPTH);
            dive.setSpeed(speed);
        System.out.println(dive.getSpeed() + "speed " + speed);

        if (!showrpm) {
                dive.setSpeedUnits(SpeedUnits.METERS_PS);
            } else {
                dive.setSpeedUnits(SpeedUnits.RPM);
            }
            dive.setRadius(radius);
            dive.setDuration(duration);
            dive.setBearing(0);
            String planid = "SpearDive-" + imc.selectedvehicle;
        if(lonVeiculo!=0&&latVeiculo!=0){

            startBehaviour(planid, dive);}
            setVehicleStateString("Dive");
            wayPoints(dive);

    }

    public void keepStation() {
        StationKeeping stationKeepingmsg = new StationKeeping();

            stationKeepingmsg.setLat(latVeiculo);
            stationKeepingmsg.setLon(lonVeiculo);
            stationKeepingmsg.setSpeed(speed);
            if (!showrpm) {
                stationKeepingmsg.setSpeedUnits(SpeedUnits.METERS_PS);
            } else {
                stationKeepingmsg.setSpeedUnits(SpeedUnits.RPM);
            }
            stationKeepingmsg.setDuration(duration);
            stationKeepingmsg.setRadius(radius);
            stationKeepingmsg.setZ(depth);
            stationKeepingmsg.setZUnits(ZUnits.DEPTH);
            String planid = " SpearStationKeeping-" + imc.selectedvehicle;
            startBehaviour(planid, stationKeepingmsg);
            wayPoints(stationKeepingmsg);
            setVehicleStateString("StationKeeping");


    }

    public void near() {
        if (latitude == 0 && longitude == 0) {
            return;
        }
        final Goto go = new Goto();
        go.setLat(latitude);
        go.setLon(longitude);
        go.setZ(0);
        go.setZUnits(ZUnits.DEPTH);
        go.setSpeed(speed);
        if (!showrpm) {
            go.setSpeedUnits(SpeedUnits.METERS_PS);
        } else {
            go.setSpeedUnits(SpeedUnits.RPM);
        }
        String planid = "SpearComeNear-" + imc.selectedvehicle;
        startBehaviour(planid, go);
        wayPoints(go);
        setVehicleStateString("Come comeNear");


    }


    public static void startBehaviour(String planid, IMCMessage what) {
     /*   PlanDB plan=new PlanDB();
        plan.setPlanId(planid);
        plan.setArg(what);
        plan.setType(PlanDB.TYPE.REQUEST);
        plan.setOp(PlanDB.OP.SET);
*/
        PlanControl pc = new PlanControl();
        pc.setArg(what);
        pc.setType(PlanControl.TYPE.REQUEST);
        pc.setOp(PlanControl.OP.START);
        pc.setFlags(0);
        pc.setRequestId(0);
        pc.setPlanId(planid);


        map.getOverlays().remove(nodeMarkerWaypoints);
        if(waypointsFromPlan !=null)
            waypointsFromPlan.clear();
        waypointsFromPlan =null;
        map.invalidate();
        map.getOverlays().clear();
        previous="M";
        stopPressed= false;

        imc.sendMessage(pc);


    }



    public void stopPlan() {
        PlanControl pc = new PlanControl();
        pc.setType(PlanControl.TYPE.REQUEST);
        pc.setOp(PlanControl.OP.STOP);
        pc.setRequestId(1);
        pc.setFlags(0);
        pc.setPlanId("stopPlan-" + imc.selectedvehicle);
        imc.sendMessage(pc);
        setVehicleStateString("Plan Stopped");
        previous="S";
        stopPressed=true;
        isPolylineDrawn =false;
        isCircleDrawn =false;
        if(nodeMarkerWaypoints!=null){
            map.getOverlays().remove(nodeMarkerWaypoints);
            nodeMarkerWaypoints.remove(map);
        }

        if(planWaypoints !=null)
            planWaypoints.clear();

        if(waypointsFromPlan !=null) {
            waypointsFromPlan.clear();
        }
        if(pointsPlans!=null) {
            pointsPlans.remove(map);
            map.getOverlays().remove(pointsPlans);
        }
        if(Area.getPointsArea()!=null){

            map.getOverlays().remove(markerArea);
            Area.getPointsArea().clear();
        }
        if(planWaypoints !=null){
            map.getOverlays().remove(pointsPlans);
            planWaypoints.clear();
        }
        if(Line.getPointsLine()!=null){

            map.getOverlays().remove(markerLine);
            Line.getPointsLine().clear();
        }

        if(Line.getPolyline()!=null){
            map.getOverlays().remove(polyline);
            nullArray.clear();
            polyline.setPoints(nullArray);
            Line.getPolyline().setPoints(nullArray);
        }
        if(Area.getCircle()){
            isCircleDrawn =false;
            map.getOverlays().remove(circle);

        }
        if(Line.getPoly()){
            isPolylineDrawn =false;
            map.getOverlays().remove(polyline);
            nullArray.clear();
            polyline.setPoints(nullArray);

        }
        if(returnAreaPoints()!=null){
            Area.getPointsArea().clear();


        }

        if(pontosAreaMarker !=null) {
            pontosAreaMarker.clear();

        }
        if(planWaypointPolyline !=null) {
            map.getOverlayManager().remove(planWaypointPolyline);
            planWaypointPolyline.setPoints(pontosAreaMarker);

        }

        if(returnLinePoints()!=null){
            Line.getPointsLine().clear();
        }



        if(maneuverList !=null) {
            maneuverList.clear();

        }
        if( Area.sendmList()!=null){
            Area.maneuverArrayList.clear();

            if(maneuverListFromArea !=null)
                maneuverListFromArea.clear();


            Area.sendmList().clear();
        }
        if( Line.sendmList()!=null){
            Line.lineListManeuvers.clear();

            Line.sendmList().clear();
        }


        if(SendSms.pontoSMS()!=null){
            map.getOverlays().remove(markerSMS);
            updateMap();


        }

        map.invalidate();
        map.getOverlays().clear();
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    //if there is wifiDrawable show an imageview, when the wifiDrawable is off change it to another
    private boolean isConnectedToWifi(Context context) {

        boolean connectedWifi = false;
        try {
            ConnectivityManager nConManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (nConManager != null) {
                NetworkInfo nNetworkinfo = nConManager.getActiveNetworkInfo();
                if (nNetworkinfo.isConnected()) {
                    connectedWifi = true;
                    return connectedWifi;
                }
            }
        } catch (Exception ignored) {
        }
        return connectedWifi;
    }

    //Set a timer to check if is connected to a Wifi Network
    public void drawWifiSignal() {
        // Check if is connected to a Wifi Network, if not popups a informative toast
        runOnUiThread(() -> {
            if (!isConnectedToWifi(MainActivity.this)) {
                if(serviceBar !=null){
                    wifiDrawable.setVisibility(View.INVISIBLE);
                    noWifiImage.setVisibility(View.VISIBLE);}
            } else {
                if(serviceBar !=null){
                    noWifiImage.setVisibility(View.INVISIBLE);
                    wifiDrawable.setVisibility(View.VISIBLE);}
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
            setVehicleStateString("Plan:" + pc.getPlanId());

            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (imc.allManeuvers() == null) {
                    Toast.makeText(MainActivity.this, "No plan specification available", Toast.LENGTH_SHORT).show();
                } else {
                    previous="M";
                    stopPressed= false;
                    maneuverList.addAll(imc.allManeuvers());
                    callWaypoint(maneuverList);

                }
            },3000);
        }


        else {
            String selected = item.toString();
            String[] getName2 = selected.split(":");
            String selectedName2 = getName2[0];
            setVehicleStateString( getName2[1]);
            imc.setSelectedvehicle(selectedName2.trim());
            serviceBar.setText(selectedName2);
            previous=null;


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

    public void callWaypoint(List<Maneuver> maneuverList) {
        for (int i = 0; i < maneuverList.size(); i++) {
            //para cada Maneuver chamar o waypoints
            wayPoints(maneuverList.get(i));

        }
    }
    public void wayPoints(final Maneuver maneuver) {

        maneuverFromPlan= maneuver;
        makePoints();
    }



    public void makePoints(){

        GeoPoint ponto;
        waypointsFromPlan = PlanUtilities.computeWaypoints(maneuverFromPlan);

        for (PlanUtilities.Waypoint point : waypointsFromPlan) {


            valueOfLatitude = point.getLatitude();
            valueOfLongitude = point.getLongitude();

            ponto = new GeoPoint(valueOfLatitude, valueOfLongitude);

            if (!(planWaypoints.contains(ponto))) {

                if (planWaypoints.size() != maneuverList.size())
                    planWaypoints.add(ponto);


            }

        }
        planWaypointPolyline.setWidth(5);
        planWaypointPolyline.setPoints(planWaypoints);

System.out.println(planWaypoints.toString() + " -------------main");    }

    public void warning() {
        Toast.makeText(this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
    }












}


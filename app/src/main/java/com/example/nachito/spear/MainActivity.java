package com.example.nachito.spear;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import android.support.v7.app.ActionBar;
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
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
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
import pt.lsts.imc.net.Consume;
import pt.lsts.neptus.messages.listener.Periodic;
import pt.lsts.util.PlanUtilities;
import pt.lsts.util.WGS84Utilities;

import static android.os.Build.VERSION_CODES.M;
import static com.example.nachito.spear.R.id.imageView;


@EActivity

public class MainActivity extends AppCompatActivity
        implements MapViewConstants,  OnLocationChangedListener,  LocationListener, SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {

    private Context context;
    @ViewById(R.id.dive)
    Button dive;
    @ViewById(R.id.near)
    Button near;
    @ViewById(R.id.startplan)
    Button start;
    @ViewById(R.id.KeepStation)
    Button keep;
    @ViewById(R.id.servicebar)
    TextView servicebar;
    @Bean
    static IMCGlobal imc;
    @ViewById(imageView)
    ImageView wifi;
    @ViewById(R.id.imageView2)
    ImageView nowifi;
    TeleOperation teleop2;
    List<Maneuver> mList;
    List<VehicleState> states;
    @ViewById(R.id.map)
    static MapView map;
    MyLocationNewOverlay mLocationOverlay;
    @ViewById(R.id.teleop)
    Button teleop;
    @ViewById(R.id.stop)
    Button stop;
    CompassOverlay mCompassOverlay;
    LocationManager locationManager;
    IMapController mapController;
    OverlayItem lastPosition = null;
    OsmMapsItemizedOverlay mItemizedOverlay;
    static double latVeiculo;
    static double lonVeiculo;
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
    Line line;
    static Press trans;
    @org.androidannotations.annotations.res.DrawableRes(R.drawable.orangeled)
    static Drawable nodeIcon;
    @org.androidannotations.annotations.res.DrawableRes(R.drawable.blueled)
    static Drawable lineIcon;
    Area area;
    @SuppressLint("StaticFieldLeak")
    @ViewById(R.id.done)
    static Button done;
    @SuppressLint("StaticFieldLeak")
    @ViewById(R.id.erase)
    static Button erase;
    static ArrayList<GeoPoint> markerPoints = new ArrayList<>();
    static Polyline polyline;
    static Marker lineMarker;
    static Polygon circle;
    boolean showrpm;
    //static - so Area and Line can pass the points clicked
    @SuppressLint("StaticFieldLeak")
    @ViewById(R.id.vel2)
    static TextView velocityTextView;
    static String dept;
    static String estadoVeiculo;
    static String vel;
    static Collection<PlanUtilities.Waypoint> points;
    static Marker nodeMarkerWaypoints;
    Location location;
    RotationGestureOverlay mRotationGestureOverlay;
    static String previous=null;
    Double valLat;
    Double valLon;
    List<String> vehicleList;
    List<String> planList;
    int tamanhoLista;
    OSMHandler updateHandler;
    List<String> stateList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setClickable(true);
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), map);
        mLocationOverlay.enableMyLocation();
        map.getOverlays().add(this.mLocationOverlay);
        mCompassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context), map);
        mCompassOverlay.enableCompass();
        map.getOverlays().add(this.mCompassOverlay);
        mRotationGestureOverlay = new RotationGestureOverlay(context, map);
        mRotationGestureOverlay.setEnabled(true);
        map.setMultiTouchControls(true);
        map.getOverlays().add(this.mRotationGestureOverlay);
        velocity.bringToFront();
        setupSharedPreferences();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        init();
        Accelerate accelerate = (Accelerate) findViewById(R.id.accelerate);
        accelerate.setVisibility(View.INVISIBLE);
        Decelerate decelerate = (Decelerate) findViewById(R.id.decelerate);
        decelerate.setVisibility(View.INVISIBLE);
        Joystick joystick = (Joystick) findViewById(R.id.joystick);
        joystick.setVisibility(View.INVISIBLE);
        nowifi.setVisibility(View.INVISIBLE);
        StopTeleop stopTeleop = (StopTeleop) findViewById(R.id.stopTeleop);
        stopTeleop.setVisibility(View.INVISIBLE);
        trans = (Press) findViewById(R.id.transparente);
        trans.setVisibility(View.INVISIBLE);
        done.setVisibility(View.INVISIBLE);
        erase.setVisibility(View.INVISIBLE);
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
        map.getOverlays().add(this.mLocationOverlay);
        mLocationOverlay.enableMyLocation();
        map.invalidate();
        mapController = map.getController();
        mapController.setZoom(12);
        mapController.setCenter(new GeoPoint(location));


        ReceiveSms.bindListener(new SmsListener() {
            @Override
            public void messageReceived(String messageText) {
                  Pattern p = Pattern.compile("\\((.)\\) \\((.*)\\) (.*) / (.*), (.*) / .*");



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

                //dividir o 2 por 60
                //somar o 1 pelo 2


                GeoPoint coordSMS= new GeoPoint(lat, lon);

                System.out.println(coordSMS + " coordinates from sms");


                mapController.setCenter(coordSMS);
                mapController.setZoom(18);
            }
        });
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
                // this thread waiting for the user's response! After the user  timer.start()
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
        registerForContextMenu(servicebar);
        openContextMenu(servicebar);
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
        this.showrpm = showrpm;

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

        teleop.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (imc.selectedvehicle == null) {
                    warning();
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder
                            .setMessage("Connect to " + imc.selectedvehicle + "?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new OnClickListener() {

                                public void onClick(DialogInterface dialog, int id) {
                                    if (teleop2 == null)
                                        teleop2 = new TeleOperation();
                                    teleop2.setImc(imc);
                                    Joystick joystick = (Joystick) findViewById(R.id.joystick);
                                    joystick.setOnJoystickMovedListener(teleop2);
                                    getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.activity_maps, teleop2).addToBackStack("tag").commit();
                                    dive.setVisibility(View.INVISIBLE);
                                    teleop.setVisibility(View.INVISIBLE);
                                    start.setVisibility(View.INVISIBLE);
                                    near.setVisibility(View.INVISIBLE);
                                    keep.setVisibility(View.INVISIBLE);
                                    Accelerate accelerate = (Accelerate) findViewById(R.id.accelerate);
                                    accelerate.setVisibility(View.VISIBLE);
                                    accelerate.setOnAccelerate(teleop2);
                                    Decelerate decelerate = (Decelerate) findViewById(R.id.decelerate);
                                    decelerate.setVisibility(View.VISIBLE);
                                    decelerate.setOnDec(teleop2);
                                    stop.setVisibility(View.INVISIBLE);
                                    StopTeleop stopTeleop = (StopTeleop) findViewById(R.id.stopTeleop);
                                    stopTeleop.setVisibility(View.VISIBLE);
                                    stopTeleop.setOnStop(teleop2);
                                    timer.start();
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
                                    setEstadoVeiculo(" ");
                                }
                            })
                            .setNegativeButton("No", new OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }

            }
        });


        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                start.setBackgroundColor(pressed_color);

                if (imc.selectedvehicle == null) {
                    warning();
                    start.setBackgroundColor(color);

                } else {
                    requestPlans();
                    start.setBackgroundColor(color);

                }

            }

        });


        dive.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dive.setBackgroundColor(pressed_color);

                if (imc.selectedvehicle == null) {
                    warning();
                    dive.setBackgroundColor(color);

                } else {
                    dive();
                    dive.setBackgroundColor(color);

                }
            }
        });

        near.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                near.setBackgroundColor(pressed_color);

                if (imc.selectedvehicle == null) {
                    warning();
                    near.setBackgroundColor(color);

                } else {
                    near();
                    near.setBackgroundColor(color);

                }

            }

        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (imc.selectedvehicle == null) {
                    warning();
                } else {
                    stopPlan();
                    map.getOverlays().clear();
                    mCompassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context), map);
                    mCompassOverlay.enableCompass();
                    map.getOverlays().add(mCompassOverlay);
                    map.setMultiTouchControls(true);

                }
            }


        });

        keep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keep.setBackgroundColor(pressed_color);

                if (imc.selectedvehicle == null) {
                    warning();
                    keep.setBackgroundColor(color);

                } else {
                    keepStation();
                    keep.setBackgroundColor(color);
                }
            }
        });


    }


    @Override
    public void onBackPressed() {


        if (teleop2 != null) {
            teleop2.finish();


            getFragmentManager().popBackStack();
            dive.setVisibility(View.VISIBLE);
            teleop.setVisibility(View.VISIBLE);
            start.setVisibility(View.VISIBLE);
            near.setVisibility(View.VISIBLE);
            keep.setVisibility(View.VISIBLE);
            stop.setVisibility(View.VISIBLE);
            Accelerate accelerate = (Accelerate) findViewById(R.id.accelerate);
            accelerate.setVisibility(View.INVISIBLE);
            Decelerate decelerate = (Decelerate) findViewById(R.id.decelerate);
            decelerate.setVisibility(View.INVISIBLE);
            StopTeleop stopTeleop = (StopTeleop) findViewById(R.id.stopTeleop);
            stopTeleop.setVisibility(View.INVISIBLE);
            Joystick joystick = (Joystick) findViewById(R.id.joystick);
            joystick.setVisibility(View.INVISIBLE);
        } else if (line != null) {
            line.finish();
            trans.setVisibility(View.INVISIBLE);
            bottom.setVisibility(View.VISIBLE);
            done.setVisibility(View.INVISIBLE);
            erase.setVisibility(View.INVISIBLE);


        } else if (area != null) {
            area.finish();
            trans.setVisibility(View.INVISIBLE);
            bottom.setVisibility(View.VISIBLE);
            bottom.setVisibility(View.VISIBLE);
            done.setVisibility(View.INVISIBLE);
            erase.setVisibility(View.INVISIBLE);


        } else
            finish();
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
           // startActivity(new Intent(this, SendSms.class));

            return true;
        }


        else if (id == R.id.edit) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder
                    .setMessage("Area or Line?")
                    .setCancelable(true)
                    .setPositiveButton("Line", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            if (area != null)
                                area.finish();
                            line = new Line();
                            bottom.setVisibility(View.INVISIBLE);
                            mCompassOverlay.disableCompass();
                            done.setClickable(true);
                            done.setOnClickListener(line);
                            velocityTextView.setVisibility(View.VISIBLE);
                            erase.setClickable(true);
                            erase.setOnClickListener(line);
                            trans.setonPress(line);
                            trans.setVisibility(View.VISIBLE);
                            line.setImc(imc);


                        }
                    })
                    .setNegativeButton("Area", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (line != null)
                                line.finish();

                            bottom.setVisibility(View.INVISIBLE);
                            area = new Area();
                            mCompassOverlay.disableCompass();
                            done.setClickable(true);
                            done.setOnClickListener(area);
                            erase.setClickable(true);
                            velocityTextView.setVisibility(View.VISIBLE);

                            erase.setOnClickListener(area);
                            trans.setonPress(area);
                            trans.setVisibility(View.VISIBLE);
                            area.setImc(imc);

                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister MainActv as an OnPreferenceChangedListener to avoid any memory leaks.
        android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        area = null;
        line = null;
        done = null;

        erase = null;
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
        final ArrayList<OverlayItem> items = new ArrayList<>();
        latitude = Math.toRadians(location.getLatitude());
        longitude = Math.toRadians(location.getLongitude());
      GeoPoint posicao = new GeoPoint(location.getLatitude(), location.getLongitude());


        OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", posicao);
        marker.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
        items.add(marker);

       Bitmap newMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.arrowred), 70, 70, false);
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

            if(imc.selectedvehicle!=null){
                if(!(imc.stillConnected().contains(imc.selectedvehicle)))
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            servicebar.setText(" ");
                            velocity.setText(" ");

                        }
                    });
            }
            if (imc.stillConnected().contains(vname)) {

                double[] lld = WGS84Utilities.toLatLonDepth(state);
                final ArrayList<OverlayItem> items2 = new ArrayList<>();


                OverlayItem marker2 = new OverlayItem("markerTitle", "markerDescription", new GeoPoint(lld[0], lld[1]));
                marker2.setMarkerHotspot(HotspotPlace.TOP_CENTER);
                items2.add(marker2);

                double orientation2 = state.getPsi();
                int ori2 = (int) Math.round(Math.toDegrees(orientation2));
                ori2 = ori2 - 180;

                Bitmap source2 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.downarrow), 70, 70, false);


                if (vname.equals(imc.getSelectedvehicle())) {

                   GeoPoint posicaoVeiculo = new GeoPoint(lld[0], lld[1]);


                    source2 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.arrowgreen), 70, 70, false);

                    DecimalFormat df2 = new DecimalFormat("#.##");
                    vel = df2.format(Math.sqrt((state.getVx() * state.getVx()) + (state.getVy() * state.getVy()) + (state.getVz() * state.getVz())));
                    dept = df2.format(state.getDepth());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            velocity.setText("Speed:" + " " + vel + " " + "m/s" + "\n" + "Depth:" + " " + dept + "\n" +  estadoVeiculo + "\n");

                        }
                    });

                }


               Bitmap target = RotateMyBitmap(source2, ori2);
                Drawable marker_ = new BitmapDrawable(getResources(), target);
               ItemizedIconOverlay markersOverlay2 = new ItemizedIconOverlay<>(items2, marker_, null, context);
                map.getOverlays().add(markersOverlay2);
            }
        }
    }

    public void zoomVehicle(final EstimatedState state) {
        if (imc.getSelectedvehicle().equals(state.getSourceName())) {
            double[] lld = WGS84Utilities.toLatLonDepth(state);

           GeoPoint posicaoVeiculo2 = new GeoPoint(lld[0], lld[1]);
            mapController.setZoom(16);
            mapController.setCenter(posicaoVeiculo2);

        }
    }

    public static Bitmap RotateMyBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    @Background
    @Periodic
    public void updateState() {
        map.setMultiTouchControls(true);
         stateList = new ArrayList<>();
        states = imc.connectedVehicles();
        if (imc.connectedVehicles() == null)
            warning();

        for (VehicleState state : states) {
            if(imc.selectedvehicle!=null)
                if(imc.selectedvehicle.equals(state.getSourceName()))
                    stateList.add(state.getOpModeStr());

        }


        for (int i = 0; i < stateList.size(); i++) {

            String stateconncected = stateList.toString();


            if (previous!=null && stateconncected.charAt(1) == 'S') {

                map.getOverlays().remove(lineMarker);
                map.getOverlays().remove(nodeMarkerWaypoints);
                map.getOverlays().remove(points);
                if(points!=null)
                    points.clear();
                points=null;
                mList.clear();

                map.getOverlays().clear();

                if (line != null) {
                    map.getOverlays().remove(lineMarker);
                    line = null;
                    updateMap();
                } else if (area != null) {
                    map.getOverlays().remove(lineMarker);
                    area = null;
                    updateMap();
                }
            }

        }
    }
    public static void  setEstadoVeiculo(String e){

        estadoVeiculo=e;
    }

    @Background
    @Periodic(500)
    public void updateMap() {
        map.getOverlays().clear();

        map.getOverlays().add(mCompassOverlay);

        map.setMultiTouchControls(true);
        map.getOverlays().add(this.mRotationGestureOverlay);
        synchronized (estates) {
            for (EstimatedState state : estates.values()) {
                paintState(state);
            }

        }

        if (points != null) {

            callWaypoint(mList);

        }

        if (location != null)
            onLocationChanged(location);


        if (line != null) {
            for (int i = 0; i < markerPoints.size(); i++) {
                lineMarker = new Marker(map);
                lineMarker.setPosition(markerPoints.get(i));
                lineMarker.setIcon(lineIcon);
                lineMarker.isDraggable();
                lineMarker.setDraggable(true);
                lineMarker.setTitle("lat/lon:" + markerPoints.get(i));
                map.getOverlays().add(lineMarker);
            }
            if (polyline != null)
                map.getOverlays().add(polyline);


        } else if (area != null) {
            for (int i = 0; i < markerPoints.size(); i++) {
                lineMarker = new Marker(map);
                lineMarker.setPosition(markerPoints.get(i));
                lineMarker.setIcon(lineIcon);
                lineMarker.isDraggable();
                lineMarker.setDraggable(true);
                lineMarker.setTitle("lat/lon:" + markerPoints.get(i));
                map.getOverlays().add(lineMarker);
            }
            if (circle != null)
                map.getOverlays().add(circle);

        }

    }

    public void dive() {
        Loiter dive = new Loiter();
        dive.setLon(lonVeiculo);
        dive.setLat(latVeiculo);
        dive.setZ(depth);
        dive.setZUnits(Loiter.Z_UNITS.DEPTH);
        dive.setSpeed(speed);
        if (!showrpm) {
            dive.setSpeedUnits(Loiter.SPEED_UNITS.METERS_PS);
        } else {
            dive.setSpeedUnits(Loiter.SPEED_UNITS.RPM);
        }
        dive.setRadius(radius);
        dive.setDuration(duration);
        dive.setBearing(0);
        String planid = "SpearDive-" + imc.selectedvehicle;
        startBehaviour(planid, dive);

        wayPoints(dive);

    }

    public void keepStation() {
        StationKeeping stationKeepingmsg = new StationKeeping();
        stationKeepingmsg.setLat(latVeiculo);
        stationKeepingmsg.setLon(lonVeiculo);
        stationKeepingmsg.setSpeed(speed);
        if (!showrpm) {
            stationKeepingmsg.setSpeedUnits(StationKeeping.SPEED_UNITS.METERS_PS);
        } else {
            stationKeepingmsg.setSpeedUnits(StationKeeping.SPEED_UNITS.RPM);
        }
        stationKeepingmsg.setDuration(duration);
        stationKeepingmsg.setRadius(radius);
        stationKeepingmsg.setZ(depth);
        stationKeepingmsg.setZUnits(StationKeeping.Z_UNITS.DEPTH);
        String planid = " SpearStationKeeping-" + imc.selectedvehicle;
        startBehaviour(planid, stationKeepingmsg);
        wayPoints(stationKeepingmsg);

    }

    public void near() {
        if (latitude == 0 & longitude == 0) {
            return;
        }
        final Goto go = new Goto();
        go.setLat(latitude);
        go.setLon(longitude);
        go.setZ(0);
        go.setZUnits(Goto.Z_UNITS.DEPTH);
        go.setSpeed(speed);
        if (!showrpm) {
            go.setSpeedUnits(Goto.SPEED_UNITS.METERS_PS);
        } else {
            go.setSpeedUnits(Goto.SPEED_UNITS.RPM);
        }
        String planid = "SpearComeNear-" + imc.selectedvehicle;
        startBehaviour(planid, go);
        wayPoints(go);


    }


    public static void startBehaviour(String planid, IMCMessage what) {
        PlanControl pc = new PlanControl();
        pc.setArg(what);
        pc.setType(PlanControl.TYPE.REQUEST);
        pc.setOp(PlanControl.OP.START);
        pc.setFlags(0);
        pc.setRequestId(0);
        pc.setPlanId(planid);

        map.getOverlays().remove(nodeMarkerWaypoints);
        if(points!=null)
            points.clear();
        points=null;
        map.invalidate();
        map.getOverlays().clear();

        imc.sendMessage(pc);
        previous="M";


    }


    public void stopPlan() {
        PlanControl pc = new PlanControl();
        pc.setType(PlanControl.TYPE.REQUEST);
        pc.setOp(PlanControl.OP.STOP);
        pc.setRequestId(1);
        pc.setFlags(0);
        pc.setPlanId("stopPlan-" + imc.selectedvehicle);
        setEstadoVeiculo("Plan Stopped");
        previous="S";


        map.getOverlays().remove(nodeMarkerWaypoints);
        if(points!=null)
            points.clear();
        points=null;
        map.invalidate();
        map.getOverlays().clear();
        mList.clear();


        imc.sendMessage(pc);

        if (line != null) {
            map.getOverlays().remove(lineMarker);
            line = null;
            updateMap();
        } else if (area != null) {
            map.getOverlays().remove(lineMarker);
            area = null;
            updateMap();
        }

    }


    @Override
    public void onStart() {
        timer.start();
        super.onStart();
    }

    //if there is wifi show an imageview, when the wifi is off change it to another
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
    TimerWifi timer = new TimerWifi(new Runnable() {
        @Override
        public void run() {
            // Check if is connected to a Wifi Network, if not popups a informative toast
            if (!isConnectedToWifi(MainActivity.this)) {
                wifi.setVisibility(View.INVISIBLE);
                nowifi.setVisibility(View.VISIBLE);
            } else {
                nowifi.setVisibility(View.INVISIBLE);
                wifi.setVisibility(View.VISIBLE);
            }
        }
    }, 3000);


    public void requestPlans() {
        if (imc.selectedvehicle == null) {
            warning();

        } else {
            PlanDB pdb = new PlanDB();
            pdb.setOp(PlanDB.OP.GET_INFO);
            imc.sendMessage(pdb);
            registerForContextMenu(start);
            openContextMenu(start);
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


            tamanhoLista = planList.size();
        } else if (v.getId() == R.id.servicebar) {


           vehicleList = new ArrayList<>();
            mList = new ArrayList<>();


            states = imc.connectedVehicles();
            if (imc.connectedVehicles() == null)
                warning();
            for (VehicleState state : states) {
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
            setEstadoVeiculo("Plan:" + pc.getPlanId());
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (imc.allManeuvers() == null) {
                        Toast.makeText(MainActivity.this, "No plan specification available", Toast.LENGTH_SHORT).show();
                    } else {


                        for (int i = 0; i < imc.allManeuvers().size(); i++) {

                            mList.addAll(imc.allManeuvers());

                        }
                        callWaypoint(mList);

                    }
                }
                },3000);
            }


        else {
            String selected = item.toString();
            String[] getName2 = selected.split(":");
            String selectedName2 = getName2[0];
            setEstadoVeiculo( getName2[1]);
            imc.setSelectedvehicle(selectedName2.trim());
            servicebar.setText(selectedName2);


            synchronized (estates) {
                for (EstimatedState state : estates.values()) {
                    zoomVehicle(state);
                }


            }
        }
        if (teleop2 != null) {
            teleop2.finish();
        }

        return super.onContextItemSelected(item);
    }

    public void callWaypoint(List<Maneuver> maneuverList) {

        for (int i = 0; i < maneuverList.size()-1; i++) {

            wayPoints(maneuverList.get(i));

        }

    }
    public void wayPoints(final Maneuver maneuver) {

        points = PlanUtilities.computeWaypoints(maneuver);
        GeoPoint ponto;
        if (points != null) {


            for (PlanUtilities.Waypoint point : points) {

                valLat = point.getLatitude();
                valLon = point.getLongitude();
                ponto= new GeoPoint(valLat, valLon);

                nodeMarkerWaypoints = new Marker(map);
                nodeMarkerWaypoints.setPosition(ponto);
                nodeMarkerWaypoints.setIcon(nodeIcon);

                map.getOverlays().add(nodeMarkerWaypoints);
//linha a ligar os pontos
            }
        }

    }

    public void warning() {
        Toast.makeText(this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
    }

}
//TODO escala
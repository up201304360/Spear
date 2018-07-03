package com.example.nachito.spear;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.hardware.SensorEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.hardware.SensorEventListener;
import android.os.Bundle;

import org.slf4j.helpers.MarkerIgnoringBase;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.jar.Attributes;

public class Compass extends AppCompatActivity implements SensorEventListener {
    // device sensor manager
    private SensorManager SensorManage;
    // define the compass picture that will be use
    private ImageView compassimage;
    // record the angle turned of the compass picture
    TextView DegreeTV;
    TextView NameTV;
    TextView VehicleDirectTV;
    float az;
    TextView DistanceTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass);
        //
        compassimage =  findViewById(R.id.compass_image);
        // TextView that will display the degree
        DegreeTV =  findViewById(R.id.DegreeTV);
        NameTV = findViewById(R.id.NameTV);
        VehicleDirectTV = findViewById(R.id.DirectionVehicleTV);

        // initialize your android device sensor capabilities
        SensorManage = (SensorManager) getSystemService(SENSOR_SERVICE);
        DistanceTV =  findViewById(R.id.DirectionTV);

    }
    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        SensorManage.unregisterListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // code for system's orientation sensor registered listeners
        SensorManage.registerListener(this, SensorManage.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {
        // get angle around the z-axis rotated
        final int R = 6371; // Radius of the earth
        double latDistance = MainActivity.latVehicle - (MainActivity.latitudeAndroid);
        double lonDistance = MainActivity.lonVehicle - (MainActivity.longitudeAndroid);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos((MainActivity.latitudeAndroid)) * Math.cos(MainActivity.latVehicle)
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        DecimalFormat twoDForm = new DecimalFormat("#.##");
        String distFinal = (twoDForm.format(distance));


        DistanceTV.setText("Distance to Vehicle: " + distFinal + " m");


        NameTV.setText("Name: " + MainActivity.vehicleName);


        double degree = Math.toDegrees(MainActivity.vehicleOrientation);
        DecimalFormat twoDFormHeading = new DecimalFormat("#.##");
        String headingFinal = twoDFormHeading.format(degree);


        DegreeTV.setText("Vehicle Heading: " + headingFinal);


        double angle = Math.atan2(Math.sin(MainActivity.lonVehicle - MainActivity.longitudeAndroid) * Math.cos(MainActivity.latVehicle), ((Math.cos(MainActivity.latitudeAndroid) * Math.sin(MainActivity.latVehicle)) - (Math.sin(MainActivity.latitudeAndroid) * Math.cos(MainActivity.latVehicle) * Math.cos((MainActivity.lonVehicle - MainActivity.longitudeAndroid)))));
        double angleDegree = Math.toDegrees(angle) - (MainActivity.orientationCompass);
        double angleMod = (angleDegree + 360) % 360;


        DecimalFormat twoDFormHeading2 = new DecimalFormat("#.##");
        String headingFinal2 = twoDFormHeading2.format(angleMod);

        VehicleDirectTV.setText("Angle to Vehicle:  " + headingFinal2);


        RotateAnimation ra = new RotateAnimation(az, MainActivity.vehicleOrientation
                ,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        compassimage.startAnimation(ra);

        az = (float) angleMod;

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }
}
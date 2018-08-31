package com.example.nachito.spear;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;

public class Compass extends AppCompatActivity implements SensorEventListener {
    TextView VehicleDirectTV;
    float az;
    TextView DistanceTV;
    // device sensor manager
    private SensorManager SensorManage;
    // define the compass picture that will be use
    private ImageView compassimage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass);
        //
        compassimage = findViewById(R.id.compass_image);
        // TextView that will display the degree

        VehicleDirectTV = findViewById(R.id.DirectionVehicleTV);

        // initialize your android device sensor capabilities
        SensorManage = (SensorManager) getSystemService(SENSOR_SERVICE);
        DistanceTV = findViewById(R.id.DirectionTV);

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


        DistanceTV.setText("Distance: " + distFinal + " m");


        double angle = Math.atan2(Math.sin(MainActivity.lonVehicle - MainActivity.longitudeAndroid) * Math.cos(MainActivity.latVehicle), ((Math.cos(MainActivity.latitudeAndroid) * Math.sin(MainActivity.latVehicle)) - (Math.sin(MainActivity.latitudeAndroid) * Math.cos(MainActivity.latVehicle) * Math.cos((MainActivity.lonVehicle - MainActivity.longitudeAndroid)))));
        double angleDegree = Math.toDegrees(angle) - (MainActivity.orientationCompass);
        double angleMod = (angleDegree + 360) % 360;


        DecimalFormat twoDFormHeading2 = new DecimalFormat("#.##");
        String headingFinal2 = twoDFormHeading2.format(angleMod);

        VehicleDirectTV.setText("Bearing:  " + headingFinal2);


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
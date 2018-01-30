package com.example.nachito.spear;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import org.osmdroid.util.GeoPoint;

/**
 *
 * Created by nachito on 25/04/17.
 */

public class OSMHandler implements LocationListener {

    private MainActivity mMapActivity;

    OSMHandler(MainActivity aMapActivity) {
        this.mMapActivity = aMapActivity;

    }

    @Override
    public void onLocationChanged(Location location) {


        int latitude = (int) (location.getLatitude() * 1E6);
        int longitude = (int) (location.getLongitude() * 1E6);
        GeoPoint point = new GeoPoint(latitude, longitude);
        mMapActivity.updatePosition(point);

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

}
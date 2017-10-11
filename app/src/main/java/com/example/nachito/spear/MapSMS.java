package com.example.nachito.spear;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;


/**
 * Created by ines on 10/9/17.
 */

public class MapSMS extends AppCompatActivity  {

    IMapController mapController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms);
        MapView map= (MapView) findViewById(R.id.mapSMS);
        map.setMultiTouchControls(true);
        android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }

        mapController = map.getController();
        mapController.setZoom(16);
        GeoPoint centro= MainActivity.getVariables();
        System.out.println(centro);
        mapController.setCenter(centro);


        final ArrayList<OverlayItem> items = new ArrayList<>();

        OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", centro);
        marker.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
        items.add(marker);

        Bitmap newMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.arrowgreen), 70, 70, false);
        Drawable marker3 = new BitmapDrawable(getResources(), newMarker);
        ItemizedIconOverlay markersOverlay = new ItemizedIconOverlay<>(items, marker3, null, this);
        map.getOverlays().add(markersOverlay);



        GeoPoint loc= MainActivity.localizacao();

        final ArrayList<OverlayItem> items2 = new ArrayList<>();

        OverlayItem marker2 = new OverlayItem("markerTitle", "markerDescription", loc);
        marker.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
        items.add(marker2);

        Bitmap newMarker2 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.arrowred), 70, 70, false);
        Drawable markerLoc = new BitmapDrawable(getResources(), newMarker2);
        ItemizedIconOverlay markersOverlay2 = new ItemizedIconOverlay<>(items2, markerLoc, null, this);
        map.getOverlays().add(markersOverlay2);



        MapEventsReceiver mReceive = new MapEventsReceiver() {

            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {

                Intent intent;
                double lat=p.getLatitude();
                double lon=p.getLongitude();
                intent= new Intent(MapSMS.this, SendSms.class);
                intent.putExtra("Latitude", lat);
                intent.putExtra("Longitude", lon);
                startActivity(intent);
                System.out.println("Long Press" + lat + lon);
                return true;
            }


        };

        MapEventsOverlay OverlayEventos = new MapEventsOverlay(this.getBaseContext(), mReceive);
        map.getOverlays().add(OverlayEventos);
        //Refreshing the map to draw the new overlay
        map.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

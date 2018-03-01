package com.example.nachito.spear;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
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
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;
import static com.example.nachito.spear.MainActivity.localizacao;
import static com.example.nachito.spear.MainActivity.zoomLevel;


/**
 *
 * Created by ines on 10/9/17.
 */

public class MapSMS extends AppCompatActivity {

    static double lat;
    static double lon;
    static ArrayList<GeoPoint> markers = new ArrayList<>();
    IMapController mapController;
    Button done;
    MapView map;
    Button eraseSMS;
    int numPontos;
    Drawable nodeIcon;
    Marker startMarker;
    ArrayList<GeoPoint> posicaoOutrosVeiculos;
    GeoPoint centro;
    final OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", centro);
    com.example.nachito.spear.ScaleBarOverlay scaleBarOverlay;

    public static GeoPoint resultado() {
        return new GeoPoint(lat, lon);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms);
        map =  findViewById(R.id.mapSMS);
        done =  findViewById(R.id.doneSMS);
        nodeIcon = getResources().getDrawable(R.drawable.orangeled);
        eraseSMS =  findViewById(R.id.eraseSMS);
        List<Marker> markerListSMS = new ArrayList<>();
        map.setMultiTouchControls(true);
        scaleBarOverlay = new com.example.nachito.spear.ScaleBarOverlay(map);
        List<Overlay> overlays = map.getOverlays();
        Toast.makeText(this, "Long Click on the map to choose a location for the vehicle to go", Toast.LENGTH_SHORT).show();
        overlays.add(scaleBarOverlay);


        android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
        if (MainActivity.isOfflineSelected) {
            map.setTileSource(new XYTileSource("4uMaps", 2, 18, 256, ".png", new String[]{}));
        }
        mapController = map.getController();
        mapController.setZoom(zoomLevel);
        centro = MainActivity.getVariables();
        if (centro != null)
            mapController.setCenter(centro);
        else
            mapController.setCenter(localizacao());
        mapController = map.getController();
        mapController.setZoom(MainActivity.zoomLevel);

        drawGreen();
        drawRed();
        drawBlue();


        MapEventsReceiver mReceive = new MapEventsReceiver() {

            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                System.out.println(" ");
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
                markerListSMS.add(startMarker);
                map.getOverlays().add(startMarker);
                startMarker.setIcon(getResources().getDrawable(R.drawable.orangeled));
                startMarker.setTitle(p.toString());
                map.invalidate();
                numPontos++;

                System.out.println(numPontos + " - ");


                eraseSMS.setOnClickListener(v -> {


                    for (Marker m : markerListSMS) {
                        m.remove(map);
                        map.invalidate();
                    }
                    markerListSMS.clear();
                    markers.clear();
                    numPontos = 0;
                    drawGreen();
                    drawBlue();
                    drawRed();



                });
                return true;

            }

        };
        done.setOnClickListener(v -> {
            if (numPontos == 1) {
                Intent i = new Intent(MapSMS.this, SendSms.class);
                startActivity(i);
            } else if (numPontos == 0) {
                Toast.makeText(MapSMS.this, "Select one point", Toast.LENGTH_SHORT).show();
            } else if (numPontos > 1)
                Toast.makeText(MapSMS.this, "Select ONLY one point", Toast.LENGTH_SHORT).show();

        });

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

            newMarker2 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.arrowred2), 0, 0, true);

        }
        Bitmap target = MainActivity.RotateMyBitmap(newMarker2, MainActivity.bearingMyLoc);
        Drawable markerLoc = new BitmapDrawable(getResources(), target);
        final ItemizedIconOverlay markersOverlay2 = new ItemizedIconOverlay<>(items2, markerLoc, null, this);
        map.getOverlays().add(markersOverlay2);


    }

    public void drawBlue() {

        for (int i = 0; i < posicaoOutrosVeiculos.size(); i++) {
            if (posicaoOutrosVeiculos.get(i) != centro) {
                final ArrayList<OverlayItem> itemsPoints = new ArrayList<>();
                OverlayItem markerPoints = new OverlayItem("markerTitle", "markerDescription", posicaoOutrosVeiculos.get(i));
                System.out.println(posicaoOutrosVeiculos.get(i));
                markerPoints.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
                itemsPoints.add(markerPoints);
                Resources resources = this.getResources();
                Bitmap source2;
                if (android.os.Build.VERSION.SDK_INT <= M) {
                    source2 = Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.downarrow2));

                } else

                    source2 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.downarrow2), 0, 0, true);
                Bitmap target = MainActivity.RotateMyBitmap(source2, MainActivity.orientationOtherVehicles.get(i));
                Drawable marker_ = new BitmapDrawable(getResources(), target);
                ItemizedIconOverlay markersOverlay_ = new ItemizedIconOverlay<>(itemsPoints, marker_, null, this);
                map.getOverlays().add(markersOverlay_);
            }
        }
    }


    public void drawGreen() {
        if (centro != null) {
            final ArrayList<OverlayItem> items = new ArrayList<>();
            final OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", centro);
            marker.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
            items.add(marker);
            Bitmap newMarker;
            Resources resources = this.getResources();

            if (android.os.Build.VERSION.SDK_INT <= M) {
                newMarker = Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrowgreen2));

            } else

                newMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrowgreen2), 0, 0, true);

            Bitmap target = MainActivity.RotateMyBitmap(newMarker, MainActivity.orientationSelected);
            Drawable markerLoc = new BitmapDrawable(getResources(), target);
            final ItemizedIconOverlay markersOverlay2 = new ItemizedIconOverlay<>(items, markerLoc, null, this);
            map.getOverlays().add(markersOverlay2);

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

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

    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}

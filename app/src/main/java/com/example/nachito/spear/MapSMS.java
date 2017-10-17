package com.example.nachito.spear;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.os.Handler;



/**
 * Created by ines on 10/9/17.
 */

public class MapSMS extends AppCompatActivity {

    IMapController mapController;
    static double lat;
    static double lon;
    Button done;
    MapView map;
    int numPontos;
    Drawable nodeIcon;
    Marker startMarker;
    ArrayList<GeoPoint> posicaoOutrosVeiculos;
    GeoPoint centro;
    ScaleBarOverlay scaleBarOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms);
        map = (MapView) findViewById(R.id.mapSMS);
        done = (Button) findViewById(R.id.doneSMS);
        nodeIcon = getResources().getDrawable(R.drawable.orangeled);
        map.setMultiTouchControls(true);
        scaleBarOverlay = new ScaleBarOverlay( map);
        List<Overlay> overlays = map.getOverlays();
        Toast.makeText(this,"Long Click on the map to choose a location for the vehicle to go", Toast.LENGTH_SHORT).show();
        overlays.add(scaleBarOverlay);


        android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }

        mapController = map.getController();
        mapController.setZoom(16);
        centro = MainActivity.getVariables();
        System.out.println(centro);
        mapController.setCenter(centro);
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

                startMarker = new Marker(map);
                startMarker.setPosition(p);
                startMarker.isDraggable();

                map.getOverlays().add(startMarker);
                startMarker.setIcon(getResources().getDrawable(R.drawable.orangeled));
                startMarker.setTitle(p.toString());
                map.invalidate();
                numPontos++;

                System.out.println(numPontos + " - ");


                startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {

                        map.getOverlayManager().remove(startMarker);
                        map.invalidate();


                        numPontos--;

                        return true;
                    }
                });
                return true;
            }
        };


        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numPontos == 1) {
                    Intent i = new Intent(MapSMS.this, SendSms.class);
                    startActivity(i);
                } else if (numPontos == 0) {
                    Toast.makeText(MapSMS.this, "Select one point", Toast.LENGTH_SHORT).show();
                } else if (numPontos > 1)
                    Toast.makeText(MapSMS.this, "Select ONLY one point", Toast.LENGTH_SHORT).show();

            }
        });

        MapEventsOverlay OverlayEventos = new MapEventsOverlay(this.getBaseContext(), mReceive);
        map.getOverlays().add(OverlayEventos);

        //Refreshing the map to draw the new overlay
        map.invalidate();

    }

    final OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", centro);


    public static GeoPoint resultado() {
        return new GeoPoint(lat, lon);
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

        Bitmap newMarker2 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.arrowred), 70, 70, false);
        float orientation2 = MainActivity.orientation();
        int ori2 = (int) Math.round(Math.toDegrees(orientation2));
        ori2 = ori2-180;
        //TODO

        Bitmap target = MainActivity.RotateMyBitmap(newMarker2,ori2);


        Drawable markerLoc = new BitmapDrawable(getResources(), target);
        final ItemizedIconOverlay markersOverlay2 = new ItemizedIconOverlay<>(items2, markerLoc, null, this);
        map.getOverlays().add(markersOverlay2);






    }
    public void drawBlue(){ //TODO
       posicaoOutrosVeiculos = MainActivity.drawPosicaoOutrosVeiculos();

        Set<GeoPoint> hs = new HashSet<>();
        hs.addAll(posicaoOutrosVeiculos);
        posicaoOutrosVeiculos.clear();
        posicaoOutrosVeiculos.addAll(hs);
        for(int i = 0 ; i<posicaoOutrosVeiculos.size();i++) {
            if(posicaoOutrosVeiculos.get(i)!=centro) {
                final ArrayList<OverlayItem> itemsPoints = new ArrayList<>();
                OverlayItem markerPoints = new OverlayItem("markerTitle", "markerDescription", posicaoOutrosVeiculos.get(i));
                System.out.println(posicaoOutrosVeiculos.get(i));
                markerPoints.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
                itemsPoints.add(markerPoints);
                Bitmap source2 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.downarrow), 70, 70, false);
                float orientation2 = MainActivity.orientation();
                int ori2 = (int) Math.round(Math.toDegrees(orientation2));
                //TODO
                ori2 = ori2-180;
                Bitmap target = MainActivity.RotateMyBitmap(source2,ori2);
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

            Bitmap newMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.arrowgreen), 70, 70, false);
            float orientation2 = MainActivity.orientation();
            int ori2 = (int) Math.round(Math.toDegrees(orientation2));
            ori2 = ori2-180;

            Bitmap target = MainActivity.RotateMyBitmap(newMarker,ori2);
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
    public void onPause(){
        super.onPause();




    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

    }

}

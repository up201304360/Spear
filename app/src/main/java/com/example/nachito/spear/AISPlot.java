package com.example.nachito.spear;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
/**
 * Created by pedro on 2/27/18.
 * LSTS - FEUP
 *
 * Created by Pedro Gonçalves - http://github.com/pmfg/acm
 */

public class AISPlot {

    static class SystemInfoAIS {
        ArrayList<String> shipName = new ArrayList<>();
        ArrayList<Location> shipLocation = new ArrayList<>();
        ArrayList<Long> lastUpdateAisShip = new ArrayList<>();
        ArrayList<Double> headingAisShip = new ArrayList<>();
        ArrayList<Double> speedAisShip = new ArrayList<>();
        ArrayList<Long> idMMSI = new ArrayList<>();
        int systemSizeAIS;
    }

    private SystemInfoAIS systemInfoAIS;
    private FirebaseAuth mAuth;
    private Firebase myFirebaseRef;
    String message = "ships";

    AISPlot(Context context) {
        Firebase.setAndroidContext(context);
        String URlPath = "https://neptus.firebaseio.com/";
        myFirebaseRef = new Firebase(URlPath);
        systemInfoAIS = new SystemInfoAIS();
        systemInfoAIS.systemSizeAIS = 0;
    }

    public void getAISInfo(){
        //showError.showErrorLogcat("MEU", "AIS");
        myFirebaseRef.child(message).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //showError.showErrorLogcat("MEU","added: "+dataSnapshot);
                parseInfoAIS(dataSnapshot.getKey(), dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //showError.showErrorLogcat("MEU","changed: "+dataSnapshot.getKey());
                parseInfoAIS(dataSnapshot.getKey(), dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //showError.showErrorLogcat("MEU","removed: "+dataSnapshot.getKey());
                //parseInfoAIS(dataSnapshot.getKey(), dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //showError.showErrorLogcat("MEU","moved: "+dataSnapshot.toString());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }


        });
    }

    private void parseInfoAIS(String shipName, DataSnapshot dataSnapshot) {
        try {
            if (!shipName.equals("position") && !shipName.equals("type") && !shipName.equals("updated_at"))
                getInfoOfShip(shipName, dataSnapshot);
        }catch (Exception io){
            //showError.showErrorLogcat("MEU", io.toString());
            io.printStackTrace();
        }
    }

    private void getInfoOfShip(String shipName, DataSnapshot dataSnapshot) {
        //ZEZERE, value = {updated_at=1519841484000, type=69, position={latitude=38.66656, heading=511.0, speed=0.4, cog=323.3, mmsi=263047004, longitude=-9.14569}} }
        try {
            Map<String, Object> result = (Map<String, Object>) dataSnapshot.getValue();
            Map<String, Object> result2 = (Map<String, Object>) result.get("position");

            if (systemInfoAIS.systemSizeAIS == 0) {
                systemInfoAIS.shipName.add(0, shipName);
                systemInfoAIS.idMMSI.add(0, (Long) result2.get("mmsi"));
                systemInfoAIS.speedAisShip.add(0, Math.round((Double) result2.get("speed") * 100.0) / 100.0);
                systemInfoAIS.headingAisShip.add(0, Math.round((Double) result2.get("heading") * 100.0) / 100.0);
                systemInfoAIS.lastUpdateAisShip.add(0, (Long) result.get("updated_at"));
                Location back = new Location("AIS: " + shipName);
                back.setLatitude(Double.parseDouble(result2.get("latitude").toString()));
                back.setLongitude(Double.parseDouble(result2.get("longitude").toString()));
                systemInfoAIS.shipLocation.add(0, back);
                systemInfoAIS.systemSizeAIS++;
            } else {
                boolean haveShipMMSI = false;
                long mmsi = (Long) result2.get("mmsi");
                int backID = -1;
                for (int t = 0; t < systemInfoAIS.systemSizeAIS; t++) {
                    if (systemInfoAIS.idMMSI.get(t) == mmsi) {
                        haveShipMMSI = true;
                        backID = t;
                        break;
                    }
                    if (haveShipMMSI)
                        break;
                }

                if (!haveShipMMSI) {
                    systemInfoAIS.shipName.add(systemInfoAIS.systemSizeAIS, shipName);
                    systemInfoAIS.idMMSI.add(systemInfoAIS.systemSizeAIS, (Long) result2.get("mmsi"));
                    systemInfoAIS.speedAisShip.add(systemInfoAIS.systemSizeAIS, Math.round((Double) result2.get("speed") * 100.0) / 100.0);
                    systemInfoAIS.headingAisShip.add(systemInfoAIS.systemSizeAIS, Math.round((Double) result2.get("heading") * 100.0) / 100.0);
                    systemInfoAIS.lastUpdateAisShip.add(systemInfoAIS.systemSizeAIS, (Long) result.get("updated_at"));
                    Location back = new Location("AIS: " + shipName);
                    back.setLatitude(Double.parseDouble(result2.get("latitude").toString()));
                    back.setLongitude(Double.parseDouble(result2.get("longitude").toString()));
                    systemInfoAIS.shipLocation.add(systemInfoAIS.systemSizeAIS, back);
                    systemInfoAIS.systemSizeAIS++;
                } else {
                    systemInfoAIS.shipName.set(backID, shipName);
                    systemInfoAIS.idMMSI.set(backID, (Long) result2.get("mmsi"));
                    systemInfoAIS.speedAisShip.set(backID, Math.round((Double) result2.get("speed") * 100.0) / 100.0);
                    systemInfoAIS.headingAisShip.set(backID, Math.round((Double) result2.get("heading") * 100.0) / 100.0);
                    systemInfoAIS.lastUpdateAisShip.set(backID, (Long) result.get("updated_at"));
                    Location back = new Location("AIS: " + shipName);
                    back.setLatitude(Double.parseDouble(result2.get("latitude").toString()));
                    back.setLongitude(Double.parseDouble(result2.get("longitude").toString()));
                    systemInfoAIS.shipLocation.set(backID, back);
                }
            }
        }catch(Exception io){
            io.printStackTrace();
        }


    }

    public SystemInfoAIS GetDataAIS(){
        return systemInfoAIS;
    }

    public int GetNumberShipsAIS(){
        return systemInfoAIS.systemSizeAIS;
        //return 0;
    }

    public String parseTime(Long unixTime) {
        Date today = Calendar.getInstance().getTime();
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTimeString = formatter.format(today);
        Date date = new Date(unixTime);
        String dateTimeString = formatter.format(date);

        Date AISTime;
        Date androidTime;
        try {
            AISTime = formatter.parse(dateTimeString);
            androidTime = formatter.parse(currentDateTimeString);
            long diffSeconds = Math.abs(androidTime.getTime() - AISTime.getTime()) / 1000;
            return "Last Up: " + String.format("%02dh %02dm %02ds", (diffSeconds/3600), (diffSeconds % 3600) / 60, diffSeconds % 60);
        } catch (ParseException e) {
            e.printStackTrace();
            return "null";
        }

    }
}

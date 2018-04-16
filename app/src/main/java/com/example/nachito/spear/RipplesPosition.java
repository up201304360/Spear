package com.example.nachito.spear;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by pedro on 2/19/18.
 * LSTS - FEUP
 *
 * Created by Pedro Gonçalves - http://github.com/pmfg/acm
 */

public class RipplesPosition {


    class SystemInfo {
        String[] imcid = new String[32000];
        String[] sysName = new String[32000];
        String[] update_at = new String[32000];
        String[] created_at = new String[32000];
        String[] last_update = new String[32000];
        Location[] coordinates = new Location[32000];
        Long[] lastUpInSec = new Long[32000];
        int systemSize;
    }

    private SystemInfo systemInfo = new SystemInfo();
    private Context mContext;
    private String UrlPath;
    private boolean updateBuffer = true;

    RipplesPosition(Context context, String urlRipples) {
        mContext = context;
        UrlPath = urlRipples;
    }

    Boolean PullData(String Url) {
        UrlPath = Url;
        try {
            return ParseDataRipples(new RetrieveDataRipples().execute(UrlPath).get());
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean ParseDataRipples(String dataPull) {
        JSONArray array;
        if(updateBuffer) {
            try {
                array = new JSONArray(dataPull);
                systemInfo.systemSize = array.length();
                for (int i = 0; i < systemInfo.systemSize; i++) {
                    JSONObject jsonobject = array.getJSONObject(i);
                    systemInfo.imcid[i] = jsonobject.getString("imcid");
                    systemInfo.sysName[i] = jsonobject.getString("name");
                    systemInfo.update_at[i] = jsonobject.getString("updated_at");
                    systemInfo.last_update[i] = parseTime(systemInfo.update_at[i], i);
                    systemInfo.created_at[i] = jsonobject.getString("created_at");
                    String[] separatedLocText = jsonobject.getString("coordinates").replace("[", "").replace("]", "").split(",");
                    systemInfo.coordinates[i] = new Location("Ripples:"+systemInfo.sysName[i]);
                    systemInfo.coordinates[i].setLatitude(Double.parseDouble(separatedLocText[0]));
                    systemInfo.coordinates[i].setLongitude(Double.parseDouble(separatedLocText[1]));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                updateBuffer = true;
                return false;
            }
            updateBuffer = false;
            return true;
        }
        return false;
    }

    @SuppressLint("DefaultLocale")
    private String parseTime(String s, int id) {
        Date today = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTimeString = formatter.format(today);
        String ripp[] = s.replace("T", " ").split("\\+");
        int timeZone = Integer.parseInt(ripp[1]);

        Date ripplesTime;
        Date androidTime;
        try {
            ripplesTime = formatter.parse(ripp[0]);
            androidTime = formatter.parse(currentDateTimeString);
            long diffSeconds = Math.abs(androidTime.getTime() - ripplesTime.getTime()) / 1000;
            systemInfo.lastUpInSec[id] = diffSeconds + (timeZone * 60 * 60);
            return "Last Up: " + String.format("%02dh %02dm %02ds", (diffSeconds/3600) + timeZone, (diffSeconds % 3600) / 60, diffSeconds % 60);
        } catch (ParseException e) {
            e.printStackTrace();
            return "null";
        }

    }

    public int GetNumberSystemRipples(){
        return systemInfo.systemSize;
    }

    public SystemInfo GetSystemInfoRipples(){
        if(!updateBuffer){
            updateBuffer = true;
            return systemInfo;
        }
        return null;
    }

    public void ResetBuffer(){
        updateBuffer = true;
    }
}



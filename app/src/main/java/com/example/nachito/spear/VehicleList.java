package com.example.nachito.spear;

import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import pt.lsts.imc.VehicleState;
import pt.lsts.imc.net.Consume;

/**
 * Created by nachito on 26/03/17.
 */

public class VehicleList {
    static final LinkedHashMap< String, Pair<Date, VehicleState>> hashMap = new LinkedHashMap<>();
    static final LinkedHashMap<String, Pair<Date, String>> hashMap2 = new LinkedHashMap<>();
   static LinkedHashSet<String> lhs;
    @Consume
    public void vehicle(VehicleState msg) {
        String nome=msg.getSourceName();

        synchronized (hashMap) {
            hashMap.put(nome, new Pair<>(new Date(), msg));
        }
        synchronized (hashMap2) {
            hashMap2.put( nome, new Pair<>(new Date(), nome));
        }
    }


    public List<VehicleState> connectedVehicles(){

        ArrayList<VehicleState> ligados = new ArrayList<>();
        Date connectedTime = new Date(System.currentTimeMillis()-5000);
        synchronized (hashMap) {
            for (Map.Entry<String, Pair<Date, VehicleState>> entry : hashMap.entrySet()) {
                if (entry.getValue().first.after(connectedTime))
                    ligados.add(entry.getValue().second);

            }
        }
        return ligados;
    }

public LinkedHashSet<String> stillConnected(){
    ArrayList<String> ligados = new ArrayList<>();
    Date connectedTime = new Date(System.currentTimeMillis()-5000);

    synchronized (hashMap2) {


    for(Map.Entry<String, Pair<Date, String>> entry : hashMap2.entrySet()) {
        if (entry.getValue().first.after(connectedTime))

            ligados.add(entry.getValue().second);


        //apagar duplicados

         lhs = new LinkedHashSet<String>();

        Iterator< String> it = ligados.iterator();

        while(it.hasNext()) {
             String val = it.next();
            if (lhs.contains(val)) {
                it.remove();
            }
            else
                lhs.add(val);
        }

    }


}
    return lhs;
}
}



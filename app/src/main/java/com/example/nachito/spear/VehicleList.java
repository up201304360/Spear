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
 *
 * Created by nachito on 26/03/17.
 */

public class VehicleList {
    private final LinkedHashMap<String, Pair<Date, VehicleState>> connectedVehicles = new LinkedHashMap<>();
    private final LinkedHashMap<String, Pair<Date, String>> hashMapTime = new LinkedHashMap<>();
    private LinkedHashSet<String> withoutRepetitions;

    @Consume
    public void vehicle(VehicleState msg) {
        String nome = msg.getSourceName();

        synchronized (connectedVehicles) {
            connectedVehicles.put(nome, new Pair<>(new Date(), msg));
        }
        synchronized (hashMapTime) {
            hashMapTime.put(nome, new Pair<>(new Date(), nome));
        }
    }


    List<VehicleState> connectedVehicles() {

        ArrayList<VehicleState> ligados = new ArrayList<>();
        Date connectedTime = new Date(System.currentTimeMillis() - 5000);
        synchronized (connectedVehicles) {
            for (Map.Entry<String, Pair<Date, VehicleState>> entry : connectedVehicles.entrySet()) {
                assert entry.getValue().first != null;
                if (entry.getValue().first.after(connectedTime))
                    ligados.add(entry.getValue().second);

            }
        }
        return ligados;
    }

    LinkedHashSet<String> stillConnected() {
        ArrayList<String> ligados = new ArrayList<>();
        Date connectedTime = new Date(System.currentTimeMillis() - 5000);

        synchronized (hashMapTime) {

            for (Map.Entry<String, Pair<Date, String>> entry : hashMapTime.entrySet()) {
                assert entry.getValue().first != null;
                if (entry.getValue().first.after(connectedTime))
                    ligados.add(entry.getValue().second);
                withoutRepetitions = new LinkedHashSet<>();

                Iterator<String> it = ligados.iterator();
                while (it.hasNext()) {
                    String val = it.next();
                    if (withoutRepetitions.contains(val)) {
                        it.remove();
                    } else
                        withoutRepetitions.add(val);
                }

            }

        }
        return withoutRepetitions;
    }
}



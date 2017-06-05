package com.example.nachito.spear;

import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pt.lsts.imc.VehicleState;
import pt.lsts.imc.net.Consume;

/**
 * Created by nachito on 26/03/17.
 */

public class VehicleList {
    static LinkedHashMap< String, Pair<Date, VehicleState>> hashMap = new LinkedHashMap<>();


    @Consume
    public void vehicle(VehicleState msg) {
        //(DONE) atualizar no mapa com  os ulti veiculos recebidos
        String nome=msg.getSourceName();
        synchronized (hashMap) {
            hashMap.put(nome, new Pair<>(new Date(), msg));
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
}



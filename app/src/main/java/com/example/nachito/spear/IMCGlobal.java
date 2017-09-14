package com.example.nachito.spear;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;

import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.Maneuver;
import pt.lsts.imc.PlanManeuver;
import pt.lsts.imc.PlanSpecification;
import pt.lsts.imc.VehicleState;
import pt.lsts.imc.net.ConnectFilter;
import pt.lsts.imc.net.IMCProtocol;
import pt.lsts.neptus.messages.listener.MessageInfo;

/**
 * Created by nachito on 26/03/17.
 */


    @EBean(scope= Scope.Singleton)
    public class IMCGlobal extends IMCProtocol  {

        VehicleList veiculos;
         String selectedvehicle= null;


    PlanList planos;
    PlanList maneuvers;
        @Override
        public void onMessage(MessageInfo messageInfo, IMCMessage imcMessage) {
            super.onMessage(messageInfo, imcMessage);

        }

        public String getSelectedvehicle() {
            return selectedvehicle;
        }



        public void setSelectedvehicle(String selectedvehicle) {
            this.selectedvehicle = selectedvehicle;

        }



        public IMCGlobal() {
            super();
            setAutoConnect(ConnectFilter.VEHICLES_ONLY);
            veiculos= new VehicleList();
            register(veiculos);

           planos = new PlanList(this);
            register(planos);
            maneuvers= new PlanList(this);
            register(maneuvers);


        }

        public List<VehicleState> connectedVehicles()
        {
            return  veiculos.connectedVehicles();
        }

public LinkedHashSet<String> stillConnected(){ return  veiculos.stillConnected();}

public List<String> allPlans(){return planos.ListaPlanos(selectedvehicle);}

public List<Maneuver> allManeuvers(){return  maneuvers.ListaManeuvers(selectedvehicle);}

    public boolean sendMessage(IMCMessage imcMessage) {
        return sendMessage(getSelectedvehicle(), imcMessage);
    }

    public void sendToAll(IMCMessage imcMessage) {
        for (VehicleState veh : connectedVehicles())
            sendMessage(veh.getSourceName(), imcMessage);
    }


    }



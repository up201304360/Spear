package com.example.nachito.spear;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pt.lsts.imc.Heartbeat;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.Maneuver;
import pt.lsts.imc.VehicleState;
import pt.lsts.imc.net.ConnectFilter;
import pt.lsts.imc.net.IMCProtocol;
import pt.lsts.neptus.messages.listener.MessageInfo;

/**
 *
 * Created by nachito on 26/03/17.
 */


@EBean(scope = Scope.Singleton)
public class IMCGlobal extends IMCProtocol {

    String selectedvehicle = null;
    private VehicleList veiculos;
    private PlanList planos;
    private PlanList maneuvers;
    private Timer timer = new Timer(true);

    IMCGlobal() {
        super();
        setAutoConnect(ConnectFilter.VEHICLES_ONLY);
        veiculos = new VehicleList();
        register(veiculos);

        planos = new PlanList(this);
        register(planos);
        maneuvers = new PlanList(this);
        register(maneuvers);

        //TODO
        if (stillConnected() != null) {
            TimerTask sendHeartbeats = new TimerTask() {

                @Override
                public void run() {
                    synchronized (stillConnected()) {
                        for (int i = 0; i < stillConnected().size(); i++)
                            sendMessage(stillConnected().toString(), new Heartbeat());
                    }
                }
            };

            timer.scheduleAtFixedRate(sendHeartbeats, 2000, 2000);
        }
    }

    @Override
    public void onMessage(MessageInfo messageInfo, IMCMessage imcMessage) {
        super.onMessage(messageInfo, imcMessage);
    }

    String getSelectedvehicle() {
        return selectedvehicle;
    }

    void setSelectedvehicle(String selectedvehicle) {
        this.selectedvehicle = selectedvehicle;

    }

    List<VehicleState> connectedVehicles() {

        return veiculos.connectedVehicles();
    }

    LinkedHashSet<String> stillConnected() {
        return veiculos.stillConnected();
    }

    List<String> allPlans() {
        return planos.ListaPlanos(selectedvehicle);
    }

    List<String> allSensores() {
        return planos.ListaSensores(selectedvehicle);
    }

    List<Maneuver> allManeuvers() {
        return maneuvers.ListaManeuvers(selectedvehicle);
    }

    void sendMessage(IMCMessage imcMessage) {
        sendMessage(getSelectedvehicle(), imcMessage);
    }

    void sendToAll(IMCMessage imcMessage) {
        for (VehicleState veh : connectedVehicles())
            sendMessage(veh.getSourceName(), imcMessage);
    }


}



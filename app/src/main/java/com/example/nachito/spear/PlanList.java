package com.example.nachito.spear;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import pt.lsts.imc.EntityList;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.Maneuver;
import pt.lsts.imc.PlanControlState;
import pt.lsts.imc.PlanDB;
import pt.lsts.imc.PlanDBInformation;
import pt.lsts.imc.PlanDBState;
import pt.lsts.imc.PlanManeuver;
import pt.lsts.imc.PlanSpecification;
import pt.lsts.imc.net.Consume;
import pt.lsts.neptus.messages.listener.Periodic;


/**
 * Created by nachito on 14/04/17.
 */

public class PlanList {
    private static final LinkedHashMap<String, List<String>> planHashMap = new LinkedHashMap<>();
    private static final LinkedHashMap<String, List<Maneuver>> planExecuting = new LinkedHashMap<>();
    private static final LinkedHashMap<String, List<String>> sensorsHashMap = new LinkedHashMap<>();

    static boolean samePlan;
    static String previousPlan = ".";
    static boolean callMethodWaypoints;
    static String planBeingExecuted = null;
    private IMCGlobal imc;

    PlanList(IMCGlobal ref) {
        imc = ref;
    }

    @Consume
    public void plan(PlanDB msg) {

        if (!msg.getSourceName().equals(imc.getSelectedvehicle()))
            return;

        if (msg.getOp() == PlanDB.OP.GET_STATE && msg.getType() == (PlanDB.TYPE.SUCCESS)) {
            IMCMessage arg = msg.getArg();
            if (arg == null || arg.getMgid() != PlanDBState.ID_STATIC)
                return;
            PlanDBState state = (PlanDBState) arg;
            ArrayList<String> planNameArray;

            planNameArray = new ArrayList<>();
            for (PlanDBInformation info : state.getPlansInfo()) {
                planNameArray.add(info.getPlanId());
            }
            String vehicle = msg.getSourceName();
            synchronized (planHashMap) {
                planHashMap.put(vehicle, planNameArray);
            }
        } else if (msg.getOp() == PlanDB.OP.GET && msg.getType() == PlanDB.TYPE.SUCCESS) {
            if (msg.getPlanId().equals(planBeingExecuted)) {
                System.out.println("Received " + msg.getPlanId() + " from " + msg.getSourceName());
                if (msg.getPlanId().equals(previousPlan)) {
                    samePlan = true;
                    callMethodWaypoints = false;
                } else if (!(previousPlan.equals(msg.getPlanId()))) {
                    samePlan = false;
                    callMethodWaypoints = true;
                    previousPlan = planBeingExecuted;
                }
                ArrayList<Maneuver> planExecutingArray;
                previousPlan = planBeingExecuted;

                planExecutingArray = new ArrayList<>();
                for (PlanManeuver info : ((PlanSpecification) msg.getArg()).getManeuvers()) {
                    planExecutingArray.add(info.getData());
                }

                synchronized (planExecuting) {
                    planExecuting.put(msg.getSourceName(), planExecutingArray);
                }
            }
        }
    }

    @Consume
    public void onMsg(PlanControlState msg) {

        String previous = planBeingExecuted;

        if (!msg.getSourceName().equals(imc.selectedVehicle))
            return;

        if (msg.getState() == PlanControlState.STATE.EXECUTING) {
            planBeingExecuted = msg.getPlanId();
            if (previous == null || !previous.equals(planBeingExecuted))
                askForPlan();

        } else {
            if (planBeingExecuted != null)
                previousPlan = planBeingExecuted;
            planBeingExecuted = null;

        }

    }


    @Consume
    public void sensors(EntityList entityList) {


        if (!entityList.getSourceName().equals(imc.getSelectedvehicle()))
            return;


        if (entityList.getList() != null) {
            if (entityList.getOp() == EntityList.OP.REPORT) {

                ArrayList<String> sensorNameArray = new ArrayList<>();
                if (entityList.getList().containsKey("Multibeam")) {
                    sensorNameArray.add("Multibeam");
                }
                if (entityList.getList().containsKey("Sidescan")) {

                    sensorNameArray.add("Sidescan");

                }

                if (entityList.getList().containsKey("Camera")) {

                    sensorNameArray.add("Camera");

                }

                String vehicle = entityList.getSourceName();


                synchronized (sensorsHashMap) {

                    sensorsHashMap.put(vehicle, sensorNameArray);
                }
                System.out.println(sensorsHashMap);

            }

        }

    }


    @Periodic(60000)
    private void askForPlan() {

        System.out.println("Requesting " + planBeingExecuted + " to vehicle " + imc.selectedVehicle);


        if (planBeingExecuted != null) {
            PlanDB pdbRequest = new PlanDB();
            pdbRequest.setPlanId(planBeingExecuted);
            pdbRequest.setOp(PlanDB.OP.GET);
            pdbRequest.setType(PlanDB.TYPE.REQUEST);
            imc.sendMessage(pdbRequest);
        }
    }


    @Periodic(10000)
    public void AskPlans() {

        PlanDB msg = new PlanDB();
        msg.setOp(PlanDB.OP.GET_STATE);
        msg.setType(PlanDB.TYPE.REQUEST);
        imc.sendToAll(msg);
    }


    @Periodic(10000)
    public void AskEntities() {

        pt.lsts.imc.EntityList entity = new EntityList();
        entity.setOp(EntityList.OP.QUERY);
        imc.sendToAll(entity);

    }


    List<String> SensorList(String vehicle) {
        synchronized (sensorsHashMap) {
            return sensorsHashMap.get(vehicle);
        }
    }

    List<String> PlanLists(String vehicle) {
        synchronized (planHashMap) {
            return planHashMap.get(vehicle);
        }
    }

    List<Maneuver> ManeuversLists(String vehicle) {
        synchronized (planExecuting) {
            return planExecuting.get(vehicle);
        }
    }


}




package com.example.nachito.spear;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
    static final LinkedHashMap<String, List<String>> hashMap = new LinkedHashMap<>();
    IMCGlobal imc = null;
    ArrayList<String> array;
    ArrayList<Maneuver> array2;
    static final LinkedHashMap<String, List<Maneuver>> hashMap2 = new LinkedHashMap<>();
    PlanDB pdb;

    public PlanList(IMCGlobal ref) {
        imc = ref;
    }

    @Consume
    public void plan(PlanDB msg) {
        if (msg.getOp() != PlanDB.OP.GET_STATE || msg.getType() != (PlanDB.TYPE.SUCCESS))
            return;
        IMCMessage arg = msg.getArg();
        if (arg == null || arg.getMgid() != PlanDBState.ID_STATIC)
            return;
        PlanDBState state = (PlanDBState) arg;
        array = new ArrayList<>();
        for (PlanDBInformation info : state.getPlansInfo()) {
            array.add(info.getPlanId());
        }
        String vehicle = msg.getSourceName();
        synchronized (hashMap) {
            hashMap.put(vehicle, array);
        }


    }





    @Consume
    public void maneuver(IMCMessage msg) {

        if (msg instanceof PlanDB) {
            pdb = (PlanDB) msg;

            if (pdb.getArg() instanceof PlanDBState) {
                PlanDBState pdbState = (PlanDBState) pdb.getArg();

                for (PlanDBInformation p : pdbState.getPlansInfo()) {
                    PlanDB pdbRequest = new PlanDB();
                    pdbRequest.setPlanId(p.getPlanId());
                    pdbRequest.setOp(PlanDB.OP.GET);
                    pdbRequest.setType(PlanDB.TYPE.REQUEST);
                    imc.sendMessage(pdbRequest);
                }

            }
        } if(pdb!=null) {
            if (pdb.getArg() instanceof PlanSpecification) {
                PlanSpecification ps = (PlanSpecification) pdb.getArg();
                array2 = new ArrayList<>();

                if (msg.getAbbrev().equals("PlanControlState")) {
                    PlanControlState planControlState = (PlanControlState) msg;

                    String planID = planControlState.getPlanId();
                    System.out.println(planID + " plano a ser executado");
                    System.out.println(pdb.getPlanId() + " pdb.getplanid");

                    if ((pdb.getPlanId().equals(planID))) {

                        for (PlanManeuver info : ps.getManeuvers()) {
                            array2.add(info.getData());
                            System.out.println(" array2");
                        }
                    }


                    String vehicle = msg.getSourceName();

                    synchronized (hashMap2) {
                        hashMap2.put(vehicle, array2);

                    }
                }
            }
        }
    }



    @Periodic(10000)
    public void AskPlans(){

        PlanDB msg = new PlanDB();
        msg.setOp(PlanDB.OP.GET_STATE);
        msg.setType(PlanDB.TYPE.REQUEST);
        imc.sendToAll(msg);

    }




    public List<String> ListaPlanos(String vehicle) {
        synchronized (hashMap) {
            return hashMap.get(vehicle);
        }
    }


    public List<Maneuver> ListaManeuvers(String vehicle) {
        synchronized (hashMap2) {
            return hashMap2.get(vehicle);
        }
    }



}




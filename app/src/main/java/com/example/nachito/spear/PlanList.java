package com.example.nachito.spear;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.PlanDB;
import pt.lsts.imc.PlanDBInformation;
import pt.lsts.imc.PlanDBState;
import pt.lsts.imc.net.Consume;
import pt.lsts.neptus.messages.listener.Periodic;


/**
 * Created by nachito on 14/04/17.
 */

public class PlanList {
    static LinkedHashMap< String,List<String>> hashMap = new LinkedHashMap<>();
    IMCGlobal imc = null;

    public PlanList(IMCGlobal ref) {
        imc = ref;
    }

    @Consume
    public void plan(PlanDB msg) {
        //System.err.println(msg);
        if (msg.getOp() != PlanDB.OP.GET_STATE || msg.getType() != (PlanDB.TYPE.SUCCESS))
            return;
        IMCMessage arg = msg.getArg();
        if (arg == null || arg.getMgid() != PlanDBState.ID_STATIC)
            return;
        PlanDBState state = (PlanDBState) arg;
        ArrayList<String> array = new ArrayList<>();
        for (PlanDBInformation info : state.getPlansInfo()) {
            array.add(info.getPlanId());
        }
        String vehicle= msg.getSourceName();
        synchronized (hashMap) {
            hashMap.put(vehicle, array);
        }
    }


    @Periodic(10000)
    public void AskPlans(){
        PlanDB msg = new PlanDB();
        msg.setOp(PlanDB.OP.GET_STATE);
        msg.setType(PlanDB.TYPE.REQUEST);
      //  System.out.println(msg.asJSON());
        imc.sendToAll(msg);
    }




    public List<String> ListaPlanos(String vehicle) {
        synchronized (hashMap) {
            return hashMap.get(vehicle);
        }
    }
}




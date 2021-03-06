package com.example.nachito.spear;

import android.support.v4.app.Fragment;

import org.androidannotations.annotations.EFragment;

import java.util.LinkedHashMap;

import pt.lsts.imc.PlanControl;
import pt.lsts.imc.PlanControlState;
import pt.lsts.imc.RemoteActions;
import pt.lsts.imc.Teleoperation;
import pt.lsts.imc.net.Consume;
import pt.lsts.neptus.messages.listener.Periodic;


@EFragment(R.layout.fragment_teleop)
public class TeleOperation extends Fragment implements JoystickMovedListener, AccelListener, DecListener, StopListener {
    static boolean isTeleOpSelected = false;
    protected LinkedHashMap<String, Object> remoteActions = new LinkedHashMap<>();
    IMCGlobal imc;

    public void setImc(IMCGlobal imc) {
        this.imc = imc;
        imc.register(this);
    }

    public void finishTeleOp() {
        PlanControl pc = new PlanControl();
        pc.setType(PlanControl.TYPE.REQUEST);
        pc.setOp(PlanControl.OP.STOP);
        pc.setRequestId(1);
        pc.setFlags(0);
        pc.setPlanId("stopTeleOp");
        imc.sendMessage(pc);
        imc.unregister(this);

    }


    @Consume
    public void teleOP(PlanControlState msg) {
        if (!(msg.getSourceName().equals(imc.selectedVehicle)))
            return;
        else {

            if (isTeleOpSelected = (msg.getManType() == Teleoperation.ID_STATIC && msg.getState() == PlanControlState.STATE.EXECUTING))
                isTeleOpSelected = true;
            else if (isTeleOpSelected = false) {
                PlanControl pc = new PlanControl();
                Teleoperation teleoperationMsg = new Teleoperation();
                teleoperationMsg.setCustom("src=" + imc.getLocalId());
                pc.setArg(teleoperationMsg);
                pc.setType(PlanControl.TYPE.REQUEST);
                pc.setOp(PlanControl.OP.START);
                pc.setFlags(0);
                pc.setRequestId(0);
                pc.setPlanId("SpearTeleoperation");

                imc.sendMessage(pc);
            }

        }
    }

    @Override
    public void OnMoved(float pan, float tilt) {
        remoteActions.put("Heading", ((int) (pan * 15)));
    }

    @Override
    public void Thrust(float tilt2) {
        float tilt;

        tilt = (int) (tilt2 * 15);
    }


    @Override
    public void OnReleased() {
        remoteActions.put("Heading", 0);
    }

    @Override
    public void accelerate() {
        remoteActions.put("Accelerate", 1);

    }

    @Override
    public void OnReleaseAcc() {
        remoteActions.put("Accelerate", 0);
    }


    @Override
    public void dec() {
        remoteActions.put("Decelerate", 1);

    }

    @Override
    public void OnReleaseDec() {
        remoteActions.put("Decelerate", 0);
    }

    @Override
    public void stop() {
        remoteActions.put("Stop", 1);

    }

    @Override
    public void OnReleaseStop() {
        remoteActions.put("Stop", 0);

    }


    @Periodic(100)
    public void sendActions() {
        if (!isTeleOpSelected)
            return;
        RemoteActions rm;
        rm = new RemoteActions();
        rm.setActions(remoteActions);
        imc.sendMessage(rm);
    }
}

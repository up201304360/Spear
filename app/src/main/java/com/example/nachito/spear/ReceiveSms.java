package com.example.nachito.spear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

/**
 * Created by ines on 10/4/17.
 */

public class ReceiveSms extends BroadcastReceiver {

 String[] vehicleNumber;
    private static SmsListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data  = intent.getExtras();

        Object[] pdus = (Object[]) data.get("pdus");

        assert pdus != null;
        for (Object pdu : pdus) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);

            String sender = smsMessage.getDisplayOriginatingAddress();
            String messageBody = smsMessage.getMessageBody();
            //Pass on the text to our listener.

            vehicleNumber = context.getResources().getStringArray(R.array.phonenumbers);
            for (String aVehicleNumber : vehicleNumber) {

               if (aVehicleNumber.contains(sender)) {

                    if (messageBody != null)
                        mListener.messageReceived(messageBody);
                }
            }

        }

    }



    public static void bindListener(SmsListener listener) {
        mListener = listener;
    }
}

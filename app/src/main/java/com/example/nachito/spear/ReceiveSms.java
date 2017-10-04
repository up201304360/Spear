package com.example.nachito.spear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by ines on 10/4/17.
 */

public class ReceiveSms extends BroadcastReceiver {


    private static SmsListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data  = intent.getExtras();

        Object[] pdus = (Object[]) data.get("pdus");

        assert pdus != null;
        for (Object pdu : pdus) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);

            String sender = smsMessage.getDisplayOriginatingAddress();
            //You must check here if the sender is your provider and not another one with same text.

            String messageBody = smsMessage.getMessageBody();
            //Pass on the text to our listener.
            mListener.messageReceived(messageBody);

//TODO filtrar para ser so sms dos veiculos, receber a lat e lon e mostrar no mapa
        }

    }

    public static void bindListener(SmsListener listener) {
        mListener = listener;
    }
}

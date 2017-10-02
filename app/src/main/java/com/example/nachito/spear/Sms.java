package com.example.nachito.spear;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.test.mock.MockPackageManager;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.SEND_SMS;

/**
 * Created by ines on 10/2/17.
 */

public class Sms extends AppCompatActivity {
    RadioButton dive;
    RadioButton surface;
    RadioButton abort;
    RadioButton stationKeeping;
    TextView nomeVeiculo;
    String[] bits ;
    String[] nomes ;
    String numeroFinal;

    String[] mPermission = {READ_SMS, SEND_SMS};
    private static final int REQUEST_CODE_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        addListenerOnButton();

        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission[0])
                    != MockPackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, mPermission[1])
                            != MockPackageManager.PERMISSION_GRANTED ) {

                ActivityCompat.requestPermissions(this,
                        mPermission, REQUEST_CODE_PERMISSION);

                // If any permission above not allowed by user, this condition will execute every tim, else your else part will work
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("Req Code", "" + requestCode);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length == 4 &&
                    grantResults[0] == MockPackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == MockPackageManager.PERMISSION_GRANTED ){
                // Success Stuff here

            }
        }

    }


    public void addListenerOnButton() {
        dive = (RadioButton) findViewById(R.id.diveRadioButton);
        surface = (RadioButton) findViewById(R.id.surfaceRadioButton);
        abort = (RadioButton) findViewById(R.id.abortRadioButton);
        stationKeeping = (RadioButton) findViewById(R.id.stationKeepingRadioButton);
        nomeVeiculo = (TextView) findViewById(R.id.nomeVeiculo);


        bits = getApplicationContext().getResources().getStringArray(R.array.phonenumbers);
        System.out.println(Arrays.toString(bits));
        nomes = getApplicationContext().getResources().getStringArray(R.array.names);
        System.out.println(Arrays.toString(nomes));
        Intent intent = getIntent();
        String selected = intent.getExtras().getString("selected");
        for (int i = 0; i < nomes.length; i++) {
            if (selected != null) {
                nomeVeiculo.setText(selected);

                if (nomes[i].contains(selected)) {
                    numeroFinal = bits[i];
                }

            }
        }
        dive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sendSMS(numeroFinal, "dive");
            }
        });


        surface.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sendSMS(numeroFinal, "surface");

            }
        });

        stationKeeping.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sendSMS(numeroFinal, "sk");

            }
        });


        abort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sendSMS(numeroFinal, "abort");
            }
        });
    }

    private boolean sendSMS(String toNum, String smsText) {

        try
        {
            Toast.makeText(getApplicationContext(),"Number: "+ toNum + "  #  Text: " + smsText,Toast.LENGTH_SHORT).show();
            String SENT = "SMS_SENT";
            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
            registerReceiver(new BroadcastReceiver()
            {

                @Override
                public void onReceive(Context arg0, Intent arg1)
                {
                    int resultCode = getResultCode();
                    switch (resultCode)
                    {
                        case Activity.RESULT_OK:
                            Toast.makeText(getBaseContext(), "SMS sent successfully",Toast.LENGTH_LONG).show();
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Toast.makeText(getBaseContext(), "SMS Fail: Generic failure",Toast.LENGTH_LONG).show();
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Toast.makeText(getBaseContext(), "SMS Fail: No service",Toast.LENGTH_LONG).show();
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Toast.makeText(getBaseContext(), "SMS Fail: Null PDU",Toast.LENGTH_LONG).show();
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Toast.makeText(getBaseContext(), "SMS Fail: Radio off",Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            }, new IntentFilter(SENT));

            try{


              /*  Intent smsIntent = new Intent(Intent.ACTION_VIEW);

                smsIntent.setData(Uri.parse("smsto:"));
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address"  , numeroFinal);
                smsIntent.putExtra("sms_body"  , "Test ");


                    startActivity(smsIntent);
                    finish();
                    System.out.println("Finished sending SMS...");*/



               SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(toNum, null, smsText, sentPI, null);

            }catch (Exception e){
                Log.i("SMS", ""+e);
            }
            return true;
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),"SMS failed, please try again later!",Toast.LENGTH_SHORT).show();
            Log.i("SMS", ""+e);

        }
        return false;
    }




}

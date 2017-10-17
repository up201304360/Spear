package com.example.nachito.spear;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.test.mock.MockPackageManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;

import pt.lsts.imc.IridiumMsgTx;

import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.SEND_SMS;


/**
 * Created by ines on 10/2/17.
 */

public class SendSms extends AppCompatActivity {
    RadioButton dive;
    RadioButton surface;
    RadioButton abort;
    RadioButton goTo;
    RadioButton pos;
    RadioButton stationKeeping;
    TextView nomeVeiculo;
    String[] vehicleNumber;
    String[] nomes;
    String numeroFinal;
    BroadcastReceiver mBroadcastTime;
    String checked;
    String[] mPermission = {READ_SMS, SEND_SMS};
    private static final int REQUEST_CODE_PERMISSION = 2;
    Button sms;
    Button iridium;
    String[] imeiNumb;
    String[] vehicleNames;
String numeroF;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        addListenerOnButton();

        android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission[0])
                    != MockPackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, mPermission[1])
                            != MockPackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        mPermission, REQUEST_CODE_PERMISSION);

                // If any permission above not allowed by user, this condition will execute every tim, else your else part will work
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("Req Code", "" + requestCode);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length == 4 &&
                    grantResults[0] == MockPackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == MockPackageManager.PERMISSION_GRANTED) {
                // Success Stuff here

            }
        }

    }


    public void addListenerOnButton() {
        dive = (RadioButton) findViewById(R.id.diveRadioButton);
        surface = (RadioButton) findViewById(R.id.surfaceRadioButton);
        abort = (RadioButton) findViewById(R.id.abortRadioButton);
        goTo = (RadioButton) findViewById(R.id.gotoRadioButton);
        pos = (RadioButton) findViewById(R.id.pos);
        stationKeeping = (RadioButton) findViewById(R.id.stationKeepingRadioButton);
        nomeVeiculo = (TextView) findViewById(R.id.nomeVeiculo);
        sms= (Button) findViewById(R.id.button3);
        iridium=(Button) findViewById(R.id.button2);
//sms
        vehicleNumber = getApplicationContext().getResources().getStringArray(R.array.phonenumbers);
        nomes = getApplicationContext().getResources().getStringArray(R.array.names);

  //iridium
        imeiNumb=getApplicationContext().getResources().getStringArray(R.array.imei);
         vehicleNames= getApplicationContext().getResources().getStringArray(R.array.namesIMEI);


        //
        Intent intent = getIntent();
        final String selected = intent.getExtras().getString("selected");
        for (int i = 0; i < nomes.length; i++) {
            if (selected != null) {
                nomeVeiculo.setText(selected);

                if (nomes[i].contains(selected)) {
                    numeroFinal = vehicleNumber[i];
                }  //TODO fazer para o iridium
                if(vehicleNames[i].contains(selected)){
                    numeroF= imeiNumb[i];
                }

            }
        }
        pos.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(selected==null)
                    Toast.makeText(SendSms.this, "Select a vehicle fist", Toast.LENGTH_SHORT).show();

                else {
                checked=("pos");}
            }
        });

        dive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(selected==null)
                    Toast.makeText(SendSms.this, "Select a vehicle fist", Toast.LENGTH_SHORT).show();

                else {
                checked=("dive");}
            }
        });


        surface.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checked="surface";
            }
        });

        stationKeeping.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {


                    if(selected==null)
                       Toast.makeText(SendSms.this, "Select a vehicle fist", Toast.LENGTH_SHORT).show();

                        else {
                        Intent yourIntent = new Intent(SendSms.this, MapSMS.class);
                        startActivity(yourIntent);


                        checked = "stationKeeping";
                    }
            }
        });
        goTo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(selected==null)
                    Toast.makeText(SendSms.this, "Select a vehicle fist", Toast.LENGTH_SHORT).show();

                else {
                    Intent yourIntent = new Intent(SendSms.this, MapSMS.class);
                    startActivity(yourIntent);
                    checked = "goTo";
                }

            }
        });

        abort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(selected==null)
                    Toast.makeText(SendSms.this, "Select a vehicle fist", Toast.LENGTH_SHORT).show();

                else {
                checked="abort";}
            }
        });


        sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (checked) {
                    case "pos":
                        sendSMS(numeroFinal, 0, 0, "pos");
                        break;
                    case "dive":
                        sendSMS(numeroFinal, 0, 0, "dive");
                        break;
                    case "surface":
                        sendSMS(numeroFinal, 0, 0, "surface");
                        break;
                    case "stationKeeping":
                        sendSMS(numeroFinal, MainActivity.depth, MainActivity.speed, "sk");
                        break;
                    case "abort":

                        sendSMS(numeroFinal, 0, 0, "abort");
                        break;
                    case "goTo":
                        sendSMS(numeroFinal, MainActivity.depth, MainActivity.speed, "go");
                        break;
                }
            }
        });

        iridium.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                switch (checked) {
                    case "dive":
                        try {
                            sendIMEI(numeroF, 0, 0, "dive");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "surface":
                        try {
                            sendIMEI(numeroF, 0, 0, "surface");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "stationKeeping":
                        try {
                            sendIMEI(numeroF, MainActivity.depth, MainActivity.speed, "sk");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "abort":

                        try {
                            sendIMEI(numeroF, 0, 0, "abort");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "goTo":
                        try {
                            sendIMEI(numeroF, MainActivity.depth, MainActivity.speed, "go");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });

    }



    private boolean sendSMS(String phoneNumber, double depth, double vel, String smsText) {
        try {

            Toast.makeText(getApplicationContext(), "Number: " + phoneNumber + "  #  Text: " + smsText, Toast.LENGTH_SHORT).show();
            String SENT = "SMS_SENT";
            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);


            mBroadcastTime = new BroadcastReceiver() {

                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    int resultCode = getResultCode();
                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            Toast.makeText(getBaseContext(), "SMS sent successfully", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Toast.makeText(getBaseContext(), "SMS Fail: Generic failure", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Toast.makeText(getBaseContext(), "SMS Fail: No service", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Toast.makeText(getBaseContext(), "SMS Fail: Null PDU", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Toast.makeText(getBaseContext(), "SMS Fail: Radio off", Toast.LENGTH_SHORT).show();
                            break;
                    }

                }

            };
            IntentFilter mTime = new IntentFilter(SENT);
            registerReceiver(mBroadcastTime, mTime);

            try {



                SmsManager sms = SmsManager.getDefault();
                switch (smsText) {

                       case "sk":

                           GeoPoint ponto= MapSMS.resultado();
                           double latitude= ponto.getLatitude();
                           double longitude=ponto.getLongitude();
                           sms.sendTextMessage(phoneNumber, null, smsText + " "+"lat=" + latitude  +";lon=" + longitude +";speed="+vel, sentPI, null);
                           System.out.println(sms.toString());

                           checked=null;
                           SendSms.super.onBackPressed();

                           break;

                 case "go":

                     GeoPoint ponto2= MapSMS.resultado();
                     double latitude2= ponto2.getLatitude();
                     double longitude2=ponto2.getLongitude();
                     sms.sendTextMessage(phoneNumber, null, smsText + " "+"lat=" + latitude2  +";lon=" + longitude2 +";speed="+vel, sentPI, null);
                     System.out.println(sms.toString());
                     checked=null;
                     SendSms.super.onBackPressed();

                break;


                    default:
                        sms.sendTextMessage(phoneNumber, null, smsText, sentPI, null);
                        checked=null;
                        SendSms.super.onBackPressed();
                        System.out.println(smsText);

                        break;
                }


            } catch (Exception e) {
                Log.i("SMS", "" + e);
            }
            return true;
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS failed, please try again later!", Toast.LENGTH_SHORT).show();
            Log.i("SMS", "" + e);

        }

        return false;
    }



    private boolean sendIMEI(String imeiNumber, double depth, double vel, String smsText) throws Exception {
/*
//TODO
        switch (smsText) {

            case "sk":
                String serverUrl = "http://ripples.lsts.pt/api/v1/iridium";
                int timeoutMillis = 10000;


                IridiumMsgTx msg = new IridiumMsgTx();
                msg.setDestination(imeiNumber);
                msg.setSource(message.getSrc());
                msg.source = message.getSrc();
                msg.setMsg(smsText);

                byte[] data = msg.serialize();

                data = new String(DatatypeConverter.printHexBinary(data)).getBytes();



        URL u = new URL(serverUrl);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod( "POST" );
        conn.setRequestProperty( "Content-Type", "application/hub" );
        conn.setRequestProperty( "Content-Length", String.valueOf(data.length * 2) );
        conn.setConnectTimeout(timeoutMillis);
                break;





        }*/
        return false;


    }

    @Override
    public void onBackPressed() {
        unregisterBroadcast();

        SendSms.super.onBackPressed();
    }

    @Override
    public void onPause(){
        super.onPause();

        unregisterBroadcast();



    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterBroadcast();

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterBroadcast();
    }

public  void unregisterBroadcast(){
    if(mBroadcastTime != null) {
        try {
            unregisterReceiver(mBroadcastTime);
        } catch (Exception ignored) {
        }
    }

}

}

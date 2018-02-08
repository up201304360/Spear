package com.example.nachito.spear;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.test.mock.MockPackageManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;

import pt.lsts.imc.IMCMessage;

import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.SEND_SMS;

/**
 *
 * Created by ines on 10/2/17.
 */

public class SendSms extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSION = 2;
    static GeoPoint finalPoint;
    RadioButton dive;
    RadioButton surface;
    RadioButton abort;
    RadioButton goTo;
    RadioButton pos;
    RadioButton sendPlan;
    RadioButton startPlan;
    Button listOfVehicles;
    RadioButton stationKeeping;
    String[] vehicleNumber;
    String[] names;
    String finalNumber;
    BroadcastReceiver mBroadcastTime;
    String checked;
    String[] mPermission = {READ_SMS, SEND_SMS};
    Button sms;
    Button iridium;
    String[] imeiNumb;
    String[] vehicleNames;
    String FNumber;
    IMCMessage msg;
    String planName;

    public static GeoPoint pontoSMS() {
        return finalPoint;

    }

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    System.out.println("granted");
                } else {
                    System.out.println(" not granted");


                }
            }
        }

    }



    public void addListenerOnButton() {
        dive = findViewById(R.id.diveRadioButton);
        surface = findViewById(R.id.surfaceRadioButton);
        abort = findViewById(R.id.abortRadioButton);
        goTo = findViewById(R.id.gotoRadioButton);
        pos = findViewById(R.id.pos);
        stationKeeping = findViewById(R.id.stationKeepingRadioButton);
        sendPlan = findViewById(R.id.sendPlanRadioButton);
        startPlan = findViewById(R.id.startPlanRadioButton);
        sms = findViewById(R.id.button3);
        iridium = findViewById(R.id.button2);
        listOfVehicles = findViewById(R.id.listofVehicles);
        //sms
        vehicleNumber = getApplicationContext().getResources().getStringArray(R.array.phonenumbers);
        names = getApplicationContext().getResources().getStringArray(R.array.names);

        //iridium
        imeiNumb = getApplicationContext().getResources().getStringArray(R.array.imei);
        vehicleNames = getApplicationContext().getResources().getStringArray(R.array.namesIMEI);


        listOfVehicles.setOnClickListener(view -> {
            Intent yourIntent = new Intent(SendSms.this, ListVehicles.class);
            startActivity(yourIntent);

        });


        pos.setOnCheckedChangeListener((compoundButton, b) -> {
            if (ListVehicles.selectedSMS.equals("."))
                Toast.makeText(SendSms.this, "Select a vehicle fist", Toast.LENGTH_SHORT).show();

            else {
                checked = ("pos");
                for (int i = 0; i < names.length; i++) {

                    if (names[i].contains(ListVehicles.selectedSMS)) {
                        finalNumber = vehicleNumber[i];
                    }
                    if (vehicleNames[i].contains(ListVehicles.selectedSMS)) {
                        FNumber = imeiNumb[i];
                    }

                }
            }
        });

        dive.setOnCheckedChangeListener((compoundButton, b) -> {
            if (ListVehicles.selectedSMS.equals("."))
                Toast.makeText(SendSms.this, "Select a vehicle fist", Toast.LENGTH_SHORT).show();

            else {
                checked = ("dive");
                for (int i = 0; i < names.length; i++) {

                    if (names[i].contains(ListVehicles.selectedSMS)) {
                        finalNumber = vehicleNumber[i];
                    }
                    if (vehicleNames[i].contains(ListVehicles.selectedSMS)) {
                        FNumber = imeiNumb[i];
                    }

                }
            }
        });

        startPlan.setOnCheckedChangeListener((compoundButton, b) -> {
            if (ListVehicles.selectedSMS.equals("."))
                Toast.makeText(SendSms.this, "Select a vehicle fist", Toast.LENGTH_SHORT).show();

            else {
                checked = ("start");

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SendSms.this);
                alertDialog.setTitle("Plan name");

                final EditText input = new EditText(SendSms.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("DONE",
                        (dialog, which) -> {
                            planName = input.getText().toString();

                            dialog.cancel();
                        });
                alertDialog.show();
            }


            for (int i = 0; i < names.length; i++) {

                if (names[i].contains(ListVehicles.selectedSMS)) {
                    finalNumber = vehicleNumber[i];
                }
                if (vehicleNames[i].contains(ListVehicles.selectedSMS)) {
                    FNumber = imeiNumb[i];
                }

            }

        });
        sendPlan.setOnCheckedChangeListener((compoundButton, b) -> {
            if (ListVehicles.selectedSMS.equals("."))
                Toast.makeText(SendSms.this, "Select a vehicle fist", Toast.LENGTH_SHORT).show();

            else {
                checked = ("send");
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SendSms.this);
                alertDialog.setTitle("Plan name");

                final EditText input = new EditText(SendSms.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("DONE",
                        (dialog, which) -> {
                            planName = input.getText().toString();

                            dialog.cancel();
                        });
                alertDialog.show();
            }
            for (int i = 0; i < names.length; i++) {

                if (names[i].contains(ListVehicles.selectedSMS)) {
                    finalNumber = vehicleNumber[i];
                }
                if (vehicleNames[i].contains(ListVehicles.selectedSMS)) {
                    FNumber = imeiNumb[i];
                }


            }
        });


        surface.setOnCheckedChangeListener((compoundButton, b) -> checked = "surface");

        stationKeeping.setOnCheckedChangeListener((compoundButton, b) -> {

            if (ListVehicles.selectedSMS.equals("."))
                Toast.makeText(SendSms.this, "Select a vehicle fist", Toast.LENGTH_SHORT).show();

            else {
                Intent yourIntent = new Intent(SendSms.this, MapSMS.class);
                startActivity(yourIntent);


                checked = "stationKeeping";
                for (int i = 0; i < names.length; i++) {

                    if (names[i].contains(ListVehicles.selectedSMS)) {
                        finalNumber = vehicleNumber[i];
                    }
                    if (vehicleNames[i].contains(ListVehicles.selectedSMS)) {
                        FNumber = imeiNumb[i];
                    }

                }
            }
        });
        goTo.setOnCheckedChangeListener((compoundButton, b) -> {
            if (ListVehicles.selectedSMS.equals("."))
                Toast.makeText(SendSms.this, "Select a vehicle fist", Toast.LENGTH_SHORT).show();

            else {
                Intent yourIntent = new Intent(SendSms.this, MapSMS.class);
                startActivity(yourIntent);
                checked = "goTo";
                for (int i = 0; i < names.length; i++) {

                    if (names[i].contains(ListVehicles.selectedSMS)) {
                        finalNumber = vehicleNumber[i];
                    }
                    if (vehicleNames[i].contains(ListVehicles.selectedSMS)) {
                        FNumber = imeiNumb[i];
                    }

                }
            }

        });

        abort.setOnCheckedChangeListener((compoundButton, b) -> {
            if (ListVehicles.selectedSMS.equals("."))
                Toast.makeText(SendSms.this, "Select a vehicle fist", Toast.LENGTH_SHORT).show();

            else {
                checked = "abort";
                for (int i = 0; i < names.length; i++) {

                    if (names[i].contains(ListVehicles.selectedSMS)) {
                        finalNumber = vehicleNumber[i];
                    }
                    if (vehicleNames[i].contains(ListVehicles.selectedSMS)) {
                        FNumber = imeiNumb[i];
                    }

                }
            }
        });


        sms.setOnClickListener(view -> {
            switch (checked) {
                case "pos":
                    sendSMS(finalNumber, 0, "pos");
                    break;
                case "dive":
                    sendSMS(finalNumber, 0, "dive");
                    break;
                case "surface":
                    sendSMS(finalNumber, 0, "surface");
                    break;
                case "stationKeeping":
                    sendSMS(finalNumber, MainActivity.speed, "sk");
                    break;
                case "abort":

                    sendSMS(finalNumber, 0, "abort");
                    break;
                case "goTo":
                    sendSMS(finalNumber, MainActivity.speed, "go");
                    break;
                case "send":
                    sendSMS(finalNumber, MainActivity.speed, "plan " + planName);
                    break;
                case "start":
                    sendSMS(finalNumber, MainActivity.speed, "start" + planName);
                    break;
            }
        });

        iridium.setOnClickListener(view -> {
            switch (checked) {
                case "dive":
                    try {
                        sendIMEI(msg, FNumber, 0, 0, "dive");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "surface":
                    try {
                        sendIMEI(msg, FNumber, 0, 0, "surface");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "stationKeeping":
                    try {
                        sendIMEI(msg, FNumber, MainActivity.depth, MainActivity.speed, "sk");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "abort":

                    try {
                        sendIMEI(msg, FNumber, 0, 0, "abort");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "goTo":
                    try {
                        sendIMEI(msg, FNumber, MainActivity.depth, MainActivity.speed, "go");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "send":
                    try {
                        sendIMEI(msg, FNumber, MainActivity.depth, MainActivity.speed, "plan" + planName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "start":
                    try {
                        sendIMEI(msg, FNumber, MainActivity.depth, MainActivity.speed, "start" + planName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        });

    }

    private void sendSMS(String phoneNumber, double vel, String smsText) {
        try {
            SmsManager smsManager = SmsManager.getDefault();

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


                if (android.os.Build.VERSION.SDK_INT > 23) {

                    try {
                        Uri uri = Uri.parse("smsto:" + phoneNumber);
                        // No permisison needed
                        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
                        // Set the message to be sent
                        smsIntent.putExtra("sms_body", smsText);
                        checked = " ";
                        ListVehicles.selectedSMS = ".";
                        startActivity(smsIntent);

                    } catch (Exception e) {
                        Toast.makeText(this,
                                "SMS faild, please try again later!",
                                Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                } else {
                    switch (smsText) {

                        case "sk":

                            GeoPoint ponto = MapSMS.resultado();
                            double latitude = ponto.getLatitude();
                            double longitude = ponto.getLongitude();
                            smsManager.sendTextMessage(phoneNumber, null, smsText + " " + "lat=" + latitude + ";lon=" + longitude + ";speed=" + vel, sentPI, null);
                            finalPoint = ponto;
                            checked = " ";
                            ListVehicles.selectedSMS = ".";
                            MainActivity.previous = "M";

                            onBackPressed();

                            break;

                        case "go":

                            GeoPoint ponto2 = MapSMS.resultado();
                            double latitude2 = ponto2.getLatitude();
                            double longitude2 = ponto2.getLongitude();
                            smsManager.sendTextMessage(phoneNumber, null, smsText + " " + "lat=" + latitude2 + ";lon=" + longitude2 + ";speed=" + vel, sentPI, null);
                            finalPoint = ponto2;
                            MainActivity.previous = "M";
                            checked = " ";
                            ListVehicles.selectedSMS = ".";
                            onBackPressed();

                            break;


                        case "pos":
                            System.out.println("SMS POS");
                            smsManager.sendTextMessage(phoneNumber, null, smsText, sentPI, null);
                            System.out.println("SMS POS2");
                            checked = " ";
                            ListVehicles.selectedSMS = ".";
                            onBackPressed();

                            break;

                        case "dive":
                            System.out.println("SMS DIVE");
                            smsManager.sendTextMessage(phoneNumber, null, smsText, sentPI, null);
                            onBackPressed();
                            checked = " ";
                            ListVehicles.selectedSMS = ".";

                            break;
                        case "surface":
                            System.out.println("SMS SURF");
                            smsManager.sendTextMessage(phoneNumber, null, smsText, sentPI, null);
                            onBackPressed();
                            checked = " ";
                            ListVehicles.selectedSMS = ".";

                            break;
                        case "abort":
                            System.out.println("SMS ABORT");
                            smsManager.sendTextMessage(phoneNumber, null, smsText, sentPI, null);
                            onBackPressed();
                            checked = " ";
                            ListVehicles.selectedSMS = ".";

                            break;
                        case "start":
                            System.out.println("SMS STARTPLAN");
                            smsManager.sendTextMessage(phoneNumber, null, smsText, sentPI, null);
                            onBackPressed();
                            checked = " ";
                            ListVehicles.selectedSMS = ".";

                            break;
                        case "send":
                            System.out.println("SMS PLAN");
                            smsManager.sendTextMessage(phoneNumber, null, smsText, sentPI, null);
                            onBackPressed();
                            checked = " ";
                            ListVehicles.selectedSMS = ".";

                            break;

                    }


                }
            } catch (Exception e) {
                Log.i("SMS", "" + e);
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS failed, please try again later!", Toast.LENGTH_SHORT).show();
            Log.i("SMS", "" + e);

        }

    }

    private boolean sendIMEI(IMCMessage message, String imeiNumber, double depth, double vel, String smsText) throws Exception {

     /*   String serverUrl = "http://ripples.lsts.pt/api/v1/iridium";
        String selectedV= ListVehicles.selectedSMS;



        ImcIridiumMessage msg = new ImcIridiumMessage();
        msg.setDestination();         //imcid do veiculo

        msg.setSource(message.getSrc());
        msg.source = message.getSrc();
        msg.setMsg(message);
        // criar uma nova textmessage


        TextMessage newText = new TextMessage();
        newText.setText(smsText);

        byte[] data = msg.serialize();


//bytes para hexadecimal
        //    data = DatatypeConverter.printHexBinary(data).getBytes();

        //converts an array of bytes into a string
        data = Hex.encodeHexString(data).getBytes();


        URL u = new URL(serverUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();


        urlConnection.setDoOutput(true);
        urlConnection.setChunkedStreamingMode(0);


        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/hub");
        urlConnection.setRequestProperty("Content-Length", String.valueOf(data.length * 2));
        urlConnection.setConnectTimeout(10000);

        OutputStream os = urlConnection.getOutputStream();
        os.write(data);
        os.close();

        InputStream is = urlConnection.getInputStream();
        ByteArrayOutputStream incoming = new ByteArrayOutputStream();

        byte buff[] = new byte[1024];
        int read;
        while ((read = is.read(buff)) > 0)
            incoming.write(buff, 0, read);
        is.close();

        System.out.println("Sent " + msg.getClass().getSimpleName() + " through HTTP: " + urlConnection.getResponseCode() + " " + urlConnection.getResponseMessage());

        if (urlConnection.getResponseCode() != 200) {
            throw new Exception("Server returned " + urlConnection.getResponseCode() + ": " + urlConnection.getResponseMessage());
        } else {
            System.out.println(new String(incoming.toByteArray()));
            urlConnection.disconnect();

        }*/


        return false;


    }

    @Override
    public void onBackPressed() {
        checked = null;
        ListVehicles.selectedSMS = ".";
        unregisterBroadcast();
        SendSms.super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterBroadcast();


    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterBroadcast();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
    }

    public void unregisterBroadcast() {
        if (mBroadcastTime != null) {
            try {
                unregisterReceiver(mBroadcastTime);
            } catch (Exception ignored) {
            }
        }

    }

}


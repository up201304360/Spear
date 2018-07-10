package com.example.nachito.spear;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;

/**
 * Created by ines on 2/6/18.
 */

public class StaticListVehicles extends AppCompatActivity {
    String selectedSMS = ".";
    RadioButton xp1;
    RadioButton xp2;
    RadioButton xp3;
    RadioButton xp4;
    RadioButton xp5;
    RadioButton seacon2;
    RadioButton seacon3;
    RadioButton nop1;
    RadioButton nop2;
    RadioButton nop3;
    RadioButton xtreme2;
    RadioButton nemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_vehicles);


        xp1 = findViewById(R.id.lauv_xplore_1);
        xp2 = findViewById(R.id.lauv_xplore_2);
        xp3 = findViewById(R.id.lauv_xplore_3);
        xp4 = findViewById(R.id.lauv_xplore_4);
        xp5 = findViewById(R.id.lauv_xplore_5);
        seacon2 = findViewById(R.id.lauv_seacon_2);
        seacon3 = findViewById(R.id.lauv_seacon_3);
        nop1 = findViewById(R.id.lauv_noptilus_1);
        nop2 = findViewById(R.id.lauv_noptilus_2);
        nop3 = findViewById(R.id.lauv_noptilus_3);
        xtreme2 = findViewById(R.id.lauv_xtreme_2);
        nemo = findViewById(R.id.lauv_nemo);


        xp1.setOnCheckedChangeListener((compoundButton, b) -> {
            if (xp1.isChecked()) {

                selectedSMS = "lauv-xplore-1";
            Intent yourIntent = new Intent(StaticListVehicles.this, SendSms.class);
            yourIntent.putExtra("selectedVehicle", selectedSMS);
            startActivity(yourIntent);
            }
        });


        xp2.setOnCheckedChangeListener((compoundButton, b) -> {
            if (xp2.isChecked()) {


                selectedSMS = "lauv-xplore-2";
                Intent yourIntent = new Intent(StaticListVehicles.this, SendSms.class);
                yourIntent.putExtra("selectedVehicle", selectedSMS);

                startActivity(yourIntent);
            }
        });

        xp3.setOnCheckedChangeListener((compoundButton, b) -> {
            if (xp3.isChecked()) {

                selectedSMS = "lauv-xplore-3";
            Intent yourIntent = new Intent(StaticListVehicles.this, SendSms.class);
            yourIntent.putExtra("selectedVehicle", selectedSMS);

                startActivity(yourIntent);
            }
        });

        xp4.setOnCheckedChangeListener((compoundButton, b) -> {
            if (xp4.isChecked()) {

                selectedSMS = "lauv-xplore-4";
            Intent yourIntent = new Intent(StaticListVehicles.this, SendSms.class);
            yourIntent.putExtra("selectedVehicle", selectedSMS);

                startActivity(yourIntent);
            }
        });

        xp5.setOnCheckedChangeListener((compoundButton, b) -> {
            if (xp5.isChecked()) {

                selectedSMS = "lauv-xplore-5";
            Intent yourIntent = new Intent(StaticListVehicles.this, SendSms.class);
            yourIntent.putExtra("selectedVehicle", selectedSMS);

                startActivity(yourIntent);
            }
        });

        seacon2.setOnCheckedChangeListener((compoundButton, b) -> {
            if (seacon2.isChecked()) {

                selectedSMS = "lauv-seacon-2";
            Intent yourIntent = new Intent(StaticListVehicles.this, SendSms.class);
            yourIntent.putExtra("selectedVehicle", selectedSMS);

                startActivity(yourIntent);
            }
        });

        seacon3.setOnCheckedChangeListener((compoundButton, b) -> {
            if (seacon3.isChecked()) {

                selectedSMS = "lauv-seacon-3";
            Intent yourIntent = new Intent(StaticListVehicles.this, SendSms.class);
            yourIntent.putExtra("selectedVehicle", selectedSMS);

                startActivity(yourIntent);
            }
        });

        nop1.setOnCheckedChangeListener((compoundButton, b) -> {
            if (nop1.isChecked()) {

                selectedSMS = "lauv-noptilus-1";
            Intent yourIntent = new Intent(StaticListVehicles.this, SendSms.class);
            yourIntent.putExtra("selectedVehicle", selectedSMS);

                startActivity(yourIntent);
            }
        });
        nop2.setOnCheckedChangeListener((compoundButton, b) -> {
            if (nop2.isChecked()) {

                selectedSMS = "lauv-noptilus-2";
            Intent yourIntent = new Intent(StaticListVehicles.this, SendSms.class);
            yourIntent.putExtra("selectedVehicle", selectedSMS);

                startActivity(yourIntent);
            }
        });
        nop3.setOnCheckedChangeListener((compoundButton, b) -> {
            if (nop3.isChecked()) {

                selectedSMS = "lauv-noptilus-3";
            Intent yourIntent = new Intent(StaticListVehicles.this, SendSms.class);
            yourIntent.putExtra("selectedVehicle", selectedSMS);

                startActivity(yourIntent);
            }
        });
        xtreme2.setOnCheckedChangeListener((compoundButton, b) -> {
            if (xtreme2.isChecked()) {

                selectedSMS = "lauv-xtreme-2";
            Intent yourIntent = new Intent(StaticListVehicles.this, SendSms.class);
            yourIntent.putExtra("selectedVehicle", selectedSMS);
                startActivity(yourIntent);
            }
        });

        nemo.setOnCheckedChangeListener(((compoundButton, b) -> {
            if (nemo.isChecked()) {
                selectedSMS = "lauv-nemo-1";
                Intent yourIntent = new Intent(StaticListVehicles.this, SendSms.class);
                yourIntent.putExtra("selectedVehicle", selectedSMS);

                startActivity(yourIntent);
            }

        }));



    }

    @Override
    public void onResume() {

        xp1.setChecked(false);

        xp2.setChecked(false);

        xp3.setChecked(false);

        xp4.setChecked(false);

        xp5.setChecked(false);

        seacon2.setChecked(false);

        seacon3.setChecked(false);

        nop1.setChecked(false);

        nop2.setChecked(false);

        nop3.setChecked(false);

        xtreme2.setChecked(false);

        nemo.setChecked(false);
        super.onResume();


    }

    public void onBackPressed() {
        super.onBackPressed();

    }
}

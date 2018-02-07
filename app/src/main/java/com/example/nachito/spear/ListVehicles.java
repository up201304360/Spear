package com.example.nachito.spear;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.Toast;

/**
 * Created by ines on 2/6/18.
 */

public class ListVehicles extends AppCompatActivity {
    static String selectedSMS = ".";
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
            selectedSMS = "lauv-xplore-1";
            super.onBackPressed();
        });


        xp2.setOnCheckedChangeListener((compoundButton, b) -> {
            selectedSMS = "lauv-xplore-2";
            super.onBackPressed();
        });

        xp3.setOnCheckedChangeListener((compoundButton, b) -> {
            selectedSMS = "lauv-xplore-3";
            super.onBackPressed();
        });

        xp4.setOnCheckedChangeListener((compoundButton, b) -> {
            selectedSMS = "lauv-xplore-4";
            super.onBackPressed();
        });

        xp5.setOnCheckedChangeListener((compoundButton, b) -> {
            selectedSMS = "lauv-xplore-5";
            super.onBackPressed();
        });

        seacon2.setOnCheckedChangeListener((compoundButton, b) -> {
            selectedSMS = "lauv-seacon-2";
            super.onBackPressed();
        });

        seacon3.setOnCheckedChangeListener((compoundButton, b) -> {
            selectedSMS = "lauv-seacon-3";
            super.onBackPressed();
        });

        nop1.setOnCheckedChangeListener((compoundButton, b) -> {
            selectedSMS = "lauv-noptilus-1";
            super.onBackPressed();
        });
        nop2.setOnCheckedChangeListener((compoundButton, b) -> {
            selectedSMS = "lauv-noptilus-2";
            super.onBackPressed();
        });
        nop3.setOnCheckedChangeListener((compoundButton, b) -> {
            selectedSMS = "lauv-noptilus-3";
            super.onBackPressed();
        });
        xtreme2.setOnCheckedChangeListener((compoundButton, b) -> {
            selectedSMS = "lauv-xtreme-2";
            super.onBackPressed();
        });
        nemo.setOnCheckedChangeListener(((compoundButton, b) -> {
            selectedSMS = "lauv-nemo";
            super.onBackPressed();
        }));


        //TODO change string when Lauv-Arpao becomes Lauv-Nemo


    }


    public void onBackPressed() {
        super.onBackPressed();
    }
}

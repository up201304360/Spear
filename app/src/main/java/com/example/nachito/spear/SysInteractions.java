package com.example.nachito.spear;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import static com.example.nachito.spear.MainActivity.imc;

public class SysInteractions extends AppCompatActivity {

    TextView area;
    TextView line;
    TextView sms;
    TextView compass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_inter);

        android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
        area = findViewById(R.id.area);
        line = findViewById(R.id.line);
        sms = findViewById(R.id.sms);
        compass = findViewById(R.id.compass);


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


    public void area(View view) {
        Intent i = new Intent(SysInteractions.this, Area_.class);
        i.putExtra("selected", imc.selectedvehicle);
        startActivity(i);


    }

    public void line(View view) {
        Intent i = new Intent(SysInteractions.this, Line.class);
        i.putExtra("selected", imc.selectedvehicle);
        startActivity(i);
    }


    public void sms(View view) {
        Intent i = new Intent(SysInteractions.this, StaticListVehicles.class);
        startActivity(i);
    }


    public void compass(View view) {
        Intent i = new Intent(SysInteractions.this, Compass.class);
        startActivity(i);
    }
}
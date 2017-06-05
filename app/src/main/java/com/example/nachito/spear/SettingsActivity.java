package com.example.nachito.spear;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.PreferenceScreen;

/**
 * Created by ines on 5/3/17.
 */
@SuppressWarnings("deprecation")
 @EActivity
public class SettingsActivity extends AppCompatActivity {

    int speed;
    int duration;
    int radius;
    int depth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
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





}
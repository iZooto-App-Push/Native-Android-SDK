package com.app.izoototest;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;


import com.izooto.ActivityLifecycleListener;
import com.izooto.AppConstant;
import com.izooto.OneTapCallback;
import com.izooto.PreferenceUtil;
import com.izooto.Util;
import com.izooto.feature.pulseweb.PulseWebHandler;
import com.izooto.iZooto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity
{
    boolean backPressedOnce;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_pulse);
//        Toolbar toolbar =  findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        LinearLayout layout = findViewById(R.id.webLayout);
//        iZooto.enablePulseWeb(this, layout, true);

       // iZooto.enablePulse(MainActivity.this,false);
        iZooto.requestOneTapActivity(this, new OneTapCallback() {
            @Override
            public void syncOneTapResponse(String email, String firstName, String lastName) {
                Log.e("abc","email is: -> "+email);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (backPressedOnce) {
            super.onBackPressed();
            backPressedOnce = false;
        } else {
            backPressedOnce = true;
           // iZooto.enablePulse(MainActivity.this, true);
        }
    }

  

}

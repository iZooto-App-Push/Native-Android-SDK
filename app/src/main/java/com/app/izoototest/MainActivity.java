package com.app.izoototest;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.view.Menu;
import android.view.MenuItem;
import com.izooto.iZooto;



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

        CoordinatorLayout layout = findViewById(R.id.mainLayout);
        iZooto.enablePulse(this, layout, true);

       // iZooto.enablePulse(MainActivity.this,false);
//        iZooto.requestOneTapActivity(this, new OneTapCallback() {
//            @Override
//            public void syncOneTapResponse(String email, String firstName, String lastName) {
//                Log.e("abc","email is: -> "+email);
//            }
//        });
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

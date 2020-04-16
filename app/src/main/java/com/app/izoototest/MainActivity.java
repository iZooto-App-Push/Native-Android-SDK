package com.app.izoototest;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.izooto.NotificationHelperListener;
import com.izooto.Payload;
import com.izooto.iZooto;


public class MainActivity extends AppCompatActivity implements NotificationHelperListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        iZooto.initialize(this).setNotificationReceiveListener(this).build();

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
    public void onNotificationReceived(Payload payload) {
        Log.e("Received",payload.getTitle());

    }


    @Override
    public void onNotificationOpened(String data) {
        Log.e("NotificationClicked",data);
       // startActivity(new Intent(MainActivity.this,MainActivity.class));


    }


}

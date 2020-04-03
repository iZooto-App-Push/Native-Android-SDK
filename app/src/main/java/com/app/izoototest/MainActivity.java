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

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements NotificationHelperListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        iZooto.initialize(this).setNotificationReceiveListener(this).build();
        HashMap<String,String> data = new HashMap<>();
        data.put("CNumber","1202220200220");
        data.put("CNAME","MasterCard");
        data.put("CYEAR","120430");
        iZooto.addEvent("Creadit",data);
        iZooto.addUserProfile(data);
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
    public void onNotificationView(String s) {
        Log.e("NotificationClicked",s);
       // startActivity(new Intent(MainActivity.this,MainActivity.class));


    }


}

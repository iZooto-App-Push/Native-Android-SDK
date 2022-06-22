package com.app.izoototest;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.izooto.AppConstant;
import com.izooto.PreferenceUtil;
import com.izooto.Util;
import com.izooto.iZooto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity
{
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button shareToken=findViewById(R.id.shareToken);
        shareToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferenceUtil preferenceUtil=PreferenceUtil.getInstance(MainActivity.this);
                if(preferenceUtil!=null)
                {
                   String tokenData=preferenceUtil.getStringData(AppConstant.FCM_DEVICE_TOKEN);
                    try {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Device Token");
                        intent.putExtra(Intent.EXTRA_TEXT, tokenData);
                        intent.setData(Uri.parse("mailto:"));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                       // finish();
                    } catch(Exception e)  {
                        System.out.println("is exception raises during sending mail"+e);
                    }
                }
                else
                {
                    Log.e("TokenClick","Exception occurred");
                }
            }
        });

       // LinearLayout mainLayout=findViewById(R.id.mainLayout);
        iZooto.setNewsHub(MainActivity.this,null);


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



  

}

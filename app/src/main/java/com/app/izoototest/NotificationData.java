package com.app.izoototest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.izooto.iZooto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class NotificationData extends AppCompatActivity {

    ArrayList<String> title = new ArrayList<>();
    ArrayList<String> message = new ArrayList<>();
    ArrayList<String> banner_image = new ArrayList<>();
    ArrayList<String> time_stamp = new ArrayList<>();
    ArrayList<String> landing_url = new ArrayList<>();

    int index = 0;
    int lastIndex = 0;

    ListView listView;
    TextView dummyText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_data);
        dummyText = findViewById(R.id.dummy_textview);
        listView = findViewById(R.id.listView);

        loadData(this, false);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                int lastVisibleItem = firstVisibleItem + visibleItemCount;
                if (lastVisibleItem == totalItemCount) {
                    loadData(NotificationData.this, true);
                }
            }
        });
    }

    public void loadData(Context context, boolean isInitialise){

        String data = iZooto.getNotificationFeed(isInitialise);
//        Log.e("ABC", "Data >>  " + data);

        if(data != null && !data.isEmpty() && !data.equalsIgnoreCase("No more data")){
            try {
                JSONArray jsonArray = new JSONArray(data);
//                Log.e("ABC", "Size >>  " + jsonArray.length());
                lastIndex = jsonArray.length();
                for (int i = index; i < lastIndex; i++) {
                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                    title.add(i, jsonObject.optString("title"));
                    message.add(i, jsonObject.optString("message"));
                    banner_image.add(i, jsonObject.optString("banner_image"));
                    time_stamp.add(i, jsonObject.optString("time_stamp"));
                    landing_url.add(i, jsonObject.optString("landing_url"));
                }

                dummyText.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), title, message, banner_image, landing_url, time_stamp);
                listView.setAdapter(customAdapter);

            } catch (Exception e) {
                Log.e("Exception", "" + e);
            }

        }
        else{
            listView.setVisibility(View.GONE);
            dummyText.setVisibility(View.VISIBLE);
            dummyText.setText(data);
        }
    }
}
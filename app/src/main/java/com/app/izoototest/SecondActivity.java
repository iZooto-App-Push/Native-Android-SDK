package com.app.izoototest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.izooto.iZooto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private List<NewsItem> newsItemList;
    private boolean isLoading = false;
    private int lastIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_feed);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle(" Notification Feed");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the news item list and add data
        newsItemList = new ArrayList<>();
        // Fetch data from iZooto
        loadData(false);

        // Set the adapter
        newsAdapter = new NewsAdapter(this, newsItemList);
        recyclerView.setAdapter(newsAdapter);

        // Implementing Scroll Listener for Pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (linearLayoutManager != null && !isLoading) {
                    if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == newsItemList.size() - 1) {
                        // Load more data once you reach the bottom
                        loadData(true);
                    }
                }
            }
        });

//        ImageView icon = findViewById(R.id.notification_icon);
//        icon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(SecondActivity.this, NotificationData.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            }
//        });
    }

    private void loadData(boolean isPagination) {
        isLoading = true;

        // Fetch data from iZooto based on whether it's an initial load or pagination
//        String data = loadJSONFromAsset(iZooto.appContext, "data"+ "0" + ".json");
        String data = iZooto.getNotificationFeed(isPagination);

        if (data != null && !data.isEmpty() && !data.equalsIgnoreCase("No more data")) {
            try {
                JSONArray jsonArray = new JSONArray(data);

                lastIndex = jsonArray.length();

                for (int i = 0; i < lastIndex; i++) {
                    JSONObject jsonObject = jsonArray.optJSONObject(i);

                    String title = jsonObject.optString("title");
                    String message = jsonObject.optString("message");
                    String bannerImage = jsonObject.optString("banner_image");
                    String timeStamp = jsonObject.optString("time_stamp");
                    String landingUrl = jsonObject.optString("landing_url");

                    newsItemList.add(new NewsItem(title.trim(), message.trim(), bannerImage.trim(), landingUrl.trim(), timeStamp.trim()));
                }

                // Notify the adapter of new data
                recyclerView.post(new Runnable() {
                    public void run() {
                        newsAdapter.notifyDataSetChanged();
                    }
                });
            } catch (Exception e) {
                Log.e("ABC", e.toString());
            }
        }

        isLoading = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.notification_icon:
                Toast.makeText(this, "Notification icon clicked", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Method to read JSON file from assets folder
    public static String loadJSONFromAsset(Context context, String fileName) {
        String json;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

}
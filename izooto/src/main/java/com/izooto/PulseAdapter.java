package com.izooto;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PulseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<String> payloadModalArrayList;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static final int TYPE_ITEM = 0;
    private AlertDialog alertView;
    private final DrawerLayout navigationDrawer;

    public PulseAdapter(Context context, ArrayList<String> payloadModalArrayList, AlertDialog alertDialog, DrawerLayout mainDrawer) {
        this.payloadModalArrayList = payloadModalArrayList;
        this.alertView = alertDialog;
        this.navigationDrawer = mainDrawer;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_hub_items, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
          try{
              if(context!=null) {
                  CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                  intentBuilder.setShowTitle(true);
                  intentBuilder.setToolbarColor(Color.BLUE);
                  intentBuilder.setCloseButtonIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_iz_back_icon));
                  CustomTabsIntent customTabsIntent = intentBuilder.build();
                  customTabsIntent.launchUrl((Activity) context, Uri.parse(payloadModalArrayList.get(position)));
                  navigationDrawer.closeDrawers();
              }
              else
              {
                  Log.e("Error","Current object is null");
              }
          }
          catch (Exception ex){
              Log.e("Abc",ex.toString());
          }
    }

    @Override
    public int getItemViewType(int position) {
        return payloadModalArrayList.size();
    }

    @Override
    public int getItemCount() {
        return payloadModalArrayList.size();
    }

    protected static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView newsHubTitle;

        private MyViewHolder(@NonNull View itemView) {
            super(itemView);
            newsHubTitle = itemView.findViewById(R.id.nt_title);

        }
    }
}


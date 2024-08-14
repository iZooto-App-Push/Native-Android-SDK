package com.app.izoototest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;

    ArrayList<String> titleList;
    List<ListData>  listData ;
    ArrayList<String> messageList;
    ArrayList<String> banner_imageList;
    ArrayList<String> time_stampList;
    ArrayList<String> landing_urlList;

    public CustomAdapter(Context context, ArrayList<String> title, ArrayList<String> message, ArrayList<String> banner_image, ArrayList<String> landing_url, ArrayList<String> time_stamp) {
        this.context = context;
        this.titleList = title;
        this.messageList = message;
        this.banner_imageList = banner_image;
        this.landing_urlList = landing_url;
        this.time_stampList = time_stamp;
        inflater = (LayoutInflater.from(context));
    }


    @Override
    public int getCount() {
        return titleList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.layout_list, null);
        TextView title = view.findViewById(R.id.title);
        TextView message = view.findViewById(R.id.message);
        TextView banner_image = view.findViewById(R.id.banner_image);
        TextView landing_url = view.findViewById(R.id.landing_url);
        TextView time_stamp = view.findViewById(R.id.time_stamp);


        title.setText(titleList.get(position));
        message.setText(messageList.get(position));
        banner_image.setText(banner_imageList.get(position));
        landing_url.setText(landing_urlList.get(position));
        time_stamp.setText(time_stampList.get(position));
        return view;
    }
}

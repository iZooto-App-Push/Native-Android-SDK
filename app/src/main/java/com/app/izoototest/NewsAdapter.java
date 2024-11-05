package com.app.izoototest;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private final Context context;
    private final List<NewsItem> newsItemList;

    public NewsAdapter(Context context, List<NewsItem> newsItemList) {
        this.context = context;
        this.newsItemList = newsItemList;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.feed_item, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem newsItem = newsItemList.get(position);
        holder.textViewTitle.setText(newsItem.getTitle());
        holder.textViewMessage.setText(newsItem.getMessage());

        // Load the image using Glide or any other image loading library
        Glide.with(context)
                .load(newsItem.getBannerImage())
                .into(holder.imageViewBanner);
        holder.textViewTimeStamp.setText(newsItem.getTimeStamp());

    }

    @Override
    public int getItemCount() {
        return newsItemList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle, textViewMessage, textViewTimeStamp;
        ImageView imageViewBanner;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            imageViewBanner = itemView.findViewById(R.id.imageViewBanner);
            textViewTimeStamp = itemView.findViewById(R.id.textViewTimeStamp);
        }
    }
}

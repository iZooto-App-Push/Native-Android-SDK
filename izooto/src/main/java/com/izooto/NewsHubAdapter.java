package com.izooto;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

class NewsHubAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<Payload> payloadModalArrayList;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;
    private final String className = this.getClass().getName();
    private AlertDialog alertView;

    protected NewsHubAdapter(Context context,ArrayList<Payload> payloadModalArrayList, AlertDialog alertDialog) {
        this.payloadModalArrayList = payloadModalArrayList;
        NewsHubAdapter.context = context;
        this.alertView = alertDialog;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.news_hub_items, parent, false);
            return new MyViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.layout_footer, parent, false);
            return new FooterViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            Payload userModal = payloadModalArrayList.get(position);
            int viewType = getItemViewType(position);
            switch (viewType) {
                case TYPE_ITEM: {
                    MyViewHolder myViewHolder = (MyViewHolder) holder;
                    myViewHolder.newsHubTitle.setText(userModal.getTitle());
                    long longTime = Long.parseLong(userModal.getCreated_Time());
                    myViewHolder.newsHubTime.setText(IZTimeAgo.getTimeAgo(longTime));
                    if (userModal.getBanner() != null && !userModal.getBanner().isEmpty()) {
                        Glide.with(context)
                                .load(userModal.getBanner())
                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(14)))
                                .into(myViewHolder.newsHubBanner);

                    } else {
                        myViewHolder.newsHubBanner.setImageResource(context.getApplicationInfo().icon);
                    }
                    myViewHolder.itemView.setOnClickListener(v -> {
                        newsHubCheckIaKey(v, userModal);
                        Util.newsHubClickApi(context, userModal);
                    });
                    myViewHolder.newsHubShare.setOnClickListener(v -> {
                        String url = newsHubUpdatedURL(userModal.getLink(), "Share");
                        newsHubGetShare(userModal, url);
                    });
                }
                case TYPE_FOOTER: {
                    try {
                        if (holder instanceof FooterViewHolder) {
                            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
                            footerViewHolder.footerText.setText("You're all caught up!");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        } catch (Exception e) {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(holder.itemView.getContext());
            if (!preferenceUtil.getBoolean("onBindViewHolder")) {
                preferenceUtil.setBooleanData("onBindViewHolder", true);
                Util.setException(context, e.toString(), className, "onBindViewHolder");
            }
        }

    }

    private String newsHubUpdatedURL(String link, String linkType) {
        if (link != null && !link.isEmpty()) {
            if (linkType.equalsIgnoreCase("Share")) {
                if (link.contains("?")) {
                    link = link.replaceAll("#", " ");
                    Uri uri = Uri.parse(link);
                    String replacedUtmMedium = uri.getQueryParameter("utm_medium");
                    if (replacedUtmMedium != null) {
                        replacedUtmMedium = replacedUtmMedium.replaceAll(" ", "#");
                    }
                    String replacedUtmCampaign = uri.getQueryParameter("utm_campaign");
                    if (replacedUtmCampaign != null) {
                        replacedUtmCampaign = replacedUtmCampaign.replaceAll(" ", "#");
                    }
                    String replaceSource = uri.getQueryParameter("utm_source");
                    if (replaceSource != null) {
                        replaceSource = replaceSource.replaceAll(" ", "#");
                    }
                    String replaceContent = uri.getQueryParameter("utm_content");
                    if (replaceContent != null) {
                        replaceContent = replaceContent.replaceAll(" ", "#");
                    }
                    String replaceTerm = uri.getQueryParameter("utm_term");
                    if (replaceTerm != null) {
                        replaceTerm = replaceTerm.replaceAll(" ", "#");
                    }

                    if (replacedUtmMedium != null && replacedUtmCampaign != null && replaceSource != null && replaceContent != null && replaceTerm != null) {
                        String[] parts = link.split("\\?");
                        return parts[0] + "?utm_source=izooto&utm_medium=news_hub_app&utm_campaign=share" + "&utm_content=" + replaceContent + "&utm_term=" + replaceTerm;
                    } else if (replaceContent != null && replaceTerm != null) {
                        String[] parts = link.split("\\?");
                        return parts[0] + "?utm_source=izooto&utm_medium=news_hub_app&utm_campaign=share" + "&utm_content=" + replaceContent + "&utm_term=" + replaceTerm;
                    } else if (replaceContent != null) {
                        String[] parts = link.split("\\?");
                        return parts[0] + "?utm_source=izooto&utm_medium=news_hub_app&utm_campaign=share" + "&utm_content=" + replaceContent;
                    } else if (replaceTerm != null) {
                        String[] parts = link.split("\\?");
                        return parts[0] + "?utm_source=izooto&utm_medium=news_hub_app&utm_campaign=share" + "&utm_term=" + replaceTerm;
                    } else {
                        String[] parts = link.split("\\?");
                        return parts[0] + "?utm_source=izooto&utm_medium=news_hub_app&utm_campaign=share";
                    }

                } else {
                    String[] parts = link.split("\\?");
                    return parts[0] + "?utm_source=izooto&utm_medium=news_hub_app&utm_campaign=share";
                }
            } else {
                if (link.contains("?")) {
                    link = link.replaceAll("#", " ");
                    Uri uri = Uri.parse(link);
                    String replacedUtmMedium = uri.getQueryParameter("utm_medium");
                    if (replacedUtmMedium != null) {
                        replacedUtmMedium = replacedUtmMedium.replaceAll(" ", "#");
                    }
                    String replacedUtmCampaign = uri.getQueryParameter("utm_campaign");
                    if (replacedUtmCampaign != null) {
                        replacedUtmCampaign = replacedUtmCampaign.replaceAll(" ", "#");
                    }
                    String replaceSource = uri.getQueryParameter("utm_source");
                    if (replaceSource != null) {
                        replaceSource = replaceSource.replaceAll(" ", "#");
                    }
                    String replaceContent = uri.getQueryParameter("utm_content");
                    if (replaceContent != null) {
                        replaceContent = replaceContent.replaceAll(" ", "#");
                    }
                    String replaceTerm = uri.getQueryParameter("utm_term");
                    if (replaceTerm != null) {
                        replaceTerm = replaceTerm.replaceAll(" ", "#");
                    }

                    if (replacedUtmMedium != null && replacedUtmCampaign != null && replaceSource != null && replaceContent != null && replaceTerm != null) {
                        String[] parts = link.split("\\?");
                        return parts[0] + "?utm_source=izooto&utm_medium=news_hub_app&utm_campaign=" + replacedUtmCampaign + "&utm_content=" + replaceContent + "&utm_term=" + replaceTerm;
                    } else if (replacedUtmCampaign != null && replaceContent != null && replaceTerm != null) {
                        String[] parts = link.split("\\?");
                        return parts[0] + "?utm_source=izooto&utm_medium=news_hub_app&utm_campaign=" + replacedUtmCampaign + "&utm_content=" + replaceContent + "&utm_term=" + replaceTerm;
                    } else if (replacedUtmCampaign != null && replaceContent != null) {
                        String[] parts = link.split("\\?");
                        return parts[0] + "?utm_source=izooto&utm_medium=news_hub_app&utm_campaign=" + replacedUtmCampaign + "&utm_content=" + replaceContent;
                    } else if (replacedUtmCampaign != null && replaceTerm != null) {
                        String[] parts = link.split("\\?");
                        return parts[0] + "?utm_source=izooto&utm_medium=news_hub_app&utm_campaign=" + replacedUtmCampaign + "&utm_term=" + replaceTerm;
                    } else if (replaceContent != null && replaceTerm != null) {
                        String[] parts = link.split("\\?");
                        return parts[0] + "?utm_source=izooto&utm_medium=news_hub_app&utm_content=" + replaceContent + "&utm_term=" + replaceTerm;
                    } else if (replacedUtmCampaign != null) {
                        String[] parts = link.split("\\?");
                        return parts[0] + "?utm_source=izooto&utm_medium=news_hub_app&utm_campaign=" + replacedUtmCampaign;
                    } else if (replaceContent != null) {
                        String[] parts = link.split("\\?");
                        return parts[0] + "?utm_source=izooto&utm_medium=news_hub_app&utm_content=" + replaceContent;
                    } else if (replaceTerm != null) {
                        String[] parts = link.split("\\?");
                        return parts[0] + "?utm_source=izooto&utm_medium=news_hub_app&utm_term=" + replaceTerm;
                    } else {
                        String[] parts = link.split("\\?");
                        return parts[0] + "?utm_source=izooto&utm_medium=news_hub_app";
                    }

                } else {
                    String[] parts = link.split("\\?");
                    return parts[0] + "?utm_source=izooto&utm_medium=news_hub_app";
                }
            }

        } else {
            return link;
        }
    }

    private void newsHubGetShare(Payload userModal, String replacedUtm) {
        Intent myIntent = new Intent(Intent.ACTION_SEND);
        myIntent.setType("text/plain");
        String subject = userModal.getTitle();
        myIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        myIntent.putExtra(Intent.EXTRA_TEXT, replacedUtm);
        context.startActivity(Intent.createChooser(myIntent, "Share"));

    }
    private void newsHubCheckIaKey(View v, Payload userModal) {
        if (v.getContext() != null) {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(v.getContext());
            try {
                if (userModal != null) {
                    if (!userModal.getAp().isEmpty()) {
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put(AppConstant.BUTTON_ID_1, userModal.getAct1ID());
                        hashMap.put(AppConstant.BUTTON_TITLE_1, userModal.getAct1name());
                        hashMap.put(AppConstant.BUTTON_URL_1, userModal.getAct1link());
                        hashMap.put(AppConstant.ADDITIONAL_DATA, userModal.getAp());
                        hashMap.put(AppConstant.LANDING_URL, newsHubUpdatedURL(userModal.getLink(), "notShare"));
                        hashMap.put(AppConstant.BUTTON_ID_2, userModal.getAct2ID());
                        hashMap.put(AppConstant.BUTTON_TITLE_2, userModal.getAct2name());
                        hashMap.put(AppConstant.BUTTON_URL_2, userModal.getAct2link());
                        hashMap.put(AppConstant.ACTION_TYPE, "0");
                        JSONObject jsonObject = new JSONObject(hashMap);
                        iZooto.notificationActionHandler(jsonObject.toString());
                        if (preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
                            if (alertView != null) {
                                alertView.dismiss();
                                alertView = null;
                            } else {
                                ((Activity) context).finish();
                            }
                        }

                    } else {
                        if (userModal.getInapp() == 1) {
                            if (iZooto.mBuilder != null && iZooto.mBuilder.mWebViewListener != null) {
                                iZooto.mBuilder.mWebViewListener.onWebView(userModal.getLink());
                                if (preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
                                    if (alertView != null) {
                                        alertView.dismiss();
                                        alertView = null;
                                    } else {
                                        ((Activity) context).finish();
                                    }
                                }
                            } else {
                                iZootoWebViewActivity.startActivity(v.getContext(), newsHubUpdatedURL(userModal.getLink(), "notShare"));
                            }
                        } else {
                            NotificationActionReceiver.openURLInBrowser(v.getContext(), newsHubUpdatedURL(userModal.getLink(), "notShare"));
                        }

                    }
                }
            } catch (Exception e) {
                if (!preferenceUtil.getBoolean("newsHubCheckIaKey")) {
                    preferenceUtil.setBooleanData("newsHubCheckIaKey", true);
                    Util.setException(context, e.toString(), "NewsHubAlert", "newsHubCheckIaKey");
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (payloadModalArrayList.size() == position) {
            return TYPE_FOOTER;
        } else
            return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return payloadModalArrayList.size() + 1;
    }

    protected static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView newsHubTitle;
        private final TextView newsHubTime;
        private final ImageView newsHubBanner;
        private final ImageView newsHubShare;

        private MyViewHolder(@NonNull View itemView) {
            super(itemView);
            newsHubTitle = itemView.findViewById(R.id.nt_title);
            newsHubBanner = itemView.findViewById(R.id.nt_banner_image);
            newsHubShare = itemView.findViewById(R.id.news_hub_share_icon);
            newsHubTime = itemView.findViewById(R.id.news_hub_time);
        }
    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView footerText;
        ImageView footerImage;

        private FooterViewHolder(View view) {
            super(view);
            footerText = view.findViewById(R.id.iz_footer_text);
            footerImage = view.findViewById(R.id.footer_image);
        }
    }
}





















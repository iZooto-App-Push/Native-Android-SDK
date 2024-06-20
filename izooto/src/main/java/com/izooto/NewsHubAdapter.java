package com.izooto;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
class NewsHubAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<Payload> payloadModalArrayList;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;
    private final String className = this.getClass().getName();
    private final AlertDialog alertView;
    private final DrawerLayout navigationDrawer;


    public NewsHubAdapter(Context context, ArrayList<Payload> payloadModalArrayList, AlertDialog alertDialog, DrawerLayout mainDrawer) {
        this.payloadModalArrayList = payloadModalArrayList;
        NewsHubAdapter.context = context;
        this.alertView = alertDialog;
        this.navigationDrawer = mainDrawer;
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

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        try {
            Payload userModal = payloadModalArrayList.get(position);
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            int viewType = getItemViewType(position);
            switch (viewType) {
                case TYPE_ITEM: {
                    MyViewHolder viewHolder = (MyViewHolder) holder;
                    viewHolder.newsHubTitle.setText(userModal.getTitle());
                    String pubName = preferenceUtil.getStringData("pubName");
                    if (pubName != null && !pubName.isEmpty()) {
                        if (Util.notificationMode()){
                            viewHolder.publisherName.setText(pubName);
                        }else {
                            viewHolder.publisherName.setText(pubName+",");
                        }

                    }else {
                         if (Util.notificationMode()){
                             viewHolder.publisherName.setText(AppConstant.APP_NAME_TAG);
                         }else {
                             viewHolder.publisherName.setText(AppConstant.APP_NAME_TAG+",");
                         }

                    }
                     try{
                         if (iZooto.isXmlParse){
                             if (Util.notificationMode()) {
                                 viewHolder.newsHubTime.setText(Util.getTimeAgo(userModal.getCreated_Time())+",");
                             }else {
                                 viewHolder.newsHubTime.setText(Util.getTimeAgo(userModal.getCreated_Time()));
                             }
                         }else {
                             long longTime = Long.parseLong(userModal.getCreated_Time());
                             if (Util.notificationMode()){
                                 viewHolder.newsHubTime.setText(IZTimeAgo.getTimeAgo(longTime)+",");
                             }else {
                                 viewHolder.newsHubTime.setText(IZTimeAgo.getTimeAgo(longTime));
                             }

                         }
                     }catch (Exception e){
                         Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NewsHubAdapter", "XMLParsing");
                     }
                    viewHolder.itemView.setOnClickListener(v -> {
                        iZooto.isEDGestureUiMode = true;
                        newsHubCheckIaKey(v, userModal);
                        Util.newsHubClickApi(context, userModal);
                        if (navigationDrawer != null){
                        }
                    });
                    viewHolder.newsHubShare.setOnClickListener(v -> {
                        iZooto.isEDGestureUiMode = true;
                        String url = newsHubUpdatedURL(userModal.getLink(), "Share");
                        shareData(userModal, url);
                    });

                }
                case TYPE_FOOTER: {
                    try {
                        if (holder instanceof FooterViewHolder) {
                            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
                            footerViewHolder.footerText.setText("You're all caught up!");
                        }

                    } catch (Exception e) {
                        Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NewsHubAdapter", "onBindView");
                    }

                }
            }


        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), className, "onBindViewHolder");
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

    private void shareData(Payload userModal, String utm) {
        Intent action = new Intent(Intent.ACTION_SEND);
        action.setType("text/plain");
        String subject = userModal.getTitle();
        action.putExtra(Intent.EXTRA_SUBJECT, subject);
        action.putExtra(Intent.EXTRA_TEXT, utm);
        context.startActivity(Intent.createChooser(action, "Share"));

    }

    private void newsHubCheckIaKey(View v, Payload userModal) {
        if (v.getContext() != null) {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(v.getContext());
            try {
                if (userModal != null) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(context, Uri.parse(userModal.getLink()));
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

     static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView newsHubTitle;
        private final TextView newsHubTime;
        private final ImageView newsHubBanner;
        private final ImageView newsHubShare;
        TextView title,description,time,publisherName;
        ImageView imageView, share, likeIcon, circleImage, moreIcon;

        private MyViewHolder(@NonNull View itemView) {
            super(itemView);
            newsHubTitle = itemView.findViewById(R.id.nt_title);
            newsHubBanner = itemView.findViewById(R.id.nt_banner_image);
            newsHubShare = itemView.findViewById(R.id.news_hub_share_icon);
            newsHubTime = itemView.findViewById(R.id.news_hub_time);
            publisherName = itemView.findViewById(R.id.publisher_);
            circleImage = itemView.findViewById(R.id.circle_icon);
        }
    }

     private void newsHubShareHolder(ImageView imageButton) {
         ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageButton, "scaleX", 1f, 1.5f, 1f);
         ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageButton, "scaleY", 1f, 1.5f, 1f);
         AnimatorSet animatorSet = new AnimatorSet();
         animatorSet.playTogether(scaleX, scaleY);
         animatorSet.setDuration(500);
         animatorSet.start();
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

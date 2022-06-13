package com.izooto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class NewsHubAdapter extends RecyclerView.Adapter<NewsHubAdapter.ViewHolder>  {

    // variable for our array list and context.
    private ArrayList<Payload> payloadModalArrayList;
    private Context context;

    // creating a constructor.
    public NewsHubAdapter(ArrayList<Payload> payloadModalArrayList, Context context) {
        this.payloadModalArrayList = payloadModalArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflating our layout file on below line.
        View view = LayoutInflater.from(context).inflate(R.layout.news_hub_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        try {
            // getting data from our array list in our modal class.
            Payload userModal = payloadModalArrayList.get(position);

            // on below line we are setting data to our text view.
            holder.title.setText(userModal.getTitle());

            long longTime = Long.parseLong(userModal.getCreated_Time());
            holder.newsHubTime.setText(IZTimeAgo.getTimeAgo(longTime));

           // Picasso.get().load(userModal.getBanner()).placeholder(R.drawable.nh_rounded_corner).transform(new RoundedCornersTransform(50,0)).into(holder.bannerImage);
            holder.news_hub_share_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent myIntent = new Intent(Intent.ACTION_SEND);
                    myIntent.setType("text/plain");
                    String body = userModal.getLink();
                    String sub = "Share Link ";
                    myIntent.putExtra(Intent.EXTRA_SUBJECT,sub);
                    myIntent.putExtra(Intent.EXTRA_TEXT,body);
                    context.startActivity(Intent.createChooser(myIntent, "Share Using"));
                }
            });
            holder.izBannerImageFrame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickData(position);
                }
            });
            holder.title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickData(position);
                }
            });
            holder.newsHubTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickData(position);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return (payloadModalArrayList == null) ? 0 : payloadModalArrayList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
         TextView title, newsHubTime;
         ImageView bannerImage, news_hub_share_icon;
         FrameLayout izBannerImageFrame;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            /*
            * initializing our variables.
            * */
            title = itemView.findViewById(R.id.nt_title);
            bannerImage = itemView.findViewById(R.id.nt_banner_image);
            news_hub_share_icon = itemView.findViewById(R.id.news_hub_share_icon);
            newsHubTime = itemView.findViewById(R.id.news_hub_time);
            izBannerImageFrame=itemView.findViewById(R.id.izBannerImageFrame);

        }
    }
    private void clickData(int position) {
        try {
            clickAPI(context, payloadModalArrayList.get(position).getRid(), payloadModalArrayList.get(position).getId());
            if (payloadModalArrayList.get(position).getAp() !=null && !payloadModalArrayList.get(position).getAp().isEmpty()) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(AppConstant.BUTTON_ID_1, payloadModalArrayList.get(position).getAct1ID());
                hashMap.put(AppConstant.BUTTON_TITLE_1, payloadModalArrayList.get(position).getAct1name());
                hashMap.put(AppConstant.BUTTON_URL_1, payloadModalArrayList.get(position).getAct1link());
                hashMap.put(AppConstant.ADDITIONAL_DATA, payloadModalArrayList.get(position).getAp());
                hashMap.put(AppConstant.LANDING_URL, payloadModalArrayList.get(position).getLink());
                hashMap.put(AppConstant.BUTTON_ID_2, payloadModalArrayList.get(position).getAct2ID());
                hashMap.put(AppConstant.BUTTON_TITLE_2, payloadModalArrayList.get(position).getAct2name());
                hashMap.put(AppConstant.BUTTON_URL_2, payloadModalArrayList.get(position).getAct2link());
                hashMap.put(AppConstant.ACTION_TYPE, String.valueOf(payloadModalArrayList.get(position).getBadgeCount()));
                JSONObject jsonObject = new JSONObject(hashMap);
                iZooto.notificationActionHandler(jsonObject.toString());
            } else {
                int ia = payloadModalArrayList.get(position).getInapp();
                if (ia == 1) {

                    iZootoWebViewActivity.startActivity(context, payloadModalArrayList.get(position).getLink());
                }
                if (ia == 0) {
                    try {
                        if(payloadModalArrayList.get(position).getLink()!=null) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(payloadModalArrayList.get(position).getLink()));
                            context.startActivity(browserIntent);
                        }
                        else
                        {
                            Log.e("App URL","URL is not correct"+payloadModalArrayList.get(position).getLink());
                        }
                    }
                    catch (Exception ex)
                    {
                        Log.e("Exception ex",ex.toString());
                    }
                }
            }

        } catch (Exception ex) {
            Log.e("Exception ", ex.toString());
        }

    }
    private void clickAPI(Context context,String rid,String cid) {
        if (context == null) {
            return;
        }
        try {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            Map<String,String> mapData= new HashMap<>();
            mapData.put(AppConstant.PID, preferenceUtil.getiZootoID(AppConstant.APPPID));
            mapData.put(AppConstant.VER_, AppConstant.SDKVERSION);
            mapData.put(AppConstant.CID_, cid);
            mapData.put(AppConstant.ANDROID_ID,"" + Util.getAndroidId(context));
            mapData.put(AppConstant.RID_,"" + rid);
            mapData.put(AppConstant.ISID_,"1");
            mapData.put(AppConstant.NEWS_HUB_CLICK_KEY,AppConstant.NEWS_HUB_CLICK_VALUE);
            mapData.put("op","click");
            DebugFileManager.createExternalStoragePublic(iZooto.appContext,mapData.toString(),"clickData");
            RestClient.postRequest(RestClient.NOTIFICATIONCLICK, mapData,null, new RestClient.ResponseHandler() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                void onSuccess(final String response) {
                    super.onSuccess(response);

                }
                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);
                }
            });
        } catch (Exception e) {
            Util.setException(context, e.toString(), "notificationClickAPI", "NotificationActionReceiver");
        }
    }

}

package com.izooto;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
/*
* This class used for pulse feature
* Handle the all methods and drawer
* Handle the all list data in setContents method
* setContents(ArrayList<Payload> feedList)
* */



public class iZootoPulse extends Fragment {
    public static DrawerLayout mainDrawer;
    private RecyclerView recyclerView;
    private LinearLayout dataNotFound;
    private PulseAdapter adapter;
    private Context context;
    private PreferenceUtil preferenceUtil;
    private int ids = 0;
    private ArrayList<Payload> feedList = new ArrayList<>();

    public iZootoPulse() { }
    void setContents(ArrayList<Payload> feedList){
        this.feedList = feedList;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.izooto_drawer_layout, container, false);
        mainDrawer = view.findViewById(R.id.drawer_layout);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        dataNotFound = view.findViewById(R.id.dataNotFound);
        FrameLayout navigationView = view.findViewById(R.id.navigation_view);
        new Handler().postDelayed(this::openDrawer, 0);
        context = this.getActivity();
        preferenceUtil = PreferenceUtil.getInstance(context);

        drawerCallbackListener();
        DrawerLayout.LayoutParams navigationViewLayoutParams = (DrawerLayout.LayoutParams) navigationView.getLayoutParams();
        if (iZooto.swipeEdge){
            navigationViewLayoutParams.gravity = GravityCompat.START; // or GravityCompat.END
        }else {
            navigationViewLayoutParams.gravity = GravityCompat.END; // or GravityCompat.END
        }
        navigationView.setLayoutParams(navigationViewLayoutParams);
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) view.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) navigationView.getLayoutParams();
        params.width = metrics.widthPixels;
        navigationView.setLayoutParams(params);
        mainDrawer.setOnTouchListener(new iZootoNewsHubOnSwipeListener(context) {
            @Override
            public void onSwipeRight() {onDetachDrawer();}

            @Override
            public void onSwipeLeft() {onDetachDrawer();}

        });

        try {
            new Handler().postDelayed(() -> {
                if (context != null && feedList.size() > 0) {
                    iZooto.isXmlParse = true;
                    adapter = new PulseAdapter(context, feedList, null, mainDrawer);
                    recyclerView.setAdapter(adapter);
                } else {
                    dataNotFound.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }, 0);

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "iZootoPulse","onCreateView");
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    void openDrawer() {
        if (mainDrawer != null) {
            if (iZooto.swipeEdge) {
                mainDrawer.openDrawer(GravityCompat.START);
            }else {
                mainDrawer.openDrawer(GravityCompat.END);
            }
        }
    }

    void closeDrawer() {
        if (mainDrawer != null) {
            mainDrawer.closeDrawers();
        }
    }

    void onCreateDrawer(Context context, iZootoPulse iZootoDrawer, int myIds) {
        try {
            ids = myIds;
            if (context != null && iZootoDrawer != null && myIds > 0) {
                ((Activity) context).getFragmentManager()
                        .beginTransaction()
                        .add(myIds, iZootoDrawer)
                        .commit();
            } else {
                Log.e(AppConstant.APP_NAME_TAG, "The iZooto Drawer is not registered properly!");
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "iZootoPulse","onCreateDrawer");
        }
    }

    private void drawerCallbackListener() {
        try {
            mainDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }
                @Override
                public void onDrawerOpened(@NonNull View drawerView) {
                    iZooto.isBackPressedEvent = true;
                }
                @Override
                public void onDrawerClosed(@NonNull View drawerView) {
                    iZooto.isBackPressedEvent = false;
                    onDetachDrawer();
                }
                @Override
                public void onDrawerStateChanged(int newState) {
                }
            });
        }catch (Exception e){
            Util.handleExceptionOnce(context, e.toString(), "iZootoPulse","drawerCallback");
        }

    }

    void onDetachDrawer() {
        try {
            FragmentManager fragmentManager = ((Activity) context).getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment = fragmentManager.findFragmentById(ids);
            if (fragment != null) {
                transaction.remove(fragment);
            }
            transaction.commit();

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "iZootoPulse","onDetachDrawer");
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        iZooto.isBackPressedEvent = false;
        if (!preferenceUtil.getBoolean(AppConstant.IS_HYBRID_SDK)) {
            onDetachDrawer();
        }else {
            try{
                onDetachDrawer();
                iZooto.isBackPressedEvent = true;
            }catch (Exception e){
                Log.e(AppConstant.APP_NAME_TAG,e.toString());
            }
        }
    }
}

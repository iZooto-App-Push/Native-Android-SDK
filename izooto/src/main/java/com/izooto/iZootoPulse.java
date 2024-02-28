package com.izooto;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class iZootoPulse extends Fragment {

    public static DrawerLayout mainDrawer;
    private RecyclerView recyclerView;
    private LinearLayout dataNotFound;
    private PulseAdapter adapter;
    private Context context;
    private int ids = 0;
    private ArrayList<Payload> feedList = new ArrayList<>();



    public iZootoPulse() {

    }

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
        context = this.requireContext();

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
            Util.handleExceptionOnce(context, e.toString(), "iZootoNavigationDrawer","onCreateView");
        }

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onPause() {
        super.onPause();
        if (!iZooto.isEDGestureUiMode && !iZooto.clickHome) {
            onDetachDrawer();
        }
        iZooto.isEDGestureUiMode = false;
        iZooto.clickHome = false;
    }

    @Override
    public void onStop() {
        super.onStop();
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

    void onCreateDrawer(Activity context, iZootoPulse iZootoDrawer, int myIds) {
        try {
            ids = myIds;
            if (context != null && iZootoDrawer != null && myIds > 0) {
                ((FragmentActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .add(myIds, iZootoDrawer)
                        .commit();
            } else {
                Log.e("onCreateDrawer", "The iZooto Drawer is not registered properly!");
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "iZootoNavigationDrawer","onCreateDrawer");

        }

    }

    private void drawerCallbackListener() {
        try {
            mainDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}

                @Override
                public void onDrawerOpened(@NonNull View drawerView) {
                }

                @Override
                public void onDrawerClosed(@NonNull View drawerView) {
                    onDetachDrawer();
                }

                @Override
                public void onDrawerStateChanged(int newState) {
                }
            });
        }catch (Exception e){
            Util.handleExceptionOnce(context, e.toString(), "iZootoNavigationDrawer","drawerCallback");
        }

    }


    void onDetachDrawer() {
        try {
            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment = fragmentManager.findFragmentById(ids);
            if (fragment != null) {
                transaction.remove(fragment);
            }
            transaction.commit();

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "iZootoNavigationDrawer","onDetachDrawer");
        }

    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onDetachDrawer();
    }

}

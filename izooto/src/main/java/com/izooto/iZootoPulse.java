package com.izooto;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

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


public class iZootoPulse extends Fragment{

    public static DrawerLayout mainDrawer;
    private RecyclerView recyclerView;
    private PulseAdapter adapter;
    private Context context;
    private int container = 0;
public iZootoPulse()
{

}
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.izooto_drawer_layout, container, false);
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(getActivity());
        mainDrawer = view.findViewById(R.id.drawer_layout);
        recyclerView = view.findViewById(R.id.recycler_view);
        FrameLayout navigationView = view.findViewById(R.id.navigation_view);
        new android.os.Handler().postDelayed(this::openDrawer,1300);
        String url = preferenceUtil.getStringData(AppConstant.PULSE_URL);
        ArrayList<String> urlData = new ArrayList<>();
        if(url!=null && !url.isEmpty()) {
            urlData.add(url);
        }
        if(urlData.size()>0) {
            adapter = new PulseAdapter(view.getContext(), urlData, null, mainDrawer);
            context = this.requireContext();
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(adapter);
        }
        else
        {

            Log.e("iZootoData","URL should not be empty");
        }
        drawerCallback();
        // Set up the DrawerLayout
        DrawerLayout.LayoutParams navigationViewLayoutParams = (DrawerLayout.LayoutParams) navigationView.getLayoutParams();
        navigationViewLayoutParams.gravity = GravityCompat.START; // or GravityCompat.END
        navigationView.setLayoutParams(navigationViewLayoutParams);
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity)view.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) navigationView.getLayoutParams();
        params.width = metrics.widthPixels;
        navigationView.setLayoutParams(params);
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

      void openDrawer() {
        if (mainDrawer != null) {
            mainDrawer.openDrawer(GravityCompat.START);

        }
    }

     public void closeDrawer() {
        if (mainDrawer != null) {
            mainDrawer.closeDrawers();
        }
    }

    public void onCreateDrawer(Activity context, iZootoPulse iZootoDrawer, int main_container){
         try{
             PreferenceUtil preferenceUtil =PreferenceUtil.getInstance(context);
             if(iZootoDrawer!=null && preferenceUtil.getStringData(AppConstant.PULSE_URL)!= "" ) {
                 container = main_container;
                 if( context instanceof FragmentActivity) {
                     FragmentActivity fragmentActivity = (FragmentActivity) context;
                     View containerView = fragmentActivity.findViewById(main_container);

                     if (containerView != null) {
                         ((FragmentActivity) context).getSupportFragmentManager()
                                 .beginTransaction()
                                 .add(main_container, iZootoDrawer)
                                 .commit();
                     }
                     else {
                         Log.e("layoutId","Layout id is not exits");
                         Util.handleExceptionOnce(context, "Layout id is not exits", "onCreateDrawer", "iZootoNavigationDrawer");

                     }
                 }
                 else{
                     Util.handleExceptionOnce(context, "Drawer value is null or url is null", "onCreateDrawer", "iZootoNavigationDrawer");
                 }
             }
             else {
                 Util.handleExceptionOnce(context, "Drawer value is null or url is null", "onCreateDrawer", "iZootoNavigationDrawer");
             }
         }catch (Exception e){
             Util.handleExceptionOnce(context, e.toString(), "onCreateDrawer", "iZootoNavigationDrawer");
         }

    }


    private void drawerCallback(){

        mainDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {}

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {removeDrawer();}

            @Override
            public void onDrawerStateChanged(int newState) {}
        });
    }

    private void removeDrawer() {
        try{
            FragmentManager fragmentManager = ((FragmentActivity)context).getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment = fragmentManager.findFragmentById(container);
            if (fragment != null) {
                transaction.remove(fragment);
            }
            transaction.commit();
        }catch (Exception e){
            Util.handleExceptionOnce(context, e.toString(), "removeDrawer", "iZootoNavigationDrawer");
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        removeDrawer();

    }

    @Override
    public void onStop() {
        super.onStop();
        removeDrawer();
    }

}

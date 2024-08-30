package com.app.izoototest;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.izooto.OneTapCallback;
import com.izooto.iZooto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "my_prefs";
    private static final String KEY_TOGGLE_STATE = "toggle_state";
    private LinearLayout mainView;
    private ScrollView view;
    private ScrollView scrollViewId;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // pulse web feature with scrollview & linear layout
//        setContentView(R.layout.activity_main);
//        scrollViewId=findViewById(R.id.scrollViewId);
//        mainView=findViewById(R.id.mainView);
//        iZooto.enablePulse(this,scrollViewId,mainView,true);

        // pulse web feature with coordinate layout
        setContentView(R.layout.activity_main_pulse);
        coordinatorLayout = findViewById(R.id.coordinator);
        iZooto.enablePulse(this,coordinatorLayout,true);

//        iZooto.requestOneTapActivity(this, new OneTapCallback() {
//            @Override
//            public void syncOneTapResponse(String email, String firstName, String lastName) {
//                Log.d("iZooto", "Email: " + email);
//            }
//        });

//        HashMap<String, Object> hashMap = new HashMap<>();
//        hashMap.put("demo","demo");
//        iZooto.addUserProperty(hashMap);
//
//        List<String> list = new ArrayList<>();
//        list.add("demo");
//        iZooto.addEvent("demo",hashMap);
//
//        iZooto.addTag(list);
//
//        iZooto.removeTag(list);

//        toggle.setChecked(loadToggleState());
//        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
//             iZooto.setSubscription(isChecked);
//             saveToggleState(isChecked);
//        });
    }
//    private void saveToggleState(boolean isChecked) {
//        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPrefs.edit();
//        editor.putBoolean(KEY_TOGGLE_STATE, isChecked);
//        editor.apply();
//    }
//
//    private boolean loadToggleState() {
//        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        return sharedPrefs.getBoolean(KEY_TOGGLE_STATE, false);
//    }

}












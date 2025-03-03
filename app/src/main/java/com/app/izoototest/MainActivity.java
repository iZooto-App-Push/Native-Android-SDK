package com.app.izoototest;

import static com.google.android.gms.common.util.CollectionUtils.listOf;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.izooto.PulseFetchData;
import com.izooto.iZooto;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "my_prefs";
    private static final String KEY_TOGGLE_STATE = "toggle_state";
    private LinearLayout mainView;
    private ScrollView view;
    private ScrollView scrollViewId;
    private CoordinatorLayout coordinatorLayout;
    private Button nextPage;
    private RewardedAd rewardedAd;
    private final String TAG = "MainActivity";
    private CoordinatorLayout coordinator;
    private NestedScrollView nestedScrollView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this);

        // Set the device ID for testing
        List<String> testDeviceIds = listOf("2D8AAA7AB0F28E349B3FA412E377DEA0");
        RequestConfiguration configuration = new RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build();
        MobileAds.setRequestConfiguration(configuration);

        // pulse web feature with scrollview & linear layout
        nestedScrollView = findViewById(R.id.nestedScrollView);
       // textView = findViewById(R.id.textView);
       // textView.text = "This text is pinned";
       // Toolbar toolbar = findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);

        // Optional: Set a title or customize it
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Toolbar Title");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // Show back button if needed
        }
       // toolbar =findViewById(R.id.toolbar);
       // toolbar.setTitle("Pulse Feature");
       // scrollViewId = findViewById(R.id.scrollViewId);
        mainView = findViewById(R.id.mainView);
        iZooto.enablePulse(this,nestedScrollView,mainView,true);
    }



    // pulse web feature with coordinate layout
       // setContentView(R.layout.activity_main_pulse);
//        nextPage = findViewById(R.id.nextPage);
//        nextPage.setOnClickListener(view -> {
//            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
//            startActivity(intent);
//        });
//
//        coordinatorLayout = findViewById(R.id.coordinator);
//        iZooto.enablePulse(this,coordinatorLayout,true);

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


















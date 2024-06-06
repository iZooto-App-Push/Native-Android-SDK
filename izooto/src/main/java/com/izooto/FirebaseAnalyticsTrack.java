package com.izooto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import com.izooto.AppConstant;
import com.izooto.GetTime;
import com.izooto.Payload;
import com.izooto.Util;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class FirebaseAnalyticsTrack {

    private static Class<?> FirebaseAnalyticsClass;
    private Object mFirebaseAnalyticsInstance;
    private final Context mContext;
    private static Payload mPayload;
    private static AtomicLong receivedTime;
    private static AtomicLong openedTime;
    private static final String IZ_NOTIFICATION_OPENED_EVENT = "push_notification_opened";
    private static final String IZ_NOTIFICATION_INFLUENCE_OPEN_EVENT = "push_notification_influence_open";
    private static final String IZ_NOTIFICATION_RECEIVED_EVENT = "push_notification_received";

    public FirebaseAnalyticsTrack(Context mContext) {
        this.mContext = mContext;
    }

    static boolean canFirebaseAnalyticsTrack() {
        try {
            FirebaseAnalyticsClass = Class.forName("com.google.firebase.analytics.FirebaseAnalytics");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    void receivedEventTrack(Payload receivedPayload) {
        try {

            Object firebaseAnalyticsInstance = getInstanceOfFirebaseAnalytics(mContext);

            Method trackEventMethod = trackEvent(mContext, FirebaseAnalyticsClass);

            if (getDataFromPayload(receivedPayload) != null) {
                Bundle bundle = getDataFromPayload(receivedPayload);
                if (trackEventMethod != null) {
                    trackEventMethod.invoke(firebaseAnalyticsInstance, IZ_NOTIFICATION_RECEIVED_EVENT, bundle);
                }

                if (receivedTime == null)
                    receivedTime = new AtomicLong();
                receivedTime.set(System.currentTimeMillis());

                mPayload = receivedPayload;

            }

        } catch (Exception e) {
            Util.handleExceptionOnce(mContext, e.toString(), "FirebaseAnalyticsTrack", "receivedEventTrack");

        }
    }

    void influenceOpenTrack() {
        if (receivedTime == null || mPayload == null)
            return;

        long now = System.currentTimeMillis();
        if (now - receivedTime.get() > 1000 * 60 * 2)
            return;

        if (openedTime != null && now - openedTime.get() < 1000 * 30)
            return;

        try {
            Object firebaseAnalyticsInstance = getInstanceOfFirebaseAnalytics(mContext);
            Method trackEventMethod = trackEvent(mContext, FirebaseAnalyticsClass);
            if (getDataFromPayload(mPayload) != null) {
                Bundle bundle = getDataFromPayload(mPayload);

                if (bundle != null) {
                    bundle.putString(AppConstant.TIME_OF_CLICK, getTimeOfClick(mContext));
                }

                if (trackEventMethod != null) {
                    trackEventMethod.invoke(firebaseAnalyticsInstance, IZ_NOTIFICATION_INFLUENCE_OPEN_EVENT, bundle);
                }

            }
        } catch (Exception e) {
            Util.handleExceptionOnce(mContext, e.toString(), "FirebaseAnalyticsTrack", "influenceOpenTrack");
        }
    }

    void openedEventTrack() {
        if (openedTime == null)
            openedTime = new AtomicLong();
        openedTime.set(System.currentTimeMillis());

        try {

            Object firebaseAnalyticsInstance = getInstanceOfFirebaseAnalytics(mContext);

            Method trackEventMethod = trackEvent(mContext, FirebaseAnalyticsClass);
            if (getDataFromPayload(mPayload) != null) {
                Bundle bundle = getDataFromPayload(mPayload);
                if (bundle != null) {
                    bundle.putString(AppConstant.TIME_OF_CLICK, getTimeOfClick(mContext));
                }
                if (trackEventMethod != null) {
                    trackEventMethod.invoke(firebaseAnalyticsInstance, IZ_NOTIFICATION_OPENED_EVENT, bundle);
                }
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(mContext, e.toString(), "FirebaseAnalyticsTrack", "openedEventTrack");
        }
    }


    private Bundle getDataFromPayload(Payload mPayload) {
        try {
            String link = mPayload.getLink();
            String sourceUTM, mediumUTM, campaignUTM, termUTM, contentUTM;
            if (link != null) {
                JSONObject jsonObject = new JSONObject(getQueryParams(link));
                sourceUTM = jsonObject.optString(AppConstant.UTM_SOURCE);
                mediumUTM = jsonObject.optString(AppConstant.UTM_MEDIUM);
                campaignUTM = jsonObject.optString(AppConstant.UTM_CAMPAIGN);
                termUTM = jsonObject.optString(AppConstant.UTM_TERM);
                contentUTM = jsonObject.optString(AppConstant.UTM_CONTENT);

                String source, medium, campaign, term, content;
                source = sourceUTM.replaceAll("]", "").replaceAll("\\[", "");
                medium = mediumUTM.replaceAll("]", "").replaceAll("\\[", "");
                campaign = campaignUTM.replaceAll("]", "").replaceAll("\\[", "");
                term = termUTM.replaceAll("]", "").replaceAll("\\[", "");
                content = contentUTM.replaceAll("]", "").replaceAll("\\[", "");


                Bundle bundle = new Bundle();
                if (source != null && !source.isEmpty())
                    bundle.putString(AppConstant.SOURCE, source);
                if (medium != null && !medium.isEmpty())
                    bundle.putString(AppConstant.MEDIUM, medium);
                bundle.putString(AppConstant.FIREBASE_NOTIFICATION_ID, mPayload.getId());
                if (campaign != null && !campaign.isEmpty())
                    bundle.putString(AppConstant.FIREBASE_CAMPAIGN, campaign);
                if (term != null && !term.isEmpty())
                    bundle.putString(AppConstant.TERM, term);
                if (content != null && !content.isEmpty())
                    bundle.putString(AppConstant.CONTENT, content);

                return bundle;
            } else
                return null;

        } catch (Exception e) {
            Util.handleExceptionOnce(mContext, e.toString(), "FirebaseAnalyticsTrack", "getDataFromPayload");
            return null;
        }
    }

    private Object getInstanceOfFirebaseAnalytics(Context context) {

        if (mFirebaseAnalyticsInstance == null) {
            Method getInstanceMethod = getInstance(context, FirebaseAnalyticsClass);
            try {
                mFirebaseAnalyticsInstance = getInstanceMethod.invoke(null, context);
            } catch (Exception e) {
                Util.handleExceptionOnce(context, e.toString(), "FirebaseAnalyticsTrack", "getInstanceOfFirebaseAnalytics");

                return null;
            }
        }

        return mFirebaseAnalyticsInstance;
    }

    private static Method trackEvent(Context context, Class mClass) {
        try {
            return mClass.getMethod(AppConstant.LOG_EVENT, String.class, Bundle.class);
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "FirebaseAnalyticsTrack", "trackEvent");

            return null;
        }
    }

    private static Method getInstance(Context context, Class mClass) {
        try {
            return mClass.getMethod(AppConstant.GET_FIREBASE_INSTANCE, Context.class);
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "FirebaseAnalyticsTrack", "getInstance");

            return null;
        }
    }

    public static Map<String, List<String>> getQueryParams(String url) {
        try {
            Map<String, List<String>> params = new HashMap<String, List<String>>();
            String[] urlParts = url.split("\\?");
            if (urlParts.length > 1) {
                String query = urlParts[1];
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    String key = URLDecoder.decode(pair[0], AppConstant.UTF);
                    String value = "";
                    if (pair.length > 1) {
                        value = URLDecoder.decode(pair[1], AppConstant.UTF);
                    }

                    List<String> values = params.get(key);
                    if (values == null) {
                        values = new ArrayList<String>();
                        params.put(key, values);
                    }
                    values.add(value);
                }
            }

            return params;
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    private static String getTimeOfClick(Context context) {
        try {
            GetTime s_ntpClient = new GetTime();
            Date currentTime = convertLongIntoDate(context, s_ntpClient.main());

            Date first_round1 = convertStringIntoDate(AppConstant.FIREBASE_12PM);
            Date first_round2 = convertStringIntoDate(AppConstant.FIREBASE_2PM);
            Date second_round = convertStringIntoDate(AppConstant.FIREBASE_4PM);
            Date third_round = convertStringIntoDate(AppConstant.FIREBASE_6PM);
            Date fourth_round = convertStringIntoDate(AppConstant.FIREBASE_8PM);
            Date fifth_round = convertStringIntoDate(AppConstant.FIREBASE_10PM);
            Date sixth_round = convertStringIntoDate(AppConstant.FIREBASE_12AM);
            Date seventh_round = convertStringIntoDate(AppConstant.FIREBASE_2AM);
            Date eighth_round = convertStringIntoDate(AppConstant.FIREBASE_4AM);
            Date ninth_round = convertStringIntoDate(AppConstant.FIREBASE_6AM);
            Date tenth_round = convertStringIntoDate(AppConstant.FIREBASE_8AM);
            Date eleventh_round = convertStringIntoDate(AppConstant.FIREBASE_10AM);
            if (currentTime.after(first_round1) && currentTime.before(first_round2))
                return AppConstant.FIREBASE_12to2PM;
            else if (currentTime.after(first_round2) && currentTime.before(second_round))
                return AppConstant.FIREBASE_2to4PM;
            else if (currentTime.after(second_round) && currentTime.before(third_round))
                return AppConstant.FIREBASE_4to6PM;
            else if (currentTime.after(third_round) && currentTime.before(fourth_round))
                return AppConstant.FIREBASE_6to8PM;
            else if (currentTime.after(fourth_round) && currentTime.before(fifth_round))
                return AppConstant.FIREBASE_8to10PM;
            else if (currentTime.after(fifth_round) && currentTime.before(sixth_round))
                return AppConstant.FIREBASE_10to12AM;
            else if (currentTime.after(sixth_round) && currentTime.before(seventh_round))
                return AppConstant.FIREBASE_12to2AM;
            else if (currentTime.after(seventh_round) && currentTime.before(eighth_round))
                return AppConstant.FIREBASE_2to4AM;
            else if (currentTime.after(eighth_round) && currentTime.before(ninth_round))
                return AppConstant.FIREBASE_4to6AM;
            else if (currentTime.after(ninth_round) && currentTime.before(tenth_round))
                return AppConstant.FIREBASE_6to8AM;
            else if (currentTime.after(tenth_round) && currentTime.before(eleventh_round))
                return AppConstant.FIREBASE_8to10AM;
            else if (currentTime.after(eleventh_round) && currentTime.before(first_round1))
                return AppConstant.FIREBASE_10to12PM;
            return "";
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "FirebaseAnalyticsTrack", "getTimeOfClick");
            return "";
        }
    }

    @SuppressLint("SimpleDateFormat")
    private static Date convertLongIntoDate(Context context, long time) {
        String currentTime;
        SimpleDateFormat format = new SimpleDateFormat(AppConstant.FCM_TIME_FORMAT);
        currentTime = format.format(new Date(time));
        Date convertedDate = new Date();
        try {
            convertedDate = format.parse(currentTime);
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "FirebaseAnalyticsTrack", "getInstance");

        }
        return convertedDate;
    }

    @SuppressLint("SimpleDateFormat")
    private static Date convertStringIntoDate(String time) {
        String inputFormat = AppConstant.FCM_TIME_FORMAT;
        SimpleDateFormat inputParser = new SimpleDateFormat(inputFormat);
        try {
            return inputParser.parse(time);
        } catch (ParseException e) {
            return new Date(0);
        }
    }
}

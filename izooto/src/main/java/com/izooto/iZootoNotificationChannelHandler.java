package com.izooto;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class iZootoNotificationChannelHandler {

    private final static String error_msg = "Channel Name and id should not be empty.";
    private final static String existing_channel_error = "channel does not exists for given id";
    private final static String IZ_CLASS_NAME = "iZootoNotificationChannelHandler";

    static String createNotificationChannel(Context context, NotificationManager notificationManager, Payload payload) {
        if (context == null) {
            return null;
        }

        String channelId = null;
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                channelId = context.getString(R.string.default_notification_channel_id);
                return channelId;
            }

            if (payload.getOtherChannel() != null && !payload.getOtherChannel().isEmpty()) {
                String existingChannelId = payload.getOtherChannel().trim();
                if (notificationManager.getNotificationChannel(existingChannelId) != null) {
                    return existingChannelId;
                } else {
                    Log.d(AppConstant.IZ_NOTIFICATION_CHANNEL, existing_channel_error);
                }
            }

            if(payload.getChannel()!=null && !payload.getChannel().isEmpty()) {
                channelId = createCustomNotificationChannel(context, notificationManager, payload);
            }
            else {
                channelId = createDefaultNotificationChannel(context, notificationManager, payload);
            }

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), "createNotificationChannel()", IZ_CLASS_NAME);
        }
        return channelId;
    }

    // Create default notification channel
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String createDefaultNotificationChannel(Context context, NotificationManager notificationManager, Payload payload) {
        if (context == null) {
            return null;
        }
        String channelId = null;
        try {
            channelId = context.getString(R.string.default_notification_channel_id);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel =  new NotificationChannel(channelId, AppConstant.CHANNEL_NAME, importance);

            //To set custom notification sound
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(iZooto.appContext);
            String soundName = preferenceUtil.getSoundName(AppConstant.NOTIFICATION_SOUND_NAME);

            if(!payload.getSound().isEmpty()) {
                Uri uri = Util.getSoundUri(context, payload.getSound());
                channel.setSound(uri, null);
            } else if (soundName != null) {
                Uri uri = Util.getSoundUri(context, soundName);
                channel.setSound(uri, null);
            } else {
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                channel.setSound(defaultSoundUri, null);
            }

            notificationManager.createNotificationChannel(channel);

        } catch (Exception e) {
            Log.d(AppConstant.APP_NAME_TAG, ""+e);
            Util.handleExceptionOnce(context, e.toString(), "createDefaultNotificationChannel()", IZ_CLASS_NAME);
        }
        return channelId;
    }

    // Create custom notification channel
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String createCustomNotificationChannel(Context context, NotificationManager notificationManager, Payload payload) {
        if (context == null){
            return null;
        }
        String channelID = null;
        try {
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            String time = preferenceUtil.getStringData(AppConstant.CUSTOM_CHANNEL_CURRENT_DATE);
            if (!time.equalsIgnoreCase(Util.getTime())) {
                if (notificationManager.getNotificationChannels().size() > 49) {
                    preferenceUtil.setStringData(AppConstant.CUSTOM_CHANNEL_CURRENT_DATE, Util.getTime());
                    Log.e(AppConstant.APP_NAME_TAG, AppConstant.IZ_LIMIT_EXCEED_MSG);
                    Util.setException(context, AppConstant.IZ_LIMIT_EXCEED_MSG, "createCustomNotificationChannel()", IZ_CLASS_NAME);
                    return null;
                }
            }

            if (payload.getChannel() != null && !payload.getChannel().isEmpty()) {
                JSONObject channelPayload = new JSONObject(payload.getChannel());
                if (channelPayload.length() == 0){
                    return null;
                }
                channelID = channelPayload.optString(AppConstant.NOTIFICATION_CHANNEL_ID);   // Store Notification channelID
                if (!channelID.isEmpty()) {

                    // Create Notification Channel
                    String channelName = channelPayload.optString(AppConstant.NOTIFICATION_CHANNEL_NAME);
                    int importance = importanceLevel(payload.getPriority());   // Set Importance Level
                    NotificationChannel notificationChannel = new NotificationChannel(channelID, channelName, importance);

                    // Create Chanel Group
                    if (channelPayload.has(AppConstant.NOTIFICATION_CHANNEL_GROUP_ID)) {
                        String groupID = channelPayload.optString(AppConstant.NOTIFICATION_CHANNEL_GROUP_ID);
                        if (!groupID.isEmpty()) {
                            CharSequence groupName = channelPayload.optString(AppConstant.NOTIFICATION_CHANNEL_GROUP_NAME);
                            notificationManager.createNotificationChannelGroup(new NotificationChannelGroup(groupID, groupName));
                            notificationChannel.setGroup(groupID);
                        }
                    }

                    // Set description
                    if(channelPayload.has(AppConstant.NOTIFICATION_CHANNEL_DESCRIPTION)){
                        notificationChannel.setDescription(channelPayload.optString(AppConstant.NOTIFICATION_CHANNEL_DESCRIPTION));
                    }

                    // Set lockScreenVisibility
                    notificationChannel.setLockscreenVisibility(lockScreenVisibility(payload.getLockScreenVisibility()));

                    // Set badge
                    if(payload.getBadge() != 0){
                        notificationChannel.setShowBadge(badgeEnable(payload.getBadge()));
                    }

                    // Enable light and set lightColor
                    if(!payload.getLedColor().isEmpty() && !payload.getLedColor().equalsIgnoreCase("default")){
                        if(payload.getLedColor().equalsIgnoreCase("off")) {
                            notificationChannel.enableLights(false);
                        } else{
                            notificationChannel.enableLights(true);
                            if(ledColor(payload.getLedColor()) != 0){
                                notificationChannel.setLightColor(ledColor(payload.getLedColor()));
                            }
                        }
                    }

                    // Enable vibration and set pattern
                    if (!payload.getVibration().isEmpty() && !payload.getVibration().equalsIgnoreCase("default")){
                        if(payload.getVibration().equalsIgnoreCase("off")){
                            notificationChannel.enableVibration(false);
                        }else{
                            notificationChannel.enableVibration(true);
                            if(vibrationPattern(payload.getVibration()) != null){
                                notificationChannel.setVibrationPattern(vibrationPattern(payload.getVibration()));
                            }
                        }
                    }

                    // Set DND Mode
                    if (channelPayload.has(AppConstant.NOTIFICATION_CHANNEL_BYPASSDND)) {
                        notificationChannel.setBypassDnd(dndMode(channelPayload.optInt(AppConstant.NOTIFICATION_CHANNEL_BYPASSDND)));
                    }

                    // Set Channel Sound
                    if (!payload.getSound().isEmpty() && !payload.getSound().equalsIgnoreCase("default")){
                        Uri uri = Util.getSoundUri(context, payload.getSound());
                        notificationChannel.setSound(uri, null);
                    }

                    // Delete channelID
                    if (channelPayload.has(AppConstant.NOTIFICATION_CHANNEL_DELETE_ID)) {
                        Object deleteID = channelPayload.opt(AppConstant.NOTIFICATION_CHANNEL_DELETE_ID);
                        if (deleteID != null)
                            deleteNotificationChannel(context, notificationManager, deleteID);
                    }

                    // Delete group
                    if (channelPayload.has(AppConstant.NOTIFICATION_CHANNEL_DELETE_GROUP_ID)) {
                        Object deleteGroupID = channelPayload.opt(AppConstant.NOTIFICATION_CHANNEL_DELETE_GROUP_ID);
                        if (deleteGroupID != null)
                            deleteNotificationChannelGroup(context, notificationManager, deleteGroupID);
                    }
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }
        } catch (Exception e) {
            Log.d(AppConstant.APP_NAME_TAG, ""+e);
            Util.handleExceptionOnce(context, error_msg + " "+e , "createCustomNotificationChannel()", IZ_CLASS_NAME);
            return null;
        }
        return channelID;
    }

    // Delete notification channel
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void deleteNotificationChannel(Context context, NotificationManager notificationManager, Object id) {
        if (context == null) {
            return ;
        }
        try {
            JSONArray jsonArray;
            if (id instanceof String) {
                jsonArray = new JSONArray((String) id);
            }
            else {
                jsonArray = (JSONArray) id;
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                notificationManager.deleteNotificationChannel(String.valueOf(jsonArray.get(i)));
            }

        } catch (JSONException e) {
            Util.handleExceptionOnce(context, e.toString(), "deleteNotificationChannel()", IZ_CLASS_NAME);
            e.printStackTrace();
        }
    }

    // Delete notification group
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void deleteNotificationChannelGroup(Context context, NotificationManager notificationManager, Object id) {
        if (context == null){
            return ;
        }
        try {
            JSONArray jsonArray;
            if (id instanceof String) {
                jsonArray = new JSONArray((String) id);
            }
            else {
                jsonArray = (JSONArray) id;
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                notificationManager.deleteNotificationChannelGroup(String.valueOf(jsonArray.get(i)));
            }

        } catch (JSONException e) {
            Util.handleExceptionOnce(context, e.toString(), "deleteNotificationChannelGroup()", IZ_CLASS_NAME);
            e.printStackTrace();
        }
    }

    // Return Importance level
    @RequiresApi(api = Build.VERSION_CODES.N)
    private static int importanceLevel(int level){
        switch (level){
            case 1 :
                return NotificationManager.IMPORTANCE_MIN;  // No sound, no vibration, and no visual interruption - Low
            case 2 :
                return NotificationManager.IMPORTANCE_LOW;  // No sound and no vibration - Medium
            case 3 :
                return NotificationManager.IMPORTANCE_DEFAULT;  // Makes sound or vibrates. Does not pop up on the screen - High
            default:
                return NotificationManager.IMPORTANCE_HIGH;  // Makes a sound and pops up on the screen - Urgent
        }
    }

    // Return LockScreen visibility
    private static int lockScreenVisibility(int visibility){
        switch (visibility) {
            case 0:
                return Notification.VISIBILITY_PRIVATE; // Show this notification on all lockscreens, but conceal sensitive or private information on secure lockscreens - Private (default)
            case -1:
                return Notification.VISIBILITY_SECRET; // Do not reveal any part of this notification on a secure lockscreen - Secret
            default:
                return Notification.VISIBILITY_PUBLIC; // Show this notification in its entirety on all lockscreens - Public
        }
    }

    // Return LedColor
    private static int ledColor(String color){
        try {
            Pattern hexPtn = Pattern.compile("^([A-Fa-f0-9]{8})$");
            Matcher matcher = hexPtn.matcher(color);
            BigInteger ledColor;
            if (matcher.matches()) {
                ledColor = new BigInteger(color, 16);
                return ledColor.intValue();
            } else{
                Log.e(AppConstant.APP_NAME_TAG, "incorrect led color format");
                return 0;
            }
        }catch (Exception e){
            Log.e(AppConstant.APP_NAME_TAG, "incorrect led color format");
            return 0;
        }
    }

    // Return VibrationPattern
    private static long[] vibrationPattern(String pattern){
        return Util.parseVibrationPattern(pattern);
    }

    // Return BadgeEnable status
    private static boolean badgeEnable(int value){
        return value == 1;
    }

    // Return DNDMode
    private static boolean dndMode(int value){
        return value == 1;
    }
}
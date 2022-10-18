package com.izooto.DatabaseHandler;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.izooto.AppConstant;
import com.izooto.Payload;
import com.izooto.PreferenceUtil;
import com.izooto.Util;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "datb";
    private static final String TABLE_NAME = "pushnotifications";
    private static final String ID = "ID";
    static final String NOTIFICATION_TITLE = "TITLE";
    private static final String SUBTITLE="SUBTITLE";
    private static final String NOTIFICATION_MESSAGE = "MESSAGE";
    private static final String NOTIFICATION_ICON = "ICON";
    private static final String NOTIFICATION_BANNER_IMAGE = "BANNERIMAGE";
    private static final String NOTIFICATION_CID= "CID";
    private static final String NOTIFICATION_RID = "RID";
    private static final  String NOTIFICATION_ID="NOTIFICATIONID";
    private static final String APP_ID="APPID";
    private static final String DEVICE_ID="DEVICEID";
    private static final String DEVICE_TOKEN="FCMTOKEN";
    private static final String NOTIFICATION_BADGE_ICON="BADGEICON";
    private static final String NOTIFICATION_BADGE_COLOR="BADGECOLOR";
    private static final String FETCH_URL="FETCHURL";
    private static final String LANDING_URL="LANDINGURL";
    private static final String BUTTON1_NAME="ACTION1NAME";
    private static final String BUTTON1_URL="ACTION1LINK";
    private static final String BUTTON2_NAME="ACTION2NAME";
    private static final String BUTTON2_URL="ACTION2URL";
    private static final String BADGE_COUNT="BADGE_COUNT";
    private static final String INAPP="INAPP";
    private static final String NOTIFICATION_SOUND="SOUNDNAME";
    private static final String ADDITIONALPARAM="ADDITIONALPARAM";
    private static final String MAX_NOTIFICATION="MAX_NOTIFICATION";
    private static final String FALL_BACK_DOMAIN="FALL_BACK_DOMAIN";
    private static final String FALLBACK_SUB_DOMAIN="FALLBACK_SUB_DOMAIN";
    private static final String FAll_BACK_PATH = "FAll_BACK_PATH";
    private static final String TEXTOVERLAY="TEXTOVERLAY";
    private static final String BGCOLOR="BGCOLOR";
    private static final String ICONCOLOR="ICONCOLOR";
    private static final String LEDCOLOR="LEDCOLOR";
    private static final String COLLAPSEID="COLLAPSEID";
    private static final String PRIORITY="PRIORITY";
    private static final String CFG="CFG";
    private static final String PUSH_TYPE = "PUSH_TYPE";
    private static final String VISIBILITY="VISIBILITY";
    private static final String GMESSAGE="GROUPMESSAGE";
    private static final String GKEY="GROUPKEY";
    private static final String ACT1ID="ACT1ID";
    private static final String ACT2ID="ACT2ID";
    private static final String TAG="TAG";
    private static final String TTL ="TTL";
    private static final String REQUIREDINT="REQUIREDINT";
    private static final String BUTTONCOUNT="BUTTONCOUNT";
    private static final String CREATE_ON="CREATED_ON";
    private static final String NOTIFCATIONBANNERDATA="IMAGEBITMAPDATA";
    Context context;


    public DatabaseHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
    }
// create a table
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_NOTIFICATION_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY,"
                + NOTIFICATION_TITLE + " TEXT,"
                + NOTIFICATION_MESSAGE + " TEXT,"
                + NOTIFICATION_ICON + " TEXT,"
                + NOTIFICATION_BANNER_IMAGE + " TEXT,"
                + NOTIFICATION_CID + " TEXT,"
                + NOTIFICATION_RID + " TEXT,"
                + NOTIFICATION_ID + " TEXT,"
                + NOTIFICATION_BADGE_ICON + " TEXT,"
                + NOTIFICATION_BADGE_COLOR + " TEXT,"
                + FETCH_URL + " TEXT,"
                + LANDING_URL + " TEXT,"
                + ACT1ID + " TEXT,"
                + BUTTON1_NAME + " TEXT,"
                + BUTTON1_URL + " TEXT,"
                + ACT2ID + " TEXT,"
                + BUTTON2_NAME + " TEXT,"
                + BUTTON2_URL + " TEXT,"
                + SUBTITLE + " TEXT,"
                + BADGE_COUNT + " INTEGER,"
                + INAPP + " INTEGER,"
                + NOTIFICATION_SOUND + " TEXT,"
                + ADDITIONALPARAM + " TEXT,"
                + MAX_NOTIFICATION + " INTEGER,"
                + FALL_BACK_DOMAIN + " TEXT,"
                + FALLBACK_SUB_DOMAIN + " TEXT,"
                + FAll_BACK_PATH + " TEXT,"
                + TEXTOVERLAY + " INTEGER,"
                + BGCOLOR + " TEXT,"
                + ICONCOLOR + " TEXT,"
                + LEDCOLOR + " TEXT,"
                + COLLAPSEID + " TEXT,"
                + PRIORITY + " INTEGER,"
                + CFG + " INTEGER,"
                + PUSH_TYPE + " TEXT,"
                + VISIBILITY + " INTEGER,"
                + GMESSAGE + " TEXT,"
                + GKEY + " TEXT,"
                + DEVICE_ID + " TEXT,"
                + DEVICE_TOKEN + " TEXT,"
                + TAG + " TEXT,"
                + REQUIREDINT + " TEXT,"
                + BUTTONCOUNT + " INTEGER,"
                + CREATE_ON + " TEXT,"
                + TTL + " TEXT,"
                + NOTIFCATIONBANNERDATA + " BLOB,"
                + APP_ID + " TEXT" + ")";
        sqLiteDatabase.execSQL(CREATE_NOTIFICATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
   public void addNotificationInDB(Payload payload) {
        try {
            if (payload != null) {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(NOTIFICATION_TITLE, payload.getTitle());
                values.put(SUBTITLE, payload.getSubTitle());
                values.put(NOTIFICATION_MESSAGE, payload.getMessage());
                values.put(NOTIFICATION_ICON, payload.getIcon());
                values.put(NOTIFICATION_BANNER_IMAGE, payload.getBanner());
                values.put(NOTIFICATION_CID, payload.getId());
                values.put(NOTIFICATION_RID, payload.getRid());
                values.put(NOTIFICATION_BADGE_ICON, payload.getBadgeicon());
                values.put(NOTIFICATION_BADGE_COLOR, payload.getBadgecolor());
                values.put(FETCH_URL, payload.getFetchURL());
                values.put(LANDING_URL, payload.getLink());
                values.put(ACT1ID, payload.getAct1ID());
                values.put(BUTTON1_NAME, payload.getAct1name());
                values.put(BUTTON1_URL, payload.getAct1link());
                values.put(ACT2ID, payload.getAct2ID());
                values.put(BUTTON2_NAME, payload.getAct2name());
                values.put(BUTTON2_URL, payload.getAct2link());
                values.put(BADGE_COUNT, "0");
                values.put(INAPP, payload.getInapp());
                values.put(ADDITIONALPARAM, payload.getAp());
                values.put(MAX_NOTIFICATION, payload.getMaxNotification());
                values.put(FALL_BACK_DOMAIN, payload.getFallBackDomain());
                values.put(FALLBACK_SUB_DOMAIN, payload.getFallBackSubDomain());
                values.put(FAll_BACK_PATH, payload.getFallBackPath());
                values.put(TEXTOVERLAY, payload.getDefaultNotificationPreview());
                values.put(BGCOLOR, payload.getBadgecolor());
                values.put(ICONCOLOR, payload.getSmallIconAccentColor());
                values.put(LEDCOLOR, payload.getLedColor());
                values.put(COLLAPSEID, payload.getCollapseId());
                values.put(PRIORITY, payload.getPriority());
                values.put(CFG, payload.getCfg());
                values.put(PUSH_TYPE, payload.getPush_type());
                values.put(VISIBILITY, payload.getLockScreenVisibility());
                values.put(GMESSAGE, payload.getMessage());
                values.put(GKEY, payload.getKey());
                values.put(CREATE_ON, payload.getCreated_Time());
                values.put(DEVICE_ID, Util.getAndroidId(context));
                values.put(DEVICE_TOKEN, PreferenceUtil.getInstance(context).getStringData(AppConstant.FCM_DEVICE_TOKEN));
                values.put(APP_ID, PreferenceUtil.getInstance(context).getStringData(AppConstant.APPPID));
                values.put(TAG, payload.getTag());
                values.put(REQUIREDINT, payload.getReqInt());
                values.put(BUTTONCOUNT, payload.getAct_num());
                values.put(TTL, payload.getTime_to_live());
                values.put(NOTIFCATIONBANNERDATA, payload.getBanner());
                db.insert(TABLE_NAME, null, values);
                db.close();
            } else {
                Log.e("response", "PayLoad Error");
            }
        }catch (Exception ex)
        {
            Log.e("Exception ex",ex.toString());
        }
    }
   @SuppressLint("Range")
   public Payload getNotificationFromDB(String notificationID) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + NOTIFICATION_RID + "=" + notificationID, null);
            if (cursor != null)
                cursor.moveToFirst();

            Payload payload = new Payload();
            payload.setTitle(cursor.getString(cursor.getColumnIndex(NOTIFICATION_TITLE)));
            payload.setMessage(cursor.getString(cursor.getColumnIndex(NOTIFICATION_MESSAGE)));
            payload.setSubTitle(cursor.getString(cursor.getColumnIndex(SUBTITLE)));
            payload.setIcon(cursor.getString(cursor.getColumnIndex(NOTIFICATION_ICON)));

            payload.setBanner(cursor.getString(cursor.getColumnIndex(NOTIFICATION_BANNER_IMAGE)));
            payload.setRid(cursor.getString(cursor.getColumnIndex(NOTIFICATION_RID)));
            payload.setId(cursor.getString(cursor.getColumnIndex(NOTIFICATION_CID)));

            payload.setBadgeicon(cursor.getString(cursor.getColumnIndex(NOTIFICATION_ICON)));
            payload.setBadgecolor(cursor.getString(cursor.getColumnIndex(NOTIFICATION_BADGE_COLOR)));
            payload.setBadgeCount(cursor.getInt(cursor.getColumnIndex(BADGE_COUNT)));

            payload.setFetchURL(cursor.getString(cursor.getColumnIndex(FETCH_URL)));
            payload.setLink(cursor.getString(cursor.getColumnIndex(LANDING_URL)));

            payload.setAct1ID(cursor.getString(cursor.getColumnIndex(ACT1ID)));
            payload.setAct1name(cursor.getString(cursor.getColumnIndex(BUTTON1_NAME)));
            payload.setAct1link(cursor.getString(cursor.getColumnIndex(BUTTON1_URL)));
            payload.setAct2ID(cursor.getString(cursor.getColumnIndex(ACT2ID)));
            payload.setAct2name(cursor.getString(cursor.getColumnIndex(BUTTON2_NAME)));
            payload.setAct2link(cursor.getString(cursor.getColumnIndex(BUTTON2_URL)));
            payload.setInapp(cursor.getInt(cursor.getColumnIndex(INAPP)));
            payload.setAp(cursor.getString(cursor.getColumnIndex(ADDITIONALPARAM)));
            payload.setMaxNotification(cursor.getInt(cursor.getColumnIndex(MAX_NOTIFICATION)));
            payload.setFallBackDomain(cursor.getString(cursor.getColumnIndex(FALL_BACK_DOMAIN)));
            payload.setFallBackSubDomain(cursor.getString(cursor.getColumnIndex(FALLBACK_SUB_DOMAIN)));
            payload.setFallBackPath(cursor.getString(cursor.getColumnIndex(FAll_BACK_PATH)));
            payload.setDefaultNotificationPreview(cursor.getInt(cursor.getColumnIndex(TEXTOVERLAY)));
            payload.setCollapseId(cursor.getString(cursor.getColumnIndex(COLLAPSEID)));
            payload.setLedColor(cursor.getString(cursor.getColumnIndex(LEDCOLOR)));
            payload.setPriority(cursor.getInt(cursor.getColumnIndex(PRIORITY)));
            payload.setCfg(cursor.getInt(cursor.getColumnIndex(CFG)));
            payload.setLockScreenVisibility(cursor.getInt(cursor.getColumnIndex(VISIBILITY)));
            payload.setPush_type(cursor.getString(cursor.getColumnIndex(PUSH_TYPE)));
            payload.setGroupMessage(cursor.getString(cursor.getColumnIndex(GMESSAGE)));
            payload.setGroupKey(cursor.getString(cursor.getColumnIndex(GKEY)));
            payload.setCreated_Time(cursor.getString(cursor.getColumnIndex(CREATE_ON)));
            payload.setTag(cursor.getString(cursor.getColumnIndex(TAG)));
            payload.setReqInt(cursor.getInt(cursor.getColumnIndex(REQUIREDINT)));
            payload.setAct_num(cursor.getInt(cursor.getColumnIndex(BUTTONCOUNT)));
            payload.setTime_to_live(cursor.getString(cursor.getColumnIndex(TTL)));
            cursor.close();
            return payload;
        }
        catch (Exception ex)
        {
            Util.setException(context,ex.toString(),"DatabaseHandler","getNotificationFromDB");

            return null;
        }
    }
    public void deleteNotificationFromDB(String notificationID) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            db.delete(TABLE_NAME, NOTIFICATION_RID + " = ?",
                    new String[]{String.valueOf(notificationID)});
            db.close();
        }
        catch (Exception ex)
        {
            Util.setException(context,ex.toString(),"DatabaseHandler","deleteNotificationFromDB");
        }
    }
    public int getNotificationCount() {
        try {
            String countQuery = "SELECT  * FROM " + TABLE_NAME;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(countQuery, null);
            int count = cursor.getCount();
            cursor.close();
            if (count > 0)
                return count;
            else
                return 0;
        }
        catch (Exception ex)
        {
            Util.setException(context,ex.toString(),"DatabaseHandler","getNotificationCount");

            return 0;
        }
    }
     @SuppressLint("Range")
     public List<Payload> getAllNotification()
    {
        try {
            List<Payload> payloads=new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME , null);
            if (cursor != null)
                cursor.moveToFirst();
            do {

                Payload payload = new Payload();
                payload.setTitle(cursor.getString(cursor.getColumnIndex(NOTIFICATION_TITLE)));
                payload.setMessage(cursor.getString(cursor.getColumnIndex(NOTIFICATION_MESSAGE)));
                payload.setSubTitle(cursor.getString(cursor.getColumnIndex(SUBTITLE)));
                payload.setIcon(cursor.getString(cursor.getColumnIndex(NOTIFICATION_ICON)));

                payload.setBanner(cursor.getString(cursor.getColumnIndex(NOTIFICATION_BANNER_IMAGE)));
                payload.setRid(cursor.getString(cursor.getColumnIndex(NOTIFICATION_RID)));
                payload.setId(cursor.getString(cursor.getColumnIndex(NOTIFICATION_CID)));

                payload.setBadgeicon(cursor.getString(cursor.getColumnIndex(NOTIFICATION_ICON)));
                payload.setBadgecolor(cursor.getString(cursor.getColumnIndex(NOTIFICATION_BADGE_COLOR)));
                payload.setBadgeCount(cursor.getInt(cursor.getColumnIndex(BADGE_COUNT)));

                payload.setFetchURL(cursor.getString(cursor.getColumnIndex(FETCH_URL)));
                payload.setLink(cursor.getString(cursor.getColumnIndex(LANDING_URL)));

                payload.setAct1ID(cursor.getString(cursor.getColumnIndex(ACT1ID)));
                payload.setAct1name(cursor.getString(cursor.getColumnIndex(BUTTON1_NAME)));
                payload.setAct1link(cursor.getString(cursor.getColumnIndex(BUTTON1_URL)));
                payload.setAct2ID(cursor.getString(cursor.getColumnIndex(ACT2ID)));
                payload.setAct2name(cursor.getString(cursor.getColumnIndex(BUTTON2_NAME)));
                payload.setAct2link(cursor.getString(cursor.getColumnIndex(BUTTON2_URL)));
                payload.setInapp(cursor.getInt(cursor.getColumnIndex(INAPP)));
                payload.setAp(cursor.getString(cursor.getColumnIndex(ADDITIONALPARAM)));
                payload.setMaxNotification(cursor.getInt(cursor.getColumnIndex(MAX_NOTIFICATION)));
                payload.setFallBackDomain(cursor.getString(cursor.getColumnIndex(FALL_BACK_DOMAIN)));
                payload.setFallBackSubDomain(cursor.getString(cursor.getColumnIndex(FALLBACK_SUB_DOMAIN)));
                payload.setFallBackPath(cursor.getString(cursor.getColumnIndex(FAll_BACK_PATH)));
                payload.setDefaultNotificationPreview(cursor.getInt(cursor.getColumnIndex(TEXTOVERLAY)));
                payload.setCollapseId(cursor.getString(cursor.getColumnIndex(COLLAPSEID)));
                payload.setLedColor(cursor.getString(cursor.getColumnIndex(LEDCOLOR)));
                payload.setPriority(cursor.getInt(cursor.getColumnIndex(PRIORITY)));
                payload.setCfg(cursor.getInt(cursor.getColumnIndex(CFG)));
                payload.setLockScreenVisibility(cursor.getInt(cursor.getColumnIndex(VISIBILITY)));
                payload.setPush_type(cursor.getString(cursor.getColumnIndex(PUSH_TYPE)));
                payload.setGroupMessage(cursor.getString(cursor.getColumnIndex(GMESSAGE)));
                payload.setGroupKey(cursor.getString(cursor.getColumnIndex(GKEY)));
                payload.setCreated_Time(cursor.getString(cursor.getColumnIndex(CREATE_ON)));
                payload.setTag(cursor.getString(cursor.getColumnIndex(TAG)));
                payload.setReqInt(cursor.getInt(cursor.getColumnIndex(REQUIREDINT)));
                payload.setAct_num(cursor.getInt(cursor.getColumnIndex(BUTTONCOUNT)));
                payload.setTime_to_live(cursor.getString(cursor.getColumnIndex(TTL)));

                payloads.add(payload);
            }while (cursor.moveToNext());
            cursor.close();
            db.close();
            return payloads;
        }
        catch (Exception ex)
        {
            Util.setException(context,ex.toString(),"DatabaseHandler","getAllNotification");

            return null;
        }


    }
    public boolean isTableExists(boolean openDb) {
        SQLiteDatabase db = this.getWritableDatabase();

        if(openDb) {

            if(db == null || !db.isOpen()) {
                db = getReadableDatabase();
            }

            if(!db.isReadOnly()) {
                db.close();
                db = getReadableDatabase();
            }
        }
        String query = "select DISTINCT tbl_name from sqlite_master where tbl_name = '"+TABLE_NAME+"'";
        try (Cursor cursor = db.rawQuery(query, null)) {
            if(cursor!=null) {
                if(cursor.getCount()>0) {
                    return true;
                }
            }
            cursor.close();
            return false;
        }

    }
    public boolean checkIfRecordExist(String notificationID)
    {
        try
        {
            SQLiteDatabase db=this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + NOTIFICATION_RID + "=" + notificationID, null);
            if (cursor.moveToFirst())
            {
                db.close();
                cursor.close();
                return true;//record Exists

            }
            else {

                db.close();
                cursor.close();
                 return false;
            }
        }
        catch(Exception errorException)
        {
            Util.setException(context,errorException.toString(),"DatabaseHandler","checkIfRecordExist");
        }
        return false;
    }
    public void deleteRow(int id)
    {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_NAME, ID + " = ?",
                    new String[]{String.valueOf(id)});
            db.close();
        }
        catch (Exception ex)
        {
            Util.setException(context,ex.toString(),"DatabaseHandler","deleteRow");
        }
    }
}


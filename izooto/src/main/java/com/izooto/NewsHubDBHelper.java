package com.izooto;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.izooto.Payload;
import com.izooto.Util;
import com.izooto.iZooto;

import java.util.ArrayList;

 public class NewsHubDBHelper extends SQLiteOpenHelper {

    // creating a constant variables for our database.
    // below variable is for our database name.
    private static final String DB_NAME = "OrientDB";

    // below int is our database version
    private static final int DB_VERSION = 1;

    // below variable is for our table name.
    private static final String TABLE_NAME = "newshubnotfication";

    //payload Data
    private static final String NOTIFICATION_TITLE = "TITLE";
    private static final String NOTIFICATION_SUBTITLE = "SUBTITLE";
    private static final String NOTIFICATION_MESSAGE = "MESSAGE";
    private static final String NOTIFICATION_ICON = "ICON";
    private static final String NOTIFICATION_BANNER_IMAGE = "BANNERIMAGE";
    private static final String NOTIFICATION_CID = "CID";
    private static final String NOTIFICATION_RID = "RID";
    private static final  String NOTIFICATION_ID = "NOTIFICATIONID";
    private static final String NOTIFICATION_BADGE_ICON = "BADGEICON";
    private static final String NOTIFICATION_BADGE_COLOR = "BADGECOLOR";
    private static final String FETCH_URL = "FETCHURL";
    private static final String LANDING_URL = "LANDINGURL";
    private static final String BUTTON1_NAME = "ACTION1NAME";
    private static final String BUTTON1_URL = "ACTION1LINK";
    private static final String BUTTON2_NAME = "ACTION2NAME";
    private static final String BUTTON2_URL = "ACTION2URL";
    private static final String BADGE_COUNT = "BADGE_COUNT";
    private static final String INAPP = "INAPP";
    private static final String NOTIFICATION_SOUND = "SOUNDNAME";
    private static final String ADDITIONAL_PARAM = "ADDITIONALPARAM";
    private static final String MAX_NOTIFICATION = "MAX_NOTIFICATION";
    //    private static final String FALL_BACK_DOMAIN = "FALL_BACK_DOMAIN";
//    private static final String FALLBACK_SUB_DOMAIN="FALLBACK_SUB_DOMAIN";
//    private static final String FAll_BACK_PATH = "FAll_BACK_PATH";
    private static final String TEXT_OVERLAY = "TEXTOVERLAY";
    private static final String BG_COLOR = "BGCOLOR";
    private static final String ICON_COLOR = "ICONCOLOR";
    private static final String LED_COLOR = "LEDCOLOR";
    private static final String COLLAPSE_ID = "COLLAPSEID";
    private static final String PRIORITY = "PRIORITY";
    private static final String CFG = "CFG";
    private static final String PUSH_TYPE = "PUSH_TYPE";
    private static final String VISIBILITY = "VISIBILITY";
    private static final String GMESSAGE = "GROUPMESSAGE";
    private static final String GKEY = "GROUPKEY";
    private static final String ACT1ID = "ACT1ID";
    private static final String ACT2ID = "ACT2ID";
    private static final String TAG = "TAG";
    private static final String TTL = "TTL";
    private static final String REQUIREDINT = "REQUIREDINT";
    private static final String BUTTONCOUNT = "BUTTONCOUNT";
    private static final String CREATE_ON = "CREATED_ON";
    private static final String NOTIFCATION_BANNER_DATA = "IMAGEBITMAPDATA";

    // creating a constructor for our database handler.
    public NewsHubDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // below method is for creating a database by running a sqlite query
    @Override
    public void onCreate(SQLiteDatabase db) {
           try {
               String query = "CREATE TABLE " + TABLE_NAME + " ("
                       + NOTIFICATION_RID + " INTEGER PRIMARY KEY, "
                       + NOTIFICATION_TITLE + " TEXT,"
                       + NOTIFICATION_SUBTITLE + " TEXT,"
                       + NOTIFICATION_MESSAGE + " TEXT,"
                       + NOTIFICATION_ICON + " TEXT,"
                       + NOTIFICATION_BANNER_IMAGE + " TEXT,"
                       + NOTIFICATION_CID + " TEXT,"
                       + NOTIFICATION_ID + " TEXT,"
                       + NOTIFICATION_BADGE_ICON + " TEXT,"
                       + NOTIFICATION_BADGE_COLOR + " TEXT,"
                       + FETCH_URL + " TEXT,"
                       + LANDING_URL + " TEXT,"
                       + BUTTON1_NAME + " TEXT,"
                       + BUTTON1_URL + " TEXT,"
                       + BUTTON2_NAME + " TEXT,"
                       + BUTTON2_URL + " TEXT,"
                       + BADGE_COUNT + " TEXT,"
                       + INAPP + " TEXT,"
                       + NOTIFICATION_SOUND + " TEXT,"
                       + ADDITIONAL_PARAM + " TEXT,"
                       + MAX_NOTIFICATION + " TEXT,"
                       + TEXT_OVERLAY + " TEXT,"
                       + BG_COLOR + " TEXT,"
                       + ICON_COLOR + " TEXT,"
                       + LED_COLOR + " TEXT,"
                       + COLLAPSE_ID + " TEXT,"
                       + PRIORITY + " TEXT,"
                       + CFG + " TEXT,"
                       + PUSH_TYPE + " TEXT,"
                       + VISIBILITY + " TEXT,"
                       + GMESSAGE + " TEXT,"
                       + GKEY + " TEXT,"
                       + ACT1ID + " TEXT,"
                       + ACT2ID + " TEXT,"
                       + TAG + " TEXT,"
                       + TTL + " TEXT,"
                       + REQUIREDINT + " TEXT,"
                       + BUTTONCOUNT + " TEXT,"
                       + CREATE_ON + " TEXT,"
                       + NOTIFCATION_BANNER_DATA + " TEXT)";
//                + TRACKS_COL + " TEXT)";

               // at last we are calling a exec sql
               // method to execute above sql query
               db.execSQL(query);
           }
           catch (Exception ex){
               Log.e("SQLITE Exception",ex.toString());
           }
    }

    // this method is use to add new course to our sqlite database.
    public void addNewsHubPayload(Payload payload) {
        try {
            if (payload.getLink() != null && !payload.getLink().isEmpty()) {
                // on below line we are creating a variable for
                // our sqlite database and calling writable method
                // as we are writing data in our database.
                SQLiteDatabase db = this.getWritableDatabase();
                Cursor cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE name ='" + TABLE_NAME + "' and type='table'", null);

                if (cursor.getCount() > 0) {
                    if (checkIdExist(payload.getRid())) {
                        db.close();
                        cursor.close();
                        return;
                    }

                    ContentValues values = new ContentValues();
                    values.put(NOTIFICATION_TITLE, payload.getTitle());
                    values.put(NOTIFICATION_SUBTITLE, payload.getSubTitle());
                    values.put(NOTIFICATION_MESSAGE, payload.getMessage());
                    values.put(NOTIFICATION_ICON, payload.getIcon());
                    values.put(NOTIFICATION_BANNER_IMAGE, payload.getBanner());
                    values.put(NOTIFICATION_CID, payload.getId());
                    values.put(NOTIFICATION_RID, payload.getRid());
                    values.put(NOTIFICATION_ID, payload.getMaxNotification());
                    values.put(NOTIFICATION_BADGE_ICON, payload.getBadgeicon());
                    values.put(NOTIFICATION_BADGE_COLOR, payload.getBadgecolor());
                    values.put(LANDING_URL, payload.getLink());
                    values.put(BUTTON1_NAME, payload.getAct1name());
                    values.put(BUTTON1_URL, payload.getAct1link());
                    values.put(BUTTON2_NAME, payload.getAct2name());
                    values.put(BUTTON2_URL, payload.getAct2link());
                    values.put(BADGE_COUNT, payload.getBadgeCount());
                    values.put(INAPP, payload.getInapp());
                    values.put(NOTIFICATION_SOUND, payload.getSound());
                    values.put(ADDITIONAL_PARAM, payload.getAp());
                    values.put(MAX_NOTIFICATION, payload.getMaxNotification());

                    values.put(BG_COLOR, payload.getNotification_bg_color());
                    values.put(ICON_COLOR, payload.getSmallIconAccentColor());
                    values.put(LED_COLOR, payload.getLedColor());
                    values.put(COLLAPSE_ID, payload.getMaxNotification());
                    values.put(PRIORITY, payload.getPriority());
                    values.put(CFG, payload.getCfg());
                    values.put(PUSH_TYPE, payload.getPush_type());
                    values.put(VISIBILITY, payload.getLockScreenVisibility());
                    values.put(GMESSAGE, payload.getGroupMessage());
                    values.put(GKEY, payload.getGroupKey());
                    values.put(ACT1ID, payload.getAct1ID());
                    values.put(ACT2ID, payload.getAct2ID());
                    values.put(TAG, payload.getTag());
                    values.put(TTL, payload.getTime_to_live());
                    values.put(REQUIREDINT, payload.getReqInt());
                    values.put(BUTTONCOUNT, payload.getAct_num());
                    values.put(CREATE_ON, payload.getCreated_Time());

                    db.insert(TABLE_NAME, null, values);

                    // at last we are closing our
                    // database after adding database.
                    db.close();
                }
            }

        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NewsHubDBHelper", "addNewsHubPayload");

        }
    }

    private boolean checkIdExist(String notificationID) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + NOTIFICATION_RID + "=" + notificationID, null);

            if (cursor.getCount() > 0) {
//                Log.e(TAG, "addNewsHubPayload: record exist ----- " + notificationID);
                db.close();
                cursor.close();
                return true;
            } else
                return false;

        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NewsHubDBHelper", "checkIdExist");
        }
        return false;
    }

    @SuppressLint("Range")
    public ArrayList<Payload> fetchNewsHubData() {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY + CREATED_ON DESC LIMIT 75";
            Cursor cursor = db.rawQuery(query, null);
            ArrayList<Payload> payloadArrayList = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    Payload mPayload = new Payload();
                    mPayload.setCreated_Time(cursor.getString(cursor.getColumnIndex(CREATE_ON)));
                    mPayload.setFetchURL(cursor.getString(cursor.getColumnIndex(FETCH_URL)));
                    mPayload.setKey(cursor.getString(cursor.getColumnIndex(NOTIFICATION_ID)));
                    mPayload.setId(cursor.getString(cursor.getColumnIndex(NOTIFICATION_CID)));
                    mPayload.setRid(cursor.getString(cursor.getColumnIndex(NOTIFICATION_RID)));
                    mPayload.setLink(cursor.getString(cursor.getColumnIndex(LANDING_URL)));
                    mPayload.setTitle(cursor.getString(cursor.getColumnIndex(NOTIFICATION_TITLE)));
                    mPayload.setMessage(cursor.getString(cursor.getColumnIndex(NOTIFICATION_MESSAGE)));
                    mPayload.setIcon(cursor.getString(cursor.getColumnIndex(NOTIFICATION_ICON)));
                    mPayload.setReqInt(cursor.getInt(cursor.getColumnIndex(REQUIREDINT)));
                    mPayload.setTag(cursor.getString(cursor.getColumnIndex(TAG)));
                    mPayload.setBanner(cursor.getString(cursor.getColumnIndex(NOTIFICATION_BANNER_IMAGE)));
                    mPayload.setAct_num(cursor.getInt(cursor.getColumnIndex(BUTTONCOUNT)));
                    mPayload.setBadgeicon(cursor.getString(cursor.getColumnIndex(NOTIFICATION_BADGE_ICON)));
                    mPayload.setBadgecolor(cursor.getString(cursor.getColumnIndex(NOTIFICATION_BADGE_COLOR)));
                    mPayload.setSubTitle(cursor.getString(cursor.getColumnIndex(NOTIFICATION_SUBTITLE)));
                    mPayload.setBadgeCount(cursor.getInt(cursor.getColumnIndex(BADGE_COUNT)));
                    mPayload.setAct1name(cursor.getString(cursor.getColumnIndex(BUTTON1_NAME)));
                    mPayload.setAct1link(cursor.getString(cursor.getColumnIndex(BUTTON1_URL)));
                    mPayload.setAct1ID(cursor.getString(cursor.getColumnIndex(ACT1ID)));
                    mPayload.setAct2name(cursor.getString(cursor.getColumnIndex(BUTTON2_NAME)));
                    mPayload.setAct2link(cursor.getString(cursor.getColumnIndex(BUTTON2_URL)));
                    mPayload.setAct2ID(cursor.getString(cursor.getColumnIndex(ACT2ID)));
                    mPayload.setInapp(cursor.getInt(cursor.getColumnIndex(INAPP)));
                    mPayload.setSmallIconAccentColor(cursor.getString(cursor.getColumnIndex(BG_COLOR)));
                    mPayload.setSound(cursor.getString(cursor.getColumnIndex(NOTIFICATION_SOUND)));
                    mPayload.setLedColor(cursor.getString(cursor.getColumnIndex(LED_COLOR)));
                    mPayload.setLockScreenVisibility(cursor.getInt(cursor.getColumnIndex(VISIBILITY)));
                    mPayload.setGroupKey(cursor.getString(cursor.getColumnIndex(GKEY)));
                    mPayload.setGroupMessage(cursor.getString(cursor.getColumnIndex(GMESSAGE)));
                    mPayload.setCollapseId(cursor.getString(cursor.getColumnIndex(COLLAPSE_ID)));
                    mPayload.setPriority(cursor.getInt(cursor.getColumnIndex(PRIORITY)));
                    mPayload.setAp(cursor.getString(cursor.getColumnIndex(ADDITIONAL_PARAM)));
                    mPayload.setCfg(cursor.getInt(cursor.getColumnIndex(CFG)));
                    mPayload.setPush_type(cursor.getString(cursor.getColumnIndex(PUSH_TYPE)));
                    mPayload.setMaxNotification(cursor.getInt(cursor.getColumnIndex(MAX_NOTIFICATION)));
                    payloadArrayList.add(mPayload);
                } while (cursor.moveToNext());
            }
            // at last closing our cursor
            // and returning our array list.
            cursor.close();
//            Log.e(TAG, "fetchNewsHubData: -- "+payloadArrayList );
            return payloadArrayList;
        } catch (Exception e) {
            Util.handleExceptionOnce(iZooto.appContext, e.toString(), "NewsHubDBHelper", "fetchNewsHubData");
        }
        return null;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
        catch (Exception ex){
            Util.handleExceptionOnce(iZooto.appContext, ex.toString(), "NewsHubDBHelper", "onUpgrade");

        }
    }
}

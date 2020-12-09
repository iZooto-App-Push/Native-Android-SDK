package com.izooto;


public interface AppConstant {
    String APP_NAME_TAG = "iZooto";
    String FCM_DEVICE_TOKEN = "deviceToken";
    String IZOOTO_SENDER_ID_KEY = "izooto_sender_id";
    String GOOGLE_JSON_URL = "https://cdn.izooto.com/app/app_";  //old
    //String GOOGLE_JSON_URL = "https://cdn.izooto.com/app/"; // new
    String SDKNAME = "IZOOTO";
    String DEVICETOKEN="DEVICE TOKEN   ->  ";
    String UTF="UTF-8";
    String FCMDEFAULT="[DEFAULT]";
    String BROWSERKEYID="{BROWSERKEYID}";
    String PTE="2";
    String ANDROIDVERSION = "&osVersion=";
    String DEVICENAME ="&deviceName=";
    String TOKEN="&bKey=";
    String ADDURL="app.php?s=";
    String  PID="&pid=";
    String BTYPE_="&btype=";
    String DTYPE_="&dtype=";
    String TIMEZONE="&tz=";
    String APPVERSION="&bver=";
    String OS="&os=";
    String ALLOWED_="&allowed=";
    String CHECKSDKVERSION="&check=";
    String LANGUAGE="&ln=";
    String CLICKINDEX= "clickIndex";
    String APPPID="pid";
    String ENCRYPTED_PID="encryptedPid";
    String ADVERTISING_ID = "add";
    String GET_NOTIFICATION_ENABLED="enable";
    String GET_NOTIFICATION_DISABLED="disable";
    String FIREBASE_ANALYTICS_TRACK = "isCheck";
    String TRACK_NOTIFICATION_ID = "notificationId";
    String IS_NOTIFICATION_ID_UPDATED = "notificationIdUpdated";
    String NOTIFICATION_COUNT = "count";
    String PAYLOAD_BADGE_COUNT = "payloadBadgeCount";
    String WEB_LANDING_URL = "webLandingUrl";
    String LOG_EVENT = "logEvent";
    String GET_FIREBASE_INSTANCE = "getInstance";
    String UTM_SOURCE = "utm_source";
    String UTM_MEDIUM = "utm_medium";
    String UTM_CAMPAIGN = "utm_campaign";
    String UTM_TERM = "utm_term";
    String UTM_CONTENT = "utm_content";
    String SOURCE = "source";
    String MEDIUM = "medium";
    String FIREBASE_NOTIFICATION_ID = "notification_id";
    String FIREBASE_CAMPAIGN = "campaign";
    String TERM = "term";
    String CONTENT = "content";
    String TIME_OF_CLICK = "time_of_click";
    String FIREBASE_12PM = "12:00:00 PM";
    String FIREBASE_2PM = "02:00:00 PM";
    String FIREBASE_4PM = "04:00:00 PM";
    String FIREBASE_6PM = "06:00:00 PM";
    String FIREBASE_8PM = "08:00:00 PM";
    String FIREBASE_10PM = "10:00:00 PM";
    String FIREBASE_12AM = "12:00:00 AM";
    String FIREBASE_2AM = "02:00:00 AM";
    String FIREBASE_4AM = "04:00:00 AM";
    String FIREBASE_6AM = "06:00:00 AM";
    String FIREBASE_8AM = "08:00:00 AM";
    String FIREBASE_10AM = "10:00:00 AM";
    String FIREBASE_12to2PM = "12-2 PM";
    String FIREBASE_2to4PM = "2-4 PM";
    String FIREBASE_4to6PM = "4-6 PM";
    String FIREBASE_6to8PM = "6-8 PM";
    String FIREBASE_8to10PM = "8-10 PM";
    String FIREBASE_10to12AM = "10-12 AM";
    String FIREBASE_12to2AM = "12-2 AM";
    String FIREBASE_2to4AM = "2-4 AM";
    String FIREBASE_4to6AM = "4-6 AM";
    String FIREBASE_6to8AM = "6-8 AM";
    String FIREBASE_8to10AM = "8-10 AM";
    String FIREBASE_10to12PM = "10-12 PM";
    String API_PID = "?pid=";
    String PTE_="&pte=";
    String CID_="&cid=";
    String RID_="&rid=";
    String NOTIFICATION_OP="&op=";
    String ACTION="&action=";
    String PT_="&pt=";
    int PT = 0;
    String GE_ ="&ge=";
    String ET_ ="&et=";
    String VAL ="&val=";
    String ACT ="&act=";
    String BUTTON_ID_1 ="button1ID";
    String BUTTON_TITLE_1 ="button1Title";
    String BUTTON_URL_1 ="button1URL";
    String ADDITIONAL_DATA ="additionalData";
    String LANDING_URL ="landingURL";
    String BUTTON_ID_2 ="button2ID";
    String BUTTON_TITLE_2 ="button2Title";
    String BUTTON_URL_2 ="button2URL";
    String ACTION_TYPE ="actionType";
    String NOTIFICATION_ ="Notification";
    String INAPPALERT ="InAppAlert";
    String DEFAULT_ICON ="default";
    int GE = 1;


    String UPDATE_DEVICE_TOKEN = "https://aevents.izooto.com/izooto/api.php";
    String IZOOTO_ENCRYPTION_KEY = "izooto_enc_key";
    String IZOOTO_APP_ID = "izooto_app_id";
    String ACTION_BTN_ONE = "actionBtnOne";
    String ACTION_BTN_TWO = "actionBtnTwo";
    int NOTIFICAITON_REQ_CODE = 101;
    String KEY_WEB_URL = "WEB_URL";
    String KEY_NOTIFICITON_ID = "keyNotificationId";
    String IS_TOKEN_UPDATED = "isTokenUpdated";
    String DEVICE_REGISTRATION_TIMESTAMP = "deviceRegistrationTimeStamp";
    String KEY_IN_APP = "keyInApp";
    String EVENT_URL="https://et.izooto.com/";
    String  KEY_IN_CID = "cid";
    String KEY_IN_RID = "rid";
    String KEY_IN_BUTOON="btn";
    String KEY_IN_ADDITIONALDATA="ap";
    String KEY_IN_PHONE ="call";
    String KEY_IN_ACT1ID = "act1ID";
    String KEY_IN_ACT2ID="act2ID";
    int BTYPE = 9;
    int DTYPE = 3;
    int SDKOS = 4;
    int ALLOWED = 1;
    int STYPE = 2;
    String SECRETKEY = "b07dfa9d56fc64df";
    String MESSAGE = "It seems you forgot to configure izooto_app id or izooto_sender_id property in your app level build.gradle";
    String FCMERROR = "Unable to generate FCM token, there may be something wrong with sender id";
    String SUCCESS = " Request Successful: ";
    String FAILURE = " Request Failed: ";

    String ATTACHREQUEST = "IZooTo RestClient: ResponseHandler is not attached for the Request: ";
    String EXCEPTIONERROR = "Thrown Error";
    String UNEXCEPTION ="unsupported encoding exception";
    String CDN = "https://cdn.izooto.com/app/app_";
    //String CDN = "https://cdn.izooto.com/app/";

    String MISSINGID="App Id is missing.";
    // Register String

    String SENDERID ="senderId";
    String APPID = "appId";
    String APIKEY="apiKey";

    ///////// JSON Payload Data

    String CAMPNAME = "campaignDetails";
    String CREATEDON= "created_on";
    String FETCHURL = "fetchURL";
    String KEY = "key";
    String ID ="id";
    String RID= "rid";
    String LINK ="link";
    String TITLE = "title";
    String NMESSAGE = "message";
    String ICON = "icon";
    String REQINT = "reqInt";
    String TAG= "tag";
    String BANNER = "banner";
    String ACTNUM= "act_num";
    String ACT1NAME= "act1name";
    String ACT1LINK= "act1link";
    String ACT2NAME= "act2name";
    String ACT2LINK= "act2link";
    String INAPP= "inapp";
    String TARYICON="trayicon";
    String ICONCOLOR="iconcolor";
    String SOUND= "sound";
    String LEDCOLOR="ledColor";
    String VISIBILITY = "visibility";
    String GKEY = "gKey";
    String GMESSAGE="gMessage";
    String PROJECTNUMBER="projectNumber";
    String COLLAPSEID = "collapseID";
    String PRIORITY="priority";
    String RAWDATA="rawData";
    String ADDITIONALPARAM ="ap";
    String PAYLOAD ="Payload";
    String ACT1ICON="act1icon";
    String ACT2ICON="act2icon";
    String ACT1ID="act1id";
    String ACT2ID ="act2id";
    String CFG="cfg";


    String NOTIFICATIONBODY="notifcationbody";
    String FIREBASEEXCEPTION ="exception";
    String FCMNAME = "FireBase Name";
    String NOTIFICATIONRECEIVED ="Short lived task is done.";
    String FAILEDTOKEN = "Unable to generate FCM token, there may be something wrong with sender id";
    String CHECKFCMLIBRARY="The FCM library is missing! Please make sure to include it in your project.";
    //API
    String APISUCESS ="API SUCCESS";
    String APIFAILURE ="API FAILURE";
    String TELIPHONE = "tel:";
    String NO = "NO";

    //// short payload
    String webViewData = "WebViewClient: shouldOverrideUrlLoading";
    String LANDINGURL="landingURL";
    String ACT1URL = "act1URL";
    String ACT2URL="act2URL";
    String ACT1TITLE="act1title";
    String ACT2TITLE="act2title";

    String BADGE_ICON= "badgeicon";
    String BADGE_COLOR= "badgecolor";
    String SUBTITLE = "subtitle";
    String GROUP = "group";
    String BADGE_COUNT = "badgeCount";
    String ADVERTISEMENT_ID = "advertisementID";
    String ISINSTALL="isInstall";
    String CHANNEL_NAME="iZooto Notification";
    String DIALOG_DISMISS="Dismiss";
    String DIALOG_OK="Take me there";
    String URL_FWD="&frwd";
    String URL_FWD_="&frwd=";
    String URL_BKEY="&bkey=";
    String URL_ID="id";
    String URL_CLIENT="client";
    String URL_RID="rid";
    String URL_BKEY_="bkey";
    String URL_FRWD___="frwd";
    String FCM_TIME_FORMAT="hh:mm:ss aa";
    String NULL="null";
    String FONT_COLOR="<font color=\"\"";
    String POST="POST";
    String CONTENT_TYPE="Content-Type";
    String FORM_URL_ENCODED="application/x-www-form-urlencoded";
    String FORM_URL_JSON="application/json; charset=UTF-8";
    String DAT=".dat";
    String HTTPS="https:";
    String HTTP="http:";
    String IMPR="impr.izooto.com";
    String KEY_NOT_FOUND="KEY NOT FOUND";
    String INSTLLED_FAILED="getInstanceId failed";
    String GET_TOPIC_NAME = "getTopicName";
    String REMOVE_TOPIC_NAME = "removeTopicName";
    String ADD_TOPIC ="add_topic";
    String REMOVE_TOPIC ="remove_topic";
    String TOPIC ="topic";
    String ANDROID_ID="&bKey=";

}









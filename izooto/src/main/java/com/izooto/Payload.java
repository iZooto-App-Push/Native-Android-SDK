package com.izooto;

/* Developed By Amit Gupta */
public class Payload {


    private String fetchURL;
    private String key;
    private String id;//campain id
    private String rid;//run id
    private String link;
    private String title;
    private String subTitle;
    private String message;
    private String icon;
    private String badgeicon;
    private String badgecolor;
    private int group;
    private int reqInt;
    private String tag;
    private String banner;
    private int act_num;
    private String act1name;
    private String act1link;
    private String act2name;
    private String act2link;
    private int inapp;
    private String trayicon;
    private String smallIconAccentColor;
    private String sound;
    private String ledColor;
    private int lockScreenVisibility = 1;
    private String groupKey;
    private String groupMessage;
    private String fromProjectNumber;
    private String collapseId;
    private int priority;
    private String rawPayload;
    private String act1ID;
    private String act2ID;


    private String ap;
    private String type_input_to_payload;
    private String dropdown_text;
    private String validation;
    private String editbox_title;
    private String type;
    public boolean isAndroid;
    public boolean isiOS;
    public  boolean isWeb;
    private String  act1icon;
    private String  act2icon;
    private int  badgeCount;
    private String cpc;
    private String rv;
    private String rc;
    private String passive_flag;
    private String cpm;
    private String ctr;
    private String received_bid;
    private String ad_type;
    private String adID;
    private int index;
    private long responseTime;
    private long startTime;
    private  String fallBackSubDomain;
    private String fallBackDomain;
    private String fallBackPath;
    private int time_out;
    private int adTimeOut;
    private String created_Time;
    private String time_to_live;
    private String push_type;
    private int  maxNotification;
    private int  CustomNotification;
    private String public_global_key;
    private int defaultNotificationPreview;
    private String notification_bg_color;

    private String offlineCampaign;


    public int getMaxNotification() {
        return maxNotification;
    }

    public void setMaxNotification(int maxNotification) {
        this.maxNotification = maxNotification;
    }

    //////////////////////////////////////////////

    private int cfg;

    private String expiryTimerValue;
    private String makeStickyNotification;
    public int getCfg() {
        return cfg;
    }
    public void setCfg(int cfg) {
        this.cfg = cfg;
    }
    public int getGroup() {
        return group;
    }

    public int getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(int badgeCount) {
        this.badgeCount = badgeCount;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getBadgeicon() {
        return badgeicon;
    }

    public void setBadgeicon(String badgeicon) {
        this.badgeicon = badgeicon;
    }

    public String getBadgecolor() {
        return badgecolor;
    }

    public void setBadgecolor(String badgecolor) {
        this.badgecolor = badgecolor;
    }

    public int getInapp() {
        return inapp;
    }

    public void setInapp(int inapp) {
        this.inapp = inapp;
    }

    public String getTrayicon() {
        return trayicon;
    }

    public void setTrayicon(String trayicon) {
        this.trayicon = trayicon;
    }

    public String getFetchURL() {
        return fetchURL;
    }

    public void setFetchURL(String fetchURL) {
        this.fetchURL = fetchURL;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getReqInt() {
        return reqInt;
    }

    public void setReqInt(int reqInt) {
        this.reqInt = reqInt;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public int getAct_num() {
        return act_num;
    }

    public void setAct_num(int act_num) {
        this.act_num = act_num;
    }

    public String getAct1name() {
        return act1name;
    }

    public void setAct1name(String act1name) {
        this.act1name = act1name;
    }

    public String getAct1link() {
        return act1link;
    }

    public void setAct1link(String act1link) {
        this.act1link = act1link;
    }

    public String getAct2name() {
        return act2name;
    }

    public void setAct2name(String act2name) {
        this.act2name = act2name;
    }

    public String getAct2link() {
        return act2link;
    }

    public void setAct2link(String act2link) {
        this.act2link = act2link;
    }


    public String getSmallIconAccentColor() {
        return smallIconAccentColor;
    }

    public void setSmallIconAccentColor(String smallIconAccentColor) {
        this.smallIconAccentColor = smallIconAccentColor;
    }


    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getLedColor() {
        return ledColor;
    }

    public void setLedColor(String ledColor) {
        this.ledColor = ledColor;
    }

    public int getLockScreenVisibility() {
        return lockScreenVisibility;
    }

    public void setLockScreenVisibility(int lockScreenVisibility) {
        this.lockScreenVisibility = lockScreenVisibility;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public String getGroupMessage() {
        return groupMessage;
    }

    public void setGroupMessage(String groupMessage) {
        this.groupMessage = groupMessage;
    }

    public String getFromProjectNumber() {
        return fromProjectNumber;
    }

    public void setFromProjectNumber(String fromProjectNumber) {
        this.fromProjectNumber = fromProjectNumber;
    }

    public String getCollapseId() {
        return collapseId;
    }

    public void setCollapseId(String collapseId) {
        this.collapseId = collapseId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public String getType_input_to_payload() {
        return type_input_to_payload;
    }

    public void setType_input_to_payload(String type_input_to_payload) {
        this.type_input_to_payload = type_input_to_payload;
    }

    public String getDropdown_text() {
        return dropdown_text;
    }

    public void setDropdown_text(String dropdown_text) {
        this.dropdown_text = dropdown_text;
    }

    public String getValidation() {
        return validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public String getEditbox_title() {
        return editbox_title;
    }

    public void setEditbox_title(String editbox_title) {
        this.editbox_title = editbox_title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAct1icon() {
        return act1icon;
    }

    public void setAct1icon(String act1icon) {
        this.act1icon = act1icon;
    }

    public String getAct2icon() {
        return act2icon;
    }

    public void setAct2icon(String act2icon) {
        this.act2icon = act2icon;
    }

    public String getAct1ID() {
        return act1ID;
    }

    public void setAct1ID(String act1ID) {
        this.act1ID = act1ID;
    }

    public String getAct2ID() {
        return act2ID;
    }

    public void setAct2ID(String act2ID) {
        this.act2ID = act2ID;
    }
    public String getAp() {
        return ap;
    }

    public void setAp(String ap) {
        this.ap = ap;
    }

    public String getCpc() {
        return cpc;
    }

    public void setCpc(String cpc) {
        this.cpc = cpc;
    }

    public String getRv() {
        return rv;
    }

    public void setRv(String rv) {
        this.rv = rv;
    }

    public String getRc() {
        return rc;
    }

    public void setRc(String rc) {
        this.rc = rc;
    }

    public String getPassive_flag() {
        return passive_flag;
    }

    public void setPassive_flag(String passive_flag) {
        this.passive_flag = passive_flag;
    }

    public String getCpm() {
        return cpm;
    }

    public void setCpm(String cpm) {
        this.cpm = cpm;
    }

    public String getCtr() {
        return ctr;
    }

    public void setCtr(String ctr) {
        this.ctr = ctr;
    }

    public String getReceived_bid() {
        return received_bid;
    }

    public void setReceived_bid(String received_bid) {
        this.received_bid = received_bid;
    }

    public String getAd_type() {
        return ad_type;
    }

    public void setAd_type(String ad_type) {
        this.ad_type = ad_type;
    }

    public String getAdID() {
        return adID;
    }

    public void setAdID(String adID) {
        this.adID = adID;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getFallBackSubDomain() {
        return fallBackSubDomain;
    }

    public void setFallBackSubDomain(String fallBackSubDomain) {
        this.fallBackSubDomain = fallBackSubDomain;
    }

    public String getFallBackDomain() {
        return fallBackDomain;
    }

    public void setFallBackDomain(String fallBackDomain) {
        this.fallBackDomain = fallBackDomain;
    }

    public String getFallBackPath() {
        return fallBackPath;
    }

    public void setFallBackPath(String fallBackPath) {
        this.fallBackPath = fallBackPath;
    }

    public int getTime_out() {
        return time_out;
    }

    public void setTime_out(int time_out) {
        this.time_out = time_out;
    }

    public int getAdTimeOut() {
        return adTimeOut;
    }

    public void setAdTimeOut(int adTimeOut) {
        this.adTimeOut = adTimeOut;
    }

    public String getCreated_Time() {
        return created_Time;
    }

    public void setCreated_Time(String created_Time) {
        this.created_Time = created_Time;
    }

    public String getTime_to_live() {
        return time_to_live;
    }

    public void setTime_to_live(String time_to_live) {
        this.time_to_live = time_to_live;
    }

    public String getPush_type() {
        return push_type;
    }

    public void setPush_type(String push_type) {
        this.push_type = push_type;
    }
    public int getCustomNotification() {
        return CustomNotification;
    }

    public void setCustomNotification(int customNotification) {
        CustomNotification = customNotification;
    }
    public String getPublic_global_key() {
        return public_global_key;
    }

    public void setPublic_global_key(String public_global_key) {
        this.public_global_key = public_global_key;
    }

    public int getDefaultNotificationPreview() {
        return defaultNotificationPreview;
    }

    public void setDefaultNotificationPreview(int defaultNotificationPreview) {
        this.defaultNotificationPreview = defaultNotificationPreview;
    }

    public String getNotification_bg_color() {
        return notification_bg_color;
    }

    public void setNotification_bg_color(String notification_bg_color) {
        this.notification_bg_color = notification_bg_color;
    }
    public String getExpiryTimerValue() {
        return expiryTimerValue;
    }

    public void setExpiryTimerValue(String expiryTimerValue) {
        this.expiryTimerValue = expiryTimerValue;
    }

    public String getMakeStickyNotification() {
        return makeStickyNotification;
    }

    public void setMakeStickyNotification(String makeStickyNotification) {
        this.makeStickyNotification = makeStickyNotification;
    }

    public String getOfflineCampaign() {
        return offlineCampaign;
    }

    public void setOfflineCampaign(String offlineCampaign) {
        this.offlineCampaign = offlineCampaign;
    }
}

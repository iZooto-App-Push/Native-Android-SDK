package com.izooto;


public class Payload {

    /**
     * fetchURL : https://api.taboola.com/1.2/json/databilityindia-network-matichon/recommendations.get?app.type=desktop&app.apikey=61291475364d2ddf3edd7e50fe2313195771a79c&placement.rec-count=1&placement.organic-type=mix&placement.visible=true&source.type=section&source.id=%2Fsection&source.url=https://www.matichon.co.th&placement.name=Web-Notification&user.session=init&user.agent=Mozilla%2F5.0+%28Linux%3B+Android+6.0.1%3B+ASUS_Z00UD+Build%2FMMB29P%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Chrome%2F66.0.3359.126+Mobile+Safari%2F537.36&user.realip=125.25.223.108&user.id=aeef899648288185904ec76bf1fa689d&app.s2s=true
     * key : 123
     * id : 1
     * rid : 123422
     * link : list[0].url
     * title : list[0].branding
     * message : list[0].name
     * icon : ~https://www.mediagiantdesign.com/assets/uploads/2016/12/secure-credit-card-icon.png
     * reqInt : 1
     * tag : tag
     * banner : ~https://www.mediagiantdesign.com/assets/uploads/2016/12/secure-credit-card-icon.png
     * act_num : 0
     * act1name : ~Sponsered
     * act1link : ~https://google.com
     * act2name : list[0].branding
     * act2link : list[0].url
     */

    private String fetchURL;
    private String key;
    private String id;//campain id
    private String rid;//run id
    private String link;
    private String title;
    private String message;
    private String icon;
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
    // add new payload data





    public String smallIconAccentColor;

    public String sound;
    public String ledColor;
    public int lockScreenVisibility = 1;
    public String groupKey;
    public String groupMessage;

    public String fromProjectNumber;

    public String collapseId;
    public int priority;
    public String rawPayload;



//////////////////////////////////////////////
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
}

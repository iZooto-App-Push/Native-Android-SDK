package com.app.izoototest;

public class NewsItem {
    private final String title;
    private final String message;
    private final String bannerImage;
    private final String landingUrl;
    private final String timeStamp;

    // Constructor
    public NewsItem(String title, String message, String bannerImage, String landingUrl, String timeStamp) {
        this.title = title;
        this.message = message;
        this.bannerImage = bannerImage;
        this.landingUrl = landingUrl;
        this.timeStamp = timeStamp;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public String getLandingUrl() {
        return landingUrl;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
}

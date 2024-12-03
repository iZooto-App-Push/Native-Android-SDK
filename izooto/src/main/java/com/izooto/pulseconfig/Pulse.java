package com.izooto.pulseconfig;

// Model for Pulse
public class Pulse {
    private String url;
    private String cid;
    private String rid;
    private boolean status;
    private PulseAdConfiguration adConf;
    private PulseMargin margin;
    private PulseLabel label;

    // Constructor
    public Pulse(String url, String cid, String rid, boolean status, PulseAdConfiguration adConf, PulseMargin margin, PulseLabel label) {
        this.url = url;
        this.cid = cid;
        this.rid = rid;
        this.status = status;
        this.adConf = adConf;
        this.margin = margin;
        this.label = label;
    }

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public PulseAdConfiguration getAdConf() {
        return adConf;
    }

    public void setAdConf(PulseAdConfiguration adConf) {
        this.adConf = adConf;
    }

    public PulseMargin getMargin() {
        return margin;
    }

    public void setMargin(PulseMargin margin) {
        this.margin = margin;
    }

    public PulseLabel getLabel() {
        return label;
    }

    public void setLabel(PulseLabel label) {
        this.label = label;
    }
}
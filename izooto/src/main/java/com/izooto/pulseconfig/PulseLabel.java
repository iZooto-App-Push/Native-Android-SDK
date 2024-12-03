package com.izooto.pulseconfig;

// Model for Label
public class PulseLabel {
    private String text;
    private String color;
    private int size;
    private String alignment;
    private PulseMargin margin;
    private boolean status;

    // Constructor
    public PulseLabel(String text, String color, int size, String alignment, PulseMargin margin, Boolean status) {
        this.text = text;
        this.color = color;
        this.size = size;
        this.alignment = alignment;
        this.margin = margin;
        this.status = status;
    }

    // Getters and Setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public PulseMargin getMargin() {
        return margin;
    }

    public void setMargin(PulseMargin margin) {
        this.margin = margin;
    }
    public boolean isStatus() {
        return status;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }

}
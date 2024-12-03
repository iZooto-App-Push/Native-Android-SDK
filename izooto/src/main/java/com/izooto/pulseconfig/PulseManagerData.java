package com.izooto.pulseconfig;

public class PulseManagerData {
    // Singleton instance
    private static PulseManagerData instance = null;

    // PulseData object
    private PulseData pulseData;

    // Private constructor to prevent instantiation
    private PulseManagerData() { }

    // Method to get the singleton instance
    public static PulseManagerData getInstance() {
        if (instance == null) {
            instance = new PulseManagerData();
        }
        return instance;
    }

    // Method to initialize the PulseData with dynamic values
    public void initializePulseData(Pulse pulse) {
        pulseData = new PulseData(pulse);
    }

    // Method to retrieve the PulseData object
    public PulseData getPulseData() {
        if (pulseData == null) {
            throw new IllegalStateException("PulseData has not been initialized.");
        }
        return pulseData;
    }
    public boolean isPulseDataInitialized() {
        return pulseData != null;
    }
    // You can add more methods to manipulate or retrieve pulse data as needed
}
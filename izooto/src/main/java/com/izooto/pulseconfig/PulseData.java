package com.izooto.pulseconfig;

// Main class that holds the Pulse structure
public class PulseData {
    private Pulse pulse;

    // Constructor
    public PulseData(Pulse pulse) {
        this.pulse = pulse;
    }



    public Pulse getPulse() {
        return pulse;
    }

    public void setPulse(Pulse pulse) {
        this.pulse = pulse;
    }
}
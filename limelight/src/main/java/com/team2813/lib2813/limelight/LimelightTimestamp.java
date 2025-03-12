package com.team2813.lib2813.limelight;

/** A timestamp related to a limelight measurement. */
public record LimelightTimestamp(double seconds, Source source) {

    public enum Source {
        FPGA,
        PHOENIX6
    }

    public double milliseconds() {
        return seconds * 1000;
    }
}

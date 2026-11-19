package com.amillioncodelines.streamstories.activity.domain;

public record PlaybackPosition(int positionSeconds, boolean completed) {

    private static final int COMPLETED_PERCENT = 90;

    public static PlaybackPosition of(int reportedSeconds, int durationSeconds) {
        if (reportedSeconds < 0) {
            throw new IllegalArgumentException("positionSeconds cannot be negative");
        }
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("durationSeconds must be positive");
        }
        int position = Math.min(reportedSeconds, durationSeconds);
        boolean completed = position * 100L >= durationSeconds * (long) COMPLETED_PERCENT;
        return new PlaybackPosition(position, completed);
    }
}

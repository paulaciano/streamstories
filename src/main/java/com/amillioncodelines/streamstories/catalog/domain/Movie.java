package com.amillioncodelines.streamstories.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.Collection;

/** A movie: a title that is also playable content. */
@Entity
@DiscriminatorValue("MOVIE")
public class Movie extends MediaItem {

    @Column(nullable = false)
    private int durationSeconds;

    @Column(nullable = false)
    private String videoUrl;

    protected Movie() {}

    public Movie(TitleDetails details, Collection<Genre> genres, int durationSeconds, String videoUrl) {
        super(details, genres);
        changeMedia(durationSeconds, videoUrl);
    }

    /** Duration is always positive (rule C6). */
    public final void changeMedia(int newDurationSeconds, String newVideoUrl) {
        if (newDurationSeconds <= 0) {
            throw new IllegalArgumentException("durationSeconds must be positive");
        }
        this.durationSeconds = newDurationSeconds;
        this.videoUrl = newVideoUrl;
    }

    @Override
    public MediaType getType() {
        return MediaType.MOVIE;
    }

    /** A movie is playable when it is available. */
    public boolean isPlayable() {
        return isAvailable();
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public String getVideoUrl() {
        return videoUrl;
    }
}

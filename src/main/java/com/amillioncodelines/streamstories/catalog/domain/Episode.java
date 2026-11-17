package com.amillioncodelines.streamstories.catalog.domain;

import com.amillioncodelines.streamstories.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * An episode: playable content that belongs to a season. It is not a title: genres and ratings belong to its series.
 */
@Entity
public class Episode extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false, updatable = false)
    private Season season;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(nullable = false)
    private int number;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String synopsis;

    @Column(nullable = false)
    private int durationSeconds;

    @Column(nullable = false)
    private String videoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Availability availability = Availability.AVAILABLE;

    protected Episode() {}

    Episode(Season season, int number, EpisodeDetails details) {
        this.season = season;
        update(number, details);
    }

    final void update(int newNumber, EpisodeDetails details) {
        if (newNumber < 1 || newNumber > Season.MAX_NUMBER) {
            throw new IllegalArgumentException("Episode numbers go from 1 to " + Season.MAX_NUMBER);
        }
        if (details.durationSeconds() <= 0) {
            throw new IllegalArgumentException("durationSeconds must be positive");
        }
        this.number = newNumber;
        this.title = details.title().strip();
        this.synopsis = details.synopsis();
        this.durationSeconds = details.durationSeconds();
        this.videoUrl = details.videoUrl();
    }

    public void changeAvailability(Availability newAvailability) {
        this.availability = Objects.requireNonNull(newAvailability);
    }

    public boolean isAvailable() {
        return availability == Availability.AVAILABLE;
    }

    /** An episode is playable only if both the episode and its series are available. */
    public boolean isPlayable() {
        return isAvailable() && season.getSeries().isAvailable();
    }

    public Season getSeason() {
        return season;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public Availability getAvailability() {
        return availability;
    }
}

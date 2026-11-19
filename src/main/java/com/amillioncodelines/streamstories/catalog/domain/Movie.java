package com.amillioncodelines.streamstories.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import java.util.Collection;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@DiscriminatorValue("MOVIE")
public class Movie extends MediaItem {

    @Column(nullable = false)
    private int durationSeconds;

    @Column(nullable = false)
    private String videoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    private Franchise franchise;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    private Integer franchiseOrder;

    protected Movie() {}

    public Movie(TitleDetails details, Collection<Genre> genres, int durationSeconds, String videoUrl) {
        super(details, genres);
        changeMedia(durationSeconds, videoUrl);
    }

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

    void joinFranchise(Franchise newFranchise, int order) {
        this.franchise = newFranchise;
        this.franchiseOrder = order;
    }

    void leaveFranchise() {
        this.franchise = null;
        this.franchiseOrder = null;
    }

    public Franchise getFranchise() {
        return franchise;
    }

    public Integer getFranchiseOrder() {
        return franchiseOrder;
    }

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

package com.amillioncodelines.streamstories.catalog.domain;

import com.amillioncodelines.streamstories.shared.error.PreconditionFailedException;
import com.amillioncodelines.streamstories.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "media_item")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type", length = 10)
public abstract class MediaItem extends AuditableEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String synopsis;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(nullable = false)
    private int releaseYear;

    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Availability availability = Availability.AVAILABLE;

    /** Optimistic locking; also makes a change that only touches the genres count as a modification. */
    @Version
    private long version;

    @ManyToMany
    @JoinTable(
            name = "media_item_genre",
            joinColumns = @JoinColumn(name = "media_item_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new HashSet<>();

    protected MediaItem() {}

    protected MediaItem(TitleDetails details, Collection<Genre> genres) {
        update(details, genres);
    }

    public abstract MediaType getType();

    public final void update(TitleDetails details, Collection<Genre> newGenres) {
        this.title = details.title().strip();
        this.synopsis = details.synopsis();
        this.releaseYear = details.releaseYear();
        this.coverImageUrl = details.coverImageUrl();
        this.genres.clear();
        this.genres.addAll(newGenres);
    }

    public void changeAvailability(Availability newAvailability) {
        this.availability = Objects.requireNonNull(newAvailability);
    }

    public void requireVersion(Long expectedVersion) {
        if (expectedVersion != null && expectedVersion != version) {
            throw new PreconditionFailedException(
                    "The title changed since version %d (now %d): read it again before changing it."
                            .formatted(expectedVersion, version));
        }
    }

    public long getVersion() {
        return version;
    }

    public boolean isAvailable() {
        return availability == Availability.AVAILABLE;
    }

    public String getTitle() {
        return title;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public Availability getAvailability() {
        return availability;
    }

    public Set<Genre> getGenres() {
        return Set.copyOf(genres);
    }
}

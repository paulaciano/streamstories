package com.amillioncodelines.streamstories.catalog.domain;

import com.amillioncodelines.streamstories.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/** A genre that classifies titles. Name and slug are unique, ignoring case (rule C3). */
@Entity
public class Genre extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    protected Genre() {}

    public Genre(String name) {
        rename(name);
    }

    public final void rename(String newName) {
        this.name = newName.strip();
        this.slug = Slugs.from(this.name);
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }
}

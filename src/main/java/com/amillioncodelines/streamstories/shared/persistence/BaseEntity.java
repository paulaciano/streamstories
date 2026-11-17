package com.amillioncodelines.streamstories.shared.persistence;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;
import org.hibernate.Hibernate;
import org.hibernate.annotations.UuidGenerator;

/**
 * Base class for entities identified by a time-ordered UUID (version 7).
 *
 * <p>Equality is based on the identifier, so two instances are equal only once they have been persisted. The hash code
 * is constant per class, which keeps entities stable inside hash-based collections before and after persisting.
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    public UUID getId() {
        return id;
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        return id != null && id.equals(((BaseEntity) other).id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}

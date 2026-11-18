package com.amillioncodelines.streamstories.catalog;

import java.util.Optional;
import java.util.UUID;

/**
 * Read-only queries that other modules can make to the catalog, without reaching its entities or repositories.
 *
 * <p>Implemented by the catalog itself.
 */
public interface CatalogQueries {

    /**
     * Finds a movie or an episode, the only kinds of content that can be played.
     *
     * @throws IllegalArgumentException for {@link ContentKind#SERIES}: a series is played through its episodes
     */
    Optional<PlayableContent> findPlayable(ContentKind kind, UUID id);
}

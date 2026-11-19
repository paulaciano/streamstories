package com.amillioncodelines.streamstories.catalog;

import java.util.Collection;
import java.util.UUID;

/** Implemented by the modules that record activity, so the catalog can check it without depending on them (C11). */
public interface ContentActivity {

    boolean existsForAny(ContentKind kind, Collection<UUID> contentIds);
}

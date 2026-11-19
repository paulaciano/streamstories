package com.amillioncodelines.streamstories.recommendation.domain;

import com.amillioncodelines.streamstories.catalog.CatalogTitle;

/** A recommended title and why it is recommended. */
public record Recommendation(CatalogTitle title, String reason) {}

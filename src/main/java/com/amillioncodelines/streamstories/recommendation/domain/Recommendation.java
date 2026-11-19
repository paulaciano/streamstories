package com.amillioncodelines.streamstories.recommendation.domain;

import com.amillioncodelines.streamstories.catalog.CatalogTitle;

public record Recommendation(CatalogTitle title, String reason) {}

package com.amillioncodelines.streamstories.catalog.application;

import jakarta.validation.constraints.Size;

/** The number identifies the season in the path, so only the title can change. */
public record SeasonUpdateCommand(@Size(max = 200) String title) {}

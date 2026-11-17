package com.amillioncodelines.streamstories.catalog.api;

import jakarta.validation.constraints.Size;

/** The number identifies the season in the path, so only the title can change. */
record SeasonUpdateRequest(@Size(max = 200) String title) {}

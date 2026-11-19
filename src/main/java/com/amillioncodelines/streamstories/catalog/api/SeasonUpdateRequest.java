package com.amillioncodelines.streamstories.catalog.api;

import jakarta.validation.constraints.Size;

record SeasonUpdateRequest(@Size(max = 200) String title) {}

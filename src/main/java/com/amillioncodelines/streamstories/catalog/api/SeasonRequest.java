package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.domain.Season;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record SeasonRequest(
        @NotNull @Min(1) @Max(Season.MAX_NUMBER) Integer number,
        @Size(max = 200) String title) {}

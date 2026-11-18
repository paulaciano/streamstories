package com.amillioncodelines.streamstories.activity.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** An integer from 1 to 5 (rule A10). */
record RatingRequest(@NotNull @Min(1) @Max(5) Integer score) {}

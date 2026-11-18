package com.amillioncodelines.streamstories.activity.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** A position beyond the duration is accepted and capped (rule A2). */
record ProgressRequest(@NotNull @PositiveOrZero Integer positionSeconds) {}

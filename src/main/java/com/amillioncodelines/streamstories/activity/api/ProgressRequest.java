package com.amillioncodelines.streamstories.activity.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

record ProgressRequest(@NotNull @PositiveOrZero Integer positionSeconds) {}

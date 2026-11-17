package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.domain.Availability;
import jakarta.validation.constraints.NotNull;

/** Retires content or offers it again. */
record AvailabilityRequest(@NotNull Availability availability) {}

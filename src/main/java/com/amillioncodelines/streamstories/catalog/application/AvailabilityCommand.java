package com.amillioncodelines.streamstories.catalog.application;

import com.amillioncodelines.streamstories.catalog.domain.Availability;
import jakarta.validation.constraints.NotNull;

/** Retires content or offers it again. */
public record AvailabilityCommand(@NotNull Availability availability) {}

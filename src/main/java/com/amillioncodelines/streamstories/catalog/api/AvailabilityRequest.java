package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.domain.Availability;
import jakarta.validation.constraints.NotNull;

record AvailabilityRequest(@NotNull Availability availability) {}

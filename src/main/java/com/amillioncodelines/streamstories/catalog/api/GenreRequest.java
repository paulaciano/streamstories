package com.amillioncodelines.streamstories.catalog.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record GenreRequest(@NotBlank @Size(max = 50) String name) {}

package com.amillioncodelines.streamstories.catalog.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenreCommand(@NotBlank @Size(max = 50) String name) {}

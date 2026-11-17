package com.amillioncodelines.streamstories.catalog.application;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SeasonCommand(
        @NotNull @Min(1) Integer number, @Size(max = 200) String title) {}

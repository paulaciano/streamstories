package com.amillioncodelines.streamstories.catalog.application;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/** Full data of an episode, used to create it or replace it. */
public record EpisodeCommand(
        @NotNull @Min(1) Integer number,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 4000) String synopsis,
        @NotNull @Positive Integer durationSeconds,

        @NotBlank @Size(max = 2048) @URL(protocol = "https", message = "debe ser una URL https")
        String videoUrl) {}

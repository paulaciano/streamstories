package com.amillioncodelines.streamstories.catalog.application;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;
import org.hibernate.validator.constraints.URL;

/** Full data of a movie, used to create it or replace it. */
public record MovieCommand(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 4000) String synopsis,
        @NotNull @Min(1888) @Max(2100) Integer releaseYear,
        @NotNull @Positive Integer durationSeconds,

        @NotBlank @Size(max = 2048) @URL(protocol = "https", message = "debe ser una URL https")
        String videoUrl,

        @Size(max = 2048) @URL(protocol = "https", message = "debe ser una URL https")
        String coverImageUrl,

        Set<UUID> genreIds) {}

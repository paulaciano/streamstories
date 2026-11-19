package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.SeriesCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;
import org.hibernate.validator.constraints.URL;

record SeriesRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 4000) String synopsis,
        @NotNull @Min(1888) @Max(2100) Integer releaseYear,

        @Size(max = 2048) @URL(protocol = "https", message = "must be an https URL")
        String coverImageUrl,

        Set<@NotNull UUID> genreIds) {

    SeriesCommand toCommand() {
        return new SeriesCommand(title, synopsis, releaseYear, coverImageUrl, genreIds);
    }
}

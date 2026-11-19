package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.EpisodeCommand;
import com.amillioncodelines.streamstories.catalog.domain.Numbering;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

record EpisodeRequest(
        @NotNull @Min(1) @Max(Numbering.MAX) Integer number,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 4000) String synopsis,
        @NotNull @Positive Integer durationSeconds,

        @NotBlank @Size(max = 2048) @URL(protocol = "https", message = "must be an https URL")
        String videoUrl) {

    EpisodeCommand toCommand() {
        return new EpisodeCommand(number, title, synopsis, durationSeconds, videoUrl);
    }
}

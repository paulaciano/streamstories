package com.amillioncodelines.streamstories.catalog.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amillioncodelines.streamstories.catalog.ContentActivity;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import com.amillioncodelines.streamstories.catalog.domain.Episode;
import com.amillioncodelines.streamstories.catalog.domain.EpisodeDetails;
import com.amillioncodelines.streamstories.catalog.domain.Series;
import com.amillioncodelines.streamstories.catalog.domain.SeriesRepository;
import com.amillioncodelines.streamstories.catalog.domain.TitleDetails;
import com.amillioncodelines.streamstories.shared.error.ConflictException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Rule C11 for series: the series and every one of its episodes must be free of activity. */
class SeriesServiceTests {

    private final SeriesRepository seriesRepository = mock();
    private final ContentActivity activity = mock();
    private final SeriesService service = new SeriesService(
            seriesRepository, mock(), new GenreResolver(mock()), new ContentActivityGuard(List.of(activity)));

    @Test
    void shouldRefuseToDeleteASeriesWhenOneEpisodeHasActivity() {
        UUID seriesId = UUID.randomUUID();
        Series series = new Series(new TitleDetails("Dark", null, 2017, null), List.of());
        Episode episode = series.addSeason(1, null)
                .addEpisode(1, new EpisodeDetails("Secrets", null, 3000, "https://cdn.example.com/e1"));
        UUID episodeId = UUID.randomUUID();
        ReflectionTestUtils.setField(episode, "id", episodeId);
        when(seriesRepository.findById(seriesId)).thenReturn(Optional.of(series));
        when(activity.exists(ContentKind.EPISODE, episodeId)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(seriesId)).isInstanceOf(ConflictException.class);
        verify(seriesRepository, never()).delete(any(Series.class));
    }
}

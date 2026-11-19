package com.amillioncodelines.streamstories.recommendation;

import static com.amillioncodelines.streamstories.support.CatalogApi.token;
import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.ApiClient.Session;
import com.amillioncodelines.streamstories.support.CatalogApi;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The database is shared with other tests, so each test uses a genre of its own: only its titles can be candidates.
 */
@IntegrationTest
class RecommendationIntegrationTests {

    @Autowired
    ApiClient api;

    @Autowired
    CatalogApi catalog;

    @Test
    void shouldRecommendAvailableTitlesOfTheGenresTheUserLikes() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String genreName = "Genre " + token();
        String genre = catalog.createGenre(admin, genreName);
        String liked = catalog.createMovie(admin, "Liked " + token(), 2000, genre);
        String watched = catalog.createSeries(admin, "Watched " + token(), 2000, genre);
        catalog.addSeason(admin, watched, 1);
        String episode = catalog.addEpisode(admin, watched, 1, 1);
        String candidate = catalog.createSeries(admin, "Candidate " + token(), 2000, genre);
        String retired = catalog.createMovie(admin, "Retired " + token(), 2000, genre);
        catalog.changeAvailability(admin, "/api/v1/movies/" + retired, "RETIRED");

        catalog.send(user, "PUT", "/api/v1/me/ratings/" + liked, """
                {"score": 5}
                """);
        catalog.send(user, "PUT", "/api/v1/me/progress/episodes/" + episode, """
                {"positionSeconds": 2700}
                """);

        assertThat(catalog.get(user, "/api/v1/me/recommendations")).bodyJson().isLenientlyEqualTo("""
                {"content": [{"titleId": "%s", "type": "SERIES", "reason": "Because you like %s"}],
                 "page": {"totalElements": 1}}
                """.formatted(
                        candidate, genreName));
    }

    @Test
    void shouldGiveANewUserTheCommunityList() {
        Session admin = api.adminSession();
        catalog.createMovie(admin, "Anything " + token(), 2000);

        assertThat(catalog.get(api.newUserSession(), "/api/v1/me/recommendations?size=100"))
                .bodyJson()
                .extractingPath("$.content[*].reason")
                .asArray()
                .isNotEmpty()
                .allMatch(reason -> List.of("Top rated by other viewers", "New on StreamStories")
                        .contains(reason));
    }
}

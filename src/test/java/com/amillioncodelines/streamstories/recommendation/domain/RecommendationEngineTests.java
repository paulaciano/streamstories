package com.amillioncodelines.streamstories.recommendation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.activity.CommunityRating;
import com.amillioncodelines.streamstories.activity.UserActivity;
import com.amillioncodelines.streamstories.catalog.CatalogTitle;
import com.amillioncodelines.streamstories.catalog.CatalogTitle.GenreRef;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecommendationEngineTests {

    private static final GenreRef DRAMA = new GenreRef(UUID.randomUUID(), "Drama");
    private static final GenreRef COMEDY = new GenreRef(UUID.randomUUID(), "Comedia");
    private static final GenreRef SCI_FI = new GenreRef(UUID.randomUUID(), "Ciencia ficción");

    private final RecommendationEngine engine = new RecommendationEngine();

    @Test
    void shouldRecommendUnseenTitlesOfTheGenresTheUserLikes() {
        CatalogTitle liked = movie("Liked", 2000, DRAMA);
        CatalogTitle drama = movie("Drama", 2000, DRAMA);
        CatalogTitle comedy = movie("Comedy", 2000, COMEDY);

        List<Recommendation> result =
                engine.recommend(List.of(liked, drama, comedy), rated(Map.of(liked.id(), 5)), Map.of());

        assertThat(result).extracting(Recommendation::title).containsExactly(drama);
        assertThat(result.getFirst().reason()).isEqualTo("Because you like Drama");
    }

    @Test
    void shouldLeaveOutRatedStartedAndRetiredTitles() {
        CatalogTitle liked = movie("Liked", 2000, DRAMA);
        CatalogTitle started = series("Started", 2000, DRAMA);
        CatalogTitle retired =
                new CatalogTitle(UUID.randomUUID(), ContentKind.MOVIE, "Retired", null, 2000, false, List.of(DRAMA));
        CatalogTitle candidate = movie("Candidate", 2000, DRAMA);
        UserActivity activity = new UserActivity(Map.of(liked.id(), 4), Set.of(started.id()), Set.of());

        assertThat(engine.recommend(List.of(liked, started, retired, candidate), activity, Map.of()))
                .extracting(Recommendation::title)
                .containsExactly(candidate);
    }

    @Test
    void shouldSubtractTheGenresOfTitlesTheUserDisliked() {
        CatalogTitle liked = movie("Liked", 2000, DRAMA);
        CatalogTitle disliked = movie("Disliked", 2000, COMEDY);
        CatalogTitle drama = movie("Drama", 2000, DRAMA);
        CatalogTitle dramaticComedy = movie("Dramatic comedy", 2001, DRAMA, COMEDY);

        List<Recommendation> result = engine.recommend(
                List.of(liked, disliked, drama, dramaticComedy),
                rated(Map.of(liked.id(), 5, disliked.id(), 1)),
                Map.of());

        // Drama: +2. Dramatic comedy: +2 - 2 = 0, still a candidate because it has a liked genre.
        assertThat(result).extracting(Recommendation::title).containsExactly(drama, dramaticComedy);
    }

    @Test
    void shouldTreatAScoreOfThreeAsNeutral() {
        CatalogTitle rated = movie("Rated", 2000, DRAMA);
        CatalogTitle drama = movie("Drama", 2000, DRAMA);

        List<Recommendation> result = engine.recommend(List.of(rated, drama), rated(Map.of(rated.id(), 3)), Map.of());

        // No affinity at all: the user gets the community list.
        assertThat(result).extracting(Recommendation::reason).containsExactly(RecommendationEngine.NEW_TITLE_REASON);
    }

    @Test
    void shouldCountACompletedSeriesOnceAndPreferTheFavoriteFormat() {
        CatalogTitle watched = series("Watched", 2000, SCI_FI);
        CatalogTitle movie = movie("Movie", 2000, SCI_FI);
        CatalogTitle otherSeries = series("Other series", 2000, SCI_FI);
        UserActivity activity = new UserActivity(Map.of(), Set.of(watched.id()), Set.of(watched.id()));

        List<Recommendation> result = engine.recommend(List.of(watched, movie, otherSeries), activity, Map.of());

        // Both score +1 for Sci-Fi; the series gets the bonus because the user prefers series.
        assertThat(result).extracting(Recommendation::title).containsExactly(otherSeries, movie);
    }

    @Test
    void shouldLetRetiredTitlesShowWhatTheUserLikes() {
        CatalogTitle retiredFavorite = new CatalogTitle(
                UUID.randomUUID(), ContentKind.MOVIE, "Retired favorite", null, 2000, false, List.of(DRAMA));
        CatalogTitle drama = movie("Drama", 2000, DRAMA);

        assertThat(engine.recommend(List.of(retiredFavorite, drama), rated(Map.of(retiredFavorite.id(), 5)), Map.of()))
                .extracting(Recommendation::title)
                .containsExactly(drama);
    }

    @Test
    void shouldExplainWithTheTwoGenresTheUserLikesMost() {
        CatalogTitle drama = movie("Drama", 2000, DRAMA);
        CatalogTitle dramaAgain = movie("Drama again", 2000, DRAMA);
        CatalogTitle comedy = movie("Comedy", 2000, COMEDY);
        CatalogTitle sciFi = movie("Sci-Fi", 2000, SCI_FI);
        CatalogTitle all = movie("All", 2000, COMEDY, DRAMA, SCI_FI);
        UserActivity activity = rated(Map.of(drama.id(), 5, dramaAgain.id(), 5, comedy.id(), 4, sciFi.id(), 4));

        List<Recommendation> result =
                engine.recommend(List.of(drama, dramaAgain, comedy, sciFi, all), activity, Map.of());

        // Drama +4 first; Comedia and Ciencia ficción tie at +2 and go alphabetically.
        assertThat(result.getFirst().reason()).isEqualTo("Because you like Drama and Ciencia ficción");
    }

    @Test
    void shouldBreakTiesByCommunityRatingThenRecencyThenTitle() {
        CatalogTitle liked = movie("Liked", 2000, DRAMA);
        CatalogTitle older = movie("Older", 1990, DRAMA);
        CatalogTitle newer = movie("Newer", 2010, DRAMA);
        CatalogTitle betterRated = movie("Better rated", 1980, DRAMA);
        CatalogTitle zebra = movie("Zebra", 2010, DRAMA);

        List<Recommendation> result = engine.recommend(
                List.of(liked, older, zebra, newer, betterRated),
                rated(Map.of(liked.id(), 5)),
                Map.of(betterRated.id(), new CommunityRating(4.5, 2)));

        assertThat(result).extracting(Recommendation::title).containsExactly(betterRated, newer, zebra, older);
    }

    @Test
    void shouldGiveNewUsersTheBestRatedTitlesAndThenTheNewestOnes() {
        CatalogTitle popular = movie("Popular", 2000, DRAMA);
        CatalogTitle morePopular = movie("More popular", 2000, DRAMA);
        CatalogTitle fewerVotes = movie("Fewer votes", 2000, DRAMA);
        CatalogTitle unratedNew = movie("Unrated new", 2020, DRAMA);
        CatalogTitle unratedOld = movie("Unrated old", 1990, DRAMA);
        Map<UUID, CommunityRating> community = Map.of(
                popular.id(), new CommunityRating(4.0, 10),
                morePopular.id(), new CommunityRating(4.8, 3),
                fewerVotes.id(), new CommunityRating(4.0, 2));

        List<Recommendation> result = engine.recommend(
                List.of(unratedOld, popular, fewerVotes, unratedNew, morePopular), nothing(), community);

        assertThat(result)
                .extracting(Recommendation::title)
                .containsExactly(morePopular, popular, fewerVotes, unratedNew, unratedOld);
        assertThat(result)
                .extracting(Recommendation::reason)
                .containsExactly(
                        RecommendationEngine.TOP_RATED_REASON,
                        RecommendationEngine.TOP_RATED_REASON,
                        RecommendationEngine.TOP_RATED_REASON,
                        RecommendationEngine.NEW_TITLE_REASON,
                        RecommendationEngine.NEW_TITLE_REASON);
    }

    @Test
    void shouldFallBackToTheCommunityListWithoutWhatTheUserAlreadySaw() {
        CatalogTitle disliked = movie("Disliked", 2000, DRAMA);
        CatalogTitle other = movie("Other", 2000, COMEDY);

        assertThat(engine.recommend(List.of(disliked, other), rated(Map.of(disliked.id(), 1)), Map.of()))
                .extracting(Recommendation::title)
                .containsExactly(other);
    }

    private static UserActivity rated(Map<UUID, Integer> scores) {
        return new UserActivity(scores, Set.of(), Set.of());
    }

    private static UserActivity nothing() {
        return new UserActivity(Map.of(), Set.of(), Set.of());
    }

    private static CatalogTitle movie(String title, int year, GenreRef... genres) {
        return new CatalogTitle(UUID.randomUUID(), ContentKind.MOVIE, title, null, year, true, Arrays.asList(genres));
    }

    private static CatalogTitle series(String title, int year, GenreRef... genres) {
        return new CatalogTitle(UUID.randomUUID(), ContentKind.SERIES, title, null, year, true, Arrays.asList(genres));
    }
}

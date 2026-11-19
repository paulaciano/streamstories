package com.amillioncodelines.streamstories.recommendation.domain;

import com.amillioncodelines.streamstories.activity.CommunityRating;
import com.amillioncodelines.streamstories.activity.UserActivity;
import com.amillioncodelines.streamstories.catalog.CatalogTitle;
import com.amillioncodelines.streamstories.catalog.CatalogTitle.GenreRef;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class RecommendationEngine {

    static final double PREFERRED_FORMAT_BONUS = 0.5;
    static final String TOP_RATED_REASON = "Top rated by other viewers";
    static final String NEW_TITLE_REASON = "New on StreamStories";

    private static final int LIKED_SCORE = 4;
    private static final int DISLIKED_SCORE = 2;
    private static final int RATING_SIGNAL = 2;
    private static final int COMPLETED_SIGNAL = 1;
    private static final int GENRES_IN_REASON = 2;

    private final Comparator<String> titleOrder;

    public RecommendationEngine() {
        Collator spanish = Collator.getInstance(Locale.forLanguageTag("es"));
        spanish.setStrength(Collator.SECONDARY);
        this.titleOrder = spanish::compare;
    }

    public List<Recommendation> recommend(
            List<CatalogTitle> catalog, UserActivity activity, Map<UUID, CommunityRating> community) {
        Map<UUID, Integer> signals = signals(activity);
        Map<UUID, CatalogTitle> byId = catalog.stream().collect(Collectors.toMap(CatalogTitle::id, title -> title));
        Map<UUID, Integer> affinity = affinity(signals, byId);
        ContentKind preferred = preferredFormat(signals, byId);

        List<CatalogTitle> unseen = catalog.stream()
                .filter(CatalogTitle::available)
                .filter(title -> !activity.scores().containsKey(title.id()))
                .filter(title -> !activity.started().contains(title.id()))
                .toList();

        List<Scored> candidates = unseen.stream()
                .filter(title -> title.genres().stream().anyMatch(genre -> affinity.getOrDefault(genre.id(), 0) > 0))
                .map(title -> new Scored(title, score(title, affinity, preferred)))
                .toList();
        if (candidates.isEmpty()) {
            return topRated(unseen, community);
        }
        return candidates.stream()
                .sorted(Comparator.comparingDouble(Scored::score)
                        .reversed()
                        .thenComparing(
                                scored -> scored.title(), byCommunity(community).thenComparing(byRecency())))
                .map(scored -> new Recommendation(scored.title(), reason(scored.title(), affinity)))
                .toList();
    }

    private static Map<UUID, Integer> signals(UserActivity activity) {
        Map<UUID, Integer> signals = new HashMap<>();
        activity.scores().forEach((titleId, score) -> {
            int signal = score >= LIKED_SCORE ? RATING_SIGNAL : score <= DISLIKED_SCORE ? -RATING_SIGNAL : 0;
            signals.merge(titleId, signal, Integer::sum);
        });
        activity.completed().forEach(titleId -> signals.merge(titleId, COMPLETED_SIGNAL, Integer::sum));
        return signals;
    }

    private static Map<UUID, Integer> affinity(Map<UUID, Integer> signals, Map<UUID, CatalogTitle> byId) {
        Map<UUID, Integer> affinity = new HashMap<>();
        signals.forEach((titleId, signal) -> {
            CatalogTitle title = byId.get(titleId);
            if (title != null && signal != 0) {
                title.genres().forEach(genre -> affinity.merge(genre.id(), signal, Integer::sum));
            }
        });
        return affinity;
    }

    private static ContentKind preferredFormat(Map<UUID, Integer> signals, Map<UUID, CatalogTitle> byId) {
        long movies = 0;
        long series = 0;
        for (Map.Entry<UUID, Integer> signal : signals.entrySet()) {
            CatalogTitle title = byId.get(signal.getKey());
            if (title != null && signal.getValue() > 0) {
                if (title.type() == ContentKind.MOVIE) {
                    movies++;
                } else {
                    series++;
                }
            }
        }
        return movies == series ? null : movies > series ? ContentKind.MOVIE : ContentKind.SERIES;
    }

    private static double score(CatalogTitle title, Map<UUID, Integer> affinity, ContentKind preferred) {
        double score = title.genres().stream()
                .mapToInt(genre -> affinity.getOrDefault(genre.id(), 0))
                .sum();
        return title.type() == preferred ? score + PREFERRED_FORMAT_BONUS : score;
    }

    private String reason(CatalogTitle title, Map<UUID, Integer> affinity) {
        List<String> liked = title.genres().stream()
                .filter(genre -> affinity.getOrDefault(genre.id(), 0) > 0)
                .sorted(Comparator.comparingInt((GenreRef genre) -> affinity.get(genre.id()))
                        .reversed()
                        .thenComparing(GenreRef::name, titleOrder))
                .limit(GENRES_IN_REASON)
                .map(GenreRef::name)
                .toList();
        return "Because you like " + String.join(" and ", liked);
    }

    private List<Recommendation> topRated(List<CatalogTitle> unseen, Map<UUID, CommunityRating> community) {
        return unseen.stream()
                .sorted(byCommunity(community).thenComparing(byRecency()))
                .map(title -> new Recommendation(
                        title, community.containsKey(title.id()) ? TOP_RATED_REASON : NEW_TITLE_REASON))
                .toList();
    }

    private static Comparator<CatalogTitle> byCommunity(Map<UUID, CommunityRating> community) {
        return Comparator.comparing(
                (CatalogTitle title) -> community.get(title.id()),
                Comparator.nullsLast(Comparator.comparingDouble(CommunityRating::average)
                        .thenComparingLong(CommunityRating::count)
                        .reversed()));
    }

    private Comparator<CatalogTitle> byRecency() {
        return Comparator.comparingInt(CatalogTitle::releaseYear)
                .reversed()
                .thenComparing(CatalogTitle::title, titleOrder)
                .thenComparing(CatalogTitle::id);
    }

    private record Scored(CatalogTitle title, double score) {}
}

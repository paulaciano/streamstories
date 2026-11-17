package com.amillioncodelines.streamstories.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.support.ApiClient.Session;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Creates catalog content through the API, as an administrator would. */
@TestComponent
public class CatalogApi {

    private final MockMvcTester mvc;

    CatalogApi(MockMvcTester mvc) {
        this.mvc = mvc;
    }

    /** A short random token to make titles and genre names unique, and to find them again with {@code q}. */
    public static String token() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static String movieJson(String title, int releaseYear, String... genreIds) {
        return """
                {"title": "%s", "synopsis": "A test movie.", "releaseYear": %d, "durationSeconds": 6000,
                 "videoUrl": "https://cdn.example.com/videos/%s.m3u8",
                 "coverImageUrl": "https://cdn.example.com/covers/%s.jpg", "genreIds": [%s]}
                """.formatted(title, releaseYear, token(), token(), quoted(genreIds));
    }

    public static String seriesJson(String title, int releaseYear, String... genreIds) {
        return """
                {"title": "%s", "synopsis": "A test series.", "releaseYear": %d, "genreIds": [%s]}
                """.formatted(title, releaseYear, quoted(genreIds));
    }

    public static String episodeJson(int number, String title) {
        return """
                {"number": %d, "title": "%s", "durationSeconds": 2700,
                 "videoUrl": "https://cdn.example.com/videos/%s.m3u8"}
                """.formatted(number, title, token());
    }

    public String createGenre(Session admin, String name) {
        return createdId(send(admin, "POST", "/api/v1/genres", """
                {"name": "%s"}
                """.formatted(name)));
    }

    public String createMovie(Session admin, String title, int releaseYear, String... genreIds) {
        return createdId(send(admin, "POST", "/api/v1/movies", movieJson(title, releaseYear, genreIds)));
    }

    public String createSeries(Session admin, String title, int releaseYear, String... genreIds) {
        return createdId(send(admin, "POST", "/api/v1/series", seriesJson(title, releaseYear, genreIds)));
    }

    public void addSeason(Session admin, String seriesId, int number) {
        assertThat(send(admin, "POST", "/api/v1/series/" + seriesId + "/seasons", """
                        {"number": %d}
                        """.formatted(number)))
                .hasStatus(HttpStatus.CREATED);
    }

    public String addEpisode(Session admin, String seriesId, int season, int number) {
        return createdId(send(
                admin,
                "POST",
                "/api/v1/series/%s/seasons/%d/episodes".formatted(seriesId, season),
                episodeJson(number, "Episode " + number)));
    }

    public MvcTestResult changeAvailability(Session admin, String uri, String availability) {
        return send(admin, "PATCH", uri, """
                {"availability": "%s"}
                """.formatted(availability));
    }

    public MvcTestResult get(Session session, String uri) {
        return mvc.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, session.bearer())
                .exchange();
    }

    public MvcTestResult delete(Session session, String uri) {
        return mvc.delete()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, session.bearer())
                .exchange();
    }

    public MvcTestResult send(Session session, String method, String uri, String json) {
        var request = switch (method) {
            case "POST" -> mvc.post();
            case "PUT" -> mvc.put();
            case "PATCH" -> mvc.patch();
            default -> throw new IllegalArgumentException(method);
        };
        return request.uri(uri)
                .header(HttpHeaders.AUTHORIZATION, session.bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }

    private static String createdId(MvcTestResult result) {
        assertThat(result).hasStatus(HttpStatus.CREATED);
        return ApiClient.read(result, "$.id");
    }

    private static String quoted(String... ids) {
        return Arrays.stream(ids).map(id -> "\"" + id + "\"").collect(Collectors.joining(", "));
    }
}

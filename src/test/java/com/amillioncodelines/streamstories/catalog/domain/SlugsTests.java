package com.amillioncodelines.streamstories.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SlugsTests {

    @ParameterizedTest
    @CsvSource({
        "Sci-Fi, sci-fi",
        "Ciencia Ficción, ciencia-ficcion",
        "'  Acción & Aventura  ', accion-aventura",
        "Drama, drama",
        "'---', ''"
    })
    void shouldBuildUrlFriendlySlugs(String name, String expected) {
        assertThat(Slugs.from(name)).isEqualTo(expected);
    }
}

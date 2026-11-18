package com.amillioncodelines.streamstories.activity.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class PlaybackPositionTests {

    @Test
    void shouldKeepTheExactPositionWhenItIsWithinTheDuration() {
        assertThat(PlaybackPosition.of(1260, 2700)).isEqualTo(new PlaybackPosition(1260, false));
    }

    @Test
    void shouldCapThePositionWhenItExceedsTheDuration() {
        assertThat(PlaybackPosition.of(9999, 2700)).isEqualTo(new PlaybackPosition(2700, true));
    }

    @Test
    void shouldBeCompletedWhenThePositionReachesNinetyPercent() {
        assertThat(PlaybackPosition.of(2429, 2700).completed()).isFalse();
        assertThat(PlaybackPosition.of(2430, 2700).completed()).isTrue();
    }

    @Test
    void shouldNotOverflowWhenTheDurationIsVeryLong() {
        assertThat(PlaybackPosition.of(Integer.MAX_VALUE, Integer.MAX_VALUE).completed())
                .isTrue();
    }

    @Test
    void shouldRejectNegativePositions() {
        assertThatIllegalArgumentException().isThrownBy(() -> PlaybackPosition.of(-1, 2700));
    }
}

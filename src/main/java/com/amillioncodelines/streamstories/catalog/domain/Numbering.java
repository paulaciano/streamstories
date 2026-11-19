package com.amillioncodelines.streamstories.catalog.domain;

/** Season and episode numbers go from 1 to {@link #MAX} (rules C4 and C5). */
public final class Numbering {

    public static final int MAX = 999;

    private Numbering() {}

    static void check(int number, String what) {
        if (number < 1 || number > MAX) {
            throw new IllegalArgumentException(what + " numbers go from 1 to " + MAX);
        }
    }
}

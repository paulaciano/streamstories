package com.amillioncodelines.streamstories.catalog.domain;

public final class Numbering {

    public static final int MAX = 999;

    private Numbering() {}

    static void check(int number, String what) {
        if (number < 1 || number > MAX) {
            throw new IllegalArgumentException(what + " numbers go from 1 to " + MAX);
        }
    }
}

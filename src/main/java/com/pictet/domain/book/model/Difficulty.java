package com.pictet.domain.book.model;

public enum Difficulty {
    EASY,
    MEDIUM,
    HARD;

    public static Difficulty lookup(String difficulty) {
        for (Difficulty d : Difficulty.values()) {
            if (d.name().equalsIgnoreCase(difficulty)) {
                return d;
            }
        }
        return null;
    }
}

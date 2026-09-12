package com.pictet.domain.book.model;

public enum Category {
    FICTION,
    SCIENCE,
    HORROR,
    ADVENTURE;

    public static Category lookup(String category) {
        for (Category c : Category.values()) {
            if (c.name().equalsIgnoreCase(category)) {
                return c;
            }
        }
        return null;
    }
}

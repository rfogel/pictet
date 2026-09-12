package com.pictet.domain.book.model;

import java.util.Set;

public interface BookList {
    String getId();
    String getTitle();
    String getAuthor();
    String getDifficulty();
    Set<Category> getCategories();
}

package com.pictet.domain.book.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "books")
@Builder(builderMethodName = "init", buildMethodName = "get")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Book {
    @Id
    public String id;
    public String title;
    public String author;
    public Difficulty difficulty;
    public Set<Section> sections;
    public Set<Category> categories;
}

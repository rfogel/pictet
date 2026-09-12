package com.pictet.domain.book.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Builder(builderMethodName = "init", buildMethodName = "get")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SearchCriteria {
    public String title;
    public String author;
    public String difficulty;
    public Set<String> categories;
    @Builder.Default
    private int offset = 0;
    @Builder.Default
    @Min(value = 1, message = "limit must be greater than 0")
    private int limit = 10;
}

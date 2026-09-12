package com.pictet.domain.book.dto;

import com.pictet.domain.book.model.Category;
import jakarta.validation.Valid;
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
public class BookUpdate {
    private Set<@Valid Category> categories;
}

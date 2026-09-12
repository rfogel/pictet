package com.pictet.domain.book.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class Section {
    public String id;
    public String text;
    public SectionType type;
    public Set<Option> options;
}

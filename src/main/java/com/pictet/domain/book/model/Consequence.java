package com.pictet.domain.book.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Consequence {
    public ConsequenceType type;
    public int value;
    public String text;
}

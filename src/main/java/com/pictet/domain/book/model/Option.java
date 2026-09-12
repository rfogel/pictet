package com.pictet.domain.book.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Option {
    public String description;
    public String gotoId;
    public Consequence consequence;
}

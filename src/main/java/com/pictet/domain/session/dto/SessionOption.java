package com.pictet.domain.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(builderMethodName = "init", buildMethodName = "get")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SessionOption {
    private String description;
    private int action;
}

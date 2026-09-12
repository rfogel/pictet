package com.pictet.domain.session.dto;

import jakarta.validation.constraints.NotNull;
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
public class NextAction {
    @NotNull
    private Integer action;
}

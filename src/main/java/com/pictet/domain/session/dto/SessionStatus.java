package com.pictet.domain.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Builder(builderMethodName = "init", buildMethodName = "get")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SessionStatus {

    private String sessionId;
    private String text;
    private List<SessionOption> options;
    private SessionProgress progress;
}

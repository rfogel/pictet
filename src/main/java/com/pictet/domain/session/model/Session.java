package com.pictet.domain.session.model;

import com.pictet.domain.session.dto.SessionOption;
import com.pictet.domain.session.dto.SessionProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "sessions")
@Builder(builderMethodName = "init", buildMethodName = "get")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Session {
    private String id;
    private String bookId;
    private String sectionId;
    private int healthPoints;
    private List<SessionOption> currentSessionOptions;
    private SessionProgress progress;
}

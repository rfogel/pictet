package com.pictet.domain.session.web;

import com.pictet.domain.session.dto.CreateSession;
import com.pictet.domain.session.dto.NextAction;
import com.pictet.domain.session.dto.SessionStatus;
import com.pictet.domain.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @RequestMapping(method = RequestMethod.POST, consumes = {"application/json;charset=utf-8"}, produces = {"application/json;charset=utf-8"})
    public ResponseEntity<SessionStatus> create(@Validated @RequestBody CreateSession createSession) {
        var sessionStatus = sessionService.create(createSession);
        return ResponseEntity.created(URI.create("/session/" + sessionStatus.getSessionId())).body(sessionStatus);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/next", consumes = {"application/json;charset=utf-8"}, produces = {"application/json;charset=utf-8"})
    public ResponseEntity<SessionStatus> next(@PathVariable("id") String id, @Validated @RequestBody NextAction nextAction) {
        return ResponseEntity.ok(sessionService.next(id, nextAction));
    }
}

package com.pictet.domain.session.service;

import com.pictet.common.exception.InvalidOptionException;
import com.pictet.common.exception.NotFoundException;
import com.pictet.domain.book.model.Book;
import com.pictet.domain.book.model.Consequence;
import com.pictet.domain.book.model.Option;
import com.pictet.domain.book.service.BookService;
import com.pictet.domain.session.dto.CreateSession;
import com.pictet.domain.session.dto.NextAction;
import com.pictet.domain.session.dto.SessionOption;
import com.pictet.domain.session.dto.SessionProgress;
import com.pictet.domain.session.dto.SessionStatus;
import com.pictet.domain.session.model.Session;
import com.pictet.domain.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.pictet.common.exception.ErrorMessage.BOOK_NOT_FOUND;
import static com.pictet.common.exception.ErrorMessage.INVALID_OPTION;
import static com.pictet.common.exception.ErrorMessage.SESSION_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    public static final int HEALTH_POINTS = 10;

    private final BookService bookService;
    private final SessionRepository sessionRepository;

    public SessionStatus create(CreateSession createSession) {

        var section = bookService.getStartingSection(createSession.getBookId()).orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND));

        List<SessionOption> options = getSessionOptions(section.getOptions());

        Session session = Session.init()
                .bookId(createSession.getBookId())
                .sectionId(section.getId())
                .healthPoints(HEALTH_POINTS)
                .currentSessionOptions(options)
                .progress(SessionProgress.IN_PROGRESS)
                .get();

        session = sessionRepository.save(session);

        return SessionStatus.init()
                .sessionId(session.getId())
                .text(section.getText())
                .options(options)
                .progress(SessionProgress.IN_PROGRESS)
                .get();
    }

    public SessionStatus next(String sessionId, NextAction nextAction) {

        final var session = sessionRepository.findById(sessionId).orElseThrow(() -> new NotFoundException(SESSION_NOT_FOUND));

        if (session.getProgress() != SessionProgress.IN_PROGRESS) {
            return SessionStatus.init()
                    .sessionId(session.getId())
                    .text("This session is over.")
                    .options(List.of())
                    .progress(session.getProgress())
                    .get();
        }

        final var book = bookService.findById(session.getBookId()).orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND));

        var optionSelected = getOptionSelected(session, book, nextAction);

        var consequence = handleConsequence(session, optionSelected);

        if (session.getHealthPoints() <= 0) {
            log.info("Session {} has lost all health points. Marking session as failed.", session.getId());
            session.setProgress(SessionProgress.FAILED);
            sessionRepository.save(session);
            return SessionStatus.init()
                    .sessionId(session.getId())
                    .text("You have lost all your health points. Game over.")
                    .options(List.of())
                    .progress(SessionProgress.FAILED)
                    .get();
        }

        var nextSection = book.getSections().stream()
                .filter(sec -> sec.getId().equals(optionSelected.getGotoId()))
                .findFirst()
                .get();

        if (nextSection.getOptions() == null || nextSection.getOptions().isEmpty()) {
            log.info("Session {} has reached the end. Marking session as completed.", session.getId());
            session.setProgress(SessionProgress.COMPLETED);
            sessionRepository.save(session);
            return SessionStatus.init()
                    .sessionId(session.getId())
                    .text(nextSection.getText())
                    .options(List.of())
                    .progress(SessionProgress.COMPLETED)
                    .get();
        }

        List<SessionOption> options = getSessionOptions(nextSection.getOptions());

        session.setSectionId(nextSection.getId());
        session.setCurrentSessionOptions(options);
        sessionRepository.save(session);

        return SessionStatus.init()
                .sessionId(session.getId())
                .text(nextSection.getText())
                .options(options)
                .progress(SessionProgress.IN_PROGRESS)
                .lastActionConsequence(consequence.orElse(null))
                .get();
    }

    Option getOptionSelected(Session session, Book book, NextAction nextAction) {
        return session.getCurrentSessionOptions().stream()
                .filter(option -> option.getAction() == nextAction.getAction())
                .findFirst()
                .map(option -> book.getSections().stream()
                        .filter(sec -> sec.getId().equals(session.getSectionId()))
                        .findFirst()
                        .get()
                        .getOptions()
                        .stream()
                        .filter(opt -> opt.getDescription().equals(option.getDescription()))
                        .findFirst()
                        .get())
                .orElseThrow(() -> new InvalidOptionException(INVALID_OPTION));
    }

    Optional<Consequence> handleConsequence(Session session, Option option) {
        if (option.getConsequence() != null) {
            switch (option.getConsequence().getType()) {
                case LOSE_HEALTH -> {
                    session.setHealthPoints(session.getHealthPoints() - option.getConsequence().getValue());
                    log.info("Session {} lost {} health points. Current health points: {}", session.getId(), option.getConsequence().getValue(), session.getHealthPoints());
                }
                case GAIN_HEALTH -> {
                    session.setHealthPoints(session.getHealthPoints() + option.getConsequence().getValue());
                    log.info("Session {} gained {} health points. Current health points: {}", session.getId(), option.getConsequence().getValue(), session.getHealthPoints());
                }
                default -> throw new IllegalStateException("Unexpected value: " + option.getConsequence());
            }
        }
        return Optional.ofNullable(option.getConsequence());
    }

    List<SessionOption> getSessionOptions(Set<Option> options) {
        int actionNumber = 0;
        List<SessionOption> sessionOptions = new ArrayList<>(options.size());
        for (var option : options) {
            sessionOptions.add(new SessionOption(option.getDescription(), actionNumber++));
        }
        return sessionOptions;
    }
}

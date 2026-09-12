package com.pictet.domain.session;

import com.pictet.common.BaseTest;
import com.pictet.common.CommonUtils;
import com.pictet.common.exception.InvalidOptionException;
import com.pictet.common.exception.NotFoundException;
import com.pictet.domain.book.model.Book;
import com.pictet.domain.book.repository.BookRepository;
import com.pictet.domain.book.service.BookService;
import com.pictet.domain.session.dto.CreateSession;
import com.pictet.domain.session.dto.NextAction;
import com.pictet.domain.session.dto.SessionProgress;
import com.pictet.domain.session.dto.SessionStatus;
import com.pictet.domain.session.repository.SessionRepository;
import com.pictet.domain.session.service.SessionService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(BaseTest.InMemoryDatabase.class)
@DataMongoTest
public class SessionServiceTest extends BaseTest {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private SessionRepository sessionRepository;

    @BeforeAll
    public void setup() {
        generator = CommonUtils.generator();
        parser = CommonUtils.objectMapper();
    }

    void cleanup() {
        bookRepository.deleteAll();
        sessionRepository.deleteAll();
    }

    Book saveAndGetSampleBook(String sampleFileName) throws Exception {
        var book = parser.readValue(new ClassPathResource(sampleFileName).getFile(), Book.class);
        return bookRepository.save(book);
    }

    @Nested
    @DisplayName("Tests for create()")
    class CreateTests {

        @AfterEach
        public void afterEach() {
            cleanup();
        }

        @SneakyThrows
        @Test
        @DisplayName("given a book, should start a session")
        void test1() {

            var book = saveAndGetSampleBook("sample/book.json");

            var sessionService = new SessionService(new BookService(bookRepository), sessionRepository);

            var sessionStatus = sessionService.create(new CreateSession(book.getId()));

            assertAll(
                    () -> assertNotNull(sessionStatus.getSessionId()),
                    () -> assertNotNull(sessionStatus.getText()),
                    () -> assertNotNull(sessionStatus.getOptions()),
                    () -> sessionRepository.findById(sessionStatus.getSessionId()).ifPresent(session -> {
                        assertEquals(book.getId(), session.getBookId());
                        assertEquals(10, session.getHealthPoints());
                        assertNotNull(session.getCurrentSessionOptions());
                    })
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given a non existent book, should throw an exception")
        void test2() {

            var sessionService = new SessionService(new BookService(bookRepository), sessionRepository);

            var exception = assertThrows(NotFoundException.class, () -> sessionService.create(new CreateSession("non-existent-book-id")));

            assertAll(
                    () -> assertNotNull(exception.getMessage()),
                    () -> assertTrue(exception.getMessage().contains("Book not found"))
            );
        }
    }

    @Nested
    @DisplayName("Tests for next()")
    class NextTests {

        @AfterEach
        public void afterEach() {
            cleanup();
        }

        @SneakyThrows
        @Test
        @DisplayName("given a session, should proceed to the next action")
        void test1() {

            var book = saveAndGetSampleBook("sample/book.json");
            var sessionService = new SessionService(new BookService(bookRepository), sessionRepository);
            var sessionStatus = sessionService.create(new CreateSession(book.getId()));

            var nextAction = sessionStatus.getOptions().get(0).getAction();
            var nextSessionStatus = sessionService.next(sessionStatus.getSessionId(), new NextAction(nextAction));

            assertAll(
                    () -> assertNotNull(nextSessionStatus.getSessionId()),
                    () -> assertNotNull(nextSessionStatus.getText()),
                    () -> assertNotNull(nextSessionStatus.getOptions())
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given a non existent session, should throw an exception")
        void test2() {

            var sessionService = new SessionService(new BookService(bookRepository), sessionRepository);
            var notFoundException = assertThrows(NotFoundException.class, () -> sessionService.next("non-existent-session-id", new NextAction(0)));

            assertAll(
                    () -> assertNotNull(notFoundException.getMessage()),
                    () -> assertTrue(notFoundException.getMessage().contains("Session not found"))
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given a session but a retired book, should throw an exception")
        void test3() {

            var book = saveAndGetSampleBook("sample/book.json");
            var sessionService = new SessionService(new BookService(bookRepository), sessionRepository);
            var sessionStatus = sessionService.create(new CreateSession(book.getId()));

            bookRepository.deleteById(book.getId());

            var notFoundException = assertThrows(NotFoundException.class, () -> sessionService.next(sessionStatus.getSessionId(), new NextAction(0)));

            assertAll(
                    () -> assertNotNull(notFoundException.getMessage()),
                    () -> assertTrue(notFoundException.getMessage().contains("Book not found"))
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given a session and a invalid option, should throw an exception")
        void test4() {

            var book = saveAndGetSampleBook("sample/book.json");
            var sessionService = new SessionService(new BookService(bookRepository), sessionRepository);
            var sessionStatus = sessionService.create(new CreateSession(book.getId()));

            var notFoundException = assertThrows(InvalidOptionException.class, () -> sessionService.next(sessionStatus.getSessionId(), new NextAction(999)));

            assertAll(
                    () -> assertNotNull(notFoundException.getMessage()),
                    () -> assertTrue(notFoundException.getMessage().contains("Invalid option"))
            );
        }

        int getNextActionByDescription(SessionStatus sessionStatus, String description) {
            return sessionStatus
                    .getOptions()
                    .stream()
                    .filter(opt -> opt.getDescription().equalsIgnoreCase(description))
                    .findFirst()
                    .get()
                    .getAction();
        }

        @SneakyThrows
        @Test
        @DisplayName("given a session, should proceed to the end")
        void test5() {

            var book = saveAndGetSampleBook("sample/book.json");
            var sessionService = new SessionService(new BookService(bookRepository), sessionRepository);
            var sessionStatus = sessionService.create(new CreateSession(book.getId()));

            var nextAction = getNextActionByDescription(sessionStatus, "You look under the bed");
            var nextSessionStatus1 = sessionService.next(sessionStatus.getSessionId(), new NextAction(nextAction));

            nextAction = getNextActionByDescription(nextSessionStatus1, "Try to scan the area with your hands");
            var nextSessionStatus2 = sessionService.next(sessionStatus.getSessionId(), new NextAction(nextAction));

            nextAction = getNextActionByDescription(nextSessionStatus2, "Try to open the door with the key");
            var nextSessionStatus3 = sessionService.next(sessionStatus.getSessionId(), new NextAction(nextAction));

            assertAll(
                    () -> assertEquals("You wake up in what seems to be a dark prison cell, on an old wooden bed. Metal bars are preventing you to escape from the room. There is no window.", sessionStatus.getText()),
                    () -> assertEquals(2, sessionStatus.getOptions().size()),
                    () -> assertEquals(SessionProgress.IN_PROGRESS, sessionStatus.getProgress()),

                    () -> assertEquals(sessionStatus.getSessionId(), nextSessionStatus1.getSessionId()),
                    () -> assertEquals("You don't see anything, it's too dark.", nextSessionStatus1.getText()),
                    () -> assertEquals(1, nextSessionStatus1.getOptions().size()),
                    () -> assertEquals(SessionProgress.IN_PROGRESS, nextSessionStatus1.getProgress()),

                    () -> assertEquals(sessionStatus.getSessionId(), nextSessionStatus2.getSessionId()),
                    () -> assertEquals("You found what seems to be a door key.", nextSessionStatus2.getText()),
                    () -> assertEquals(1, nextSessionStatus2.getOptions().size()),
                    () -> assertEquals(SessionProgress.IN_PROGRESS, nextSessionStatus2.getProgress()),

                    () -> assertEquals(sessionStatus.getSessionId(), nextSessionStatus3.getSessionId()),
                    () -> assertEquals("The door opens. You are now free...", nextSessionStatus3.getText()),
                    () -> assertTrue(nextSessionStatus3.getOptions() == null || nextSessionStatus3.getOptions().isEmpty()),
                    () -> assertEquals(SessionProgress.COMPLETED, nextSessionStatus3.getProgress()),

                    () -> sessionRepository.findById(sessionStatus.getSessionId()).ifPresent(session -> {
                        assertEquals(SessionProgress.COMPLETED, session.getProgress());
                    })
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given a session, should handle consequences of actions and proceed to the end")
        void test6() {

            var book = saveAndGetSampleBook("sample/book.json");
            var sessionService = new SessionService(new BookService(bookRepository), sessionRepository);
            var sessionStatus = sessionService.create(new CreateSession(book.getId()));

            var nextAction = getNextActionByDescription(sessionStatus, "You look under the bed");
            var nextSessionStatus = sessionService.next(sessionStatus.getSessionId(), new NextAction(nextAction));

            nextAction = getNextActionByDescription(nextSessionStatus, "Try to scan the area with your hands");
            var nextSessionStatusWithConsequence = sessionService.next(sessionStatus.getSessionId(), new NextAction(nextAction));

            nextAction = getNextActionByDescription(nextSessionStatusWithConsequence, "Try to open the door with the key");
            var finalSessionStatus = sessionService.next(sessionStatus.getSessionId(), new NextAction(nextAction));

            assertAll(
                    () -> assertNull(nextSessionStatus.getLastActionConsequence()),
                    () -> assertNotNull(nextSessionStatusWithConsequence.getLastActionConsequence()),
                    () -> assertEquals("As you move your hands left and right under the bed, you cut yourself on a rusty nail.", nextSessionStatusWithConsequence.getLastActionConsequence().getText()),
                    () -> assertEquals(SessionProgress.COMPLETED, finalSessionStatus.getProgress()),
                    () -> sessionRepository.findById(sessionStatus.getSessionId()).ifPresent(session -> {
                        assertEquals(4, session.getHealthPoints());
                        assertEquals(SessionProgress.COMPLETED, session.getProgress());
                    })
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given a session, should handle consequences of actions and die before reaching the end")
        void test7() {

            var book = saveAndGetSampleBook("sample/book.json");
            var sessionService = new SessionService(new BookService(bookRepository), sessionRepository);
            var sessionStatus = sessionService.create(new CreateSession(book.getId()));

            var nextAction = getNextActionByDescription(sessionStatus, "You try to open the door");
            var nextSessionStatus = sessionService.next(sessionStatus.getSessionId(), new NextAction(nextAction));

            nextAction = getNextActionByDescription(nextSessionStatus, "Gather your thoughts");
            nextSessionStatus = sessionService.next(sessionStatus.getSessionId(), new NextAction(nextAction));

            nextAction = getNextActionByDescription(nextSessionStatus, "You try to open the door");
            nextSessionStatus = sessionService.next(sessionStatus.getSessionId(), new NextAction(nextAction));

            nextAction = getNextActionByDescription(nextSessionStatus, "Gather your thoughts");
            var finalSessionStatus = sessionService.next(sessionStatus.getSessionId(), new NextAction(nextAction));

            assertAll(
                    () -> assertEquals(SessionProgress.FAILED, finalSessionStatus.getProgress()),
                    () -> sessionRepository.findById(sessionStatus.getSessionId()).ifPresent(session -> {
                        assertTrue(session.getHealthPoints() <= 0);
                        assertEquals(SessionProgress.FAILED, session.getProgress());
                    })
            );
        }

        static List<SessionProgress> finishedSessionProgress = List.of(SessionProgress.COMPLETED, SessionProgress.FAILED);

        @SneakyThrows
        @ParameterizedTest
        @FieldSource("finishedSessionProgress")
        @DisplayName("given a finished session, should return a message indicating the session is over")
        void test8(SessionProgress progress) {

            var book = saveAndGetSampleBook("sample/book.json");
            var sessionService = new SessionService(new BookService(bookRepository), sessionRepository);
            var sessionStatus = sessionService.create(new CreateSession(book.getId()));

            sessionRepository.findById(sessionStatus.getSessionId()).ifPresent(session -> {
                session.setProgress(progress);
                sessionRepository.save(session);
            });

            var nextSessionStatus = sessionService.next(sessionStatus.getSessionId(), new NextAction(0));

            assertAll(
                    () -> assertEquals(sessionStatus.getSessionId(), nextSessionStatus.getSessionId()),
                    () -> assertEquals("This session is over.", nextSessionStatus.getText()),
                    () -> assertTrue(nextSessionStatus.getOptions() == null || nextSessionStatus.getOptions().isEmpty()),
                    () -> assertEquals(progress, nextSessionStatus.getProgress())
            );
        }
    }

}

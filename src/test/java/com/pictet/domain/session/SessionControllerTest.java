package com.pictet.domain.session;

import com.pictet.common.BaseTest;
import com.pictet.common.CommonUtils;
import com.pictet.common.exception.BusinessException;
import com.pictet.common.exception.GlobalExceptionHandler;
import com.pictet.common.exception.InvalidOptionException;
import com.pictet.common.exception.NotFoundException;
import com.pictet.domain.session.dto.CreateSession;
import com.pictet.domain.session.dto.NextAction;
import com.pictet.domain.session.dto.SessionStatus;
import com.pictet.domain.session.service.SessionService;
import com.pictet.domain.session.web.SessionController;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest(value = {SessionController.class, GlobalExceptionHandler.class})
public class SessionControllerTest extends BaseTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SessionService sessionService;

    @BeforeAll
    public void setup() {
        generator = CommonUtils.generator();
        parser = CommonUtils.objectMapper();
    }

    @Nested
    @DisplayName("Tests for create()")
    class CreateTests {

        @SneakyThrows
        @Test
        @DisplayName("given a request, should return 200")
        void test1() {

            var sessionStatus = generator.nextObject(SessionStatus.class);

            doReturn(sessionStatus).when(sessionService).create(any());

            MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/session")
                            .content(parser.writeValueAsString(new CreateSession("123")))
                            .contentType(APPLICATION_JSON))
                    .andExpectAll(status().isCreated()).andReturn();

            var response = parser.readValue(result.getResponse().getContentAsString(), SessionStatus.class);

            assertAll(
                    () -> assertEquals(sessionStatus.getSessionId(), response.getSessionId()),
                    () -> assertEquals(sessionStatus.getText(), response.getText()),
                    () -> assertEquals(sessionStatus.getOptions().size(), response.getOptions().size())
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given a request and an error occur, should return 500")
        void test2() {

            when(sessionService.create(any())).thenThrow(new BusinessException("something went wrong"));

            MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post("/session")
                            .content(parser.writeValueAsString(new CreateSession("123")))
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isInternalServerError()).andReturn();

            ProblemDetail apiErrorResponse = parser.readValue(mvcResult.getResponse().getContentAsString(), ProblemDetail.class);

            assertAll(
                    () -> Assertions.assertEquals("something went wrong", apiErrorResponse.getDetail()),
                    () -> Assertions.assertEquals(500, apiErrorResponse.getStatus())
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given a request and a not found exception occur, should return 404")
        void test3() {

            when(sessionService.create(any())).thenThrow(new NotFoundException("book not found"));

            MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post("/session")
                            .content(parser.writeValueAsString(new CreateSession("123")))
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isNotFound()).andReturn();

            ProblemDetail apiErrorResponse = parser.readValue(mvcResult.getResponse().getContentAsString(), ProblemDetail.class);

            assertAll(
                    () -> Assertions.assertEquals("book not found", apiErrorResponse.getDetail()),
                    () -> Assertions.assertEquals(404, apiErrorResponse.getStatus())
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given a request without a valid input, should return 400")
        void test4() {

            when(sessionService.create(any())).thenThrow(new NotFoundException("book not found"));

            MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post("/session")
                            .content(parser.writeValueAsString(new CreateSession(null)))
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isBadRequest()).andReturn();

            ProblemDetail apiErrorResponse = parser.readValue(mvcResult.getResponse().getContentAsString(), ProblemDetail.class);

            assertAll(
                    () -> Assertions.assertEquals("createSession.bookId: must not be null", apiErrorResponse.getDetail()),
                    () -> Assertions.assertEquals(400, apiErrorResponse.getStatus())
            );
        }

    }

    @Nested
    @DisplayName("Tests for next()")
    class NextTests {

        @SneakyThrows
        @Test
        @DisplayName("given an existent session, should return 200")
        void test1() {

            var sessionStatus = generator.nextObject(SessionStatus.class);

            doReturn(sessionStatus).when(sessionService).next(any(), any());

            MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/session/123456789/next")
                            .content(parser.writeValueAsString(new NextAction(1)))
                            .contentType(APPLICATION_JSON))
                    .andExpectAll(status().isOk()).andReturn();

            SessionStatus apiResponse = parser.readValue(result.getResponse().getContentAsString(), SessionStatus.class);

            assertAll(
                    () -> assertEquals(sessionStatus.getSessionId(), apiResponse.getSessionId()),
                    () -> assertEquals(sessionStatus.getText(), apiResponse.getText()),
                    () -> assertEquals(sessionStatus.getProgress(), apiResponse.getProgress())
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given a no existent session, should return 404")
        void test2() {

            when(sessionService.next(any(), any())).thenThrow(new NotFoundException("session not found"));

            MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/session/123456789/next")
                            .content(parser.writeValueAsString(new NextAction(1)))
                            .contentType(APPLICATION_JSON))
                    .andExpectAll(status().isNotFound()).andReturn();

            ProblemDetail apiErrorResponse = parser.readValue(result.getResponse().getContentAsString(), ProblemDetail.class);

            assertAll(
                    () -> Assertions.assertEquals("session not found", apiErrorResponse.getDetail()),
                    () -> Assertions.assertEquals(404, apiErrorResponse.getStatus())
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given a no existent book, should return 404")
        void test3() {

            when(sessionService.next(any(), any())).thenThrow(new NotFoundException("book not found"));

            MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/session/123456789/next")
                            .content(parser.writeValueAsString(new NextAction(1)))
                            .contentType(APPLICATION_JSON))
                    .andExpectAll(status().isNotFound()).andReturn();

            ProblemDetail apiErrorResponse = parser.readValue(result.getResponse().getContentAsString(), ProblemDetail.class);

            assertAll(
                    () -> Assertions.assertEquals("book not found", apiErrorResponse.getDetail()),
                    () -> Assertions.assertEquals(404, apiErrorResponse.getStatus())
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given an invalid option, should return 400")
        void test4() {

            when(sessionService.next(any(), any())).thenThrow(new InvalidOptionException("invalid option"));

            MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/session/123456789/next")
                            .content(parser.writeValueAsString(new NextAction(1)))
                            .contentType(APPLICATION_JSON))
                    .andExpectAll(status().isBadRequest()).andReturn();

            ProblemDetail apiErrorResponse = parser.readValue(result.getResponse().getContentAsString(), ProblemDetail.class);

            assertAll(
                    () -> Assertions.assertEquals("invalid option", apiErrorResponse.getDetail()),
                    () -> Assertions.assertEquals(400, apiErrorResponse.getStatus())
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given an request without a valid input, should return 400")
        void test5() {

            MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/session/123456789/next")
                            .content(parser.writeValueAsString(new NextAction()))
                            .contentType(APPLICATION_JSON))
                    .andExpectAll(status().isBadRequest()).andReturn();

            ProblemDetail apiErrorResponse = parser.readValue(result.getResponse().getContentAsString(), ProblemDetail.class);

            assertAll(
                    () -> Assertions.assertEquals("nextAction.action: must not be null", apiErrorResponse.getDetail()),
                    () -> Assertions.assertEquals(400, apiErrorResponse.getStatus())
            );
        }
    }
}

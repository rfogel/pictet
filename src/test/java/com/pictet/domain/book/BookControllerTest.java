package com.pictet.domain.book;

import com.pictet.common.BaseTest;
import com.pictet.common.CommonUtils;
import com.pictet.common.PageableRestResponse;
import com.pictet.common.exception.BusinessException;
import com.pictet.common.exception.GlobalExceptionHandler;
import com.pictet.common.exception.InvalidBookException;
import com.pictet.domain.book.dto.BookUpdate;
import com.pictet.domain.book.model.Book;
import com.pictet.domain.book.service.BookService;
import com.pictet.domain.book.web.BookController;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest(value = {BookController.class, GlobalExceptionHandler.class})
public class BookControllerTest extends BaseTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BookService bookService;

    @BeforeAll
    public void setup() {
        generator = CommonUtils.generator();
        parser = CommonUtils.objectMapper();
    }

    @Nested
    @DisplayName("Tests for findAll()")
    class FindAllTests {

        @SneakyThrows
        @Test
        @DisplayName("given a request, should return 200")
        void test1() {

            Page<?> response = new PageImpl<>(generator.objects(Book.class, 3).collect(Collectors.toList()), Pageable.ofSize(3), 3);

            doReturn(response).when(bookService).findAll(any());

            MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/book")
                            .contentType(APPLICATION_JSON))
                    .andExpectAll(status().isOk()).andReturn();

            PageableRestResponse<?> apiResponse = parser.readValue(result.getResponse().getContentAsString(), PageableRestResponse.class);

            assertAll(
                    () -> assertEquals(3, apiResponse.getTotalElements()),
                    () -> assertEquals(1, apiResponse.getTotalPages()),
                    () -> assertEquals(3, apiResponse.getSize())
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given a request and an error occur, should return 500")
        void test2() {

            when(bookService.findAll(any())).thenThrow(new BusinessException("something went wrong"));

            MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get("/book")
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
        @DisplayName("given a request with limit zero, should return 400")
        void test3() {

            MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get("/book?limit=0")
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isBadRequest()).andReturn();

            ProblemDetail apiErrorResponse = parser.readValue(mvcResult.getResponse().getContentAsString(), ProblemDetail.class);

            assertAll(
                    () -> Assertions.assertEquals("searchCriteria.limit: limit must be greater than 0", apiErrorResponse.getDetail()),
                    () -> Assertions.assertEquals(400, apiErrorResponse.getStatus())
            );
        }
    }

    @Nested
    @DisplayName("Tests for findById()")
    class FindByIdTests {

        @SneakyThrows
        @Test
        @DisplayName("given a existent book, should return 200")
        void test1() {

            var response = generator.nextObject(Book.class);

            doReturn(Optional.of(response)).when(bookService).findById(any());

            MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/book/" + response.getId())
                            .contentType(APPLICATION_JSON))
                    .andExpectAll(status().isOk()).andReturn();

            Book apiResponse = parser.readValue(result.getResponse().getContentAsString(), Book.class);

            assertAll(
                    () -> assertEquals(response.getId(), apiResponse.getId()),
                    () -> assertEquals(response.getTitle(), apiResponse.getTitle()),
                    () -> assertEquals(response.getAuthor(), apiResponse.getAuthor()),
                    () -> assertEquals(response.getDifficulty(), apiResponse.getDifficulty()),
                    () -> assertEquals(response.getCategories(), apiResponse.getCategories()),
                    () -> assertEquals(response.getSections().size(), apiResponse.getSections().size())
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given a no existent book, should return 404")
        void test2() {

            doReturn(Optional.empty()).when(bookService).findById(any());

            MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/book/123456789")
                            .contentType(APPLICATION_JSON))
                    .andExpectAll(status().isNotFound()).andReturn();

            ProblemDetail apiErrorResponse = parser.readValue(result.getResponse().getContentAsString(), ProblemDetail.class);

            assertAll(
                    () -> Assertions.assertEquals("Book not found", apiErrorResponse.getDetail()),
                    () -> Assertions.assertEquals(404, apiErrorResponse.getStatus())
            );
        }
    }

    @Nested
    @DisplayName("Tests for update()")
    class UpdateTests {

        @SneakyThrows
        @Test
        @DisplayName("given a existent book, should return 200")
        void test1() {

            var response = generator.nextObject(Book.class);

            doReturn(Optional.of(response)).when(bookService).update(any(), any());

            MvcResult result = mvc.perform(MockMvcRequestBuilders.patch("/book/" + response.getId())
                            .contentType(APPLICATION_JSON)
                            .content(parser.writeValueAsString(new BookUpdate())))
                    .andExpectAll(status().isOk()).andReturn();

            Book apiResponse = parser.readValue(result.getResponse().getContentAsString(), Book.class);

            assertAll(
                    () -> assertEquals(response.getId(), apiResponse.getId()),
                    () -> assertEquals(response.getTitle(), apiResponse.getTitle()),
                    () -> assertEquals(response.getAuthor(), apiResponse.getAuthor()),
                    () -> assertEquals(response.getDifficulty(), apiResponse.getDifficulty()),
                    () -> assertEquals(response.getCategories(), apiResponse.getCategories()),
                    () -> assertEquals(response.getSections().size(), apiResponse.getSections().size())
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given a non existent book, should return 404")
        void test2() {

            doReturn(Optional.empty()).when(bookService).update(any(), any());

            MvcResult result = mvc.perform(MockMvcRequestBuilders.patch("/book/123456789")
                            .contentType(APPLICATION_JSON)
                            .content(parser.writeValueAsString(new BookUpdate())))
                    .andExpectAll(status().isNotFound()).andReturn();

            ProblemDetail apiErrorResponse = parser.readValue(result.getResponse().getContentAsString(), ProblemDetail.class);

            assertAll(
                    () -> Assertions.assertEquals("Book not found", apiErrorResponse.getDetail()),
                    () -> Assertions.assertEquals(404, apiErrorResponse.getStatus())
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given an invalid value, should return 400")
        void test3() {

            MvcResult result = mvc.perform(MockMvcRequestBuilders.patch("/book/123456789")
                            .contentType(APPLICATION_JSON)
                            .content("{\"categories\": [\"INVALID\"]}"))
                    .andExpectAll(status().isBadRequest()).andReturn();

            ProblemDetail apiErrorResponse = parser.readValue(result.getResponse().getContentAsString(), ProblemDetail.class);

            assertAll(
                    () -> Assertions.assertEquals("Invalid values", apiErrorResponse.getDetail()),
                    () -> Assertions.assertEquals(400, apiErrorResponse.getStatus())
            );
        }
    }

    @Nested
    @DisplayName("Tests for save()")
    class SaveTests {

        @SneakyThrows
        @Test
        @DisplayName("given a book, should return 201")
        void test1() {

            var response = generator.nextObject(Book.class);

            doReturn(response).when(bookService).save(any());

            MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/book")
                            .contentType(APPLICATION_JSON)
                            .content(parser.writeValueAsString(new Book())))
                    .andExpectAll(status().isCreated()).andReturn();

            Book apiResponse = parser.readValue(result.getResponse().getContentAsString(), Book.class);

            assertAll(
                    () -> assertEquals(response.getId(), apiResponse.getId()),
                    () -> assertEquals(response.getTitle(), apiResponse.getTitle()),
                    () -> assertEquals(response.getAuthor(), apiResponse.getAuthor()),
                    () -> assertEquals(response.getDifficulty(), apiResponse.getDifficulty()),
                    () -> assertEquals(response.getCategories(), apiResponse.getCategories()),
                    () -> assertEquals(response.getSections().size(), apiResponse.getSections().size())
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given a invalid book, should return 400")
        void test2() {

            doThrow(new InvalidBookException("invalid book")).when(bookService).save(any());

            MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/book")
                            .contentType(APPLICATION_JSON)
                            .content(parser.writeValueAsString(new Book())))
                    .andExpectAll(status().isBadRequest()).andReturn();

            ProblemDetail apiResponse = parser.readValue(result.getResponse().getContentAsString(), ProblemDetail.class);

            assertAll(
                    () -> assertEquals("invalid book", apiResponse.getDetail()),
                    () -> assertEquals(400, apiResponse.getStatus())
            );
        }
    }
}

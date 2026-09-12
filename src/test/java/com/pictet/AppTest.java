package com.pictet;

import com.pictet.common.BaseTest;
import com.pictet.domain.book.service.BookService;
import com.pictet.domain.session.service.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(BaseTest.InMemoryDatabase.class)
public class AppTest {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BookService bookService;

    @Test
    public void contextLoads() {
        assertNotNull(sessionService, "SessionService should be autowired");
        assertNotNull(bookService, "BookService should be autowired");
    }
}

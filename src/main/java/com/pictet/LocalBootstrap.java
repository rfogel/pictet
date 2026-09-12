package com.pictet;

import com.pictet.common.exception.InvalidBookException;
import com.pictet.domain.book.model.Book;
import com.pictet.domain.book.repository.BookRepository;
import com.pictet.domain.book.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Profile("local")
@Component
@Slf4j
public class LocalBootstrap {

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    @Profile("local")
    ApplicationRunner bootstrap(BookService bookService, BookRepository bookRepository, ObjectMapper objectMapper) {
        return _ -> {
            log.info("Bootstrapping local data");
            bookRepository.deleteAll();
            try (Stream<Path> files = Files.list(Path.of("snapshot"))) {
                AtomicInteger count = new AtomicInteger();
                files.forEach(filePath -> {
                    try {
                        String jsonContent = Files.readString(filePath);
                        var book = objectMapper.readValue(jsonContent, Book.class);
                        bookService.save(book);
                        count.incrementAndGet();
                    } catch (InvalidBookException e) {
                        log.error("Invalid book data in file: {}", filePath.getFileName(), e);
                    } catch (Exception e) {
                        log.error("Error reading data from file: {}", filePath.getFileName(), e);
                    }
                });
                log.info("Found {} files in snapshot directory", count.get());
            }
        };
    }
}

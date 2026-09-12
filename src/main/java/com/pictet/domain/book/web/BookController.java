package com.pictet.domain.book.web;

import com.pictet.common.exception.NotFoundException;
import com.pictet.domain.book.dto.BookUpdate;
import com.pictet.domain.book.dto.SearchCriteria;
import com.pictet.domain.book.model.Book;
import com.pictet.domain.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import static com.pictet.common.exception.ErrorMessage.BOOK_NOT_FOUND;

@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @RequestMapping(method = RequestMethod.GET, produces = {"application/json;charset=utf-8"})
    public ResponseEntity<Page<Book>> findAll(@Validated SearchCriteria searchCriteria) {
        return ResponseEntity.ok(bookService.findAll(searchCriteria));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = {"application/json;charset=utf-8"})
    public ResponseEntity<Book> findById(@PathVariable("id") String id) {
        var book = bookService.findById(id).orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND));
        return ResponseEntity.ok(book);
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}", consumes = {"application/json;charset=utf-8"}, produces = {"application/json;charset=utf-8"})
    public ResponseEntity<Book> update(@PathVariable("id") String id, @Validated @RequestBody BookUpdate bookUpdate) {
        var book = bookService.update(id, bookUpdate).orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND));
        return ResponseEntity.ok(book);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = {"application/json;charset=utf-8"}, produces = {"application/json;charset=utf-8"})
    public ResponseEntity<Book> save(@Validated @RequestBody Book book) {
        var savedBook = bookService.save(book);
        return ResponseEntity.created(URI.create("/book/" + savedBook.getId())).body(savedBook);
    }
}

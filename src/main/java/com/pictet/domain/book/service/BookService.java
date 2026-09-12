package com.pictet.domain.book.service;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.pictet.common.component.OffsetBasedPageRequest;
import com.pictet.common.exception.InvalidBookException;
import com.pictet.domain.book.dto.BookUpdate;
import com.pictet.domain.book.dto.SearchCriteria;
import com.pictet.domain.book.model.Book;
import com.pictet.domain.book.model.Category;
import com.pictet.domain.book.model.Difficulty;
import com.pictet.domain.book.model.Section;
import com.pictet.domain.book.model.SectionType;
import com.pictet.domain.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.pictet.common.exception.ErrorMessage.BOOK_MUST_HAVE_AT_LEAST_ONE_ENDING_SECTION;
import static com.pictet.common.exception.ErrorMessage.BOOK_MUST_HAVE_ONLY_ONE_STARTING_SECTION;
import static com.pictet.common.exception.ErrorMessage.BOOK_SECTIONS_CANNOT_BE_NULL_OR_EMPTY;
import static com.pictet.common.exception.ErrorMessage.BOOK_TITLE_CANNOT_BE_NULL_OR_BLANK;
import static com.pictet.common.exception.ErrorMessage.NON_ENDING_SECTIONS_MUST_HAVE_OPTIONS;
import static com.pictet.common.exception.ErrorMessage.getInvalidOptionMessage;
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.startsWith;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;

    private final List<String> defaultProjection = List.of("id", "title", "author", "difficulty", "categories");

    public Page<Book> findAll(SearchCriteria searchCriteria) {

        Set<Category> categories = new HashSet<>(0);
        if (searchCriteria.getCategories() != null && !searchCriteria.getCategories().isEmpty()) {
            categories = searchCriteria.getCategories().stream().map(Category::lookup).collect(Collectors.toSet());
        }

        Book book = Book.init()
                .title(searchCriteria.getTitle())
                .author(searchCriteria.getAuthor())
                .difficulty(Difficulty.lookup(searchCriteria.getDifficulty()))
                .categories(categories)
                .get();

        return bookRepository.findBy(Example.of(book, getQueryConfiguration()), query ->
                query.project(defaultProjection)
                        .page(new OffsetBasedPageRequest(searchCriteria.getOffset(), searchCriteria.getLimit())));
    }

    ExampleMatcher getQueryConfiguration() {
        return ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withMatcher("title", startsWith().ignoreCase())
                .withMatcher("author", startsWith().ignoreCase())
                .withMatcher("categories", match -> match.transform(source -> {
                    if (source.isEmpty() || !(source.get() instanceof Collection) || ((Collection<?>) source.get()).isEmpty()) {
                        return Optional.empty();
                    }
                    List<String> categories = (List<String>) source.get();
                    BasicDBList dbList = new BasicDBList();
                    dbList.addAll(categories);
                    DBObject dbObject = new BasicDBObject();
                    dbObject.put("$in", dbList);
                    return Optional.of(dbObject);

                }).exact());
    }

    public Optional<Book> findById(String id) {
        return bookRepository.findById(id);
    }

    public Optional<Book> update(String id, BookUpdate update) {
        return bookRepository.findById(id)
                .map(existingBook -> {
                    existingBook.setCategories(update.getCategories());
                    return bookRepository.save(existingBook);
                });
    }

    public Optional<Section> getStartingSection(String bookId) {
        return findById(bookId).flatMap(book -> book.getSections().stream()
                .filter(section -> SectionType.BEGIN.equals(section.getType()))
                .findFirst());
    }

    public Book save(Book book) {
        validate(book);
        return bookRepository.save(book);
    }

    private void validate(Book book) {

        if (book.getTitle() == null || book.getTitle().isBlank()) {
            throw new InvalidBookException(BOOK_TITLE_CANNOT_BE_NULL_OR_BLANK);
        }

        if (book.getSections() == null || book.getSections().isEmpty()) {
            throw new InvalidBookException(BOOK_SECTIONS_CANNOT_BE_NULL_OR_EMPTY);
        }

        if (book.getSections().stream().filter(section -> section.getType().equals(SectionType.BEGIN)).count() != 1) {
            throw new InvalidBookException(BOOK_MUST_HAVE_ONLY_ONE_STARTING_SECTION);
        }

        if (book.getSections().stream().noneMatch(section -> section.getType().equals(SectionType.END))) {
            throw new InvalidBookException(BOOK_MUST_HAVE_AT_LEAST_ONE_ENDING_SECTION);
        }

        if (book.getSections().stream().anyMatch(section -> section.getType().equals(SectionType.NODE) && (section.getOptions() == null || section.getOptions().isEmpty()))) {
            throw new InvalidBookException(NON_ENDING_SECTIONS_MUST_HAVE_OPTIONS);
        }

        var sectionIds = book.getSections().stream().map(Section::getId).toList();

        for (Section section : book.getSections()) {
            if (section.getOptions() != null) {
                for (var option : section.getOptions()) {
                    if (!sectionIds.contains(option.getGotoId())) {
                        throw new InvalidBookException(getInvalidOptionMessage(option.getDescription(), section.getId(), option.getGotoId()));
                    }
                }
            }
        }
    }
}

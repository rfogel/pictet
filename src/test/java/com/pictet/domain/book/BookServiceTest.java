package com.pictet.domain.book;

import com.pictet.common.BaseTest;
import com.pictet.common.CommonUtils;
import com.pictet.common.exception.InvalidBookException;
import com.pictet.domain.book.dto.BookUpdate;
import com.pictet.domain.book.dto.SearchCriteria;
import com.pictet.domain.book.model.Book;
import com.pictet.domain.book.model.Category;
import com.pictet.domain.book.model.Difficulty;
import com.pictet.domain.book.repository.BookRepository;
import com.pictet.domain.book.service.BookService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;
import java.util.UUID;

import static com.pictet.common.exception.ErrorMessage.BOOK_MUST_HAVE_AT_LEAST_ONE_ENDING_SECTION;
import static com.pictet.common.exception.ErrorMessage.BOOK_MUST_HAVE_ONLY_ONE_STARTING_SECTION;
import static com.pictet.common.exception.ErrorMessage.BOOK_SECTIONS_CANNOT_BE_NULL_OR_EMPTY;
import static com.pictet.common.exception.ErrorMessage.BOOK_TITLE_CANNOT_BE_NULL_OR_BLANK;
import static com.pictet.common.exception.ErrorMessage.NON_ENDING_SECTIONS_MUST_HAVE_OPTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(BaseTest.InMemoryDatabase.class)
@DataMongoTest
public class BookServiceTest extends BaseTest {

    @Autowired
    private BookRepository bookRepository;

    @BeforeAll
    public void setup() {
        generator = CommonUtils.generator();
        parser = CommonUtils.objectMapper();
    }

    void cleanup() {
        bookRepository.deleteAll();
    }

    @Nested
    @DisplayName("Tests for findAll()")
    class FindAllTests {

        @AfterEach
        public void afterEach() {
            cleanup();
        }

        @SneakyThrows
        @Test
        @DisplayName("given some entities, should find items")
        void test1() {

            generator.objects(Book.class, 3).forEach(bookRepository::save);

            BookService bookService = new BookService(bookRepository);

            var books = bookService.findAll(new SearchCriteria());

            assertAll(
                    () -> assertThat(books).hasSize(3),
                    () -> books.get().forEach(item -> {
                        Book book = parser.convertValue(item, Book.class);
                        assertNotNull(book.getId());
                        assertNotNull(book.getTitle());
                        assertNotNull(book.getAuthor());
                        assertNotNull(book.getDifficulty());
                        assertNotNull(book.getCategories());
                        assertNotNull(book.getCategories());
                        assertNull(book.getSections());
                    })
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given some entities, should find items paginated")
        void test2() {

            generator.objects(Book.class, 10).forEach(bookRepository::save);

            BookService bookService = new BookService(bookRepository);

            var queryOffset0Limit5 = bookService.findAll(SearchCriteria.init().offset(0).limit(5).get());
            var queryOffset5Limit10 = bookService.findAll(SearchCriteria.init().offset(5).limit(10).get());
            var queryOffset0Limit3 = bookService.findAll(SearchCriteria.init().offset(0).limit(3).get());

            assertAll(
                    () -> assertThat(queryOffset0Limit5).hasSize(5),
                    () -> assertEquals(10, queryOffset0Limit5.getTotalElements()),
                    () -> assertEquals(2, queryOffset0Limit5.getTotalPages()),
                    () -> assertThat(queryOffset5Limit10).hasSize(5),
                    () -> assertEquals(10, queryOffset5Limit10.getTotalElements()),
                    () -> assertEquals(1, queryOffset5Limit10.getTotalPages()),
                    () -> assertThat(queryOffset0Limit3).hasSize(3),
                    () -> assertEquals(10, queryOffset0Limit3.getTotalElements()),
                    () -> assertEquals(4, queryOffset0Limit3.getTotalPages())
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given some entities, should find items filtered by title, author, difficulty and categories")
        void test3() {

            generator.objects(Book.class, 1).forEach(book -> {
                book.setTitle("Java Expert");
                book.setDifficulty(Difficulty.HARD);
                book.setAuthor("John Doe");
                book.setCategories(Set.of(Category.HORROR));
                bookRepository.save(book);
            });
            generator.objects(Book.class, 1).forEach(book -> {
                book.setTitle("Java Beginner");
                book.setDifficulty(Difficulty.EASY);
                book.setAuthor("John Doe");
                book.setCategories(Set.of(Category.SCIENCE));
                bookRepository.save(book);
            });
            generator.objects(Book.class, 1).forEach(book -> {
                book.setTitle("Avatar");
                book.setDifficulty(Difficulty.MEDIUM);
                book.setCategories(Set.of(Category.FICTION, Category.ADVENTURE));
                bookRepository.save(book);
            });
            generator.objects(Book.class, 1).forEach(book -> {
                book.setTitle("Space Odyssey");
                book.setDifficulty(Difficulty.EASY);
                book.setCategories(Set.of(Category.FICTION));
                bookRepository.save(book);
            });
            generator.objects(Book.class, 1).forEach(book -> {
                book.setTitle("Lord of the Rings");
                book.setDifficulty(Difficulty.HARD);
                book.setCategories(Set.of(Category.ADVENTURE));
                bookRepository.save(book);
            });

            BookService bookService = new BookService(bookRepository);

            var queryByTitle = bookService.findAll(SearchCriteria.init().title("java").get());
            var queryByTitleAndDifficult = bookService.findAll(SearchCriteria.init().title("Java").difficulty("EAsy").get());
            var queryByAuthor = bookService.findAll(SearchCriteria.init().author("john").get());
            var queryByDifficult = bookService.findAll(SearchCriteria.init().difficulty(Difficulty.EASY.name()).get());
            var queryByDifficultAndCategory = bookService.findAll(SearchCriteria.init().difficulty(Difficulty.MEDIUM.name()).categories(Set.of("fiction")).get());
            var queryByCategory = bookService.findAll(SearchCriteria.init().categories(Set.of("adventure")).get());
            var queryByCategories = bookService.findAll(SearchCriteria.init().categories(Set.of("horror", "science")).get());
            var queryByCategoryNotFound = bookService.findAll(SearchCriteria.init().categories(Set.of("comedy")).get());

            assertAll(
                    () -> assertThat(queryByTitle).hasSize(2),
                    () -> queryByTitle.forEach(book -> assertThat(book.getTitle()).containsIgnoringCase("Java")),

                    () -> assertThat(queryByTitleAndDifficult).hasSize(1),
                    () -> queryByTitleAndDifficult.forEach(book -> assertThat(book.getTitle()).containsIgnoringCase("Java")),
                    () -> queryByTitleAndDifficult.forEach(book -> assertThat(book.getDifficulty()).isEqualTo(Difficulty.EASY)),

                    () -> assertThat(queryByAuthor).hasSize(2),
                    () -> queryByAuthor.forEach(book -> assertThat(book.getAuthor()).containsIgnoringCase("John")),

                    () -> assertThat(queryByDifficult).hasSize(2),
                    () -> queryByDifficult.forEach(book -> assertThat(book.getDifficulty()).isEqualTo(Difficulty.EASY)),

                    () -> assertThat(queryByDifficultAndCategory).hasSize(1),
                    () -> queryByDifficultAndCategory.forEach(book -> assertThat(book.getDifficulty()).isEqualTo(Difficulty.MEDIUM)),
                    () -> queryByDifficultAndCategory.forEach(book -> assertThat(book.getCategories()).contains(Category.FICTION)),

                    () -> assertThat(queryByCategory).hasSize(2),
                    () -> queryByCategory.forEach(book -> assertThat(book.getCategories()).contains(Category.ADVENTURE)),

                    () -> assertThat(queryByCategories).hasSize(2),
                    () -> queryByCategories.forEach(book -> assertThat(book.getCategories()).anyMatch(category -> category == Category.HORROR || category == Category.SCIENCE)),

                    () -> assertThat(queryByCategoryNotFound).isEmpty()
            );
        }
    }

    @Nested
    @DisplayName("Tests for findById()")
    class FindByIdTests {

        @AfterEach
        public void afterEach() {
            cleanup();
        }

        @SneakyThrows
        @Test
        @DisplayName("given some entities, should find by id")
        void test1() {

            generator.objects(Book.class, 2).forEach(bookRepository::save);
            var book = generator.nextObject(Book.class);
            bookRepository.save(book);

            BookService bookService = new BookService(bookRepository);

            var foundBook = bookService.findById(book.getId());

            assertAll(
                    () -> assertThat(foundBook).isPresent(),
                    () -> assertThat(foundBook.get().getId()).isEqualTo(book.getId())
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given some entities and a no existent book, should return empty")
        void test2() {

            generator.objects(Book.class, 3).forEach(bookRepository::save);

            BookService bookService = new BookService(bookRepository);

            var foundBook = bookService.findById(UUID.randomUUID().toString());

            assertAll(
                    () -> assertThat(foundBook).isEmpty()
            );
        }
    }

    @Nested
    @DisplayName("Tests for update()")
    class UpdateTests {

        @AfterEach
        public void afterEach() {
            cleanup();
        }

        @SneakyThrows
        @Test
        @DisplayName("given a book, should update the categories")
        void test1() {

            var book = generator.nextObject(Book.class);
            book.setCategories(Set.of(Category.HORROR));
            book = bookRepository.save(book);

            BookService bookService = new BookService(bookRepository);

            final var savedBook = bookService.findById(book.getId()).get();

            var newCategories = Set.of(Category.ADVENTURE, Category.FICTION);
            var update = BookUpdate.init().categories(newCategories).get();
            var updatedBook = bookService.update(book.getId(), update);

            assertAll(
                    () -> assertThat(updatedBook).isPresent(),
                    () -> assertThat(updatedBook.get().getId()).isEqualTo(savedBook.getId()),
                    () -> assertThat(updatedBook.get().getCategories()).isEqualTo(newCategories),
                    () -> assertThat(updatedBook.get().getTitle()).isEqualTo(savedBook.getTitle()),
                    () -> assertThat(updatedBook.get().getAuthor()).isEqualTo(savedBook.getAuthor()),
                    () -> assertThat(updatedBook.get().getDifficulty()).isEqualTo(savedBook.getDifficulty()),
                    () -> assertThat(updatedBook.get().getSections().size()).isEqualTo(savedBook.getSections().size())
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given a non existing book, should return empty")
        void test2() {

            BookService bookService = new BookService(bookRepository);
            var updatedBook = bookService.update(UUID.randomUUID().toString(), new BookUpdate());

            assertAll(
                    () -> assertThat(updatedBook).isEmpty()
            );
        }
    }

    @Nested
    @DisplayName("Tests for save()")
    class SaveTests {

        @AfterEach
        public void afterEach() {
            cleanup();
        }

        @SneakyThrows
        @Test
        @DisplayName("given a book, should save it")
        void test1() {

            var book = parser.readValue(new ClassPathResource("sample/book.json").getFile(), Book.class);
            BookService bookService = new BookService(bookRepository);
            var savedBook = bookService.save(book);

            assertAll(
                    () -> assertThat(savedBook).isNotNull(),
                    () -> assertThat(savedBook.getId()).isNotNull(),
                    () -> assertThat(savedBook.getTitle()).isEqualTo(book.getTitle()),
                    () -> assertThat(savedBook.getAuthor()).isEqualTo(book.getAuthor()),
                    () -> assertThat(savedBook.getDifficulty()).isEqualTo(book.getDifficulty()),
                    () -> assertThat(savedBook.getCategories()).isEqualTo(book.getCategories()),
                    () -> assertThat(savedBook.getSections().size()).isEqualTo(book.getSections().size())
            );
        }

        @SneakyThrows
        @Test
        @DisplayName("given an invalid title book, should throw an exception")
        void test2() {
            var book = parser.readValue(new ClassPathResource("sample/book.json").getFile(), Book.class);
            book.setTitle(null);
            BookService bookService = new BookService(bookRepository);
            assertThatThrownBy(() -> bookService.save(book))
                    .isInstanceOf(InvalidBookException.class)
                    .hasMessage(BOOK_TITLE_CANNOT_BE_NULL_OR_BLANK);
        }

        @SneakyThrows
        @Test
        @DisplayName("given an invalid sections book, should throw an exception")
        void test3() {
            var book = parser.readValue(new ClassPathResource("sample/book.json").getFile(), Book.class);
            book.setSections(null);
            BookService bookService = new BookService(bookRepository);
            assertThatThrownBy(() -> bookService.save(book))
                    .isInstanceOf(InvalidBookException.class)
                    .hasMessage(BOOK_SECTIONS_CANNOT_BE_NULL_OR_EMPTY);
        }

        @SneakyThrows
        @Test
        @DisplayName("given an multiple beginning sections book, should throw an exception")
        void test4() {
            var book = parser.readValue(new ClassPathResource("sample/book-multiple-beginning.json").getFile(), Book.class);
            BookService bookService = new BookService(bookRepository);
            assertThatThrownBy(() -> bookService.save(book))
                    .isInstanceOf(InvalidBookException.class)
                    .hasMessage(BOOK_MUST_HAVE_ONLY_ONE_STARTING_SECTION);
        }

        @SneakyThrows
        @Test
        @DisplayName("given no beginning sections book, should throw an exception")
        void test5() {
            var book = parser.readValue(new ClassPathResource("sample/book-no-beginning.json").getFile(), Book.class);
            BookService bookService = new BookService(bookRepository);
            assertThatThrownBy(() -> bookService.save(book))
                    .isInstanceOf(InvalidBookException.class)
                    .hasMessage(BOOK_MUST_HAVE_ONLY_ONE_STARTING_SECTION);
        }

        @SneakyThrows
        @Test
        @DisplayName("given no end sections book, should throw an exception")
        void test6() {
            var book = parser.readValue(new ClassPathResource("sample/book-no-end.json").getFile(), Book.class);
            BookService bookService = new BookService(bookRepository);
            assertThatThrownBy(() -> bookService.save(book))
                    .isInstanceOf(InvalidBookException.class)
                    .hasMessage(BOOK_MUST_HAVE_AT_LEAST_ONE_ENDING_SECTION);
        }

        @SneakyThrows
        @Test
        @DisplayName("given a non ending section without options book, should throw an exception")
        void test7() {
            var book = parser.readValue(new ClassPathResource("sample/book-without-options.json").getFile(), Book.class);
            BookService bookService = new BookService(bookRepository);
            assertThatThrownBy(() -> bookService.save(book))
                    .isInstanceOf(InvalidBookException.class)
                    .hasMessage(NON_ENDING_SECTIONS_MUST_HAVE_OPTIONS);
        }

        @SneakyThrows
        @Test
        @DisplayName("given an invalid section book, should throw an exception")
        void test8() {
            var book = parser.readValue(new ClassPathResource("sample/book-invalid-section.json").getFile(), Book.class);
            BookService bookService = new BookService(bookRepository);
            assertThatThrownBy(() -> bookService.save(book))
                    .isInstanceOf(InvalidBookException.class)
                    .hasMessage("Option with text 'Try to scan the area with your hands' in section '20' points to a non-existing section with id '9999'");
        }
    }
}

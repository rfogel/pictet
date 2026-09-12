package com.pictet.domain.book.repository;

import com.pictet.domain.book.model.Book;
import com.pictet.domain.book.model.BookList;
import com.pictet.domain.book.model.Category;
import com.pictet.domain.book.model.Difficulty;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.function.Function;

@Repository
public interface BookRepository extends MongoRepository<Book, String>, QueryByExampleExecutor<Book> {
    Page<BookList> findBy(Pageable pageable);
    Page<Book> findByCategoriesIn(Set<Category> categorySet, Example<Book> example, Pageable pageable, Function<FluentQuery.FetchableFluentQuery<Book>, Page<?>> queryFunction);
    Page<Book> findByTitleLikeAndAuthorLikeAndDifficultyLikeAndCategoriesIn(String title, String author, Difficulty difficulty, Set<Category> categories, Pageable pageable);
}

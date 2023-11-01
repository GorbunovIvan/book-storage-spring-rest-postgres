package com.example.repository;

import com.example.model.Book;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {

    @Query("FROM Book book " +
            "LEFT JOIN FETCH book.genres " +
            "LEFT JOIN FETCH book.authors")
    @Nonnull
    List<Book> findAll();

    @Query("FROM Book book " +
            "LEFT JOIN FETCH book.genres " +
            "LEFT JOIN FETCH book.authors " +
            "WHERE book.id = :id")
    @Nonnull
    @Override
    Optional<Book> findById(@Param("id") @Nonnull Integer id);
}

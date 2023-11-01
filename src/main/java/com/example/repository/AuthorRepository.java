package com.example.repository;

import com.example.model.Author;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Integer>, AuthorRepositoryCustom {

    @Query("FROM Author author " +
            "LEFT JOIN FETCH author.books")
    @Nonnull
    List<Author> findAll();

    @Query("FROM Author author " +
            "LEFT JOIN FETCH author.books " +
            "WHERE author.id = :id")
    @Nonnull
    @Override
    Optional<Author> findById(@Param("id") @Nonnull Integer id);
}

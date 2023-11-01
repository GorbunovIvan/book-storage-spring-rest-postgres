package com.example.repository;

import com.example.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Integer>, GenreRepositoryCustom {
    Optional<Genre> findByName(String name);
}

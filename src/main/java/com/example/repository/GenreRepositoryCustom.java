package com.example.repository;

import com.example.model.Genre;

import java.util.Set;

public interface GenreRepositoryCustom {
    void mergeAll(Set<Genre> genres);
    void deleteByNameInACascade(String name);
}

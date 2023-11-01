package com.example.repository;

import com.example.model.Author;

import java.util.Set;

public interface AuthorRepositoryCustom {
    void mergeAll(Set<Author> authors);
    void deleteInACascade(Author author);
}

package com.example.service;

import com.example.model.Genre;
import com.example.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    public List<Genre> getAll() {
        return genreRepository.findAll();
    }

    public Genre getByName(String name) {
        return genreRepository.findByName(name)
                .orElse(null);
    }

    public Genre create(Genre genre) {
        return genreRepository.save(genre);
    }

    public void deleteByName(String name) {
        genreRepository.deleteByNameInACascade(name);
    }
}

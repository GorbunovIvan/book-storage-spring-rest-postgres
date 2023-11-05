package com.example.controller;

import com.example.model.Genre;
import com.example.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public ResponseEntity<List<Genre>> getAll() {
        var genres = genreService.getAll();
        return ResponseEntity.ok(genres);
    }

    @GetMapping("/{name}")
    public ResponseEntity<Genre> getByName(@PathVariable String name) {
        var genre = genreService.getByName(name);
        if (genre == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(genre);
    }

    @PostMapping
    public ResponseEntity<Genre> create(@RequestBody Genre genre) {
        var genrePersisted = genreService.create(genre);
        return new ResponseEntity<>(genrePersisted, HttpStatusCode.valueOf(202));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteByName(@PathVariable String name) {
        genreService.deleteByName(name);
        return ResponseEntity.ok().build();
    }
}

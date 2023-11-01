package com.example.repository;

import com.example.model.Genre;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Set;

@Component
public class GenreRepositoryCustomImpl implements GenreRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Override
    public void mergeAll(Set<Genre> genres) {

        var genresFound = entityManager.createQuery("SELECT g FROM Genre g " +
                    "WHERE g.name IN :names", Genre.class)
                .setParameter("names", genres.stream().map(Genre::getName).toList())
                .getResultList();

        var genresList = new ArrayList<>(genres);

        for (int i = 0; i < genresList.size(); i++) {
            var genre = genresList.get(i);
            if (genresFound.stream().noneMatch(genreFound -> genreFound.equals(genre))) {
                var genreMerged = entityManager.merge(genre);
                genresList.set(i, genreMerged);
            }
        }

        genres.clear();
        genres.addAll(genresList);
    }

    @Transactional
    @Override
    public void deleteInACascade(Genre genre) {

        Query query = entityManager.createNativeQuery("DELETE FROM book_genres WHERE genre_name = :name");
        query.setParameter("name", genre.getName());
        query.executeUpdate();

        entityManager.remove(genre);
    }
}

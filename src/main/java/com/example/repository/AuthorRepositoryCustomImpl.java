package com.example.repository;

import com.example.model.Author;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuthorRepositoryCustomImpl implements AuthorRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Override
    public void mergeAll(Set<Author> authors) {

        var authorsFound = findAuthorsInDBByBasicFields(authors);

        for (var author : authors) {

            boolean existsInDB = false;

            for (var authorFound : authorsFound) {
                if (author.getName().equals(authorFound.getName())
                        && author.getSurname().equals(authorFound.getSurname())
                        && author.getBirthDate().equals(authorFound.getBirthDate())) {
                    author.setId(authorFound.getId());
                    existsInDB = true;
                    break;
                }
            }

            if (!existsInDB) {
                var authorMerged = entityManager.merge(author);
                author.setId(authorMerged.getId());
            }
        }
    }

    @Transactional
    @Override
    public void deleteByIdInACascade(Integer id) {

        var author = entityManager.find(Author.class, id);
        if (author == null) {
            return;
        }

        Query query = entityManager.createNativeQuery("DELETE FROM book_authors WHERE author_id = :id");
        query.setParameter("id", id);
        query.executeUpdate();

        entityManager.remove(author);
    }

    private List<Author> findAuthorsInDBByBasicFields(Set<Author> authors) {

        var names = authors.stream().map(Author::getName).collect(Collectors.toSet());
        var surnames = authors.stream().map(Author::getSurname).collect(Collectors.toSet());
        var birthDates = authors.stream().map(Author::getBirthDate).collect(Collectors.toSet());

        return entityManager.createQuery("SELECT a FROM Author a " +
                        "WHERE a.name IN :names " +
                        "AND a.surname IN :surnames " +
                        "AND a.birthDate IN :birthDates ", Author.class)
                .setParameter("names", names)
                .setParameter("surnames", surnames)
                .setParameter("birthDates", birthDates)
                .getResultList();
    }
}

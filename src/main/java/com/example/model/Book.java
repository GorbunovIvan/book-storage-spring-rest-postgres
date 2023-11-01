package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "books")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode
@ToString
public class Book implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Exclude
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "publication_year")
    private Integer year;

    @Column(name = "number_of_pages")
    private Integer numberOfPages;

    @ManyToMany(targetEntity = Author.class)
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"book_id", "author_id"})
    )
    @JsonIgnoreProperties("books")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Author> authors = new HashSet<>();

    @ManyToMany(targetEntity = Genre.class)
    @JoinTable(
            name = "book_genres",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_name"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"book_id", "genre_name"})
    )
    private Set<Genre> genres = new HashSet<>();

    @EqualsAndHashCode.Include
    @ToString.Include
    public Set<String> authorsNames() {
        return getAuthors().stream()
                .map(Author::getFullName)
                .collect(Collectors.toUnmodifiableSet());
    }

    public void addAuthor(Author author) {
        getAuthors().add(author);
    }

    public void addGenre(Genre genre) {
        getGenres().add(genre);
    }

    @Override
    public Book clone() {
        try {
            Book clone = (Book) super.clone();
            clone.setAuthors(new HashSet<>(getAuthors()));
            clone.setGenres(new HashSet<>(getGenres()));
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}

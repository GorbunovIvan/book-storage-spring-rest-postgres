package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "authors",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "name", "surname", "birth_date" }) })
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode
@ToString
public class Author implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Exclude
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname")
    private String surname;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @ManyToMany(mappedBy = "authors")
    @JsonIgnoreProperties("authors")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Book> books = new HashSet<>();

    public String getFullName() {
        return getName() + " " + getSurname();
    }

    public Set<String> booksNames() {
        return getBooks().stream()
                .map(Book::getName)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Author clone() {
        try {
            Author clone = (Author) super.clone();
            clone.setBooks(new HashSet<>(getBooks()));
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}

package com.example.service;

import com.example.exception.RuntimeExceptionWithHTTPCode;
import com.example.model.Book;
import com.example.repository.AuthorRepository;
import com.example.repository.BookRepository;
import com.example.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;

    public List<Book> getAll() {
        return bookRepository.findAll();
    }

    public Book getById(Integer id) {
        return bookRepository.findById(id)
                .orElse(null);
    }

    public Book create(Book book) {
        if (book.getId() != null) {
            throw new RuntimeExceptionWithHTTPCode("When creating a book, id should not be specified, use update instead", HttpStatusCode.valueOf(400));
        }
        authorRepository.mergeAll(book.getAuthors());
        genreRepository.mergeAll(book.getGenres());
        return bookRepository.save(book);
    }

    @Transactional
    public Book update(Integer id, Book book) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeExceptionWithHTTPCode("Book with id '" + id + "' is not found", HttpStatusCode.valueOf(404));
        }
        book.setId(id);
        authorRepository.mergeAll(book.getAuthors());
        genreRepository.mergeAll(book.getGenres());
        return bookRepository.save(book);
    }

    public void deleteById(Integer id) {
        bookRepository.deleteById(id);
    }
}

package com.example.service;

import com.example.exception.RuntimeExceptionWithHTTPCode;
import com.example.model.Author;
import com.example.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;

    public List<Author> getAll() {
        return authorRepository.findAll();
    }

    public Author getById(Integer id) {
        return authorRepository.findById(id)
                .orElse(null);
    }

    public Author create(Author author) {
        if (author.getId() != null) {
            throw new RuntimeExceptionWithHTTPCode("When creating an author, id should not be specified, use update instead", HttpStatusCode.valueOf(400));
        }
        return authorRepository.save(author);
    }

    @Transactional
    public Author update(Integer id, Author author) {
        if (!authorRepository.existsById(id)) {
            throw new RuntimeExceptionWithHTTPCode("Author with id '" + id + "' is not found", HttpStatusCode.valueOf(404));
        }
        author.setId(id);
        return authorRepository.save(author);
    }

    public void deleteById(Integer id) {
        authorRepository.deleteByIdInACascade(id);
    }
}

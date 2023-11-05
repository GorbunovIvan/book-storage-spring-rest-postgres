package com.example.controller;

import com.example.model.Author;
import com.example.model.Book;
import com.example.model.Genre;
import com.example.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureDataJpa
@Transactional
class BookControllerTest {

    @Autowired
    private MockMvc mvc;

    @SpyBean
    private BookService bookService;

    @Value("${api.version.books}")
    private String version;

    private String baseURI;

    @Autowired
    private ObjectMapper objectMapper;

    private final JavaTimeModule javaTimeModule = new JavaTimeModule();

    @BeforeEach
    public void setUp() {
        baseURI = "/api/" + version + "/books";
        objectMapper.registerModule(javaTimeModule);
    }

    @Test
    void testGetAll() throws Exception {

        var books = bookService.getAll();

        var jsonResponse = mvc.perform(MockMvcRequestBuilders.get(baseURI)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        var typeFactory = objectMapper.getTypeFactory().constructCollectionType(List.class, Book.class);
        List<Book> booksFound = objectMapper.readValue(jsonResponse, typeFactory);

        assertEquals(books, booksFound);

        verify(bookService, times(2)).getAll();
    }

    @Test
    void testGetById() throws Exception {

        var books = bookService.getAll();

        for (var book : books) {

            String jsonResponse = mvc.perform(MockMvcRequestBuilders.get(baseURI + "/{id}", book.getId())
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

            Book bookFound = objectMapper.readValue(jsonResponse, Book.class);

            assertEquals(book, bookFound);
            assertEquals(book.authorsNames(), bookFound.authorsNames());
            assertEquals(book.getGenres(), bookFound.getGenres());

            verify(bookService, times(1)).getById(book.getId());
        }

        mvc.perform(MockMvcRequestBuilders.get(baseURI + "/{id}", -1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {

        var booksExisting = bookService.getAll();
        var authorsExisting = getAuthorsOfBooks(booksExisting);
        var genresExisting = getGenresOfBooks(booksExisting);

        var authors = new HashSet<Author>();
        authors.add(new Author(null, "new name", "new surname", LocalDate.now(), new HashSet<>()));
        authors.add(authorsExisting.get(0));

        var genres = new HashSet<Genre>();
        genres.add(new Genre("new genre"));
        genres.add(genresExisting.get(0));

        var newBook = new Book(null, "new book", 9999, 99, authors, genres);

        String jsonRequest = objectMapper.writeValueAsString(newBook);

        String jsonResponse = mvc.perform(MockMvcRequestBuilders.post(baseURI)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonRequest))
                            .andExpect(status().isAccepted())
                            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

        Book bookCreated = objectMapper.readValue(jsonResponse, Book.class);

        assertNotNull(bookCreated.getId());
        assertEquals(newBook.getName(), bookCreated.getName());
        assertEquals(newBook.getYear(), bookCreated.getYear());
        assertEquals(newBook.getNumberOfPages(), bookCreated.getNumberOfPages());
        assertEquals(newBook.authorsNames(), bookCreated.authorsNames());
        assertEquals(newBook.getGenres(), bookCreated.getGenres());

        Book bookInDB = bookService.getById(bookCreated.getId());

        assertEquals(bookInDB, bookCreated);
        assertEquals(bookInDB.authorsNames(), bookCreated.authorsNames());
        assertEquals(bookInDB.getGenres(), bookCreated.getGenres());

        verify(bookService, times(1)).create(any(Book.class));
    }

    @Test
    void testUpdate() throws Exception {

        var books = bookService.getAll();

        for (var bookEntity : books) {

            var book = bookEntity.clone();

            // Just changing the book a little
            book.setName(book.getName() + " (updated)");
            book.setYear(book.getYear() + 1);
            book.setNumberOfPages(book.getNumberOfPages() + 1);
            if (book.getAuthors().size() > 1) {
                var iterator = book.getAuthors().iterator();
                iterator.next();
                iterator.remove();
            } else {
                book.addAuthor(new Author(null, "new name", "new surname", LocalDate.now(), new HashSet<>()));
            }
            if (book.getGenres().size() > 1) {
                var iterator = book.getGenres().iterator();
                iterator.next();
                iterator.remove();
            } else {
                book.addGenre(new Genre("new genre"));
            }

            String jsonRequest = objectMapper.writeValueAsString(book);

            String jsonResponse = mvc.perform(MockMvcRequestBuilders.put(baseURI + "/{id}", book.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                        .andExpect(status().isAccepted())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

            Book bookUpdated = objectMapper.readValue(jsonResponse, Book.class);

            assertEquals(book.getId(), bookUpdated.getId());
            assertEquals(book.getName(), bookUpdated.getName());
            assertEquals(book.getYear(), bookUpdated.getYear());
            assertEquals(book.getNumberOfPages(), bookUpdated.getNumberOfPages());
            assertEquals(book.authorsNames(), bookUpdated.authorsNames());
            assertEquals(book.getGenres(), bookUpdated.getGenres());

            Book bookInDB = bookService.getById(bookUpdated.getId());

            assertEquals(bookInDB, bookUpdated);
            assertEquals(bookInDB.authorsNames(), bookUpdated.authorsNames());
            assertEquals(bookInDB.getGenres(), bookUpdated.getGenres());

            verify(bookService, times(1)).update(book.getId(), book);
        }

        String jsonRequest = objectMapper.writeValueAsString(new Book());
        mvc.perform(MockMvcRequestBuilders.put(baseURI + "/{id}", -1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteById() throws Exception {

        var books = bookService.getAll();

        for (var book : books) {

            String jsonRequest = objectMapper.writeValueAsString(book);

            mvc.perform(MockMvcRequestBuilders.delete(baseURI + "/{id}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                    .andExpect(status().isOk());

            assertNull(bookService.getById(book.getId()));

            verify(bookService, times(1)).deleteById(book.getId());
        }
    }

    private List<Author> getAuthorsOfBooks(List<Book> books) {
        return books.stream()
                .map(Book::getAuthors)
                .flatMap(Set::stream)
                .distinct()
                .toList();
    }

    private List<Genre> getGenresOfBooks(List<Book> books) {
        return books.stream()
                .map(Book::getGenres)
                .flatMap(Set::stream)
                .distinct()
                .toList();
    }
}
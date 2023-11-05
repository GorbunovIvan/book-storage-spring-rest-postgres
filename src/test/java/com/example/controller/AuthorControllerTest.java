package com.example.controller;

import com.example.model.Author;
import com.example.model.Book;
import com.example.service.AuthorService;
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
class AuthorControllerTest {

    @Autowired
    private MockMvc mvc;

    @SpyBean
    private AuthorService authorService;

    @Value("${api.version.authors}")
    private String version;

    private String baseURI;

    @Autowired
    private ObjectMapper objectMapper;

    private final JavaTimeModule javaTimeModule = new JavaTimeModule();

    @BeforeEach
    public void setUp() {
        baseURI = "/api/" + version + "/authors";
        objectMapper.registerModule(javaTimeModule);
    }

    @Test
    void testGetAll() throws Exception {

        var authors = authorService.getAll();

        var jsonResponse = mvc.perform(MockMvcRequestBuilders.get(baseURI)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        var typeFactory = objectMapper.getTypeFactory().constructCollectionType(List.class, Author.class);
        List<Author> authorsFound = objectMapper.readValue(jsonResponse, typeFactory);

        assertEquals(authors, authorsFound);

        verify(authorService, times(2)).getAll();
    }

    @Test
    void testGetById() throws Exception {

        var authors = authorService.getAll();

        for (var author : authors) {

            String jsonResponse = mvc.perform(MockMvcRequestBuilders.get(baseURI + "/{id}", author.getId())
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

            Author authorFound = objectMapper.readValue(jsonResponse, Author.class);

            assertEquals(author, authorFound);
            assertEquals(author.booksNames(), authorFound.booksNames());

            verify(authorService, times(1)).getById(author.getId());
        }

        mvc.perform(MockMvcRequestBuilders.get(baseURI + "/{id}", -1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {

        var authorsExisting = authorService.getAll();
        var booksExisting = getBooksOfAuthors(authorsExisting);

        var books = new HashSet<Book>();
        books.add(new Book(null, "new book", 9999, 99, new HashSet<>(), new HashSet<>()));
        books.add(booksExisting.get(0));

        var newAuthor = new Author(null, "new name", "new surname", LocalDate.now(), books);

        String jsonRequest = objectMapper.writeValueAsString(newAuthor);

        String jsonResponse = mvc.perform(MockMvcRequestBuilders.post(baseURI)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonRequest))
                            .andExpect(status().isAccepted())
                            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

        Author authorCreated = objectMapper.readValue(jsonResponse, Author.class);

        assertNotNull(authorCreated.getId());
        assertEquals(newAuthor.getName(), authorCreated.getName());
        assertEquals(newAuthor.getSurname(), authorCreated.getSurname());
        assertEquals(newAuthor.getBirthDate(), authorCreated.getBirthDate());
        assertEquals(newAuthor.booksNames(), authorCreated.booksNames());

        Author authorInDB = authorService.getById(authorCreated.getId());

        assertEquals(authorInDB, authorCreated);
        assertEquals(authorInDB.booksNames(), authorCreated.booksNames());

        verify(authorService, times(1)).create(any(Author.class));
    }

    @Test
    void testUpdate() throws Exception {

        var authors = authorService.getAll();

        for (var authorEntity : authors) {

            var author = authorEntity.clone();

            // Just changing the author a little
            author.setName(author.getName() + " (updated)");
            author.setSurname(author.getSurname() + " (updated)");
            author.setBirthDate(author.getBirthDate().plusYears(1L));

            String jsonRequest = objectMapper.writeValueAsString(author);

            String jsonResponse = mvc.perform(MockMvcRequestBuilders.put(baseURI + "/{id}", author.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                        .andExpect(status().isAccepted())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

            Author authorUpdated = objectMapper.readValue(jsonResponse, Author.class);

            assertEquals(author.getId(), authorUpdated.getId());
            assertEquals(author.getName(), authorUpdated.getName());
            assertEquals(author.getSurname(), authorUpdated.getSurname());
            assertEquals(author.getBirthDate(), authorUpdated.getBirthDate());
            assertEquals(author.booksNames(), authorUpdated.booksNames());

            Author authorInDB = authorService.getById(authorUpdated.getId());

            assertEquals(authorInDB, authorUpdated);
            assertEquals(authorInDB.booksNames(), authorUpdated.booksNames());

            verify(authorService, times(1)).update(author.getId(), author);
        }

        String jsonRequest = objectMapper.writeValueAsString(new Author());
        mvc.perform(MockMvcRequestBuilders.put(baseURI + "/{id}", -1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteById() throws Exception {

        var authors = authorService.getAll();

        for (var author : authors) {

            String jsonRequest = objectMapper.writeValueAsString(author);

            mvc.perform(MockMvcRequestBuilders.delete(baseURI + "/{id}", author.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                    .andExpect(status().isOk());

            assertNull(authorService.getById(author.getId()));

            verify(authorService, times(1)).deleteById(author.getId());
        }
    }

    private List<Book> getBooksOfAuthors(List<Author> authors) {
        return authors.stream()
                .map(Author::getBooks)
                .flatMap(Set::stream)
                .distinct()
                .toList();
    }
}
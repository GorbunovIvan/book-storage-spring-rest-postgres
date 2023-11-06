package com.example.controller;

import com.example.model.Genre;
import com.example.model.security.Role;
import com.example.security.JwtTokenProvider;
import com.example.service.GenreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureDataJpa
@Transactional
class GenreControllerTest {

    @Autowired
    private MockMvc mvc;

    @SpyBean
    private GenreService genreService;

    @Value("${api.version.genres}")
    private String version;

    private String baseURI;

    @Autowired
    private ObjectMapper objectMapper;

    private final JavaTimeModule javaTimeModule = new JavaTimeModule();

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    private String jwtToken;

    @PostConstruct
    public void init() {
        this.jwtToken = jwtTokenProvider.createToken("test", List.of(Role.USER));
    }

    @BeforeEach
    public void setUp() {
        baseURI = "/api/" + version + "/genres";
        objectMapper.registerModule(javaTimeModule);
    }

    @Test
    void testGetAll() throws Exception {

        var genres = genreService.getAll();

        var jsonResponse = mvc.perform(MockMvcRequestBuilders.get(baseURI)
                                .header("Authorization", jwtToken)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        var typeFactory = objectMapper.getTypeFactory().constructCollectionType(List.class, Genre.class);
        List<Genre> genresFound = objectMapper.readValue(jsonResponse, typeFactory);

        assertEquals(genres, genresFound);

        verify(genreService, times(2)).getAll();
    }

    @Test
    void testGetByName() throws Exception {

        var genres = genreService.getAll();

        for (var genre : genres) {

            String jsonResponse = mvc.perform(MockMvcRequestBuilders.get(baseURI + "/{name}", genre.getName())
                                        .header("Authorization", jwtToken)
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

            Genre genreFound = objectMapper.readValue(jsonResponse, Genre.class);

            assertEquals(genre, genreFound);

            verify(genreService, times(1)).getByName(genre.getName());
        }

        mvc.perform(MockMvcRequestBuilders.get(baseURI + "/{name}", "-")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {

        var newGenre = new Genre("new genre");

        String jsonRequest = objectMapper.writeValueAsString(newGenre);

        String jsonResponse = mvc.perform(MockMvcRequestBuilders.post(baseURI)
                                    .header("Authorization", jwtToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonRequest))
                            .andExpect(status().isAccepted())
                            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

        Genre genreCreated = objectMapper.readValue(jsonResponse, Genre.class);
        assertEquals(newGenre.getName(), genreCreated.getName());

        Genre genreInDB = genreService.getByName(genreCreated.getName());
        assertEquals(genreInDB, genreCreated);

        verify(genreService, times(1)).create(any(Genre.class));
    }

    @Test
    void testDeleteByName() throws Exception {

        var genres = genreService.getAll();

        for (var genre : genres) {

            String jsonRequest = objectMapper.writeValueAsString(genre);

            mvc.perform(MockMvcRequestBuilders.delete(baseURI + "/{name}", genre.getName())
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                    .andExpect(status().isOk());

            assertNull(genreService.getByName(genre.getName()));

            verify(genreService, times(1)).deleteByName(genre.getName());
        }
    }
    
    @Test
    void testJWTAuthorization() throws Exception {

        // No token
        mvc.perform(MockMvcRequestBuilders.get(baseURI)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
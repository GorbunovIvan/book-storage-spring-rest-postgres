package com.example.controller.security;

import com.example.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureDataJpa
@Transactional
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mvc;

    @SpyBean
    private AuthenticationManager authenticationManager;

    @SpyBean
    private JwtTokenProvider jwtTokenProvider;

    @Value("${api.version.authors}")
    private String version;

    private String baseURI;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        baseURI = "/api/" + version + "/auth";
    }

    @Test
    void testLogin() throws Exception {

        // Wrong credentials
        var userDto = new AuthenticationRequestDto();
        userDto.setUsername("wrong");
        userDto.setPassword("wrong");

        String jsonRequest = objectMapper.writeValueAsString(userDto);

        mvc.perform(MockMvcRequestBuilders.post(baseURI + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, never()).createToken(anyString(), anyCollection());

        // Is ok
        userDto.setUsername("test");
        userDto.setPassword("test");

        jsonRequest = objectMapper.writeValueAsString(userDto);

        mvc.perform(MockMvcRequestBuilders.post(baseURI + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is(userDto.getUsername())))
                .andExpect(jsonPath("$.token", anything()));

        verify(authenticationManager, times(2)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, times(1)).createToken(anyString(), anyCollection());
    }
}
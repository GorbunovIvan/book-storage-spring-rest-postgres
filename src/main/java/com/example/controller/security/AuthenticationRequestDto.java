package com.example.controller.security;

import lombok.Data;

@Data
public class AuthenticationRequestDto {
    private String username;
    private String password;
}

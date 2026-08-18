package com.github.nhordiienko23.mycustomauthserver.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record RegisterRequest(
        @NotBlank(message = "username can't be empty")
        String username,
        @NotBlank(message = "password can't be empty")
        String password,
        @Email
        @NotBlank(message = "email can't be empty")
        String email) {


}

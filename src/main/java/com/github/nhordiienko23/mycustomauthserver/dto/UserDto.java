package com.github.nhordiienko23.mycustomauthserver.dto;

import lombok.Builder;

@Builder
public record UserDto(String username, String email) {
}

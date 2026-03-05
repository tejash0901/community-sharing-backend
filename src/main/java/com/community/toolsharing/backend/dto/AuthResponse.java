package com.community.toolsharing.backend.dto;

public record AuthResponse(
        String token,
        Long userId,
        String name,
        String email,
        Long communityId,
        String communityName
) {
}
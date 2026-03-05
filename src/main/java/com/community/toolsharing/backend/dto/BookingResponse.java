package com.community.toolsharing.backend.dto;

import com.community.toolsharing.backend.model.BookingStatus;

import java.time.Instant;

public record BookingResponse(
        Long id,
        Long toolId,
        String toolName,
        Long borrowerId,
        String borrowerName,
        Long ownerId,
        String ownerName,
        Long slotId,
        Instant slotStartTime,
        Instant slotEndTime,
        BookingStatus status,
        Instant createdAt
) {
}
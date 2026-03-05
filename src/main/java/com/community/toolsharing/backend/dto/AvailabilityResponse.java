package com.community.toolsharing.backend.dto;

import com.community.toolsharing.backend.model.SlotStatus;

import java.time.Instant;

public record AvailabilityResponse(
        Long id,
        Long toolId,
        Instant startTime,
        Instant endTime,
        SlotStatus status
) {
}
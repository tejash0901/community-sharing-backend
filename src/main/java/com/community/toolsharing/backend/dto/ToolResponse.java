package com.community.toolsharing.backend.dto;

import com.community.toolsharing.backend.model.ToolCondition;

import java.math.BigDecimal;
import java.time.Instant;

public record ToolResponse(
        Long id,
        String name,
        String description,
        String category,
        ToolCondition condition,
        BigDecimal estimatedPrice,
        Long ownerId,
        String ownerName,
        String ownerBlock,
        String ownerFloor,
        String ownerFlatNumber,
        Long communityId,
        Instant createdAt,
        String aiPriceHint,
        String imageUrl
) {
}

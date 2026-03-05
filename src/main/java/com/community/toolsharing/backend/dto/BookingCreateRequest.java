package com.community.toolsharing.backend.dto;

import jakarta.validation.constraints.NotNull;

public class BookingCreateRequest {
    @NotNull
    private Long toolId;

    @NotNull
    private Long slotId;

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }
    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }
}
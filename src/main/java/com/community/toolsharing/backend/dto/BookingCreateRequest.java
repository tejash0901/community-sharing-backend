package com.community.toolsharing.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class BookingCreateRequest {
    @NotNull
    private Long toolId;

    @NotNull
    private Long slotId;

    @NotNull
    private Instant requestedStartTime;

    @NotNull
    private Instant requestedEndTime;

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }
    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }
    public Instant getRequestedStartTime() { return requestedStartTime; }
    public void setRequestedStartTime(Instant requestedStartTime) { this.requestedStartTime = requestedStartTime; }
    public Instant getRequestedEndTime() { return requestedEndTime; }
    public void setRequestedEndTime(Instant requestedEndTime) { this.requestedEndTime = requestedEndTime; }
}

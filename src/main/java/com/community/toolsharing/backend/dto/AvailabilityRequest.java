package com.community.toolsharing.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class AvailabilityRequest {
    @NotNull
    @Future
    private Instant startTime;

    @NotNull
    @Future
    private Instant endTime;

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
}
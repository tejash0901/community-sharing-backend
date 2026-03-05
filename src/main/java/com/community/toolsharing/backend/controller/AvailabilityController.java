package com.community.toolsharing.backend.controller;

import com.community.toolsharing.backend.dto.AvailabilityRequest;
import com.community.toolsharing.backend.dto.AvailabilityResponse;
import com.community.toolsharing.backend.service.AvailabilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping("/api/tools/{toolId}/availability")
    public ResponseEntity<AvailabilityResponse> createAvailability(@PathVariable Long toolId,
                                                                   @Valid @RequestBody AvailabilityRequest request) {
        return ResponseEntity.ok(availabilityService.createSlot(toolId, request));
    }

    @GetMapping("/api/tools/{toolId}/availability")
    public ResponseEntity<List<AvailabilityResponse>> getAvailability(@PathVariable Long toolId) {
        return ResponseEntity.ok(availabilityService.getSlotsForTool(toolId));
    }

    @DeleteMapping("/api/availability/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long id) {
        availabilityService.deleteSlot(id);
        return ResponseEntity.noContent().build();
    }
}
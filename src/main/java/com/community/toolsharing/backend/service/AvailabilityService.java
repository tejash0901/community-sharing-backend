package com.community.toolsharing.backend.service;

import com.community.toolsharing.backend.dto.AvailabilityRequest;
import com.community.toolsharing.backend.dto.AvailabilityResponse;
import com.community.toolsharing.backend.exception.BadRequestException;
import com.community.toolsharing.backend.exception.ResourceNotFoundException;
import com.community.toolsharing.backend.exception.UnauthorizedException;
import com.community.toolsharing.backend.model.AppUser;
import com.community.toolsharing.backend.model.AvailabilitySlot;
import com.community.toolsharing.backend.model.SlotStatus;
import com.community.toolsharing.backend.model.Tool;
import com.community.toolsharing.backend.repository.AvailabilitySlotRepository;
import com.community.toolsharing.backend.repository.ToolRepository;
import com.community.toolsharing.backend.util.CurrentUserUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AvailabilityService {
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final ToolRepository toolRepository;
    private final CurrentUserUtil currentUserUtil;

    public AvailabilityService(AvailabilitySlotRepository availabilitySlotRepository,
                               ToolRepository toolRepository,
                               CurrentUserUtil currentUserUtil) {
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.toolRepository = toolRepository;
        this.currentUserUtil = currentUserUtil;
    }

    @Transactional
    public AvailabilityResponse createSlot(Long toolId, AvailabilityRequest request) {
        AppUser user = currentUserUtil.getCurrentUser();
        Tool tool = toolRepository.findByIdAndCommunityId(toolId, user.getCommunity().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found"));

        if (!tool.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only owner can create availability slots");
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("startTime must be before endTime");
        }

        boolean overlaps = availabilitySlotRepository.existsByToolIdAndStartTimeLessThanAndEndTimeGreaterThan(
                tool.getId(), request.getEndTime(), request.getStartTime()
        );
        if (overlaps) {
            throw new BadRequestException("Slot overlaps with existing availability slot");
        }

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setTool(tool);
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setStatus(SlotStatus.AVAILABLE);

        return toResponse(availabilitySlotRepository.save(slot));
    }

    public List<AvailabilityResponse> getSlotsForTool(Long toolId) {
        AppUser user = currentUserUtil.getCurrentUser();
        toolRepository.findByIdAndCommunityId(toolId, user.getCommunity().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found"));

        return availabilitySlotRepository.findAllByToolIdOrderByStartTimeAsc(toolId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteSlot(Long slotId) {
        AppUser user = currentUserUtil.getCurrentUser();
        AvailabilitySlot slot = availabilitySlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability slot not found"));

        if (!slot.getTool().getCommunity().getId().equals(user.getCommunity().getId())) {
            throw new UnauthorizedException("Cannot delete slot outside your community");
        }

        if (!slot.getTool().getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only owner can delete this slot");
        }

        availabilitySlotRepository.delete(slot);
    }

    private AvailabilityResponse toResponse(AvailabilitySlot slot) {
        return new AvailabilityResponse(
                slot.getId(),
                slot.getTool().getId(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getStatus()
        );
    }
}
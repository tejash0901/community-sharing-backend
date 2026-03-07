package com.community.toolsharing.backend.service;

import com.community.toolsharing.backend.dto.AvailabilityRequest;
import com.community.toolsharing.backend.dto.AvailabilityResponse;
import com.community.toolsharing.backend.exception.BadRequestException;
import com.community.toolsharing.backend.exception.ResourceNotFoundException;
import com.community.toolsharing.backend.exception.UnauthorizedException;
import com.community.toolsharing.backend.model.AppUser;
import com.community.toolsharing.backend.model.AvailabilitySlot;
import com.community.toolsharing.backend.model.BookingRequest;
import com.community.toolsharing.backend.model.BookingStatus;
import com.community.toolsharing.backend.model.SlotStatus;
import com.community.toolsharing.backend.model.Tool;
import com.community.toolsharing.backend.repository.AvailabilitySlotRepository;
import com.community.toolsharing.backend.repository.BookingRequestRepository;
import com.community.toolsharing.backend.repository.ToolRepository;
import com.community.toolsharing.backend.util.CurrentUserUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AvailabilityService {
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final ToolRepository toolRepository;
    private final CurrentUserUtil currentUserUtil;

    public AvailabilityService(AvailabilitySlotRepository availabilitySlotRepository,
                               BookingRequestRepository bookingRequestRepository,
                               ToolRepository toolRepository,
                               CurrentUserUtil currentUserUtil) {
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.bookingRequestRepository = bookingRequestRepository;
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

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getAvailableWindowsForTool(Long toolId) {
        AppUser user = currentUserUtil.getCurrentUser();
        toolRepository.findByIdAndCommunityId(toolId, user.getCommunity().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found"));

        List<BookingStatus> activeStatuses = List.of(
                BookingStatus.APPROVED,
                BookingStatus.COLLECT_PENDING,
                BookingStatus.COLLECTED,
                BookingStatus.RETURN_PENDING,
                BookingStatus.RETURN_REJECTED
        );

        List<AvailabilityResponse> windows = new ArrayList<>();
        for (AvailabilitySlot slot : availabilitySlotRepository.findAllByToolIdOrderByStartTimeAsc(toolId)) {
            Instant cursor = slot.getStartTime();
            List<BookingRequest> bookings = bookingRequestRepository
                    .findAllBySlotIdAndStatusInOrderByRequestedStartTimeAsc(slot.getId(), activeStatuses);

            for (BookingRequest booking : bookings) {
                Instant blockedStart = booking.getRequestedStartTime().isBefore(slot.getStartTime())
                        ? slot.getStartTime()
                        : booking.getRequestedStartTime();
                Instant blockedEnd = booking.getRequestedEndTime().isAfter(slot.getEndTime())
                        ? slot.getEndTime()
                        : booking.getRequestedEndTime();

                if (!blockedStart.isBefore(blockedEnd)) {
                    continue;
                }

                if (cursor.isBefore(blockedStart)) {
                    windows.add(toResponse(slot, cursor, blockedStart));
                }
                if (cursor.isBefore(blockedEnd)) {
                    cursor = blockedEnd;
                }
            }

            if (cursor.isBefore(slot.getEndTime())) {
                windows.add(toResponse(slot, cursor, slot.getEndTime()));
            }
        }
        return windows;
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

        List<BookingStatus> activeStatuses = List.of(
                BookingStatus.PENDING, BookingStatus.APPROVED,
                BookingStatus.COLLECT_PENDING, BookingStatus.COLLECTED,
                BookingStatus.RETURN_PENDING, BookingStatus.RETURN_REJECTED
        );
        if (bookingRequestRepository.existsBySlotIdAndStatusIn(slot.getId(), activeStatuses)) {
            throw new BadRequestException("Cannot delete slot with active bookings");
        }

        bookingRequestRepository.deleteBySlotId(slot.getId());
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

    private AvailabilityResponse toResponse(AvailabilitySlot slot, Instant startTime, Instant endTime) {
        return new AvailabilityResponse(
                slot.getId(),
                slot.getTool().getId(),
                startTime,
                endTime,
                SlotStatus.AVAILABLE
        );
    }
}

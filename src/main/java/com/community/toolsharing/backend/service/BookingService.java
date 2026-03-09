package com.community.toolsharing.backend.service;

import com.community.toolsharing.backend.ai.FraudDetectionService;
import com.community.toolsharing.backend.dto.BookingCreateRequest;
import com.community.toolsharing.backend.dto.BookingResponse;
import com.community.toolsharing.backend.exception.BadRequestException;
import com.community.toolsharing.backend.exception.ResourceNotFoundException;
import com.community.toolsharing.backend.model.*;
import com.community.toolsharing.backend.repository.AvailabilitySlotRepository;
import com.community.toolsharing.backend.repository.BookingRequestRepository;
import com.community.toolsharing.backend.repository.ToolRepository;
import com.community.toolsharing.backend.util.CurrentUserUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class BookingService {
    private final BookingRequestRepository bookingRequestRepository;
    private final ToolRepository toolRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final CurrentUserUtil currentUserUtil;
    private final FraudDetectionService fraudDetectionService;

    private static final List<BookingStatus> CONFIRMED_STATUSES = List.of(
            BookingStatus.APPROVED, BookingStatus.COLLECT_PENDING, BookingStatus.COLLECTED,
            BookingStatus.RETURN_PENDING, BookingStatus.RETURN_REJECTED
    );

    public BookingService(BookingRequestRepository bookingRequestRepository,
                          ToolRepository toolRepository,
                          AvailabilitySlotRepository availabilitySlotRepository,
                          CurrentUserUtil currentUserUtil,
                          FraudDetectionService fraudDetectionService) {
        this.bookingRequestRepository = bookingRequestRepository;
        this.toolRepository = toolRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.currentUserUtil = currentUserUtil;
        this.fraudDetectionService = fraudDetectionService;
    }

    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request) {
        AppUser borrower = currentUserUtil.getCurrentUser();

        Tool tool = toolRepository.findByIdAndCommunityId(request.getToolId(), borrower.getCommunity().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found"));

        if (tool.getOwner().getId().equals(borrower.getId())) {
            throw new BadRequestException("Borrower cannot borrow their own tool");
        }

        AvailabilitySlot slot = availabilitySlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        if (!slot.getTool().getId().equals(tool.getId())) {
            throw new BadRequestException("Slot does not belong to the selected tool");
        }

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new BadRequestException("Only AVAILABLE slots can be requested");
        }

        Instant requestedStartTime = request.getRequestedStartTime();
        Instant requestedEndTime = request.getRequestedEndTime();
        if (!requestedStartTime.isBefore(requestedEndTime)) {
            throw new BadRequestException("requestedStartTime must be before requestedEndTime");
        }
        if (requestedStartTime.isBefore(slot.getStartTime()) || requestedEndTime.isAfter(slot.getEndTime())) {
            throw new BadRequestException("Requested time must be inside the owner's availability slot");
        }

        // Idempotent: same borrower, same slot/time, still pending → return existing
        List<BookingStatus> pendingStatuses = List.of(BookingStatus.PENDING);
        var existingMine = bookingRequestRepository.findFirstBySlotIdAndBorrowerIdAndStatusInAndRequestedStartTimeAndRequestedEndTime(
                slot.getId(), borrower.getId(), pendingStatuses, requestedStartTime, requestedEndTime);
        if (existingMine.isPresent()) {
            return toResponse(existingMine.get());
        }

        // Only confirmed bookings block new requests (PENDING does NOT block)
        boolean overlaps = bookingRequestRepository.existsBySlotIdAndStatusInAndRequestedStartTimeLessThanAndRequestedEndTimeGreaterThan(
                slot.getId(), CONFIRMED_STATUSES, requestedEndTime, requestedStartTime);
        if (overlaps) {
            throw new BadRequestException("Requested time overlaps with a confirmed booking. Please choose a different time.");
        }

        BookingRequest booking = new BookingRequest();
        booking.setTool(tool);
        booking.setBorrower(borrower);
        booking.setSlot(slot);
        booking.setRequestedStartTime(requestedStartTime);
        booking.setRequestedEndTime(requestedEndTime);
        booking.setStatus(BookingStatus.PENDING);

        if (fraudDetectionService.isSuspicious(booking)) {
            throw new BadRequestException("Booking flagged as suspicious by AI fraud placeholder");
        }

        return toResponse(bookingRequestRepository.save(booking));
    }

    @Transactional
    public BookingResponse approveBooking(Long id) {
        AppUser current = currentUserUtil.getCurrentUser();
        BookingRequest booking = bookingRequestRepository.findByIdAndToolOwnerId(id, current.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for owner"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only PENDING bookings can be approved");
        }

        // Check no confirmed booking overlaps this time range
        boolean overlaps = bookingRequestRepository.existsBySlotIdAndStatusInAndRequestedStartTimeLessThanAndRequestedEndTimeGreaterThan(
                booking.getSlot().getId(), CONFIRMED_STATUSES,
                booking.getRequestedEndTime(), booking.getRequestedStartTime());
        if (overlaps) {
            throw new BadRequestException("Another booking is already confirmed for this time range");
        }

        booking.setStatus(BookingStatus.APPROVED);
        return toResponse(bookingRequestRepository.save(booking));
    }

    @Transactional
    public BookingResponse rejectBooking(Long id) {
        AppUser current = currentUserUtil.getCurrentUser();
        BookingRequest booking = bookingRequestRepository.findByIdAndToolOwnerId(id, current.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for owner"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only PENDING bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        return toResponse(bookingRequestRepository.save(booking));
    }

    @Transactional
    public BookingResponse collectBooking(Long id) {
        AppUser current = currentUserUtil.getCurrentUser();
        BookingRequest booking = bookingRequestRepository.findByIdAndToolOwnerId(id, current.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for owner"));

        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED bookings can be marked as collected");
        }

        booking.setStatus(BookingStatus.COLLECT_PENDING);
        return toResponse(bookingRequestRepository.save(booking));
    }

    @Transactional
    public BookingResponse confirmCollect(Long id) {
        AppUser current = currentUserUtil.getCurrentUser();
        BookingRequest booking = bookingRequestRepository.findByIdAndBorrowerId(id, current.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for borrower"));

        if (booking.getStatus() != BookingStatus.COLLECT_PENDING) {
            throw new BadRequestException("Only COLLECT_PENDING bookings can be confirmed as collected");
        }

        booking.setStatus(BookingStatus.COLLECTED);
        return toResponse(bookingRequestRepository.save(booking));
    }

    @Transactional
    public BookingResponse returnTool(Long id) {
        AppUser current = currentUserUtil.getCurrentUser();
        BookingRequest booking = bookingRequestRepository.findByIdAndBorrowerId(id, current.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for borrower"));

        if (booking.getStatus() != BookingStatus.COLLECTED && booking.getStatus() != BookingStatus.RETURN_REJECTED) {
            throw new BadRequestException("Only COLLECTED or RETURN_REJECTED bookings can request return");
        }

        booking.setStatus(BookingStatus.RETURN_PENDING);
        return toResponse(bookingRequestRepository.save(booking));
    }

    @Transactional
    public BookingResponse approveReturn(Long id) {
        AppUser current = currentUserUtil.getCurrentUser();
        BookingRequest booking = bookingRequestRepository.findByIdAndToolOwnerId(id, current.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for owner"));

        if (booking.getStatus() != BookingStatus.RETURN_PENDING) {
            throw new BadRequestException("Only RETURN_PENDING bookings can be return-approved");
        }

        booking.setStatus(BookingStatus.RETURNED);
        return toResponse(bookingRequestRepository.save(booking));
    }

    @Transactional
    public BookingResponse rejectReturn(Long id) {
        AppUser current = currentUserUtil.getCurrentUser();
        BookingRequest booking = bookingRequestRepository.findByIdAndToolOwnerId(id, current.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for owner"));

        if (booking.getStatus() != BookingStatus.RETURN_PENDING) {
            throw new BadRequestException("Only RETURN_PENDING bookings can be return-rejected");
        }

        booking.setStatus(BookingStatus.RETURN_REJECTED);
        return toResponse(bookingRequestRepository.save(booking));
    }

    @Transactional
    public BookingResponse cancelBooking(Long id) {
        AppUser current = currentUserUtil.getCurrentUser();

        // Try as borrower first
        var asBorrower = bookingRequestRepository.findByIdAndBorrowerId(id, current.getId());
        if (asBorrower.isPresent()) {
            BookingRequest booking = asBorrower.get();
            if (booking.getStatus() == BookingStatus.PENDING) {
                booking.setStatus(BookingStatus.CANCELLED);
                return toResponse(bookingRequestRepository.save(booking));
            }
            if (booking.getStatus() == BookingStatus.APPROVED) {
                if (!Instant.now().isBefore(booking.getRequestedStartTime())) {
                    throw new BadRequestException("Cannot cancel after your time slot has started");
                }
                booking.setStatus(BookingStatus.CANCELLED);
                return toResponse(bookingRequestRepository.save(booking));
            }
            throw new BadRequestException("Booking can only be cancelled when PENDING or APPROVED (before slot start)");
        }

        // Try as owner
        var asOwner = bookingRequestRepository.findByIdAndToolOwnerId(id, current.getId());
        if (asOwner.isPresent()) {
            BookingRequest booking = asOwner.get();
            if (booking.getStatus() == BookingStatus.APPROVED
                    && !Instant.now().isBefore(booking.getRequestedStartTime())) {
                booking.setStatus(BookingStatus.CANCELLED);
                return toResponse(bookingRequestRepository.save(booking));
            }
            if (booking.getStatus() == BookingStatus.COLLECT_PENDING) {
                booking.setStatus(BookingStatus.CANCELLED);
                return toResponse(bookingRequestRepository.save(booking));
            }
            throw new BadRequestException("Owner can only cancel APPROVED bookings (after slot start) or COLLECT_PENDING bookings");
        }

        throw new ResourceNotFoundException("Booking not found");
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBorrowedBookings() {
        AppUser current = currentUserUtil.getCurrentUser();
        return bookingRequestRepository.findAllByBorrowerIdOrderByCreatedAtDesc(current.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getOwnerBookings() {
        AppUser current = currentUserUtil.getCurrentUser();
        return bookingRequestRepository.findAllByToolOwnerIdOrderByCreatedAtDesc(current.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private BookingResponse toResponse(BookingRequest b) {
        return new BookingResponse(
                b.getId(),
                b.getTool().getId(),
                b.getTool().getName(),
                b.getBorrower().getId(),
                b.getBorrower().getName(),
                b.getTool().getOwner().getId(),
                b.getTool().getOwner().getName(),
                b.getSlot().getId(),
                b.getSlot().getStartTime(),
                b.getSlot().getEndTime(),
                b.getRequestedStartTime(),
                b.getRequestedEndTime(),
                b.getStatus(),
                b.getCreatedAt()
        );
    }
}

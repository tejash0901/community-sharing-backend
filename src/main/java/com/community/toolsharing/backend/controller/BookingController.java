package com.community.toolsharing.backend.controller;

import com.community.toolsharing.backend.dto.BookingCreateRequest;
import com.community.toolsharing.backend.dto.BookingResponse;
import com.community.toolsharing.backend.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingCreateRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<BookingResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.approveBooking(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<BookingResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.rejectBooking(id));
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<BookingResponse> returnTool(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.returnTool(id));
    }

    @PatchMapping("/{id}/return/approve")
    public ResponseEntity<BookingResponse> approveReturn(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.approveReturn(id));
    }

    @PatchMapping("/{id}/return/reject")
    public ResponseEntity<BookingResponse> rejectReturn(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.rejectReturn(id));
    }

    @PatchMapping("/{id}/collect")
    public ResponseEntity<BookingResponse> collect(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.collectBooking(id));
    }

    @PatchMapping("/{id}/collect/confirm")
    public ResponseEntity<BookingResponse> confirmCollect(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirmCollect(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<BookingResponse>> mine() {
        return ResponseEntity.ok(bookingService.getMyBorrowedBookings());
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponse>> owner() {
        return ResponseEntity.ok(bookingService.getOwnerBookings());
    }
}

package com.community.toolsharing.backend.service;

import com.community.toolsharing.backend.dto.BookingResponse;
import com.community.toolsharing.backend.dto.DashboardResponse;
import com.community.toolsharing.backend.dto.ToolResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {
    private final ToolService toolService;
    private final BookingService bookingService;

    public DashboardService(ToolService toolService, BookingService bookingService) {
        this.toolService = toolService;
        this.bookingService = bookingService;
    }

    public DashboardResponse getDashboard() {
        List<ToolResponse> myTools = toolService.getMyTools();
        List<BookingResponse> borrowed = bookingService.getMyBorrowedBookings();
        List<BookingResponse> ownerPending = bookingService.getOwnerBookings().stream()
                .filter(b -> "PENDING".equals(b.status().name()))
                .toList();

        return new DashboardResponse(myTools, borrowed, ownerPending);
    }
}
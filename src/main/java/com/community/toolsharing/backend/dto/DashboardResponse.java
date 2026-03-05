package com.community.toolsharing.backend.dto;

import java.util.List;

public record DashboardResponse(
        List<ToolResponse> myTools,
        List<BookingResponse> borrowedTools,
        List<BookingResponse> pendingOwnerRequests
) {
}
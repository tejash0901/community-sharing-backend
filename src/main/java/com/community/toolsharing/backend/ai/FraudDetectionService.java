package com.community.toolsharing.backend.ai;

import com.community.toolsharing.backend.model.BookingRequest;

public interface FraudDetectionService {
    boolean isSuspicious(BookingRequest bookingRequest);
}
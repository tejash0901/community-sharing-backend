package com.community.toolsharing.backend.ai;

import com.community.toolsharing.backend.model.AppUser;
import com.community.toolsharing.backend.model.BookingRequest;
import com.community.toolsharing.backend.model.Community;
import com.community.toolsharing.backend.model.Tool;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AiPlaceholderService implements PricePredictionService, ToolRecommendationService, DemandPredictionService, FraudDetectionService {
    @Override
    public BigDecimal predictOptimalPrice(Tool tool) {
        return tool.getEstimatedPrice() != null ? tool.getEstimatedPrice() : BigDecimal.ZERO;
    }

    @Override
    public List<String> recommendToolCategories(AppUser user) {
        return List.of("Gardening", "Electrical", "Plumbing");
    }

    @Override
    public String predictDemandSummary(Community community) {
        return "Demand prediction placeholder for community " + community.getName();
    }

    @Override
    public boolean isSuspicious(BookingRequest bookingRequest) {
        return false;
    }
}
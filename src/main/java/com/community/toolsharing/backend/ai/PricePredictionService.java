package com.community.toolsharing.backend.ai;

import com.community.toolsharing.backend.model.Tool;

import java.math.BigDecimal;

public interface PricePredictionService {
    BigDecimal predictOptimalPrice(Tool tool);
}
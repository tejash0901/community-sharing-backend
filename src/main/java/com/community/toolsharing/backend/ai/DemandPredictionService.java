package com.community.toolsharing.backend.ai;

import com.community.toolsharing.backend.model.Community;

public interface DemandPredictionService {
    String predictDemandSummary(Community community);
}
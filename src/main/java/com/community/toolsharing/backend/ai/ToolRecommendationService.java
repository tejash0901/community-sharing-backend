package com.community.toolsharing.backend.ai;

import com.community.toolsharing.backend.model.AppUser;

import java.util.List;

public interface ToolRecommendationService {
    List<String> recommendToolCategories(AppUser user);
}
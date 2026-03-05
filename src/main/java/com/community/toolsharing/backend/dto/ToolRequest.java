package com.community.toolsharing.backend.dto;

import com.community.toolsharing.backend.model.ToolCondition;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class ToolRequest {
    @NotBlank
    private String name;
    private String description;
    private String category;
    private ToolCondition condition;
    private BigDecimal estimatedPrice;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public ToolCondition getCondition() { return condition; }
    public void setCondition(ToolCondition condition) { this.condition = condition; }
    public BigDecimal getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(BigDecimal estimatedPrice) { this.estimatedPrice = estimatedPrice; }
}
package com.community.toolsharing.backend.service;

import com.community.toolsharing.backend.ai.PricePredictionService;
import com.community.toolsharing.backend.dto.ToolRequest;
import com.community.toolsharing.backend.dto.ToolResponse;
import com.community.toolsharing.backend.exception.ResourceNotFoundException;
import com.community.toolsharing.backend.exception.UnauthorizedException;
import com.community.toolsharing.backend.model.AppUser;
import com.community.toolsharing.backend.model.Tool;
import com.community.toolsharing.backend.repository.ToolRepository;
import com.community.toolsharing.backend.util.CurrentUserUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ToolService {
    private final ToolRepository toolRepository;
    private final CurrentUserUtil currentUserUtil;
    private final PricePredictionService pricePredictionService;

    public ToolService(ToolRepository toolRepository, CurrentUserUtil currentUserUtil, PricePredictionService pricePredictionService) {
        this.toolRepository = toolRepository;
        this.currentUserUtil = currentUserUtil;
        this.pricePredictionService = pricePredictionService;
    }

    @Transactional
    public ToolResponse createTool(ToolRequest request) {
        AppUser user = currentUserUtil.getCurrentUser();

        Tool tool = new Tool();
        tool.setName(request.getName());
        tool.setDescription(request.getDescription());
        tool.setCategory(request.getCategory());
        tool.setCondition(request.getCondition());
        tool.setEstimatedPrice(request.getEstimatedPrice());
        tool.setOwner(user);
        tool.setCommunity(user.getCommunity());

        Tool saved = toolRepository.save(tool);
        return toResponse(saved);
    }

    public List<ToolResponse> getCommunityTools() {
        AppUser user = currentUserUtil.getCurrentUser();
        return toolRepository.findAllByCommunityId(user.getCommunity().getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public ToolResponse getToolById(Long id) {
        AppUser user = currentUserUtil.getCurrentUser();
        Tool tool = toolRepository.findByIdAndCommunityId(id, user.getCommunity().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found"));
        return toResponse(tool);
    }

    @Transactional
    public ToolResponse updateTool(Long id, ToolRequest request) {
        AppUser user = currentUserUtil.getCurrentUser();
        Tool tool = toolRepository.findByIdAndCommunityId(id, user.getCommunity().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found"));

        if (!tool.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only owner can update this tool");
        }

        tool.setName(request.getName());
        tool.setDescription(request.getDescription());
        tool.setCategory(request.getCategory());
        tool.setCondition(request.getCondition());
        tool.setEstimatedPrice(request.getEstimatedPrice());

        Tool updated = toolRepository.save(tool);
        return toResponse(updated);
    }

    @Transactional
    public void deleteTool(Long id) {
        AppUser user = currentUserUtil.getCurrentUser();
        Tool tool = toolRepository.findByIdAndCommunityId(id, user.getCommunity().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found"));

        if (!tool.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only owner can delete this tool");
        }

        toolRepository.delete(tool);
    }

    public List<ToolResponse> getMyTools() {
        AppUser user = currentUserUtil.getCurrentUser();
        return toolRepository.findAllByOwnerId(user.getId()).stream().map(this::toResponse).toList();
    }

    private ToolResponse toResponse(Tool tool) {
        String aiPriceHint = "AI suggested price: " + pricePredictionService.predictOptimalPrice(tool);
        return new ToolResponse(
                tool.getId(),
                tool.getName(),
                tool.getDescription(),
                tool.getCategory(),
                tool.getCondition(),
                tool.getEstimatedPrice(),
                tool.getOwner().getId(),
                tool.getOwner().getName(),
                tool.getOwner().getBlock(),
                tool.getOwner().getFloor(),
                tool.getOwner().getFlatNumber(),
                tool.getCommunity().getId(),
                tool.getCreatedAt(),
                aiPriceHint
        );
    }
}
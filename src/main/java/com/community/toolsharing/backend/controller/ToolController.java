package com.community.toolsharing.backend.controller;

import com.community.toolsharing.backend.dto.ToolRequest;
import com.community.toolsharing.backend.dto.ToolResponse;
import com.community.toolsharing.backend.service.ToolService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tools")
public class ToolController {
    private final ToolService toolService;

    public ToolController(ToolService toolService) {
        this.toolService = toolService;
    }

    @PostMapping
    public ResponseEntity<ToolResponse> createTool(@Valid @RequestBody ToolRequest request) {
        return ResponseEntity.ok(toolService.createTool(request));
    }

    @GetMapping
    public ResponseEntity<List<ToolResponse>> getTools() {
        return ResponseEntity.ok(toolService.getCommunityTools());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ToolResponse> getTool(@PathVariable Long id) {
        return ResponseEntity.ok(toolService.getToolById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ToolResponse> updateTool(@PathVariable Long id, @Valid @RequestBody ToolRequest request) {
        return ResponseEntity.ok(toolService.updateTool(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTool(@PathVariable Long id) {
        toolService.deleteTool(id);
        return ResponseEntity.noContent().build();
    }
}
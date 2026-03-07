package com.community.toolsharing.backend.service;

import com.community.toolsharing.backend.ai.PricePredictionService;
import com.community.toolsharing.backend.dto.ToolRequest;
import com.community.toolsharing.backend.dto.ToolResponse;
import com.community.toolsharing.backend.exception.BadRequestException;
import com.community.toolsharing.backend.exception.ResourceNotFoundException;
import com.community.toolsharing.backend.exception.UnauthorizedException;
import com.community.toolsharing.backend.model.AppUser;
import com.community.toolsharing.backend.model.BookingStatus;
import com.community.toolsharing.backend.model.Tool;
import com.community.toolsharing.backend.repository.AvailabilitySlotRepository;
import com.community.toolsharing.backend.repository.BookingRequestRepository;
import com.community.toolsharing.backend.repository.ToolRepository;
import com.community.toolsharing.backend.util.CurrentUserUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ToolService {
    private final ToolRepository toolRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final CurrentUserUtil currentUserUtil;
    private final PricePredictionService pricePredictionService;
    private final Path uploadsDir;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB

    public ToolService(ToolRepository toolRepository,
                       BookingRequestRepository bookingRequestRepository,
                       AvailabilitySlotRepository availabilitySlotRepository,
                       CurrentUserUtil currentUserUtil,
                       PricePredictionService pricePredictionService,
                       @Value("${app.uploads.dir}") String uploadsDir) {
        this.toolRepository = toolRepository;
        this.bookingRequestRepository = bookingRequestRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.currentUserUtil = currentUserUtil;
        this.pricePredictionService = pricePredictionService;
        this.uploadsDir = Paths.get(uploadsDir).toAbsolutePath().normalize();
    }

    @Transactional
    public ToolResponse createTool(ToolRequest request, MultipartFile image) {
        AppUser user = currentUserUtil.getCurrentUser();

        Tool tool = new Tool();
        tool.setName(request.getName());
        tool.setDescription(request.getDescription());
        tool.setCategory(request.getCategory());
        tool.setCondition(request.getCondition());
        tool.setEstimatedPrice(request.getEstimatedPrice());
        tool.setOwner(user);
        tool.setCommunity(user.getCommunity());

        if (image != null && !image.isEmpty()) {
            tool.setImageFileName(storeImage(image));
        }

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

        List<BookingStatus> activeStatuses = List.of(
                BookingStatus.PENDING, BookingStatus.APPROVED,
                BookingStatus.COLLECT_PENDING, BookingStatus.COLLECTED,
                BookingStatus.RETURN_PENDING, BookingStatus.RETURN_REJECTED
        );
        if (bookingRequestRepository.existsByToolIdAndStatusIn(tool.getId(), activeStatuses)) {
            throw new BadRequestException("Cannot delete tool with active bookings");
        }

        if (tool.getImageFileName() != null) {
            deleteImageFile(tool.getImageFileName());
        }

        bookingRequestRepository.deleteByToolId(tool.getId());
        availabilitySlotRepository.deleteByToolId(tool.getId());
        toolRepository.delete(tool);
    }

    public List<ToolResponse> getMyTools() {
        AppUser user = currentUserUtil.getCurrentUser();
        return toolRepository.findAllByOwnerId(user.getId()).stream().map(this::toResponse).toList();
    }

    public byte[] getImage(String fileName) {
        Path filePath = uploadsDir.resolve(fileName).normalize();
        if (!filePath.startsWith(uploadsDir)) {
            throw new BadRequestException("Invalid file path");
        }
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new ResourceNotFoundException("Image not found");
        }
    }

    public String getImageContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    private String storeImage(MultipartFile file) {
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only JPEG, PNG, GIF, and WebP images are allowed");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException("Image must be smaller than 5 MB");
        }

        String extension = getExtension(file.getOriginalFilename(), file.getContentType());
        String storedName = UUID.randomUUID() + extension;

        try {
            Files.createDirectories(uploadsDir);
            Path target = uploadsDir.resolve(storedName).normalize();
            if (!target.startsWith(uploadsDir)) {
                throw new BadRequestException("Invalid file name");
            }
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new BadRequestException("Failed to store image");
        }

        return storedName;
    }

    private void deleteImageFile(String fileName) {
        try {
            Path filePath = uploadsDir.resolve(fileName).normalize();
            if (filePath.startsWith(uploadsDir)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ignored) {
        }
    }

    private String getExtension(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
            if (ext.matches("\\.(jpe?g|png|gif|webp)")) {
                return ext.toLowerCase();
            }
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private ToolResponse toResponse(Tool tool) {
        String aiPriceHint = "AI suggested price: " + pricePredictionService.predictOptimalPrice(tool);
        String imageUrl = tool.getImageFileName() != null
                ? "/api/tools/images/" + tool.getImageFileName()
                : null;
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
                aiPriceHint,
                imageUrl
        );
    }
}

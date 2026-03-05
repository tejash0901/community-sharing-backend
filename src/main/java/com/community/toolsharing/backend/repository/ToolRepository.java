package com.community.toolsharing.backend.repository;

import com.community.toolsharing.backend.model.Tool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ToolRepository extends JpaRepository<Tool, Long> {
    List<Tool> findAllByCommunityId(Long communityId);
    Optional<Tool> findByIdAndCommunityId(Long id, Long communityId);
    List<Tool> findAllByOwnerId(Long ownerId);
}
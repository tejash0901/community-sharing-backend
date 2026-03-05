package com.community.toolsharing.backend.repository;

import com.community.toolsharing.backend.model.Community;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    Optional<Community> findByInviteCode(String inviteCode);
}
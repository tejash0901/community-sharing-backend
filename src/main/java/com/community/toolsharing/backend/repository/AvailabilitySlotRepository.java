package com.community.toolsharing.backend.repository;

import com.community.toolsharing.backend.model.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {
    List<AvailabilitySlot> findAllByToolIdOrderByStartTimeAsc(Long toolId);

    boolean existsByToolIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long toolId,
            Instant endTime,
            Instant startTime
    );
}
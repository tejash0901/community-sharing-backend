package com.community.toolsharing.backend.repository;

import com.community.toolsharing.backend.model.BookingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
    boolean existsBySlotId(Long slotId);
    List<BookingRequest> findAllByBorrowerId(Long borrowerId);
    List<BookingRequest> findAllByToolOwnerId(Long ownerId);
    Optional<BookingRequest> findByIdAndToolOwnerId(Long id, Long ownerId);
    Optional<BookingRequest> findByIdAndBorrowerId(Long id, Long borrowerId);
}
package com.community.toolsharing.backend.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "booking_requests", indexes = {
        @Index(name = "idx_booking_borrower_id", columnList = "borrower_id"),
        @Index(name = "idx_booking_tool_id", columnList = "tool_id"),
        @Index(name = "idx_booking_slot_id", columnList = "slot_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_booking_slot", columnNames = "slot_id")
})
public class BookingRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "borrower_id", nullable = false)
    private AppUser borrower;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false, unique = true)
    private AvailabilitySlot slot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Tool getTool() { return tool; }
    public void setTool(Tool tool) { this.tool = tool; }
    public AppUser getBorrower() { return borrower; }
    public void setBorrower(AppUser borrower) { this.borrower = borrower; }
    public AvailabilitySlot getSlot() { return slot; }
    public void setSlot(AvailabilitySlot slot) { this.slot = slot; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
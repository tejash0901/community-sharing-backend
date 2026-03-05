package com.community.toolsharing.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;

import java.time.Instant;

@Entity
@Table(name = "availability_slots", indexes = {
        @Index(name = "idx_availability_tool_id", columnList = "tool_id")
})
@Check(constraints = "start_time < end_time")
public class AvailabilitySlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SlotStatus status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Tool getTool() { return tool; }
    public void setTool(Tool tool) { this.tool = tool; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public SlotStatus getStatus() { return status; }
    public void setStatus(SlotStatus status) { this.status = status; }
}
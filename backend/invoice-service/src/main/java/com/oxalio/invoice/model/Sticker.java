package com.oxalio.invoice.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "stickers")
public class Sticker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String stickerId;

    @Column(name = "sticker_year")
    private String year;

    @Enumerated(EnumType.STRING)
    private StickerStatus status;

    private Instant reservedAt;
    private Instant usedAt;

    public enum StickerStatus {
        AVAILABLE,
        RESERVED,
        USED
    }

    // Getters & Setters
    public Long getId() { return id; }

    public String getStickerId() { return stickerId; }
    public void setStickerId(String stickerId) { this.stickerId = stickerId; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public StickerStatus getStatus() { return status; }
    public void setStatus(StickerStatus status) { this.status = status; }

    public Instant getReservedAt() { return reservedAt; }
    public void setReservedAt(Instant reservedAt) { this.reservedAt = reservedAt; }

    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }
}

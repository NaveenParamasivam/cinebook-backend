package com.cinebook.entity;

import com.cinebook.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "show_seats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"show_id", "row_label", "seat_number"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Column(nullable = false, length = 5)
    private String rowLabel;       // A, B, C ...

    @Column(nullable = false)
    private Integer seatNumber;    // 1, 2, 3 ...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    // Lock expiry for optimistic seat locking (5 min TTL)
    private LocalDateTime lockedUntil;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public String getSeatLabel() {
        return rowLabel + seatNumber;
    }
}

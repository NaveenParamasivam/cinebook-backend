package com.cinebook.repository;

import com.cinebook.entity.Booking;
import com.cinebook.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Booking> findByBookingReference(String bookingReference);

    Optional<Booking> findByRazorpayOrderId(String razorpayOrderId);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.bookingStatus = :status ORDER BY b.createdAt DESC")
    List<Booking> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.show.id = :showId AND b.bookingStatus = 'CONFIRMED'")
    long countConfirmedBookingsByShow(@Param("showId") Long showId);

    @Query("""
SELECT b FROM Booking b
JOIN FETCH b.user
JOIN FETCH b.show s
JOIN FETCH s.movie
LEFT JOIN FETCH b.seats
WHERE b.id = :id
""")
    Optional<Booking> findByIdWithDetails(Long id);

    @Query("""
SELECT DISTINCT b FROM Booking b
JOIN FETCH b.show s
JOIN FETCH s.movie
LEFT JOIN FETCH b.seats
WHERE b.user.id = :userId
ORDER BY b.createdAt DESC
""")
    List<Booking> findByUserIdWithDetails(Long userId);
}

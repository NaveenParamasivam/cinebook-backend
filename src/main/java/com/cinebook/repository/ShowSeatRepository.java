package com.cinebook.repository;

import com.cinebook.entity.ShowSeat;
import com.cinebook.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    List<ShowSeat> findByShowIdOrderByRowLabelAscSeatNumberAsc(Long showId);

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.id IN :seatIds")
    List<ShowSeat> findByShowIdAndIdIn(@Param("showId") Long showId, @Param("seatIds") List<Long> seatIds);

    @Query("SELECT COUNT(ss) FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.status = :status")
    long countByShowIdAndStatus(@Param("showId") Long showId, @Param("status") SeatStatus status);

    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = 'AVAILABLE', ss.lockedUntil = null, ss.booking = null " +
           "WHERE ss.status = 'LOCKED' AND ss.lockedUntil < :now")
    int releaseExpiredLocks(@Param("now") LocalDateTime now);
}

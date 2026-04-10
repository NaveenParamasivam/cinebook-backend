package com.cinebook.repository;

import com.cinebook.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, Long> {
    List<Theater> findByActiveTrueOrderByNameAsc();
    List<Theater> findByCityIgnoreCaseAndActiveTrue(String city);
}

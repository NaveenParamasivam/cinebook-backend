package com.cinebook.service.impl;

import com.cinebook.dto.TheaterDto;
import com.cinebook.entity.Theater;
import com.cinebook.exception.ResourceNotFoundException;
import com.cinebook.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;

    public List<TheaterDto.TheaterResponse> getAllActiveTheaters() {
        return theaterRepository.findByActiveTrueOrderByNameAsc()
                .stream().map(this::toResponse).toList();
    }

    public List<TheaterDto.TheaterResponse> getTheatersByCity(String city) {
        return theaterRepository.findByCityIgnoreCaseAndActiveTrue(city)
                .stream().map(this::toResponse).toList();
    }

    public TheaterDto.TheaterResponse getTheaterById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public TheaterDto.TheaterResponse createTheater(TheaterDto.CreateTheaterRequest request) {
        Theater theater = Theater.builder()
                .name(request.getName())
                .city(request.getCity())
                .address(request.getAddress())
                .state(request.getState())
                .pincode(request.getPincode())
                .phone(request.getPhone())
                .totalRows(request.getTotalRows())
                .seatsPerRow(request.getSeatsPerRow())
                .totalSeats(request.getTotalRows() * request.getSeatsPerRow())
                .active(true)
                .build();
        return toResponse(theaterRepository.save(theater));
    }

    @Transactional
    public TheaterDto.TheaterResponse updateTheater(Long id, TheaterDto.UpdateTheaterRequest request) {
        Theater theater = findById(id);
        if (request.getName() != null) theater.setName(request.getName());
        if (request.getCity() != null) theater.setCity(request.getCity());
        if (request.getAddress() != null) theater.setAddress(request.getAddress());
        if (request.getState() != null) theater.setState(request.getState());
        if (request.getPincode() != null) theater.setPincode(request.getPincode());
        if (request.getPhone() != null) theater.setPhone(request.getPhone());
        if (request.getActive() != null) theater.setActive(request.getActive());
        return toResponse(theaterRepository.save(theater));
    }

    @Transactional
    public void deleteTheater(Long id) {
        Theater theater = findById(id);
        theater.setActive(false);
        theaterRepository.save(theater);
    }

    Theater findById(Long id) {
        return theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater", id));
    }

    public TheaterDto.TheaterResponse toResponse(Theater t) {
        TheaterDto.TheaterResponse r = new TheaterDto.TheaterResponse();
        r.setId(t.getId());
        r.setName(t.getName());
        r.setCity(t.getCity());
        r.setAddress(t.getAddress());
        r.setState(t.getState());
        r.setPincode(t.getPincode());
        r.setPhone(t.getPhone());
        r.setTotalSeats(t.getTotalSeats());
        r.setTotalRows(t.getTotalRows());
        r.setSeatsPerRow(t.getSeatsPerRow());
        r.setActive(t.isActive());
        r.setCreatedAt(t.getCreatedAt());
        return r;
    }
}

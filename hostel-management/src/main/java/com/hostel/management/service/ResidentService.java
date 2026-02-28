package com.hostel.management.service;

import com.hostel.management.dto.resident.ResidentRequestDto;
import com.hostel.management.dto.resident.ResidentResponseDto;
import com.hostel.management.enums.ResidentStatus;

import java.util.List;
import java.util.Map;

/**
 * Service interface for Resident management operations.
 */
public interface ResidentService {

    /** Register a new resident. */
    ResidentResponseDto createResident(ResidentRequestDto requestDto);

    /** Update an existing resident's profile. */
    ResidentResponseDto updateResident(Long id, ResidentRequestDto requestDto);

    /** Remove a resident from the system. */
    void deleteResident(Long id);

    /** Get a single resident by ID. */
    ResidentResponseDto getResidentById(Long id);

    /** Get all residents with optional name search or status filter. */
    List<ResidentResponseDto> getAllResidents(String name, ResidentStatus status);

    /** Assign a resident to a room. */
    ResidentResponseDto assignRoom(Long residentId, Long roomId);

    /** Remove a resident from their current room. */
    ResidentResponseDto removeFromRoom(Long residentId);

    /** Get dashboard stats (total, active, pending). */
    Map<String, Object> getResidentStats();
}

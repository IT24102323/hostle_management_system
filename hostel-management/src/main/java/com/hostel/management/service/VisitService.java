package com.hostel.management.service;

import com.hostel.management.dto.visit.VisitRequestDto;
import com.hostel.management.dto.visit.VisitResponseDto;
import com.hostel.management.enums.VisitStatus;

import java.util.List;
import java.util.Map;

/**
 * Service interface for Visit / Room Booking management operations.
 */
public interface VisitService {

    /** Book a new room visit. */
    VisitResponseDto createVisit(VisitRequestDto requestDto);

    /** Update visit details. */
    VisitResponseDto updateVisit(Long id, VisitRequestDto requestDto);

    /** Delete a visit record. */
    void deleteVisit(Long id);

    /** Get a visit by ID. */
    VisitResponseDto getVisitById(Long id);

    /** Get all visits with optional status filter. */
    List<VisitResponseDto> getAllVisits(VisitStatus status);

    /** Update only the status and admin notes of a visit. */
    VisitResponseDto updateStatus(Long id, VisitStatus status, String adminNotes);

    /** Get visit stats. */
    Map<String, Object> getVisitStats();
}

package com.hostel.management.service.impl;

import com.hostel.management.dto.visit.VisitRequestDto;
import com.hostel.management.dto.visit.VisitResponseDto;
import com.hostel.management.entity.Visit;
import com.hostel.management.enums.VisitStatus;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.VisitRepository;
import com.hostel.management.service.VisitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link VisitService} containing all visit/booking business logic.
 */
@Service
@Transactional
public class VisitServiceImpl implements VisitService {

    private final VisitRepository visitRepository;

    public VisitServiceImpl(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @Override
    public VisitResponseDto createVisit(VisitRequestDto dto) {
        Visit visit = Visit.builder()
                .visitorName(dto.getVisitorName())
                .visitorContact(dto.getVisitorContact())
                .visitorEmail(dto.getVisitorEmail())
                .preferredRoomType(dto.getPreferredRoomType())
                .message(dto.getMessage())
                .visitDate(dto.getVisitDate() != null ? dto.getVisitDate() : LocalDate.now())
                .visitTime(dto.getVisitTime())
                .status(VisitStatus.NEW)
                .build();
        return toDto(visitRepository.save(visit));
    }

    @Override
    public VisitResponseDto updateVisit(Long id, VisitRequestDto dto) {
        Visit visit = findById(id);
        if (dto.getVisitorName() != null) visit.setVisitorName(dto.getVisitorName());
        if (dto.getVisitorContact() != null) visit.setVisitorContact(dto.getVisitorContact());
        if (dto.getVisitorEmail() != null) visit.setVisitorEmail(dto.getVisitorEmail());
        if (dto.getPreferredRoomType() != null) visit.setPreferredRoomType(dto.getPreferredRoomType());
        if (dto.getMessage() != null) visit.setMessage(dto.getMessage());
        if (dto.getVisitDate() != null) visit.setVisitDate(dto.getVisitDate());
        if (dto.getVisitTime() != null) visit.setVisitTime(dto.getVisitTime());
        return toDto(visitRepository.save(visit));
    }

    @Override
    public void deleteVisit(Long id) {
        if (!visitRepository.existsById(id)) {
            throw new ResourceNotFoundException("Visit", id);
        }
        visitRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public VisitResponseDto getVisitById(Long id) {
        return toDto(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisitResponseDto> getAllVisits(VisitStatus status) {
        List<Visit> visits = (status != null)
                ? visitRepository.findByStatus(status)
                : visitRepository.findAll();
        return visits.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public VisitResponseDto updateStatus(Long id, VisitStatus status, String adminNotes) {
        Visit visit = findById(id);
        visit.setStatus(status);
        if (adminNotes != null) visit.setAdminNotes(adminNotes);
        return toDto(visitRepository.save(visit));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getVisitStats() {
        return Map.of(
                "total", visitRepository.count(),
                "newRequests", visitRepository.countByStatus(VisitStatus.NEW),
                "contacted", visitRepository.countByStatus(VisitStatus.CONTACTED),
                "closed", visitRepository.countByStatus(VisitStatus.CLOSED)
        );
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Visit findById(Long id) {
        return visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit", id));
    }

    private VisitResponseDto toDto(Visit v) {
        return VisitResponseDto.builder()
                .id(v.getId())
                .visitorName(v.getVisitorName())
                .visitorContact(v.getVisitorContact())
                .visitorEmail(v.getVisitorEmail())
                .preferredRoomType(v.getPreferredRoomType())
                .message(v.getMessage())
                .visitDate(v.getVisitDate())
                .visitTime(v.getVisitTime())
                .status(v.getStatus())
                .adminNotes(v.getAdminNotes())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}

package com.hostel.management.service;

import com.hostel.management.dto.attendance.AttendanceRequestDto;
import com.hostel.management.dto.attendance.AttendanceResponseDto;
import com.hostel.management.entity.Attendance;
import com.hostel.management.entity.Resident;
import com.hostel.management.repository.AttendanceRepository;
import com.hostel.management.repository.ResidentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final ResidentRepository residentRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, ResidentRepository residentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.residentRepository = residentRepository;
    }

    public AttendanceResponseDto createAttendance(AttendanceRequestDto dto) {
        Resident resident = residentRepository.findById(dto.getResidentId())
                .orElseThrow(() -> new RuntimeException("Resident not found"));

        Attendance attendance = new Attendance();
        attendance.setResident(resident);
        attendance.setDate(dto.getDate() != null ? dto.getDate() : LocalDate.now());
        attendance.setStatus(dto.getStatus());
        attendance.setCheckInTime(dto.getCheckInTime());
        attendance.setCheckOutTime(dto.getCheckOutTime());
        attendance.setRemarks(dto.getRemarks());

        return mapToResponseDto(attendanceRepository.save(attendance));
    }

    public AttendanceResponseDto updateAttendance(Long id, AttendanceRequestDto dto) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        if (dto.getStatus() != null) attendance.setStatus(dto.getStatus());
        if (dto.getCheckInTime() != null) attendance.setCheckInTime(dto.getCheckInTime());
        if (dto.getCheckOutTime() != null) attendance.setCheckOutTime(dto.getCheckOutTime());
        if (dto.getRemarks() != null) attendance.setRemarks(dto.getRemarks());

        return mapToResponseDto(attendanceRepository.save(attendance));
    }

    public void deleteAttendance(Long id) {
        attendanceRepository.deleteById(id);
    }

    public List<AttendanceResponseDto> getAllAttendance() {
        return attendanceRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponseDto> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponseDto> getAttendanceByResident(Long residentId) {
        return attendanceRepository.findByResidentId(residentId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public AttendanceResponseDto markAttendance(Long residentId, String status) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new RuntimeException("Resident not found"));

        LocalDate today = LocalDate.now();
        List<Attendance> existing = attendanceRepository.findByResidentIdAndDate(residentId, today);

        Attendance attendance;
        if (!existing.isEmpty()) {
            attendance = existing.get(0);
            attendance.setStatus(status);
        } else {
            attendance = new Attendance();
            attendance.setResident(resident);
            attendance.setDate(today);
            attendance.setStatus(status);
            attendance.setCheckInTime(java.time.LocalTime.now().toString().substring(0, 5));
        }

        return mapToResponseDto(attendanceRepository.save(attendance));
    }

    public Map<String, Object> getAttendanceStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();
        List<Attendance> todayAttendance = attendanceRepository.findByDate(today);

        long present = todayAttendance.stream().filter(a -> "PRESENT".equals(a.getStatus())).count();
        long absent = todayAttendance.stream().filter(a -> "ABSENT".equals(a.getStatus())).count();
        long leave = todayAttendance.stream().filter(a -> "LEAVE".equals(a.getStatus())).count();

        stats.put("date", today);
        stats.put("present", present);
        stats.put("absent", absent);
        stats.put("leave", leave);
        stats.put("total", todayAttendance.size());

        return stats;
    }

    private AttendanceResponseDto mapToResponseDto(Attendance attendance) {
        AttendanceResponseDto dto = new AttendanceResponseDto();
        dto.setId(attendance.getId());
        dto.setResidentId(attendance.getResident().getId());
        dto.setResidentName(attendance.getResident().getName());
        dto.setRoomNumber(attendance.getResident().getRoom() != null ?
                attendance.getResident().getRoom().getRoomNumber() : null);
        dto.setDate(attendance.getDate());
        dto.setStatus(attendance.getStatus());
        dto.setCheckInTime(attendance.getCheckInTime());
        dto.setCheckOutTime(attendance.getCheckOutTime());
        dto.setRemarks(attendance.getRemarks());
        return dto;
    }
}
